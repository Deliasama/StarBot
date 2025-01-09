package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.menus.EmbedMenu;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Map;

public class Bank extends Building {
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id, EmbedMenu menu) {

    }

    @Override
    public String getDescription() {
        return "Coming soon!!!!";
    }

    @Override
    public String getUpgradeText() {
        return "0x00848E789B8FF99";
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
