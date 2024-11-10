package de.delia.starBot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!temmie")) {
            event.getMessage().reply("hoi, im temmie!!!!!").queue();
        }
    }
}
