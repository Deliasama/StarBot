package de.delia.starBot.basics;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@ApplicationCommand(name = "test", description = "test!")
public class TestCommand {

    @ApplicationCommandMethod()
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        GuildConfig config = GuildConfig.getGuildConfig(event.getGuild().getIdLong());
        if (config.getConfigList("testii", Long.class) != null)
            event.reply(config.getConfigList("testii", Long.class).toString()).queue();
        config.setConfigList("testii", List.of(new Long[]{2L, 3L, 4L, 69L}));
        config.update();


    }
}
