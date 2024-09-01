package de.delia.starBot.features.basics;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

@ApplicationCommand(name = "status", description = "Zeige dir den Bot Status an!")
public class StatusCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Clara 4")
                .setColor(Color.green)
                .addField("Version", bot.version, false)
                .addField("Uptime", String.valueOf(TimeFormat.RELATIVE.before(Duration.between(bot.startTime.atZone(ZoneOffset.systemDefault()), Instant.now().atZone(ZoneOffset.systemDefault())))), false)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
