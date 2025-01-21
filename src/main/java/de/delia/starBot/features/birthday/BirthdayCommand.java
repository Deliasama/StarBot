package de.delia.starBot.features.birthday;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.commands.Option;
import de.delia.starBot.main.Bot;
import de.delia.starBot.main.DiscordLogging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationCommand(name = "birthday", description = "Set or delete your day of birth!")
public class BirthdayCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
    }

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
            event.reply("Birthday set to " + TimeFormat.DATE_LONG.atInstant(date.atStartOfDay(ZoneId.systemDefault()).toInstant()) + "!").setEphemeral(true).queue();

            // LOGGING
            bot.discordLogging.log(event.getGuild().getIdLong(), DiscordLogging.LoggingType.INFO, "Changed birthday",
                    event.getMember().getAsMention() + " changed their birthday date to: " + TimeFormat.DATE_LONG.atInstant(date.atStartOfDay(ZoneId.systemDefault()).toInstant()) + "!");
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
            bot.discordLogging.log(event.getGuild().getIdLong(), DiscordLogging.LoggingType.INFO, "Deleted Birthday", event.getMember().getAsMention() + " deleted their Birthday!");
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
}
