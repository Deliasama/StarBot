package de.delia.starBot.guildConfig;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.ApplicationCommandPermission;
import de.delia.starBot.listeners.SlashCommandInteractionListener;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@ApplicationCommand(name = "botconfig", description = "configure this bot!")
public class ConfigCommand {
    @ApplicationCommandPermission
    Collection<Permission> permissions = Collections.singleton(Permission.ADMINISTRATOR);

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        event.reply("Test bestanden!!!!").setEphemeral(true).queue();
    }
}
