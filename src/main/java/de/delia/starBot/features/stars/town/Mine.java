package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class Mine extends Building {
    public Ores[][] ores = new Ores[7][9];
    private final ObjectMapper objectMapper;

    public Mine(BuildingEntity entity) {
        super(4, "Mine", Emoji.fromFormatted(":hammer_pick:"), entity.getGuildId(), entity.getMemberId(), entity.getLevel(), entity.getMetadata(), Map.of(
                1, new Integer[]{3, 300},
                2, new Integer[]{3, 500},
                3, new Integer[]{4, 800}
        ));

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void readMetaData(String metaData) {
        if (metaData == null || metaData.isEmpty()) return;
        try {
            int[][] parsedArray = objectMapper.readValue(metaData, int[][].class);
            for(int i = 0; i < 7; i++) {
                for(int j = 0; j < 9; j++) {
                    ores[i][j] = Ores.getById(parsedArray[i][j]);
                    if (ores[i][j] == null) ores[i][j] = Ores.STONE;
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse metaData: " + metaData);
        }
    }

    @Override
    public String writeMetaData() {
        int[][] array = new int[7][9];
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 9; j++) {
                if(ores[i][j] == null) continue;
                array[i][j] = ores[i][j].id;
            }
        }
        try {
            return objectMapper.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to write metaData: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(Button.primary("embedMenu:town:buildings:Mine:mine", "Mine"));
    }

    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id) {
        if (id.equals("mine")) buttonInteractionEvent.reply("Mine!!!").queue();
    }

    @Override
    public String getDescription() {
        return "Coming soon";
    }

    @Override
    public String getUpgradeText() {
        return "Coming soon";
    }

    public enum Ores {
        AIR(0),
        STONE(1),
        COAL(2),
        IRON(3),
        GOLD(4),
        DIAMOND(4),
        EMERALD(5),
        ;

        final int id;

        Ores(int id) {
            this.id = id;
        }

        public static Ores getById(int id) {
            for (Ores ore : Ores.values())
                if (ore.id == id) return ore;
            return null;
        }
    }
}
