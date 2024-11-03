package de.delia.starBot.features.stars.town;

import de.delia.starBot.features.stars.tables.BuildingEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Map;

public class Telescope extends Building{
    public Telescope(BuildingEntity buildingEntity) {
        super(2, "Telescope", Emoji.fromFormatted(":telescope:"), buildingEntity.getGuildId(), buildingEntity.getMemberId(), buildingEntity.getLevel(), buildingEntity.getMetadata(),  Map.of(
                // 1. value: Building Level, 2. value: needed Townhall level, 3. value: price
                1, new Integer[] {2, 100},
                2, new Integer[] {2, 175},
                3, new Integer[] {3, 300},
                4, new Integer[] {3, 500})
        );
    }

    @Override
    public void readMetaData(String metaData) {

    }

    @Override
    public String writeMetaData() {
        return "";
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id) {

    }

    @Override
    public String getDescription() {
        return "The Telescope allows you to gaze into the vastness of space, discovering new stars and constellations.\n Upgrade it to earn more stars!";
    }

    @Override
    public String getUpgradeText() {
        return switch (getLevel()) {
            case 0 -> ":star: StarDrop-Multiplier: **1.0 + 0.5 -> 1.5**";
            case 1 -> ":star: StarDrop-Multiplier: **1.5 + 0.5 -> 2.0**";
            case 2 -> ":star: StarDrop-Multiplier: **2.0 + 0.5 -> 2.5**";
            case 3 -> ":star: StarDrop-Multiplier: **2.5 + 0.5 -> 3.0**";
            default -> ":x:";
        };
    }
}
