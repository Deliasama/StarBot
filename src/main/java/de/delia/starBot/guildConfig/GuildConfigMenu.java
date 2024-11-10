package de.delia.starBot.guildConfig;

import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class GuildConfigMenu extends EmbedMenu {
    public GuildConfigMenu(JDA jda) {
        super("guildConfig", null);

        addEmbedFunction(e -> {
            return new EmbedBuilder()
                    .setTitle("**Guild Config**")
                    .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getAvatarUrl())
                    .setDescription("Use the Navigator on the bottom to navigate through the server-config!")
                    .setColor(Color.CYAN)
                    .setTimestamp(Instant.now());
        });

        // Trading Config
        addSubMenu(new TradeMenu(this, jda));

        // Star Drop Config
        addSubMenu(new StarDropMenu(this, jda));

        addEventListener(jda);
    }

    @Override
    public void onMenuGenerate(OpenEmbedMenuEvent event) {
        super.onMenuGenerate(event);
    }

    public static class TradeMenu extends EmbedMenu {
        public TradeMenu(EmbedMenu parent, JDA jda) {
            super("Trading", parent);

            addEmbedFunction(e -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(e.getGuild().getIdLong());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getAvatarUrl())
                        .setTitle("**Trading**")
                        .setColor(Color.CYAN)
                        .addField((guildConfig.getConfig("enableStock", Boolean.class) ? ":green_circle:" : ":red_circle:") + " Enable Beta Stock Trading", "Enables the Experimental Trading System", true)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });
            addButton(Button.primary("toggleEnableBetaStock", "Toggle Stock"), (event, menu) -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                guildConfig.setConfig("enableStock", String.valueOf(!guildConfig.getConfig("enableStock", Boolean.class)));

                guildConfig.update();

                event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();
            });
            addBackButton();
        }
    }

    public static class StarDropMenu extends EmbedMenu {
        public StarDropMenu(EmbedMenu parent, JDA jda) {
            super("StarDrop", parent);
            addEmbedFunction(e -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(e.getGuild().getIdLong());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getAvatarUrl())
                        .setTitle("**StarDrops**")
                        .setColor(Color.CYAN)
                        .addField((guildConfig.getConfig("enableStarDrop", Boolean.class) ? ":green_circle:" : ":red_circle:") + " Enable StarDrops", "Enables Star Drops", false)
                        .addField(":gear: Star Drop rarity:", guildConfig.getConfig("starDropMessageMin", Integer.class) + "-" + guildConfig.getConfig("starDropMessageMax", Integer.class) + " messages", false)
                        .addField(":gear: Blacklisted Channel", "Change this setting in the Dropdown below!", false)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });
            addButton(Button.primary("toggleEnableStarDrops", "Toggle StarDrops"), (event, menu) -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                guildConfig.setConfig("enableStarDrop", String.valueOf(!guildConfig.getConfig("enableStarDrop", Boolean.class)));

                guildConfig.update();

                event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();
            });
            addEntitySelectMenu(
                    EntitySelectMenu.create("settingStarDropBC", EntitySelectMenu.SelectTarget.CHANNEL)
                            .setChannelTypes(ChannelType.TEXT)
                            .setMaxValues(10)
                            .setMinValues(0)
                            .setPlaceholder("Blacklisted Channel")
                            .setDefaultValues()
                            .build(), (event, menu) -> {
                        if (event instanceof EntitySelectInteractionEvent e) {
                            // load Guild Config
                            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                            guildConfig.setConfigList("starDropBlacklistedChannel", e.getValues().stream().map(IMentionable::getIdLong).toList());
                            guildConfig.update();

                            if (e.getInteraction().getValues().isEmpty()) return;

                            e.reply(e.getInteraction().getValues().get(e.getInteraction().getValues().size() - 1).getAsMention() + " is now Blacklisted!").queue();
                        }
                    });
            addBackButton();
        }

        @Override
        public void onMenuGenerate(OpenEmbedMenuEvent event) {
            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

            List<Long> channel = guildConfig.getConfigList("starDropBlacklistedChannel", Long.class);

            List<EntitySelectMenu.DefaultValue> defaultValues = channel.stream().map(EntitySelectMenu.DefaultValue::channel).toList();

            setSelectMenu(((EntitySelectMenu) getSelectMenu()).createCopy().setDefaultValues(defaultValues).build());
        }
    }
}
