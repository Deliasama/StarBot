package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@ApplicationCommand(name = "stars", description = "Zeige dir an wie viele Sterne du hast!")
public class StarsCommand {
    @ApplicationCommandMethod
    public void command(Bot bot, SlashCommandInteractionEvent event, @Option(isRequired = false, description = "other Member") User other) {
        User user = other==null?event.getMember().getUser():other;

        event.getGuild().retrieveMember(user).queue(member -> {
            StarProfile profile = StarProfile.getTable().get(event.getGuild().getIdLong(), member.getIdLong());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
                    .setColor(Color.cyan)
                    .setTitle("Stars")
                    .setDescription("**" + member.getEffectiveName() + "** hat **" + profile.getStars() + "** Sterne!");

            event.replyEmbeds(embedBuilder.build()).queue();
        }, throwable -> {
            if(!event.isAcknowledged())event.reply("Error!").setEphemeral(true).queue();
        });
    }
}
