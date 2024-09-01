package de.delia.starBot.features.stars.listeners;

import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MessageReceivedListener extends ListenerAdapter {
    Random random = new Random();
    int index = getRandomIndex();

    private static List<Emoji> emojiList;

    static {
        emojiList = new ArrayList<>();
        emojiList.add(Emoji.fromUnicode("⭐"));
        emojiList.add(Emoji.fromUnicode("\uD83C\uDF1F"));
        emojiList.add(Emoji.fromUnicode("✨"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            index--;
            if (index <= 0) {
                int r = random.nextInt(emojiList.size());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Sternschnuppe!")
                        .setColor(Color.magenta)
                        .setDescription("Klicke " + emojiList.get(r).getFormatted() + " an um Sterne zu erhalten!");

                List<String> ids = new ArrayList<>();
                for(int ii = 0; ii < 3; ii++) {
                    if(ii == r) {
                        ids.add("X");
                    }else {
                        ids.add("O");
                    }
                }
                event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(Main.INSTANCE.starDropMenu.getActionRow(ids)).queue();

                index = getRandomIndex();
            }
        }
    }

    private int getRandomIndex() {
        return random.nextInt(30)+25;
    }
}
