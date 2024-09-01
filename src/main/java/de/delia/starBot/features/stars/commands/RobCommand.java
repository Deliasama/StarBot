package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.features.stars.tables.StarProfile;
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

@ApplicationCommand(name = "rob", description = "Stehle Sterne von anderen Mitspielern!")
public class RobCommand {
    Random random = new Random();
    static Map<String, Instant> cooldowns = new HashMap<>();
    Duration cooldownDuration = Duration.ofHours(6);

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event, @Option(description = "spieler") User other) {
        if(cooldowns.containsKey(event.getMember().getId())) {
            if(!Instant.now().isAfter(cooldowns.get(event.getMember().getId()).plus(cooldownDuration))) {
                event.reply("Du musst noch bis " + TimeFormat.RELATIVE.atInstant(cooldowns.get(event.getMember().getId()).plus(cooldownDuration)) + " warten!").setEphemeral(true).queue();
                return;
            }
        }
        cooldowns.put(event.getMember().getId(), Instant.now());

        StarProfile profile = bot.starProfileTable.get(event.getGuild().getIdLong(), event.getMember().getIdLong());

        StarProfile profileVictim = bot.starProfileTable.get(event.getGuild().getIdLong(), other.getIdLong());

        double r = ((double) random.nextInt(10)) / 100.0;

        int toSteal = (int) (profileVictim.getStars() * r);

        profileVictim.addStars(toSteal*-1);
        profile.addStars(toSteal);

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(event.getMember().getUser().getName(), null, event.getMember().getUser().getAvatarUrl())
                .setColor(Color.green)
                .setTitle("Rob")
                .setDescription(event.getMember().getUser().getAsMention() + " stehlt " + other.getAsMention() + " **" + toSteal + "** Sterne!")
                .setTimestamp(Instant.now());

        event.replyEmbeds(builder.build()).queue();
    }
}
