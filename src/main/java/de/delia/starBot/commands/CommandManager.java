package de.delia.starBot.commands;

import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    public final Bot bot;
    private final Map<String, Command> commands;
    private final Map<String, Command<?>> applicationCommands;

    public CommandManager(Bot bot) {
        this.bot = bot;
        this.commands = new HashMap<>();
        this.applicationCommands = new HashMap<>();
    }

    public void executeCommand(String name, SlashCommandInteractionEvent event) {
        if (applicationCommands.containsKey(name)) {
            try {
                applicationCommands.get(name).performCommand(event);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> void registerCommand(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(ApplicationCommand.class)) return;
        ApplicationCommand applicationCommand = clazz.getAnnotation(ApplicationCommand.class);
        String name = applicationCommand.name();
        String description = applicationCommand.description();

        System.out.println("Registering command " + name + " " + description);

        if (applicationCommands.containsKey(name)) return;
        try {
            applicationCommands.put(name, new Command<T>(name, description, clazz, null, this));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertCommands(Guild guild) {
        for (Map.Entry<String, Command<?>> entry : applicationCommands.entrySet()) {
            entry.getValue().upsertCommand(guild);
        }
    }

    public <T> Object mapOption(OptionMapping mapping, T type) {
        if (type.equals(String.class)) {
            return mapping.getAsString();
        }
        if (type.equals(Integer.class)) {
            return mapping.getAsInt();
        }
        if (type.equals(Channel.class)) {
            return mapping.getAsChannel();
        }
        if (type.equals(Role.class)) {
            return mapping.getAsRole();
        }
        if (type.equals(User.class)) {
            return mapping.getAsUser();
        }
        return null;

    }

    public Object mapOption(OptionMapping mapping) {
        if (mapping == null) return null;

        if (mapping.getType().equals(OptionType.STRING)) {
            return mapping.getAsString();
        }
        if (mapping.getType().equals(OptionType.INTEGER)) {
            return mapping.getAsInt();
        }
        if (mapping.getType().equals(OptionType.CHANNEL)) {
            return mapping.getAsChannel();
        }
        if (mapping.getType().equals(OptionType.ROLE)) {
            return mapping.getAsRole();
        }
        if (mapping.getType().equals(OptionType.USER)) {
            return mapping.getAsUser();
        }
        return null;

    }

    public <T> OptionType mapToOption(T type) {
        if (type.equals(String.class)) {
            return OptionType.STRING;
        }
        if (type.equals(Integer.class)) {
            return OptionType.INTEGER;
        }
        if (type.equals(int.class)) {
            return OptionType.INTEGER;
        }
        if (type.equals(Channel.class)) {
            return OptionType.CHANNEL;
        }
        if (type.equals(Role.class)) {
            return OptionType.ROLE;
        }
        if (type.equals(User.class)) {
            return OptionType.USER;
        }
        return null;
    }


}
