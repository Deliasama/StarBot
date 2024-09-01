package de.delia.starBot.listeners;

import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildReadyListener extends ListenerAdapter {
    public void onGuildReady(GuildReadyEvent event) {
        Main.INSTANCE.commandManager.upsertCommands(event.getGuild());

        Main.INSTANCE.tradeManagers.put(event.getGuild().getIdLong(), new TradeManager(event.getGuild()));
    }
}
