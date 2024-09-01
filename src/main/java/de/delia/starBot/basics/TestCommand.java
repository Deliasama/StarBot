package de.delia.starBot.basics;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "test", description = "test!")
public class TestCommand {
    
    @ApplicationCommandMethod()
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        event.reply("Test passed!").setEphemeral(true).queue();
    }
}
