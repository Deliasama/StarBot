package de.delia.starBot.features.stars.town;

import de.delia.starBot.features.stars.tables.BuildingEntity;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Main;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public abstract class Building implements Comparable<Building> {
    final Map<Integer, Integer[]> upgradeRequirements;
    private final int id;
    private final String name;
    private final Emoji icon;
    private final long guildId;
    private final long memberId;
    @Setter
    private int level;

    Building(int id, String name, Emoji icon, long guildId, long memberId, int level, String metaData, Map<Integer, Integer[]> upgradeRequirements) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.guildId = guildId;
        this.memberId = memberId;
        this.level = level;
        this.upgradeRequirements = upgradeRequirements;

        readMetaData(metaData);
    }

    public static List<Building> loadBuildings(long guildId, long memberId) {
        List<BuildingEntity> buildingEntities = Main.INSTANCE.buildingTable.getBuildings(guildId, memberId);

        List<Building> buildings = new ArrayList<>();

        for (BuildingEntity buildingEntity : buildingEntities) {
            try {
                buildings.add((Building) Class.forName(buildingEntity.getType()).getConstructor(BuildingEntity.class).newInstance(buildingEntity));
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return buildings;
    }

    public static Building loadBuilding(Class<? extends Building> type, long guildId, long memberId) {
        Optional<BuildingEntity> ob = Main.INSTANCE.buildingTable.get(guildId, memberId, type.getName());

        if (ob.isPresent()) {
            try {
                return type.getConstructor(BuildingEntity.class).newInstance(ob.get());
            } catch (NoSuchMethodException e) {
                System.err.println("Could not find constructor for " + type.getName());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static Building create(Class<? extends Building> type, long guildId, long memberId) {
        BuildingEntity buildingEntity = Main.INSTANCE.buildingTable.save(new BuildingEntity(memberId, guildId, type.getName(), 0, ""));
        try {
            return type.getConstructor(BuildingEntity.class).newInstance(buildingEntity);
        } catch (NoSuchMethodException e) {
            System.err.println("Could not find constructor for " + type.getName());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void save() {
        Optional<BuildingEntity> ob = Main.INSTANCE.buildingTable.get(guildId, memberId, this.getClass().getName());
        if (ob.isPresent()) {
            BuildingEntity be = ob.get();
            be.setLevel(level);
            be.setMetadata(writeMetaData());
            Main.INSTANCE.buildingTable.update(be);
        }
    }

    public boolean upgrade() throws UpgradeFailedException {
        if (upgradeRequirements.containsKey(level + 1)) {
            TownHall townHall = (TownHall) Building.loadBuilding(TownHall.class, guildId, memberId);
            if (townHall == null) return false;
            if (!(townHall.getLevel() >= upgradeRequirements.get(level + 1)[0])) {
                throw new UpgradeFailedException("You need to upgrade your townhall for that!");
            }
            StarProfile starProfile = StarProfile.getTable().get(guildId, memberId);
            if (starProfile == null) return false;
            if (!(starProfile.getStars() >= upgradeRequirements.get(level + 1)[1])) {
                throw new UpgradeFailedException("You don't have enough stars!");
            }
            starProfile.addStars(upgradeRequirements.get(level + 1)[1] * -1);
            Main.INSTANCE.starProfileTable.update(starProfile);
            level++;
            save();
            return true;
        }
        return false;
    }

    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(icon.getFormatted() + " " + name + " (lvl. " + level + ")")
                .setColor(Color.cyan)
                .setDescription(getDescription())
                .addField(":arrow_double_up: Upgrade (" + upgradeRequirements.get(level + 1)[1] + " Stars)", getUpgradeText(), false);

        return embedBuilder.build();
    }

    public abstract void readMetaData(String metaData);

    public abstract String writeMetaData();

    public abstract void onButtonInteraction(ButtonInteractionEvent buttonInteractionEvent, String id);

    public abstract String getDescription();

    public abstract String getUpgradeText();

    @Override
    public int compareTo(@NotNull Building o) {
        return id - o.getId();
    }

    public static class UpgradeFailedException extends Exception {
        public UpgradeFailedException(String message) {
            super(message);
        }
    }
}
