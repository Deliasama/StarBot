package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.TownHall;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;
import java.util.List;

@ApplicationCommand(name = "visit", description = "Visit the town of another player!")
public class Visit {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event, @Option(description = "User to visit") User user) {
        if (!event.isFromGuild() || event.getGuild() == null) return;

        TownHall townHall = (TownHall) Building.loadBuilding(TownHall.class, event.getGuild().getIdLong(), user.getIdLong());
        if (townHall == null) {
            event.reply("Something went wrong!").setEphemeral(true).queue();
            return;
        }

        List<Building> buildings = new java.util.ArrayList<>(List.of(townHall));
        buildings.addAll(townHall.getTown());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Town from " + user.getEffectiveName())
                .setColor(Color.cyan)
                .setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

        for (Building building : buildings) {
            embedBuilder.addField(building.getIcon().getFormatted() + " **" + building.getName() + "**", "Level: " + building.getLevel(), false);
        }

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}
