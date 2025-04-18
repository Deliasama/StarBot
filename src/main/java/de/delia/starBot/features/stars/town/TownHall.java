package de.delia.starBot.features.stars.town;

import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.Map;

public class TownHall extends Building {

    private static final Map<Class<? extends Building>, Integer> buildingUnlockTable = Map.of(
            Telescope.class, 2,
            Wall.class, 2,
            Mine.class, 3,
            Bank.class, 4
    );

    public TownHall(BuildingEntity buildingEntity) {
        super(1, "Townhall", Emoji.fromFormatted(":classical_building:"), buildingEntity.getGuildId(), buildingEntity.getMemberId(), buildingEntity.getLevel(), buildingEntity.getMetadata(), Map.of(
                // 1. value: Building Level, 2. value: needed Townhall level, 3. value: price
                2, new Integer[]{0, 100},
                3, new Integer[]{0, 400},
                4, new Integer[]{0, 1000},
                5, new Integer[]{0, 4000}
        ));
    }

    @Override
    public void readMetaData(String metaData) {
        // TH doesn't need MetaData
    }

    @Override
    public String writeMetaData() {
        // TH doesn't need MetaData
        return "";
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id, EmbedMenu menu) {

    }

    @Override
    public String getDescription() {
        // Returns the Embed Description
        return "This is the heart of your town! Upgrade it to unlock new Buildings and upgrades!";
    }

    @Override
    public String getUpgradeText() {
        switch (getLevel()) {
            case 1:
                return """
                        :gem: Daily reward: **10 + 10 -> 20**
                        :unlock: Unlocks:
                        > Wall
                        > Telescope""";
            case 2:
                return """
                        :gem: Daily reward: **20 + 10 -> 30**
                        :unlock: Unlocks:
                        > Mine""";
            case 3:
                return """
                        :gem: Daily reward: **30 + 10 -> 40**
                        :unlock: Unlocks:
                        > Bank""";
            case 4:
                return ":gem: Daily reward: **40 + 10 -> 50**";
        }
        return ":x:";
    }

    public List<Building> getTown() {
        List<Building> buildings = Building.loadBuildings(getGuildId(), getMemberId());

        buildingUnlockTable.entrySet().stream()
                .filter(entry -> entry.getValue() <= getLevel())
                .forEach(entry -> {
                    Class<? extends Building> type = entry.getKey();

                    if (buildings.stream().noneMatch(b -> b.getClass().equals(type))) {
                        Building building = Building.create(type, getGuildId(), getMemberId());
                        buildings.add(building);
                    }
                });

        return buildings.stream().sorted().filter(b -> b.getId() != 1).toList();
    }
}
