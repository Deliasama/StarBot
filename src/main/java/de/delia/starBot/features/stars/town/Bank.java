package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.delia.starBot.features.stars.StarProfileManager;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Main;
import de.delia.starBot.menus.EmbedMenu;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.Map;

public class Bank extends Building {
    private static final Modal.Builder modal = Modal.create("embedMenu:town:buildings:Bank:?", "?")
            .addActionRow(TextInput.create("amount", "Amount", TextInputStyle.SHORT).setMaxLength(4).setPlaceholder("42").build());

    @Getter
    private int starsStored;

    private ObjectMapper objectMapper;

    public Bank(BuildingEntity entity) {
        super(5, "Bank", Emoji.fromFormatted(":bank:"), entity.getGuildId(), entity.getMemberId(), entity.getLevel(), entity.getMetadata(), Map.of(
                1, new Integer[]{4, 750},
                2, new Integer[]{4, 1500},
                3, new Integer[]{5, 3000},
                4, new Integer[]{6, 6000}
        ));

        if (objectMapper == null) objectMapper = new ObjectMapper();
    }

    @Override
    public void readMetaData(String metaData) {
        if (objectMapper == null) objectMapper = new ObjectMapper();
        if (metaData == null || metaData.isEmpty()) {
            this.starsStored = 0;
            return;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(metaData);
            this.starsStored = jsonNode.get("starsStored").asInt();
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public String writeMetaData() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("starsStored", this.starsStored);

        return node.toString();
    }

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(Button.secondary("embedMenu:town:buildings:Bank:deposit", Emoji.fromUnicode("\uD83D\uDCE5")),
                Button.secondary("embedMenu:town:buildings:Bank:withdraw", Emoji.fromUnicode("\uD83D\uDCE4")));
    }

    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id, EmbedMenu menu) {
        if (getLevel() <= 0) {
            buttonInteractionEvent.reply("You need at least bank level **1** to do that!").setEphemeral(true).queue();
            return;
        }

        if (id.equals("deposit")) {
            Modal modal = Modal.create("embedMenu:town:buildings:Bank:depositModal", "Deposit")
                    .addActionRow(TextInput.create("amount", "Amount to deposit", TextInputStyle.SHORT).setMaxLength(4).setPlaceholder("42").build())
                    .build();
            buttonInteractionEvent.replyModal(modal).queue();
        }
        if (id.equals("withdraw")) {
            Modal modal = Modal.create("embedMenu:town:buildings:Bank:withdrawModal", "Withdraw")
                    .addActionRow(TextInput.create("amount", "Amount to withdraw", TextInputStyle.SHORT).setMaxLength(4).setPlaceholder("42").build())
                    .build();
            buttonInteractionEvent.replyModal(modal).queue();
        }
    }

    public void onModalInteraction(ModalInteractionEvent event, String id, EmbedMenu menu) {
        if (getLevel() == 0) return;
        if (id.equals("depositModal")) {
            int amount = Integer.parseInt(event.getValue("amount").getAsString());
            if (amount <= 0) {
                event.reply("You need to deposit at least 1 Star!").setEphemeral(true).queue();
                return;
            }
            if (this.starsStored + amount > getStoreCapacity()) {
                amount = getStoreCapacity() - this.starsStored;
            }
            StarProfile starProfile = Main.INSTANCE.starProfileManager.getProfile(getGuildId(), getMemberId());
            if (starProfile == null) return;
            if (starProfile.getStars() < amount) {
                event.reply("You don't have that many stars!").setEphemeral(true).queue();
            }
            this.starsStored += amount;
            starProfile.setStars(starProfile.getStars() - amount);
            Main.INSTANCE.starProfileManager.updateProfile(starProfile);
            save();

            // TODO: refresh embed

            event.reply("You Stored **" + amount + "** Stars in your bank!").setEphemeral(true).queue();
        }

        if (id.equals("withdrawModal")) {
            int amount = Integer.parseInt(event.getValue("amount").getAsString());

            if (amount <= 0) {
                event.reply("You need to withdraw at least 1 Star!").setEphemeral(true).queue();
                return;
            }
            if (this.starsStored - amount < 0) {
                amount = this.starsStored;
            }
            StarProfile starProfile = Main.INSTANCE.starProfileManager.getProfile(getGuildId(), getMemberId());
            if (starProfile == null) return;

            this.starsStored -= amount;
            starProfile.setStars(starProfile.getStars() + amount);
            Main.INSTANCE.starProfileManager.updateProfile(starProfile);
            save();

            // TODO: refresh embed!

            event.reply("You withdrew **" + amount + "** Stars from you bank!").setEphemeral(true).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Stars Stored: **" + this.starsStored + " / " + getStoreCapacity() + "**";
    }

    @Override
    public String getUpgradeText() {
        return switch (this.getLevel()) {
            case 0 -> ":star: Capacity: **0 + 250 -> 250**";
            case 1 -> ":star: Capacity: **250 + 250 -> 500**";
            case 2 -> ":star: Capacity: **500 + 500 -> 1000**";
            case 3 -> ":star: Capacity: **1000 + 1000 -> 2000**";
            case 4 -> ":star: Capacity: **2000 + 3000 -> 5000**";
            default -> ":x:";
        };
    }

    public int getStoreCapacity() {
        return switch (this.getLevel()) {
            case 1 -> 250;
            case 2 -> 500;
            case 3 -> 1000;
            case 4 -> 2000;
            default -> 0;
        };
    }
}
