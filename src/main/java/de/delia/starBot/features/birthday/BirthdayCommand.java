package de.delia.starBot.features.birthday;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.ApplicationCommandPermission;
import de.delia.starBot.commands.Option;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.Time;
import java.util.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationCommand(name = "birthday", description = "Set or delete your day of birth!")
public class BirthdayCommand {
    @ApplicationCommand(name = "set", description = "set your birthday!")
    public static class SetCommand {

        @ApplicationCommandMethod
        public void onCommand(Bot bot, SlashCommandInteractionEvent event, @Option(description = "Birthday Date (ex: '31-12' or '31-12-2000')!") String birthday) {
            String[] birthdayArgs = birthday.split("-");
            if (birthdayArgs.length != 2 && birthdayArgs.length != 3) {
                event.reply("Invalid birthday date!").setEphemeral(true).queue();
                return;
            }
            int day = Integer.parseInt(birthdayArgs[0]);
            int month = Integer.parseInt(birthdayArgs[1]);
            int year = 1;
            if (birthdayArgs.length == 3) year = Integer.parseInt(birthdayArgs[2]);

            if (day < 1 || month < 1 || day > 31 || month > 12 || year < 1 || year > LocalDate.now().getYear()) {
                event.reply("Invalid birthday date!").setEphemeral(true).queue();
                return;
            }

            LocalDate date = LocalDate.of(year, month, day);

            Birthday b = bot.birthdayTable.get(event.getGuild().getIdLong(), event.getUser().getIdLong());

            if (b.getTimesChanged() > 3) {
               event.reply("You Changed your birthday to many times!").setEphemeral(true).queue();
               return;
            }
            b.setBirthday(date);
            b.setTimesChanged(b.getTimesChanged() + 1);
            bot.birthdayTable.update(b);
            event.reply("Birthday set to "+ TimeFormat.DATE_LONG.atInstant(date.atStartOfDay(ZoneId.systemDefault()).toInstant()) + "!").setEphemeral(true).queue();

            // LOGGING
            bot.getLogChannel(event.getGuild().getIdLong()).ifPresent(channel -> {
                EmbedBuilder logEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
                        .setTitle("Birthday changed!")
                        .addField("New birthday", String.valueOf(TimeFormat.DATE_LONG.atInstant(date.atStartOfDay(ZoneId.systemDefault()).toInstant())), false)
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(logEmbed.build()).queue();
            });


        }
    }

    @ApplicationCommand(name = "delete", description = "delete your birthday")
    public static class DeleteCommand {

        @ApplicationCommandMethod
        public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
            Birthday b = bot.birthdayTable.get(event.getGuild().getIdLong(), event.getUser().getIdLong());
            b.setBirthday(null);
            bot.birthdayTable.update(b);
            event.reply("Deleted Birthday!").setEphemeral(true).queue();

            // LOGGING
            bot.getLogChannel(event.getGuild().getIdLong()).ifPresent(channel -> {
                EmbedBuilder logEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl())
                        .setTitle("Birthday deleted!")
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(logEmbed.build()).queue();
            });
        }
    }

    @ApplicationCommand(name = "list", description = "List birthdays!")
    public static class ListCommand {

        @ApplicationCommandMethod
        public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
            List<Birthday> birthdayList = bot.birthdayTable.getBirthdays(event.getGuild().getIdLong());

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("Birthdays")
                    .setTimestamp(Instant.now());

            Map<LocalDate, List<Long>> birthdayMap = new HashMap<>();
            birthdayList.forEach(b -> {
                LocalDate date = b.getBirthday();
                birthdayMap.computeIfAbsent(date, k -> new ArrayList<>()).add(b.getMemberId());
            });

            birthdayMap.forEach((d, m) -> {
                embed.addField(String.valueOf(TimeFormat.DATE_LONG.atInstant(d.atStartOfDay(ZoneId.systemDefault()).toInstant())), m.stream().map(u -> UserSnowflake.fromId(u).getAsMention()).collect(Collectors.joining("\n")), false);
            });

            event.replyEmbeds(embed.build()).queue();
        }
    }

    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {}
}