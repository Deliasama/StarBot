package de.delia.starBot.features.stars.town;

import de.delia.starBot.features.stars.tables.BuildingEntity;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Map;

public class Wall extends Building {
    public Wall(BuildingEntity buildingEntity) {
        super(3, "Wall", Emoji.fromFormatted(":shield:"), buildingEntity.getGuildId(), buildingEntity.getMemberId(), buildingEntity.getLevel(), buildingEntity.getMetadata(), Map.of(
                // 1. value: Building Level, 2. value: needed Townhall level, 3. value: price
                1, new Integer[] {2, 100},
                2, new Integer[] {2, 200},
                3, new Integer[] {3, 400},
                4, new Integer[] {3, 800},
                5, new Integer[] {4, 1600},
                6, new Integer[] {4, 3200})
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
        return "The Wall stands as your first line of defense, protecting your stars from being stolen. With each upgrade, it reduces the amount of stars stolen by /rob!";
    }

    @Override
    public String getUpgradeText() {
        return switch (getLevel()) {
            case 0 -> ":shield: Rob-Reduction: **0.0% + 0.5% -> 0.5%**";
            case 1 -> ":shield: Rob-Reduction: **0.5% + 0.5% -> 1.0%**";
            case 2 -> ":shield: Rob-Reduction: **1.0% + 0.5% -> 1.5%**";
            case 3 -> ":shield: Rob-Reduction: **1.5% + 0.5% -> 2.0%**";
            case 4 -> ":shield: Rob-Reduction: **2.0% + 0.5% -> 2.5%**";
            case 5 -> ":shield: Rob-Reduction: **2.5% + 0.5% -> 3.0%**";
            default -> ":x:";
        };
    }
}
