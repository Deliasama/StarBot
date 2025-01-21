package de.delia.starBot.main;

import de.delia.starBot.guildConfig.Configs;
import de.delia.starBot.guildConfig.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DiscordLogging {
    public final Bot bot;
    private final Map<Long, Long> logChannel = new HashMap<>();

    public DiscordLogging(Bot bot) {
        this.bot = bot;
    }

    public void updateLogChannel(long guildId, Long channelId) {
        logChannel.put(guildId, channelId);
    }

    public Optional<TextChannel> getLogChannel(Long guildId) {
        if (logChannel.containsKey(guildId))
            return Optional.ofNullable(bot.jda.getTextChannelById(logChannel.get(guildId)));

        GuildConfig guildConfig = GuildConfig.getGuildConfig(guildId);
        if (guildConfig == null) return Optional.empty();

        if (!(boolean) guildConfig.getConfig(Configs.ENABLE_LOG)) return Optional.empty();

        Long channelId = (Long) guildConfig.getConfig(Configs.LOG_CHANNEL);
        if (channelId == null) return Optional.empty();
        TextChannel channel = bot.jda.getTextChannelById(channelId);
        if (channel == null) return Optional.empty();

        logChannel.put(guildId, channelId);
        return Optional.of(channel);
    }

    public void log(long guildId, LoggingType type, String title, String message) {
        getLogChannel(guildId).ifPresent(c -> {
            EmbedBuilder logEmbed = new EmbedBuilder()
                    .setColor(type.color)
                    .setTitle(title)
                    .setDescription(message)
                    .setTimestamp(Instant.now());

            c.sendMessageEmbeds(logEmbed.build()).queue();
        });
    }

    public enum LoggingType {
        INFO(Color.GREEN),
        WARN(Color.YELLOW),
        ERROR(Color.RED),
        ;

        final Color color;

        LoggingType(Color color) {
            this.color = color;
        }
    }
}
