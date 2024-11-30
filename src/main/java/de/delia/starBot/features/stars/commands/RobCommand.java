package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Wall;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@ApplicationCommand(name = "rob", description = "Rob other Users!")
public class RobCommand {
    static Map<String, Instant> cooldowns = new HashMap<>();
    Random random = new Random();
    Duration cooldownDuration = Duration.ofHours(6);

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event, @Option(description = "Member to rob") User other) {
        if (event.getMember().getIdLong() == other.getIdLong()) {
            event.reply("You can't rob yourself!").setEphemeral(true).queue();
            return;
        }
        if (cooldowns.containsKey(event.getMember().getId())) {
            if (!Instant.now().isAfter(cooldowns.get(event.getMember().getId()).plus(cooldownDuration))) {
                event.reply("You have to wait until " + TimeFormat.RELATIVE.atInstant(cooldowns.get(event.getMember().getId()).plus(cooldownDuration)) + " to rob again!").setEphemeral(true).queue();
                return;
            }
        }
        cooldowns.put(event.getMember().getId(), Instant.now());

        StarProfile profile = bot.starProfileManager.getProfile(event.getGuild().getIdLong(), event.getMember().getIdLong());

        StarProfile profileVictim = bot.starProfileManager.getProfile(event.getGuild().getIdLong(), other.getIdLong());

        double r = ((double) random.nextInt(10)) / 100.0;

        Wall wall = (Wall) Building.loadBuilding(Wall.class, event.getGuild().getIdLong(), other.getIdLong());
        if (wall != null) r -= (((double) wall.getLevel()) * 0.005);

        int toSteal = (int) (profileVictim.getStars() * r);

        bot.starProfileManager.addStars(profileVictim, toSteal * -1);
        bot.starProfileManager.addStars(profile, toSteal);

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(event.getMember().getUser().getName(), null, event.getMember().getUser().getAvatarUrl())
                .setColor(Color.green)
                .setTitle("Rob")
                .setDescription(event.getMember().getUser().getAsMention() + " steals " + other.getAsMention() + " **" + toSteal + "** Stars!")
                .setTimestamp(Instant.now());

        event.replyEmbeds(builder.build()).queue();
    }
}
