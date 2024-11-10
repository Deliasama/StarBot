package de.delia.starBot.features.stars.listeners;

import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Telescope;
import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class MessageReceivedListener extends ListenerAdapter {
    private final static List<Emoji> emojiList;
    private static final Map<String, Long> chatTimeout = new HashMap<>();

    static {
        emojiList = new ArrayList<>();
        emojiList.add(Emoji.fromUnicode("⭐"));
        emojiList.add(Emoji.fromUnicode("\uD83C\uDF1F"));
        emojiList.add(Emoji.fromUnicode("✨"));
    }

    Random random = new Random();
    Map<Long, Integer> index = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getChannel() instanceof PrivateChannel) return;

        // Activity stars
        if (chatTimeout.containsKey(event.getGuild().getId() + event.getMember().getId())) {
            if (!((Instant.now().getEpochSecond() - chatTimeout.get(event.getGuild().getId() + event.getMember().getId())) <= 5 * 60)) {
                addStars(event);
            }
        } else {
            addStars(event);
        }


        // StarDrops
        // check if starDrop is Enabled or if the channel is blacklisted
        GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());
        if (!guildConfig.getConfig("enableStarDrop", Boolean.class)) return;
        if (guildConfig.getConfigList("starDropBlacklistedChannel", Long.class).contains(event.getChannel().getIdLong()))
            return;

        int index = this.index.getOrDefault(event.getGuild().getIdLong(), getRandomIndex(guildConfig.getConfig("starDropMessageMin", Integer.class), guildConfig.getConfig("starDropMessageMax", Integer.class)));
        index--;
        if (index <= 0) {

            int r = random.nextInt(emojiList.size());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Shooting Star!")
                    .setColor(Color.magenta)
                    .setDescription("Click " + emojiList.get(r).getFormatted() + " to collect it!");

            List<String> ids = new ArrayList<>();
            for (int ii = 0; ii < 3; ii++) {
                if (ii == r) {
                    ids.add("X");
                } else {
                    ids.add("O");
                }
            }
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(Main.INSTANCE.starDropMenu.getActionRow(ids)).queue();

            index = getRandomIndex(guildConfig.getConfig("starDropMessageMin", Integer.class), guildConfig.getConfig("starDropMessageMax", Integer.class));
        }
        this.index.put(event.getGuild().getIdLong(), index);
    }

    private void addStars(MessageReceivedEvent event) {
        StarProfile starProfile = StarProfile.getTable().get(event.getGuild().getIdLong(), event.getMember().getIdLong());
        Telescope telescope = (Telescope) Building.loadBuilding(Telescope.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
        double multiplier = 1;
        if (telescope != null) multiplier = 1.0 + (telescope.getLevel() * 0.5);
        starProfile.addStars((int) (1.0 * Math.round(multiplier)));

        chatTimeout.put(event.getGuild().getId() + event.getMember().getId(), Instant.now().getEpochSecond());
    }

    private int getRandomIndex(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
