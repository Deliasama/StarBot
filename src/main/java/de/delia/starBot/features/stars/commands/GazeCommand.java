package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

@ApplicationCommand(name = "gaze", description = "Stargaze to earn stars!")
public class GazeCommand {
    static Map<String, Instant> cooldowns = new HashMap<>();
    Random random = new Random();
    Duration cooldownDuration = Duration.ofHours(4);

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (cooldowns.containsKey(event.getMember().getId())) {
            if (!Instant.now().isAfter(cooldowns.get(event.getMember().getId()).plus(cooldownDuration))) {
                event.reply("You have to wait until " + TimeFormat.RELATIVE.atInstant(cooldowns.get(event.getMember().getId()).plus(cooldownDuration)) + " to gaze again!").setEphemeral(true).queue();
                return;
            }
        }
        cooldowns.put(event.getMember().getId(), Instant.now());

        int type1Amount = 0;
        int type2Amount = 0;
        int type3Amount = 0;
        int r;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 80; i++) {
            if (i%8 == 0 && i != 0) stringBuilder.append("\r\n");
            r = random.nextInt(9);
            switch (r) {
                case 0:
                    stringBuilder.append("⭐");
                    type1Amount++;
                    break;
                case 1:
                    stringBuilder.append("\uD83C\uDF1F");
                    type2Amount++;
                    break;
                case 2:
                    stringBuilder.append("✨");
                    type3Amount++;
                    break;
                case 3, 4, 5, 6, 7, 8:
                    stringBuilder.append("<:air:1060322785555140740>");
            }
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Gaze");
        embedBuilder.setAuthor(event.getMember().getUser().getName(), null, event.getMember().getUser().getAvatarUrl());
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setDescription("Chose one of the Stars to collect!\r\n \r\n" + stringBuilder.toString());

        List<String> ids = List.of(event.getMember().getId() + ":" + String.valueOf(type1Amount), event.getMember().getId() + ":" + String.valueOf(type2Amount), event.getMember().getId() + ":" + String.valueOf(type3Amount));
        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).setActionRow(Main.INSTANCE.gazeMenu.getActionRow(ids)).queue();
    }
}
