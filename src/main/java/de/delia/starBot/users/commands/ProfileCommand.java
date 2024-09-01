package de.delia.starBot.users.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@ApplicationCommand(name = "profile", description = "Zeige dir dein Profil an!")
public class ProfileCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if(member == null)return;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle("Profile from " + member.getEffectiveName());
        embedBuilder.setDescription(bot.clara4UserTable.get(member.getGuild().getIdLong(), member.getIdLong()).toString());

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
