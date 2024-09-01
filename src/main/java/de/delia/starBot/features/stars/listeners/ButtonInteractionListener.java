package de.delia.starBot.features.stars.listeners;

import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class ButtonInteractionListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getButton().getId().equalsIgnoreCase("stock.offer")) {
            TradeManager tradeManager = Main.INSTANCE.tradeManagers.get(event.getGuild().getIdLong());
            if(tradeManager == null)return;
            if((tradeManager.getOfferMemberId()==null? 0L :tradeManager.getOfferMemberId()) == event.getUser().getIdLong()) {
                event.reply("Du bist bereits der Höchstbietende!").setEphemeral(true).queue();
                return;
            }
            TextInput textInput = TextInput.create("stock.offer.value", "Dein Angebot:", TextInputStyle.SHORT)
                    .setMaxLength(6)
                    .setPlaceholder("69")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("stock.offer.modal", "Angebot")
                            .addActionRow(textInput).build();

            event.replyModal(modal).queue();
        }
        if(event.getButton().getId().equalsIgnoreCase("stock.sell")) {
            TradeManager tradeManager = Main.INSTANCE.tradeManagers.get(event.getGuild().getIdLong());
            if(tradeManager == null)return;

            StarProfile profile = StarProfile.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong());
            if(profile == null)return;
            if(profile.getShares() < 1) {
                event.reply("Du hast keine Aktien!").setEphemeral(true).queue();
                return;
            }

            if(tradeManager.getMembersSell().contains(event.getUser().getIdLong())) {
                event.reply("Du kannst Maximal einen verkaufen!").setEphemeral(true);
                return;
            }

            if(tradeManager.sell(event.getUser().getIdLong())) {
                event.reply("Du verkaufst eine Aktie in der nächsten Auktion!").setEphemeral(true).queue();
            } else {
                event.reply("Etwas ist schief gelaufen :/").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if(event.getModalId().equals("stock.offer.modal")) {
            String input = event.getValue("stock.offer.value").getAsString();
            int offer = Integer.parseInt(input);

            TradeManager tradeManager = Main.INSTANCE.tradeManagers.get(event.getGuild().getIdLong());

            /*
            if(offer <= tradeManager.getOfferValue()) {
                event.reply("Das Gebot ist zu niedrig!").setEphemeral(true).queue();
                return;
            }
             */

            if(tradeManager.offer(event.getUser().getIdLong(), offer)) {
                event.editMessageEmbeds(tradeManager.createEmbed(event.getMember()).build()).queue();
            } else {
                event.reply("Etwas ist schief gelaufen :/").setEphemeral(true).queue();
            }
        }
    }
}
