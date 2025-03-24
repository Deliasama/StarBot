package de.delia.starBot.features.stars.town;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.delia.starBot.features.items.Item;
import de.delia.starBot.features.items.ItemType;
import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Main;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Mine extends Building {
    private static final Modal mineModal = Modal.create("embedMenu:town:buildings:Mine:mineModal", "Mine")
            .addActionRow(TextInput.create("x-coordinate", "x-coordinate", TextInputStyle.SHORT).setMaxLength(1).setPlaceholder("3").build())
            .addActionRow(TextInput.create("y-coordinate", "y-coordinate", TextInputStyle.SHORT).setMaxLength(1).setPlaceholder("6").build())
            .build();
    final int MINE_WIDTH = 7;
    final int MINE_HEIGHT = 9;
    public Ores[][] ores;
    int depth;
    private ObjectMapper objectMapper;

    private int lastMineLocationX = 0;
    private int lastMineLocationY = 0;


    public Mine(BuildingEntity entity) {
        super(4, "Mine", Emoji.fromFormatted(":hammer_pick:"), entity.getGuildId(), entity.getMemberId(), entity.getLevel(), entity.getMetadata(), Map.of(
                1, new Integer[]{3, 300},
                2, new Integer[]{3, 500},
                3, new Integer[]{4, 1000},
                4, new Integer[]{5, 2500}
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
            this.depth = jsonNode.get("depth").asInt(0);

            int[][] parsedArray = objectMapper.convertValue(jsonNode.get("ores"), int[][].class);
            for (int i = 0; i < MINE_WIDTH; i++) {
                for (int j = 0; j < MINE_HEIGHT; j++) {
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
        for (int i = 0; i < MINE_WIDTH; i++) {
            for (int j = 0; j < MINE_HEIGHT; j++) {
                if (ores[i][j] == null) continue;
                oreArray[i][j] = ores[i][j].id;
            }
        }
        ObjectNode node = objectMapper.createObjectNode();
        node.set("ores", objectMapper.valueToTree(oreArray));
        node.put("depth", depth);

        return node.toString();
    }

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(Button.secondary("embedMenu:town:buildings:Mine:mine", Emoji.fromUnicode("⛏️")),
                Button.secondary("embedMenu:town:buildings:Mine:reset", Emoji.fromUnicode("\uD83D\uDD04")),
                Button.secondary("embedMenu:town:buildings:Mine:quickMine", Emoji.fromUnicode("\uD83D\uDEE0")));
    }

    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id, EmbedMenu menu) {
        if (id.equals("mine")) buttonInteractionEvent.replyModal(mineModal).queue();
        if (id.equals("quickMine")) {
            try {
                Ores minedOre = quickMine();
                EmbedBuilder builder = EmbedBuilder.fromData(getEmbed().toData());
                builder.setDescription("You mined **" + minedOre.name() + "** and received **" + minedOre.stars + "** Stars!\n\n" + getEmbed().getDescription());
                buttonInteractionEvent.editMessageEmbeds(builder.build()).queue();
            } catch (MineException e) {
                buttonInteractionEvent.reply(e.getMessage()).setEphemeral(true).queue();
            }
        }
        if (id.equals("reset")) {
            if (getLevel() == 0) {
                buttonInteractionEvent.reply("You need at least level 1!").setEphemeral(true).queue();
                return;
            }
            if (depth >= 20 * getLevel()) {
                generateMine();
                buttonInteractionEvent.editMessageEmbeds(getEmbed()).queue();
            } else {
                buttonInteractionEvent.reply("You need to reach the max depth for that!").setEphemeral(true).queue();
            }
        }
    }

    public void onModalInteraction(ModalInteractionEvent event, String id, EmbedMenu menu) {
        if (id.equals("mineModal")) {
            int x = Integer.parseInt(event.getValue("x-coordinate").getAsString()) - 1;
            int y = Integer.parseInt(event.getValue("y-coordinate").getAsString()) - 1;

            try {
                Ores minedOre = mineOre(x, y);
                EmbedBuilder builder = EmbedBuilder.fromData(getEmbed().toData());
                builder.setDescription("You mined **" + minedOre.name() + "** and received **" + minedOre.stars + "** Stars!\n\n" + getEmbed().getDescription());
                event.editMessageEmbeds(builder.build()).queue();
            } catch (MineException e) {
                event.reply(e.getMessage()).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public String getDescription() {
        StarProfile starProfile = Main.INSTANCE.starProfileManager.getProfile(getGuildId(), getMemberId());
        Item pickaxe = starProfile.getItems().get(ItemType.PICKAXE);
        if (pickaxe == null) return null;

        if ((getLevel() * 6 + 5) != pickaxe.getStackSize()) {
            pickaxe.setStackSize(getLevel() * 6 + 5);
            starProfile.getItems().get(ItemType.PICKAXE).update();
        }

        StringBuilder description = new StringBuilder();

        description.append(":hole: Depth: ").append(depth).append("/").append(getLevel() * 20).append("\n")
                .append(":pick: Pickaxes: ").append(pickaxe.getAmount()).append("/").append(pickaxe.getStackSize()).append("\n\n**Mine:**\n");
        // Simple mine visualization with emoji
        for (int y = MINE_HEIGHT - 1; y >= 0; y--) {
            description.append(":number_" + (y + 1) + ":");
            for (int x = 0; x < MINE_WIDTH; x++) {
                description.append(ores[x][y].formatedEmoji);
            }
            description.append("\n");
        }
        description.append(":blue_square:");
        for (int i = 1; i <= MINE_WIDTH; i++) description.append(":number_" + i + ":");
        return description.toString();
    }

    @Override
    public String getUpgradeText() {
        switch (getLevel()) {
            case 0:
                return """
                        :pick: Daily pickaxes: **0 + 5 -> 5**
                        :hole: Max depth: **0 + 20 -> 20**
                        :pick: Max pickaxes: **5 + 6 -> 11**
                        """;
            case 1:
                return """
                        :pick: Daily pickaxes: **5 + 3 -> 8**
                        :hole: Max depth: **20 + 20 -> 40**
                        :pick: Max pickaxes: **11 + 6 -> 17**
                        """;
            case 2:
                return """
                        :pick: Daily pickaxes: **8 + 3 -> 11**
                        :hole: Max depth: **40 + 20 -> 60**
                        :pick: Max pickaxes: **17 + 6 -> 23**
                        """;
            case 3:
                return """
                        :pick: Daily pickaxes: **11 + 3 -> 14**
                        :hole: Max depth: **60 + 20 -> 80**
                        :pick: Max pickaxes: **23 + 6 -> 29**
                        """;
        }
        return "coming soon!";
    }

    public Ores quickMine() throws MineException {
        if (lastMineLocationX == 0 && lastMineLocationY == 0) {
            throw new MineException("You have to mine at least once before you can use quick mine!");
        }
        try {
            return mineOre(lastMineLocationX, lastMineLocationY-1);
        } catch (MineException e) {
            throw e;
        }
    }

    public Ores mineOre(int x, int y) throws MineException {
        if (getLevel() < 1) throw new MineException("You need at least level 1!");
        if (this.ores == null) generateMine();
        if (x < 0 || y < 0 || x >= MINE_WIDTH || y >= MINE_HEIGHT) {
            throw new MineException("Invalid coordinates!");
        }
        StarProfile starProfile = Main.INSTANCE.starProfileManager.getProfile(getGuildId(), getMemberId());
        Item pickaxe = starProfile.getItems().get(ItemType.PICKAXE);
        if (pickaxe == null)
            throw new MineException("Pickaxes could not be loaded! Please try again or contact a developer if this error persists!");
        if (pickaxe.getAmount() <= 0) {
            throw new MineException("You don't have a pickaxe!");
        }
        // checks if the mined ore is on the surface or else check if air is next by
        if (depth != MINE_HEIGHT || y != (MINE_HEIGHT - 1)) {
            List<Ores> neighbours = new ArrayList<>();
            if (y - 1 >= 0) neighbours.add(ores[x][y - 1]);
            if (y + 1 < MINE_HEIGHT) neighbours.add(ores[x][y + 1]);
            if (x - 1 >= 0) neighbours.add(ores[x - 1][y]);
            if (x + 1 < MINE_WIDTH) neighbours.add(ores[x + 1][y]);
            if (!neighbours.contains(Ores.AIR)) throw new MineException("Unreachable Ore!");
        }
        if (this.ores[x][y] != null && this.ores[x][y] != Ores.AIR) {
            Ores ore = ores[x][y];
            ores[x][y] = Ores.AIR;

            // shift the mine one up and add 1 to the depth if the 2. lowest ore is mined
            if (y <= 1) {
                if (depth + 1 > getLevel() * 20) {
                    throw new MineException("Maximal depth reached!");
                }
                depth++;
                shiftMineUp(generateMineRow(depth));
            }
            pickaxe.setAmount(pickaxe.getAmount() - 1);
            pickaxe.update();
            starProfile.setStars(starProfile.getStars() + ore.stars);
            Main.INSTANCE.starProfileManager.updateProfile(starProfile);
            save();

            lastMineLocationX = x;
            lastMineLocationY = y;
            if (y <= 1) lastMineLocationY = 2;

            return ore;
        }
        throw new MineException("Unavailable Mine Location!");
    }

    // shifts the mine one row down and generates a new row
    private void shiftMineUp(Ores[] newRow) {
        for (int y = MINE_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MINE_WIDTH; x++) {
                if (y == 0) {
                    ores[x][y] = newRow[x];
                    continue;
                }
                ores[x][y] = ores[x][y - 1];
            }
        }
    }

    // generates a row of ores based on the depth
    private Ores[] generateMineRow(int depth) {
        Ores[] ores = new Ores[MINE_WIDTH];
        double total = 0.0d;
        for (Ores ore : Ores.values()) total += ore.getProbability(depth);

        for (int i = 0; i < MINE_WIDTH; i++) {
            double random = Math.random();
            for (Ores ore : Ores.values()) {
                if (random < ore.getProbability(depth) / total) {
                    ores[i] = ore;
                    break;
                }
                random -= ore.getProbability(depth) / total;
            }
        }
        return ores;
    }

    // generates the ores starting from the top do the bottom using the generateMineRow function
    private void generateMine() {
        int d = 0;
        for (int y = MINE_HEIGHT - 1; y >= 0; y--) {
            Ores[] oresRow = generateMineRow(d);
            d++;
            for (int x = 0; x < MINE_WIDTH; x++) {
                ores[x][y] = oresRow[x];
            }
        }
        depth = d;
        save();
    }

    public enum Ores {
        AIR(0, 0, "<:air:1060322785555140740>", 0, 0, 0, 0, 0, 15),
        STONE(1, 5, "<:stone:1055867089602216018>", 0, 0, 75, 0.4d, 1.2d, 50),
        COAL(2, 10, "<:coal:1055867114428313600>", 0, 15, 50, 0.1d, 0.5d, 10),
        IRON(3, 20, "<:iron:1055867132384133210>", 5, 25, 100, 0.1d, 0.4d, 15),
        GOLD(4, 30, "<:gold:1055867149379452978>", 25, 50, 150, 0.01d, 0.2d, 20),
        DIAMOND(5, 50, "<:dia:1055867170128658526>", 30, 75, 500, 0.005d, 0.1d, 30),
        EMERALD(6, 75, "<:dia:1055867170128658526>", 40, 100, 500, 0.001d, 0.1d, 40),
        ;

        final int id;
        final int stars;
        final String formatedEmoji;
        final int minDepth;
        final int peakDepth;
        final int maxDepth;
        final double minValue;
        final double maxValue;
        final double width;


        Ores(int id, int stars, String formatedEmoji, int minDepth, int peakDepth, int maxDepth, double minValue, double maxValue, double width) {
            this.id = id;
            this.stars = stars;
            this.formatedEmoji = formatedEmoji;
            this.minDepth = minDepth;
            this.peakDepth = peakDepth;
            this.maxDepth = maxDepth;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.width = width;
        }

        public static Ores getById(int id) {
            for (Ores ore : Ores.values())
                if (ore.id == id) return ore;
            return null;
        }

        public double getProbability(int depth) {
            if (depth < minDepth || depth > maxDepth) return minValue;
            double gaussian = maxValue * Math.exp(-Math.pow(depth - (double) peakDepth, 2) / (2 * Math.pow(width, 2)));
            return Math.max(gaussian, minValue);
        }
    }

    public static class MineException extends Exception {
        public MineException(String message) {
            super(message);
        }
    }
}
