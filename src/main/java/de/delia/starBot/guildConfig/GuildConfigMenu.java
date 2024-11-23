package de.delia.starBot.guildConfig;

import de.delia.starBot.main.DiscordLogging;
import de.delia.starBot.main.Main;
import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

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

        // Log Config
        addSubMenu(new LogSettingsMenu(this));

        // Birthday Config
        addSubMenu(new BirthdayMenu(this));

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
                        .addField(((boolean) guildConfig.getConfig(Configs.ENABLE_STOCK) ? ":green_circle:" : ":red_circle:") + " Enable Beta Stock Trading", "Enables the Experimental Trading System", true)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });
            addButton(Button.primary("toggleEnableBetaStock", "Toggle Stock"), (event, menu) -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                guildConfig.setConfig(Configs.ENABLE_STOCK, String.valueOf(!guildConfig.getConfigAsBoolean(Configs.ENABLE_STOCK)));
                guildConfig.update();

                event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();

                // Logging
                Main.INSTANCE.discordLogging.log(event.getGuild().getIdLong(), DiscordLogging.LoggingType.WARN, "Config changed",
                        event.getMember().getAsMention() + (guildConfig.getConfigAsBoolean(Configs.ENABLE_STOCK) ? " enabled" : " disabled") + " Beta Stock Trading!");
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
                        .addField(((boolean) guildConfig.getConfig(Configs.ENABLE_STAR_DROP) ? ":green_circle:" : ":red_circle:") + " Enable StarDrops", "Enables Star Drops", false)
                        .addField(":gear: Star Drop rarity:", (int) guildConfig.getConfig(Configs.STAR_DROP_MESSAGE_MIN) + "-" + (int) guildConfig.getConfig(Configs.STAR_DROP_MESSAGE_MAX) + " messages", false)
                        .addField(":gear: Blacklisted Channel", "Change this setting in the Dropdown below!", false)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });
            addButton(Button.primary("toggleEnableStarDrops", "Toggle StarDrops"), (event, menu) -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                guildConfig.setConfig(Configs.ENABLE_STAR_DROP, String.valueOf(!guildConfig.getConfigAsBoolean(Configs.ENABLE_STAR_DROP)));

                guildConfig.update();

                event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();

                // Logging
                Main.INSTANCE.discordLogging.log(event.getGuild().getIdLong(), DiscordLogging.LoggingType.WARN, "Config changed",
                        event.getMember().getAsMention() + (guildConfig.getConfigAsBoolean(Configs.ENABLE_STOCK) ? " enabled" : " disabled") + " StarDrops!");
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

                            e.reply(e.getInteraction().getValues().get(e.getInteraction().getValues().size() - 1).getAsMention() + " is now Blacklisted!").setEphemeral(true).queue();
                        }
                    });
            addButton(Button.primary("changeStarDropRarity", "Set StarDrop rarity"), (event, menu) -> {
                getModal("starDropRarity").ifPresent(m -> {
                    event.replyModal(m).queue();
                });
            });
            addModal(
                    Modal.create("starDropRarity", "Set StarDrop rarity")
                            .addComponents(
                                    ActionRow.of(TextInput.create("minMessages", "Min-Messages", TextInputStyle.SHORT).setMinLength(1).setMaxLength(3).setRequired(true).build()),
                                    ActionRow.of(TextInput.create("maxMessages", "Max-Messages", TextInputStyle.SHORT).setMinLength(1).setMaxLength(4).setRequired(true).build())
                            )
                            .build(), (e, m) -> {
                        int minStars = Integer.parseInt(e.getValue("minMessages").getAsString());
                        int maxStars = Integer.parseInt(e.getValue("maxMessages").getAsString());

                        if (minStars <= 0 || maxStars <=0 || minStars > maxStars) {
                            e.reply("Please check your inputs and try it again!").setEphemeral(true).queue();
                            return;
                        }
                        // load Guild Config
                        GuildConfig guildConfig = GuildConfig.getGuildConfig(e.getGuild().getIdLong());
                        guildConfig.setConfig(Configs.STAR_DROP_MESSAGE_MIN, String.valueOf(minStars));
                        guildConfig.setConfig(Configs.STAR_DROP_MESSAGE_MAX, String.valueOf(maxStars));
                        guildConfig.update();

                        e.editMessageEmbeds(m.getEmbed(e.getMember(), e.getGuild(), e.getChannel()).get().build()).queue();

                        // Logging
                        Main.INSTANCE.discordLogging.log(e.getGuild().getIdLong(), DiscordLogging.LoggingType.WARN, "Config changed",
                                e.getMember().getAsMention() + " set StarDrop rarity: **" + minStars + "** to **" + maxStars + "**");

                    });
            addBackButton();
        }

        @Override
        public void onMenuGenerate(OpenEmbedMenuEvent event) {
            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

            List<Long> channel = (List<Long>) guildConfig.getConfigList(Configs.STAR_DROP_BLACKLISTED_CHANNEL);

            List<EntitySelectMenu.DefaultValue> defaultValues = channel.stream().map(EntitySelectMenu.DefaultValue::channel).toList();

            setSelectMenu(((EntitySelectMenu) getSelectMenu()).createCopy().setDefaultValues(defaultValues).build());
        }
    }

    public static class LogSettingsMenu extends EmbedMenu {

        public LogSettingsMenu(EmbedMenu parent) {
            super("LogSettings", parent);

            addEmbedFunction(e -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(e.getGuild().getIdLong());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getEffectiveAvatarUrl())
                        .setTitle("**LogSettings**")
                        .setColor(Color.CYAN)
                        .addField((((boolean) guildConfig.getConfig(Configs.ENABLE_LOG)) ? ":green_circle:" : ":red_circle:") + " Enable Logging", "Enables Logging on this Server!", false)
                        .addField(":gear: Log-Channel", "Change the Channel in the Dropdown below!", false)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });

            addButton(Button.primary("toggleEnableLog", "Toggle Logging"), (event, menu) -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                guildConfig.setConfig(Configs.ENABLE_LOG, String.valueOf(!guildConfig.getConfigAsBoolean(Configs.ENABLE_LOG)));

                guildConfig.update();

                event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();
            });

            addEntitySelectMenu(
                    EntitySelectMenu.create("settingLogChannel", EntitySelectMenu.SelectTarget.CHANNEL)
                            .setChannelTypes(ChannelType.TEXT)
                            .setMaxValues(1)
                            .setMinValues(1)
                            .setPlaceholder("Logging Channel")
                            .setDefaultValues()
                            .build(), (event, menu) -> {
                        if (event instanceof EntitySelectInteractionEvent e) {
                            // load Guild Config
                            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                            guildConfig.setConfig(Configs.LOG_CHANNEL, String.valueOf(e.getValues().get(0).getIdLong()));
                            guildConfig.update();
                            Main.INSTANCE.discordLogging.updateLogChannel(event.getGuild().getIdLong(), e.getValues().get(0).getIdLong());

                            if (e.getInteraction().getValues().isEmpty()) return;

                            e.reply(e.getInteraction().getValues().get(0).getAsMention() + " is now the new Log-Channel!").setEphemeral(true).queue();
                        }
                    });
            addBackButton();
        }

        @Override
        public void onMenuGenerate(OpenEmbedMenuEvent event) {
            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

            Long channelId = (Long) guildConfig.getConfig(Configs.LOG_CHANNEL);

            if(channelId != null) {
                TextChannel channel = event.getGuild().getTextChannelById(channelId);
                if(channel != null) {
                    EntitySelectMenu.DefaultValue value = EntitySelectMenu.DefaultValue.channel(channelId);
                    setSelectMenu(((EntitySelectMenu) getSelectMenu()).createCopy().setDefaultValues(value).build());
                }
            }
        }
    }

    public static class BirthdayMenu extends EmbedMenu {
        public BirthdayMenu(EmbedMenu parent) {
            super("Birthday", parent);

            addEmbedFunction(e -> {
                // load Guild Config
                GuildConfig guildConfig = GuildConfig.getGuildConfig(e.getGuild().getIdLong());

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getEffectiveAvatarUrl())
                        .setTitle("**Birthday Settings**")
                        .setColor(Color.CYAN)
                        .addField(":gear: Birthday-Channel", "Change the Channel where birthdays get mentioned!", false)
                        .setFooter("Green = Enabled, Red = Disabled")
                        .setTimestamp(Instant.now());

                return embedBuilder;
            });

            addEntitySelectMenu(
                    EntitySelectMenu.create("settingBirthdayChannel", EntitySelectMenu.SelectTarget.CHANNEL)
                            .setChannelTypes(ChannelType.TEXT)
                            .setMaxValues(1)
                            .setMinValues(1)
                            .setPlaceholder("Birthday Channel")
                            .setDefaultValues()
                            .build(), (event, menu) -> {
                        if (event instanceof EntitySelectInteractionEvent e) {
                            // load Guild Config
                            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

                            guildConfig.setConfig(Configs.BIRTHDAY_CHANNEL, String.valueOf(e.getValues().get(0).getIdLong()));
                            guildConfig.update();

                            if (e.getInteraction().getValues().isEmpty()) return;

                            e.reply(e.getInteraction().getValues().get(0).getAsMention() + " is now the new Birthday-Channel!").setEphemeral(true).queue();
                        }
                    });
            addBackButton();
        }

        @Override
        public void onMenuGenerate(OpenEmbedMenuEvent event) {
            GuildConfig guildConfig = GuildConfig.getGuildConfig(event.getGuild().getIdLong());

            Long channelId = (Long) guildConfig.getConfig(Configs.BIRTHDAY_CHANNEL);

            if(channelId != null) {
                TextChannel channel = event.getGuild().getTextChannelById(channelId);
                if(channel != null) {
                    EntitySelectMenu.DefaultValue value = EntitySelectMenu.DefaultValue.channel(channelId);
                    setSelectMenu(((EntitySelectMenu) getSelectMenu()).createCopy().setDefaultValues(value).build());
                }
            }
        }
    }
}
