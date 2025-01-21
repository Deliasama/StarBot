package de.delia.starBot.features.birthday;

import de.delia.starBot.guildConfig.Configs;
import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.Main;
import de.delia.starBot.main.StarBotListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BirthdayManager {
    public BirthdayManager(Bot bot) {
        ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
        ZonedDateTime next6am = now.withHour(6).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(next6am)) {
            next6am = next6am.plusDays(1);
        }

        Main.scheduler.scheduleAtFixedRate(() -> {
            List<Birthday> havingBirthday = bot.birthdayTable.findAllWithBirthdayAtDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());

            havingBirthday.forEach(b -> {
                long guildId = b.getGuildId();
                long memberId = b.getMemberId();


                Guild guild = bot.jda.getGuildById(guildId);
                if (guild != null) {
                    GuildConfig config = GuildConfig.getGuildConfig(guildId);
                    if (config != null) {
                        Long birthdayChannelId = (Long) config.getConfig(Configs.BIRTHDAY_CHANNEL);
                        if (birthdayChannelId != null) {
                            TextChannel textChannel = guild.getTextChannelById(birthdayChannelId);
                            if (textChannel != null) {
                                guild.retrieveMemberById(memberId).queue(m -> {
                                    textChannel.sendMessage(String.format("\uD83C\uDF89 Happy Birthday, %s! \uD83C\uDF89", m.getAsMention())).queue();
                                    StarBotListenerAdapter.callMemberBirthday(guild, m, b);
                                });
                            }
                        }
                    }
                }
            });
        }, Duration.between(now, next6am).toMillis(), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }
}
