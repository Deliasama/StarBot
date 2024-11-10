package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.stars.tables.Daily;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.TownHall;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.*;

@ApplicationCommand(name = "daily", description = "Get your daily stars!")
public class DailyCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        Daily daily = Daily.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong());
        StarProfile starProfile = StarProfile.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong());

        ZonedDateTime now = Instant.now().atZone(ZoneOffset.systemDefault());
        ZonedDateTime lastCalled = daily.getLastCalled() == null ? now.minusDays(1) : daily.getLastCalled().atZone(ZoneOffset.systemDefault());

        if (now.getDayOfYear() != lastCalled.getDayOfYear() || now.getYear() != lastCalled.getYear()) {
            if (now.minusDays(1).getDayOfYear() == lastCalled.getDayOfYear()) {
                if (daily.getStreak() < 11) {
                    daily.setStreak(daily.getStreak() + 1);
                }
            } else {
                daily.setStreak(1);
            }
            daily.setLastCalled(Instant.now());

            /*
            float share = starProfile.getShares() / ((float) Math.round((StarProfile.getTable().getSumShares(event.getGuild().getIdLong()))));

            int dividendVolume = Main.INSTANCE.dividendTable.get(event.getGuild().getIdLong()).getValue();

            int dividendBonus = (int) (((float) dividendVolume) * share);
            */

            TownHall townHall = (TownHall) Building.loadBuilding(TownHall.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            if (townHall == null)
                townHall = (TownHall) Building.create(TownHall.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            if (townHall.getLevel() == 0) {
                townHall.setLevel(1);
                townHall.save();
            }
            int starsEarned = (10 * townHall.getLevel()) + ((daily.getStreak() - 1) * townHall.getLevel()); //+ dividendBonus;

            starProfile.addStars(starsEarned);
            Daily.getTable().update(daily);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getMember().getEffectiveName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setColor(Color.cyan)
                    .setTitle("Daily")
                    .setTimestamp(Instant.now())
                    .setDescription("You earned **" + starsEarned + "** Stars!\nStreak: **" + (daily.getStreak() - 1) + "** :fire:");

            event.replyEmbeds(embedBuilder.build()).queue();
        } else {
            event.reply("You have to wait until " + TimeFormat.RELATIVE.after(Duration.between(LocalDateTime.now(), LocalDateTime.now().plusDays(1).with(LocalTime.of(0, 0, 1)))) + "!").setEphemeral(true).queue();
        }
    }
}
