package de.delia.starBot.features.stars.town.menus;

import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.TownHall;
import de.delia.starBot.menus.CacheableEmbedMenu;
import de.delia.starBot.menus.EmbedMenu;
import de.delia.starBot.menus.EmbedMenuInstance;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TownMenu extends CacheableEmbedMenu {
    private final BuildingMenu buildingMenu;

    public TownMenu(JDA jda) {
        super("town", null);
        buildingMenu = new BuildingMenu(this);

        addSubMenu(buildingMenu);

        addEventListener(jda);
    }

    public EmbedBuilder getEmbed(Member member, Guild guild, Channel channel, List<Building> buildings) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Town")
                .setColor(Color.cyan)
                .setAuthor(member.getUser().getName(), null, member.getUser().getAvatarUrl())
                .setTimestamp(Instant.now());

        for (Building building : buildings) {
            embedBuilder.addField(building.getIcon().getFormatted() + " **" + building.getName() + "**", "Level: " + building.getLevel(), false);
        }
        return embedBuilder;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getSelectMenu().getId().startsWith(getId())) return;

        if (event.getInteraction().getValues().isEmpty()) return;
        String value = event.getInteraction().getValues().get(0);

        TownMenuInstance<TownMenu> townMenuInstance = (TownMenuInstance<TownMenu>) getInstance(generateCacheKey(event.getMember(), event.getGuild()));
        if (townMenuInstance == null || townMenuInstance.isExpired(10 * 60 * 1000)) {
            event.reply("This Menu is expired!!").setEphemeral(true).queue();
            return;
        }

        for (Building building : townMenuInstance.getBuildings()) {
            if (building.getName().equals(value)) {
                EmbedMenuResponse response = buildingMenu.generate(event.getMember(), event.getGuild(), event.getChannel());

                List<ActionRow> actionRows = response.getActionRows();
                List<ItemComponent> components = actionRows.get(0).getComponents();
                components.add(Button.success(getId() + ":upgrade:" + building.getName(), Emoji.fromUnicode("U+23EB")));
                actionRows.set(0, ActionRow.of(components));
                if (building.getActionRow() != null) actionRows.add(building.getActionRow());

                actionRows.add(ActionRow.of(getCustomNavigator(townMenuInstance.buildings)));

                event.editMessageEmbeds(building.getEmbed()).setComponents(actionRows).queue();
                return;
            }
        }
    }

    @Override
    public EmbedMenuResponse generate(Member member, Guild guild, Channel channel) {
        TownMenuInstance<TownMenu> townMenuInstance = (TownMenuInstance<TownMenu>) instanceCache.get(generateCacheKey(member, guild));

        if (townMenuInstance != null && !townMenuInstance.isExpired(DEFAULT_CACHE_TTL)) {
            List<Building> buildings = townMenuInstance.getBuildings();

            EmbedBuilder embedBuilder = townMenuInstance.getEmbedBuilder();

            return new EmbedMenuResponse(embedBuilder.build(), Collections.singletonList(ActionRow.of(getCustomNavigator(buildings))), channel, member, guild);
        }

        TownHall townHall = (TownHall) Building.loadBuilding(TownHall.class, guild.getIdLong(), member.getIdLong());

        if (townHall == null) {
            townHall = (TownHall) Building.create(TownHall.class, guild.getIdLong(), member.getIdLong());
        }
        if (townHall == null) return null;
        if (townHall.getLevel() == 0) {
            townHall.setLevel(1);
            townHall.save();
        }

        List<Building> buildings = new ArrayList<>();
        buildings.add(townHall);
        buildings.addAll(townHall.getTown());

        EmbedBuilder embedBuilder = getEmbed(member, guild, channel, buildings);

        newInstance(generateCacheKey(member, guild), new TownMenuInstance<>(this, member, guild, embedBuilder, Collections.singletonList(ActionRow.of(getCustomNavigator(buildings))), System.currentTimeMillis(), townHall, buildings));

        return new EmbedMenuResponse(embedBuilder.build(), Collections.singletonList(ActionRow.of(getCustomNavigator(buildings))), channel, member, guild);
    }

    public StringSelectMenu getCustomNavigator(List<Building> buildings) {
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create(getId() + ":customNavigator");
        selectMenu.setPlaceholder("navigate");
        selectMenu.setMaxValues(1);

        for (Building building : buildings) {
            selectMenu.addOption(building.getName(), building.getName());
        }

        return selectMenu.build();
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith(getId() + ":upgrade")) {
            String value = event.getButton().getId().split(":")[event.getButton().getId().split(":").length - 1];

            EmbedMenuInstance<?> instance = getInstance(generateCacheKey(event.getMember(), event.getGuild()));
            if (instance == null || instance.isExpired(DEFAULT_CACHE_TTL)) {
                regenerate(event.getMember(), event.getGuild(), event.getChannel());
                instance = getInstance(generateCacheKey(event.getMember(), event.getGuild()));
            }

            if (!(instance instanceof TownMenuInstance<?> townMenuInstance)) return;

            for (Building building : townMenuInstance.getBuildings()) {
                if (building.getName().equals(value)) {
                    try {
                        if (building.upgrade()) {
                            event.reply(building.getName() + " is upgraded to level **" + building.getLevel() + "**").setEphemeral(true).queue();

                            EmbedMenuResponse response = regenerate(event.getMember(), event.getGuild(), event.getChannel());

                            event.getMessage().editMessageEmbeds(building.getEmbed()).queue();
                        } else {
                            event.reply("Something went wrong!").setEphemeral(true).queue();
                        }
                    } catch (Building.UpgradeFailedException e) {
                        event.reply(e.getMessage()).setEphemeral(true).queue();
                    }
                }
            }
        }
        if (event.getButton().getId().startsWith(getId() + ":buildings")) {
            String buildingName = event.getButton().getId().split(":")[event.getButton().getId().split(":").length - 2];
            String id = event.getButton().getId().split(":")[event.getButton().getId().split(":").length - 1];

            EmbedMenuInstance<?> instance = getInstance(generateCacheKey(event.getMember(), event.getGuild()));
            if (instance == null || instance.isExpired(DEFAULT_CACHE_TTL)) {
                event.reply("This Menu expired!").setEphemeral(true).queue();
                return;
            }
            if (!(instance instanceof TownMenuInstance<?> townMenuInstance)) return;
            townMenuInstance.getBuildings().stream().filter(building -> building.getName().equals(buildingName)).findFirst().ifPresent(building -> {
                building.onButtonInteraction(event, id);
            });
        }
        super.onButtonInteraction(event);
    }


    @Getter
    public static class TownMenuInstance<T extends TownMenu> extends EmbedMenuInstance<TownMenu> {
        private final TownHall townHall;
        private final List<Building> buildings;

        public TownMenuInstance(TownMenu embedMenu, Member member, Guild guild, EmbedBuilder embedBuilder, List<ActionRow> actionRows, long timestamp, TownHall townHall, List<Building> buildings) {
            super(embedMenu, member, guild, embedBuilder, actionRows, timestamp);
            this.townHall = townHall;
            this.buildings = buildings;
        }
    }


    public static class BuildingMenu extends EmbedMenu {
        public BuildingMenu(EmbedMenu parent) {
            super("buildingMenu", parent);

            this.addBackButton();
        }

        public StringSelectMenu getNavigator() {
            return null;
        }
    }
}
