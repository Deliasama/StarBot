package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.stars.town.menus.TownMenu;
import de.delia.starBot.main.Bot;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "town", description = "See and interact with your town!")
public class TownCommand {
    TownMenu menu;

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (menu == null) menu = new TownMenu(bot.jda);
        EmbedMenu.EmbedMenuResponse response = menu.generate(event.getMember(), event.getGuild(), event.getChannel());
        event.replyEmbeds(response.getEmbed()).setComponents(response.getActionRows()).setEphemeral(true).queue();
    }
}
