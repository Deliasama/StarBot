package de.delia.starBot.guildConfig;

import de.delia.starBot.menus.EmbedMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        addSubMenu(
            new EmbedMenu("stars", this)
                .addEmbedFunction(e -> {
                    // load Guild Config
                    GuildConfig guildConfig = GuildConfig.getConfig(e.getGuild().getIdLong());

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setAuthor(e.getMember().getEffectiveName(), null, e.getMember().getUser().getAvatarUrl())
                            .setTitle("**Stars**")
                            .setColor(Color.CYAN)
                            .addField("Config:", (guildConfig.isEnableStarDrop()?":green_circle:":":red_circle:") + " Enable StarDrops\n" + (guildConfig.isEnableStock()?":green_circle:":":red_circle:") + " Enable Beta Stock Trading", true)
                            .setFooter("Green = Enabled, Red = Disabled")
                            .setTimestamp(Instant.now());

                    return embedBuilder;
                })
                .addButton(Button.primary("toggleEnableStarDrops", "Toggle StarDrops"), (event, menu) -> {
                    // load Guild Config
                    GuildConfig guildConfig = GuildConfig.getConfig(event.getGuild().getIdLong());

                    guildConfig.setEnableStarDrop(!guildConfig.isEnableStarDrop());

                    guildConfig.update();

                    event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();
                })
                .addButton(Button.primary("toggleEnableBetaStock", "Toggle Stock"), (event, menu) -> {
                    // load Guild Config
                    GuildConfig guildConfig = GuildConfig.getConfig(event.getGuild().getIdLong());

                    guildConfig.setEnableStock(!guildConfig.isEnableStock());

                    guildConfig.update();

                    event.editMessageEmbeds(menu.getEmbed(event.getMember(), event.getGuild(), event.getChannel()).get().build()).queue();
                })
        );

        addEventListener(jda);
    }
}
