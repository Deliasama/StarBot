package de.delia.starBot.features.basics;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

@ApplicationCommand(name = "invite", description = "Add me to your server!")
public class InviteCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
                .setTitle("Invite Me")
                .setColor(Color.MAGENTA)
                .setDescription("Add me to your server with this link:\n" + bot.jda.getInviteUrl())
                .setTimestamp(Instant.now());

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}