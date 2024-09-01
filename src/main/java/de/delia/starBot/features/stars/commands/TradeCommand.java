package de.delia.starBot.features.stars.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.stars.TradeManager;
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
        TradeManager tradeManager = Main.INSTANCE.tradeManagers.get(event.getGuild().getIdLong());
        if (tradeManager == null)return;

        /*
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Trading")
                .setColor(Color.magenta)
                .setImage("attachment://chart.png")
                .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl());




        if(tradeManager.getPhase() == 1) {
            embedBuilder.setDescription(":red_circle: Markt ist im Moment geschlossen!\n :alarm_clock: Öffnet wieder um **6**Uhr");
        }
        if(tradeManager.getPhase() == 2) {
            embedBuilder.setDescription(":green_circle: Markt ist geöffnet zum verkauf! \n :moneybag: Aktueller Mindestpreis pro Aktie: **" + tradeManager.getOfferValue() + "**\n :alarm_clock: Auktion startet um **18**Uhr");
            button = Button.primary("stock.sell", "Verkaufen");
        }
        if(tradeManager.getPhase() == 3) {
            Member member = null;
            if(tradeManager.getOfferMemberId() != null) {
                CompletableFuture<Member> future = new CompletableFuture<>();
                event.getGuild().retrieveMemberById(tradeManager.getOfferMemberId()).queue(future::complete, future::completeExceptionally);

                try {
                    member = future.get(); // This will block until the async operation completes
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            embedBuilder.setDescription(":green_circle: Auktion ist geöffnet! \n :moneybag: Aktuelles Höchstgebot: **" + tradeManager.getOfferValue() + "** für **" + tradeManager.amountToSell() +"** Aktien von " + (member==null?"niemanden":member.getAsMention()) + "!\n :alarm_clock: Auktion endet um **0**Uhr");
            button = Button.primary("stock.offer", "Bieten");
        }
         */
        EmbedBuilder embedBuilder = tradeManager.createEmbed(event.getMember());

        Button button = null;
        if(tradeManager.getPhase() == 2)button = Button.primary("stock.sell", "Verkaufen");
        if(tradeManager.getPhase() == 3)button = Button.primary("stock.offer", "Bieten");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(tradeManager.createLineChart(), "png", baos);
            if(button == null)event.replyEmbeds(embedBuilder.build()).setEphemeral(false).addFiles(FileUpload.fromData(baos.toByteArray(), "chart.png")).queue();
            if(button != null)event.replyEmbeds(embedBuilder.build()).setActionRow(button).addFiles(FileUpload.fromData(baos.toByteArray(), "chart.png")).setEphemeral(false).queue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }
}
