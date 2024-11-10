package de.delia.starBot.listeners;

import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinEvent extends ListenerAdapter {
    @Override
    public void onGuildJoin(net.dv8tion.jda.api.events.guild.GuildJoinEvent event) {
        Main.INSTANCE.commandManager.upsertCommands(event.getGuild());

        Main.INSTANCE.tradeManagers.put(event.getGuild().getIdLong(), new TradeManager(event.getGuild()));
    }
}
