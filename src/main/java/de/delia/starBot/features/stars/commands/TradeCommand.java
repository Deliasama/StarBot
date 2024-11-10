package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ApplicationCommand(name = "trade", description = "trade")
public class TradeCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (!GuildConfig.getGuildConfig(event.getGuild().getIdLong()).getConfig("enableStock", Boolean.class)) {
            event.reply("The Stock System is disabled on this server!").setEphemeral(true).queue();
            return;
        }

        TradeManager tradeManager = Main.INSTANCE.tradeManagers.get(event.getGuild().getIdLong());
        if (tradeManager == null) return;

        EmbedBuilder embedBuilder = tradeManager.createEmbed(event.getMember());

        Button button = null;
        if (tradeManager.getPhase() == 2) button = Button.primary("stock.sell", "Verkaufen");
        if (tradeManager.getPhase() == 3) button = Button.primary("stock.offer", "Bieten");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(tradeManager.createLineChart(), "png", baos);
            if (button == null)
                event.replyEmbeds(embedBuilder.build()).setEphemeral(false).addFiles(FileUpload.fromData(baos.toByteArray(), "chart.png")).queue();
            if (button != null)
                event.replyEmbeds(embedBuilder.build()).setActionRow(button).addFiles(FileUpload.fromData(baos.toByteArray(), "chart.png")).setEphemeral(false).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
