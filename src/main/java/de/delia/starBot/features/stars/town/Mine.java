package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.menus.CacheableEmbedMenu;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Map;

public class Mine extends Building {
    public Ores[][] ores;
    private ObjectMapper objectMapper;
    int depth = 0;
    int pickaxeCount = 0;
    final int MINE_WIDTH = 7;
    final int MINE_HEIGHT = 9;

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
        this.ores = new Ores[MINE_WIDTH][MINE_HEIGHT];
        if (metaData == null || metaData.isEmpty()) {
            generateMine();
            return;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(metaData);
            depth = jsonNode.get("depth").asInt(0);
            pickaxeCount = jsonNode.get("pickaxeCount").asInt(0);

            int[][] parsedArray = objectMapper.convertValue(jsonNode.get("ores"), int[][].class);
            for(int i = 0; i < MINE_WIDTH; i++) {
                for(int j = 0; j < MINE_HEIGHT; j++) {
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
        int[][] oreArray = new int[MINE_WIDTH][MINE_HEIGHT];
        for(int i = 0; i < MINE_WIDTH; i++) {
            for(int j = 0; j < MINE_HEIGHT; j++) {
                if(ores[i][j] == null) continue;
                oreArray[i][j] = ores[i][j].id;
            }
        }
        ObjectNode node = objectMapper.createObjectNode();
        node.set("ores", objectMapper.valueToTree(oreArray));
        node.put("depth", depth);
        node.put("pickaxeCount", pickaxeCount);

        return node.toString();
    }

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(Button.secondary("embedMenu:town:buildings:Mine:mine", Emoji.fromUnicode("⛏️")));
    }

    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id, EmbedMenu menu) {
        if (id.equals("mine")) {
            pickaxeCount++;
            try {
                Ores ore = mineOre(3, 1);
                buttonInteractionEvent.reply(ore.name()).queue();
                buttonInteractionEvent.getMessage().editMessageEmbeds(this.getEmbed()).queue();
            } catch (MineException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for (int y = MINE_HEIGHT-1; y >= 0; y--) {
            description.append(y).append(":");
            for (int x = 0; x < MINE_WIDTH; x++) {
                description.append(" | ");
                if (ores[x][y] == Ores.STONE) description.append("s");
                if (ores[x][y] == Ores.COAL) description.append("c");
            }
            description.append("\n");
        }
        return description.toString();
    }

    @Override
    public String getUpgradeText() {
        return "Coming soon";
    }

    public Ores mineOre(int x, int y) throws MineException {
        System.out.println("x: " + x + ", y: " + y);
        if (this.ores == null) generateMine();
        if (x < 0 || y < 0 || x >= MINE_WIDTH || y >= MINE_HEIGHT) {
            throw new MineException("Invalid coordinates");
        }
        if (this.pickaxeCount <= 0) {
            throw new MineException("You don't have a pickaxe!");
        }
        if (this.ores[x][y] != null && this.ores[x][y] != Ores.AIR) {
            Ores ore = ores[x][y];
            System.out.println(ore);
            ores[x][y] = Ores.AIR;
            pickaxeCount--;

            // shift the mine one up and add 1 to the depth if the 2. lowest ore is mined
            if (y <= 1) {
                depth++;
                shiftMineUp(generateMineRow(depth));
            }
            save();

            return ore;
        }
        throw new MineException("Unavailable Mine Location!");
    }

    private void shiftMineUp(Ores[] newRow) {
        for (int y = MINE_HEIGHT-1; y >= 0; y--) {
            for (int x = 0; x < MINE_WIDTH; x++) {
                if (y == 0) {
                    ores[x][y] = newRow[x];
                    continue;
                }
                ores[x][y] = ores[x][y-1];
            }
        }
    }

    private Ores[] generateMineRow(int depth) {
        Ores[] ores = new Ores[MINE_WIDTH];
        for (int i = 0; i < MINE_WIDTH; i++) {
            if (depth % 2 == 0) {
                ores[i] = Ores.STONE;
            } else {
                ores[i] = Ores.COAL;
            }
        }
        return ores;
    }

    private void generateMine() {
        // TODO real ore generation
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 9; j++) {
                ores[i][j] = Ores.STONE;
            }
        }
        save();
    }

    public static class MineException extends Exception {
        public MineException(String message) {
            super(message);
        }
    }
    public enum Ores {
        AIR(0, 0),
        STONE(1, 5),
        COAL(2, 10),
        IRON(3, 20),
        GOLD(4, 30),
        DIAMOND(4, 50),
        EMERALD(5, 75),
        ;

        final int id;
        final int stars;

        Ores(int id, int stars) {
            this.id = id;
            this.stars = stars;
        }

        public static Ores getById(int id) {
            for (Ores ore : Ores.values())
                if (ore.id == id) return ore;
            return null;
        }
    }
}
