package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

@ApplicationCommand(name = "leaderboard", description = "Zeige dir das Leaderboard an!")
public class LeaderboardCommand {

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event, @Option(isRequired = false, description = "bis welchen Platz") Integer depth) {
        if(depth == null)depth = 10;
        List<StarProfile> starProfiles = StarProfile.getTable().getSorted(event.getGuild().getIdLong(), depth, "u.stars");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setTitle("Leaderboard")
                .setColor(Color.cyan);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i<=starProfiles.size(); i++) {
            stringBuilder
                    .append("**#").append(i).append("** ").append(UserSnowflake.fromId(starProfiles.get(i - 1).getMemberId()).getAsMention()).append("\n")
                    .append(" - ").append(starProfiles.get(i - 1).getStars()).append("⭐ - ").append(starProfiles.get(i - 1).getShares()).append(":scroll:\n");
        }

        embedBuilder.setDescription(stringBuilder);
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
