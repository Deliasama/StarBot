package de.delia.starBot.features.stars.listeners;

import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MessageReceivedListener extends ListenerAdapter {
    Random random = new Random();
    Map<Long, Integer> index = new HashMap<>();

    private final static List<Emoji> emojiList;

    static {
        emojiList = new ArrayList<>();
        emojiList.add(Emoji.fromUnicode("⭐"));
        emojiList.add(Emoji.fromUnicode("\uD83C\uDF1F"));
        emojiList.add(Emoji.fromUnicode("✨"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // StarDrops
        if (event.getAuthor().isBot() || event.getChannel() instanceof PrivateChannel)return;

        // check if starDrop is Enabled or if the channel is blacklisted
        GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());
        if(!guildConfig.getConfig("enableStarDrop", Boolean.class))return;
        if(guildConfig.getConfigList("starDropBlacklistedChannel", Long.class).contains(event.getChannel().getIdLong()))return;

        int index = this.index.getOrDefault(event.getGuild().getIdLong(), getRandomIndex(guildConfig.getConfig("starDropMessageMin", Integer.class), guildConfig.getConfig("starDropMessageMax", Integer.class)));
        index--;
        if (index <= 0) {

            int r = random.nextInt(emojiList.size());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Shooting Star!")
                    .setColor(Color.magenta)
                    .setDescription("Click " + emojiList.get(r).getFormatted() + " to collect it!");

            List<String> ids = new ArrayList<>();
            for(int ii = 0; ii < 3; ii++) {
                if(ii == r) {
                    ids.add("X");
                }else {
                    ids.add("O");
                }
            }
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(Main.INSTANCE.starDropMenu.getActionRow(ids)).queue();

            index = getRandomIndex(guildConfig.getConfig("starDropMessageMin", Integer.class), guildConfig.getConfig("starDropMessageMax", Integer.class));
        }
        this.index.put(event.getGuild().getIdLong(), index);
    }

    private int getRandomIndex(int min, int max) {
        return random.nextInt(max-min)+min;
    }
}
