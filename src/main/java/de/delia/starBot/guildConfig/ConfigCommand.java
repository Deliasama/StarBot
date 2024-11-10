package de.delia.starBot.guildConfig;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.ApplicationCommandPermission;
import de.delia.starBot.main.Bot;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collection;
import java.util.Collections;

@ApplicationCommand(name = "botconfig", description = "configure this bot!")
public class ConfigCommand {
    @ApplicationCommandPermission
    Collection<Permission> permissions = Collections.singleton(Permission.ADMINISTRATOR);

    GuildConfigMenu menu = null;

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (menu == null) menu = new GuildConfigMenu(bot.jda);

        EmbedMenu.EmbedMenuResponse embedMenuResponse = menu.generate(event.getMember(), event.getGuild(), event.getChannel());
        event.replyEmbeds(embedMenuResponse.getEmbed()).setComponents(embedMenuResponse.getActionRows()).setEphemeral(true).queue();
    }
}
