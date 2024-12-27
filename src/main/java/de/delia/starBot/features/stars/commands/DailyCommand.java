package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.items.Item;
import de.delia.starBot.features.items.ItemType;
import de.delia.starBot.features.stars.tables.Daily;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Mine;
import de.delia.starBot.features.stars.town.TownHall;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.*;

@ApplicationCommand(name = "daily", description = "Get your daily stars!")
public class DailyCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (event.getChannel() instanceof PrivateChannel) return;
        Daily daily = Daily.getTable().get(event.getGuild().getIdLong(), event.getMember().getIdLong());
        StarProfile starProfile = bot.starProfileManager.getProfile(event.getGuild().getIdLong(), event.getUser().getIdLong());

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

            // Pickaxes
            Item pickaxe = starProfile.getItems().get(ItemType.PICKAXE);
            if (pickaxe == null) return;
            Mine mine = (Mine) Building.loadBuilding(Mine.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            int pickaxeCount = 0;
            if (mine != null && mine.getLevel() > 0) pickaxeCount = 2 + mine.getLevel()*3;
            if ((pickaxe.getAmount() + pickaxeCount) > pickaxe.getStackSize()) pickaxeCount = pickaxe.getStackSize() - pickaxe.getAmount();

            // Stars
            TownHall townHall = (TownHall) Building.loadBuilding(TownHall.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            if (townHall == null)
                townHall = (TownHall) Building.create(TownHall.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            if (townHall.getLevel() == 0) {
                townHall.setLevel(1);
                townHall.save();
            }
            int starsEarned = (10 * townHall.getLevel()) + ((daily.getStreak() - 1) * townHall.getLevel()); //+ dividendBonus;
            starProfile.setStars(starProfile.getStars() + starsEarned);
            pickaxe.setAmount(pickaxe.getAmount() + pickaxeCount);
            pickaxe.update();
            bot.starProfileManager.updateProfile(starProfile);
            Daily.getTable().update(daily);

            StringBuilder description = new StringBuilder();
            description.append("You earned **").append(starsEarned).append("** Stars!\n").append("Streak: **").append(daily.getStreak() - 1).append("** :fire:");
            if (pickaxeCount != 0) description.append("\nPickaxes received: **").append(pickaxeCount).append("** :pick:");


            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getMember().getEffectiveName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setColor(Color.cyan)
                    .setTitle("Daily")
                    .setTimestamp(Instant.now())
                    .setDescription(description.toString());

            event.replyEmbeds(embedBuilder.build()).queue();
        } else {
            event.reply("You have to wait until " + TimeFormat.RELATIVE.after(Duration.between(LocalDateTime.now(), LocalDateTime.now().plusDays(1).with(LocalTime.of(0, 0, 1)))) + "!").setEphemeral(true).queue();
        }
    }
}
