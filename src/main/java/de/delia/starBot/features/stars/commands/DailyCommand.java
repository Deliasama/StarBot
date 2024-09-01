package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.stars.tables.Daily;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.*;

@ApplicationCommand(name = "daily", description = "Hole dir deine täglichen Sterne ab!")
public class DailyCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        Daily daily = Daily.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong());
        StarProfile starProfile = StarProfile.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong());

        ZonedDateTime now = Instant.now().atZone(ZoneOffset.systemDefault());
        ZonedDateTime lastCalled = daily.getLastCalled()==null ? now.minusDays(1) : daily.getLastCalled().atZone(ZoneOffset.systemDefault());

        if(now.getDayOfYear() != lastCalled.getDayOfYear() || now.getYear() != lastCalled.getYear()) {
            if(now.minusDays(1).getDayOfYear() == lastCalled.getDayOfYear()) {
                if(daily.getStreak()<11) {
                    daily.setStreak(daily.getStreak()+1);
                }
            } else {
                daily.setStreak(1);
            }
            daily.setLastCalled(Instant.now());

            float share = starProfile.getShares() / ((float) Math.round((StarProfile.getTable().getSumShares(event.getGuild().getIdLong()))));

            int dividendVolume = Main.INSTANCE.dividendTable.get(event.getGuild().getIdLong()).getValue();

            int dividendBonus = (int) (((float) dividendVolume) * share);

            int starsEarned = 10 + daily.getStreak()-1 + dividendBonus;

            starProfile.addStars(starsEarned);
            Daily.getTable().update(daily);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getMember().getEffectiveName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setColor(Color.cyan)
                    .setTitle("Daily")
                    .setTimestamp(Instant.now())
                    .setDescription("Du hast **" + starsEarned + "** Sterne erhalten!\nStreak: **" + (daily.getStreak()-1) + "** :fire:\nDividend Bonus: **" + dividendBonus + "**:star:");

            event.replyEmbeds(embedBuilder.build()).queue();
        } else {
            event.reply("Du musst bis " + TimeFormat.RELATIVE.after(Duration.between(LocalDateTime.now(), LocalDateTime.now().plusDays(1).with(LocalTime.of(0, 0, 1)))) + " warten!").setEphemeral(true).queue();
        }
    }
}
