package de.delia.starBot.listeners;

import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandInteractionListener extends ListenerAdapter {
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Execute Command via manager
        Main.INSTANCE.commandManager.executeCommand(event.getName(), event);
    }
}
