package de.delia.starBot.features.stars;

import de.delia.starBot.features.stars.tables.Dividend;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.tables.Stock;
import de.delia.starBot.features.stars.tables.StockHistory;
import de.delia.starBot.main.Main;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TradeManager implements Runnable {
    private final long guildId;
    // 1 = Closed; 2 = Sell Phase; 3 = Auction phase
    @Getter
    private int phase;
    @Getter
    private List<Long> membersSell;
    @Getter
    private Map<Long, Integer> membersOffer;
    private final int minOffer;
    @Getter
    private int offerValue;
    @Getter
    private Long offerMemberId;

    private final Random random = new Random();

    public TradeManager(Guild guild) {
        this.guildId = guild.getIdLong();

        if (StockHistory.getTable().getSorted(guildId, 10).isEmpty()) {
            StockHistory.getTable().save(new StockHistory(guildId, 25, Instant.now()));
        }

        // load from DB
        Stock stock = Stock.getTable().get(guildId);
        this.phase = stock.getPhase();
        this.minOffer = stock.getPrice();
        this.offerValue = stock.getOfferValue();
        this.offerMemberId = stock.getOfferMemberId();
        this.membersSell = stock.getMembersSell();
        this.membersOffer = stock.getMembersOffer();

        ZonedDateTime time = Instant.now().atZone(ZoneOffset.systemDefault());
        System.out.println(time.getHour());

        if (time.getHour() < 6) {
            if (phase != 1) switchPhase(1);
        }
        if (time.getHour() >= 6 && time.getHour() < 13) {
            if (phase != 2) switchPhase(2);
        }
        if (time.getHour() >= 13) {
            if (phase != 3) switchPhase(3);
        }

        ZonedDateTime nextHour = time.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        Main.scheduler.scheduleAtFixedRate(this, Duration.between(time, nextHour).toMillis(), TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    public EmbedBuilder createEmbed(Member member) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Trading")
                .setColor(Color.magenta)
                .setImage("attachment://chart.png")
                .setTimestamp(Instant.now())
                .setAuthor(member.getUser().getName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());

        if (getPhase() == 1) {
            embedBuilder.setDescription(":red_circle: Markt ist im Moment geschlossen!\n:moneybag: Aktuelles Dividendenvolumen: **" + getDividend() + "**\n:alarm_clock: Öffnet wieder um **6**Uhr");
        }
        if (getPhase() == 2) {
            embedBuilder.setDescription(":green_circle: Markt ist geöffnet zum verkauf! \n:moneybag: Aktuelles Dividendenvolumen: **" + getDividend() + "**\n:alarm_clock: Auktion startet um **13**Uhr");
        }
        if (getPhase() == 3) {
            StringBuilder stringBuilder = new StringBuilder();

            int price = membersOffer.values().stream().mapToInt(integer -> integer).sum();

            membersOffer.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(m -> {
                float amountSell = getAmountToSell();
                float shares = (((float) m.getValue()) / ((float) price)) * amountSell;
                shares = Math.round(shares * 100.0f) / 100.0f;
                int percent = (int) ((shares / amountSell) * 100.0f);

                stringBuilder.append(UserSnowflake.fromId(m.getKey()).getAsMention() + ":\n").append("> " + m.getValue() + ":star: [" + percent + "%] [" + shares + ":scroll:]").append("\n");
            });

            embedBuilder.addField(":moneybag: Auktion:", stringBuilder.toString(), false);
            embedBuilder.setDescription(":green_circle: Auktion ist geöffnet! \n:moneybag: Insgesamt geboten: **" + membersOffer.values().stream().mapToInt(Integer::intValue).sum() + "**:star:!\n:chart_with_upwards_trend: Aktuelles Dividendenvolumen: **" + getDividend() + "**\n:scroll: Aktienanzahl: **" + getAmountToSell() + "**\n:alarm_clock: Auktion endet um **0**Uhr");
        }
        return embedBuilder;
    }

    public boolean sell(long memberId) {
        if (phase != 2) return false;

        StarProfile profile = StarProfile.getTable().get(guildId, memberId);

        if (membersSell.contains(memberId)) return false;
        if (profile == null) return false;
        if (profile.getShares() < 1) return false;

        profile.setShares(profile.getShares() - 1);
        membersSell.add(memberId);

        updateDB();

        return true;
    }

    public boolean offer(long memberId, int offer) {
        if (phase != 3) return false;

        StarProfile profile = StarProfile.getTable().get(guildId, memberId);

        if (profile == null) return false;
        if ((profile.getStars() + membersOffer.getOrDefault(memberId, 0)) < offer) return false;
        if (offer <= membersOffer.getOrDefault(memberId, 0)) return false;
        // if(offerValue >= offer)return false;

        if (offerMemberId != null) {
            StarProfile lastOfferMember = StarProfile.getTable().get(guildId, offerMemberId);
            lastOfferMember.addStars(offerValue);
        }

        int deltaOffer = offer - membersOffer.getOrDefault(memberId, 0);

        membersOffer.put(memberId, offer);

        /*
        offerMemberId = memberId;

        offerValue = offer;
        */

        profile.addStars(deltaOffer * -1);

        updateDB();

        return true;
    }

    public int getAmountToSell() {
        return membersSell.size() + 1;
    }

    public int getDividend() {
        return Main.INSTANCE.dividendTable.get(guildId).getValue();
    }

    private int updateDividend() {
        Dividend dividend = Main.INSTANCE.dividendTable.get(guildId);


        // calculate new trends
        ZonedDateTime now = ZonedDateTime.now();
        if (now.getHour() < 1) {
            // updates daily
            if (random.nextDouble() < 0.75) {
                if (dividend.getValue() <= 200) dividend.setTrendDay(1);
                if (dividend.getValue() > 200) dividend.setTrendDay(-1);
            } else {
                dividend.setTrendDay(random.nextInt(3) - 1);
            }

            // updates Weekly
            if (now.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                if (random.nextDouble() < 0.75) {
                    if (dividend.getValue() <= 200) dividend.setTrendWeek(1);
                    if (dividend.getValue() > 200) dividend.setTrendWeek(-1);
                } else {
                    dividend.setTrendWeek(random.nextInt(3) - 1);
                }
            }
        }


        int value = dividend.getValue();
        value += (random.nextInt(5) - 2);
        value += dividend.getTrendDay();
        value += dividend.getTrendWeek();
        dividend.setValue(value);

        Main.INSTANCE.dividendTable.update(dividend);

        return value;
    }

    private void switchPhase(int newPhase) {
        this.phase = newPhase;
        if (phase == 1) {
            System.out.println("Phase is now set to Closed!");

            int price = membersOffer.values().stream().mapToInt(integer -> integer).sum();
            int amountSold = membersSell.size() + 1;
            int pricePerShare = price / amountSold;

            for (Long id : membersSell) {
                StarProfile p = StarProfile.getTable().get(guildId, id);
                p.addStars(pricePerShare);
            }
            membersSell.clear();

            membersOffer.forEach((key, value) -> {
                long id = key;
                int offer = value;

                float shares = (((float) offer) / ((float) price)) * amountSold;
                shares = Math.round(shares * 100.0f) / 100.0f;

                StarProfile p = StarProfile.getTable().get(guildId, id);

                if (shares == 0) p.addStars(offer);
                if (shares >= 0.1f) {
                    p.setShares(p.getShares() + shares);
                    StarProfile.getTable().update(p);
                }
            });

            StockHistory.getTable().save(new StockHistory(guildId, pricePerShare, Instant.now()));

            membersOffer.clear();

            updateDB();
        }

        if (phase == 2) {
            System.out.println("Phase is now set to Sell!");
            offerValue = minOffer;
            updateDB();
        }
        if (phase == 3) {
            System.out.println("Phase is now set to Auction!");
            offerValue = minOffer * (membersSell.size() + 1);
            updateDB();
        }
    }

    private void updateDB() {
        Stock stock = Stock.getTable().get(guildId);
        stock.setPhase(phase);
        stock.setOfferValue(offerValue);
        stock.setOfferMemberId(offerMemberId);
        stock.setPrice(minOffer);
        stock.setMembersSell(membersSell);
        stock.setMembersOffer(membersOffer);

        Stock.getTable().update(stock);
    }

    public BufferedImage createLineChart() {
        TimeSeries timeSeries = new TimeSeries("Time");

        List<StockHistory> stockHistories = StockHistory.getTable().getSorted(guildId, 21);

        for (StockHistory stockHistory : stockHistories) {
            timeSeries.add(new Day(Date.from(stockHistory.getTimestamp())), stockHistory.getValue());
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Markt Preis", "Datum", "Preis", new TimeSeriesCollection(timeSeries));

        chart.setBackgroundPaint(Color.black);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.darkGray);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer renderer = plot.getRenderer();

        if (renderer instanceof XYLineAndShapeRenderer r) {
            r.setDefaultShapesVisible(true);
            r.setDefaultShapesFilled(true);
            r.setDrawSeriesLineAsPath(true);
        }

        ChartPanel panel = new ChartPanel(chart);
        return chart.createBufferedImage(720, 480);
    }

    @Override
    public void run() {
        ZonedDateTime time = Instant.now().atZone(ZoneOffset.systemDefault());

        if ((time.getHour() % 3) == 0) {
            System.out.println("Neue dividende: " + updateDividend());
        }

        if (time.getHour() < 6) {
            if (phase != 1) switchPhase(1);
        }
        if (time.getHour() >= 6 && time.getHour() < 13) {
            if (phase != 2) switchPhase(2);
        }
        if (time.getHour() >= 13) {
            if (phase != 3) switchPhase(3);
        }
    }
}