package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.api.client.json.Json;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Mine extends Building {
    public Ores[][] ores;
    private ObjectMapper objectMapper;
    int depth = 0;
    int pickaxeCount = 0;

    public Mine(BuildingEntity entity) {
        super(4, "Mine", Emoji.fromFormatted(":hammer_pick:"), entity.getGuildId(), entity.getMemberId(), entity.getLevel(), entity.getMetadata(), Map.of(
                1, new Integer[]{3, 300},
                2, new Integer[]{3, 500},
                3, new Integer[]{4, 800}
        ));
        if (objectMapper == null) this.objectMapper = new ObjectMapper();
    }

    @Override
    public void readMetaData(String metaData) {
        if (objectMapper == null) this.objectMapper = new ObjectMapper();
        this.ores = new Ores[7][9];
        if (metaData == null || metaData.isEmpty()) {
            generateMine();
            return;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(metaData);
            String oreString = jsonNode.get("ores").toString();
            depth = jsonNode.get("depth").asInt(0);
            pickaxeCount = jsonNode.get("pickaxeCount").asInt(0);

            int[][] parsedArray = objectMapper.readValue(oreString, int[][].class);
            for(int i = 0; i < 7; i++) {
                for(int j = 0; j < 9; j++) {
                    ores[i][j] = Ores.getById(parsedArray[i][j]);
                    if (ores[i][j] == null) ores[i][j] = Ores.STONE;
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public String writeMetaData() {
        if (objectMapper == null) this.objectMapper = new ObjectMapper();
        int[][] oreArray = new int[7][9];
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 9; j++) {
                if(ores[i][j] == null) continue;
                oreArray[i][j] = ores[i][j].id;
            }
        }
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("ores", objectMapper.writeValueAsString(oreArray));
            node.put("depth", depth);
            node.put("pickaxeCount", pickaxeCount);

            return node.toString();
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

    private void generateMine() {
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 9; j++) {
                ores[i][j] = Ores.STONE;
            }
        }
        save();
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
