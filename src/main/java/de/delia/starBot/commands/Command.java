package de.delia.starBot.commands;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class Command<T> {

    @NotNull
    public final Class<T> clazz;
    @NotNull
    public final T object;

    public final List<OptionData> options;
    private final Collection<Permission> permissions = new ArrayList<>();
    private final CommandManager commandManager;
    @NotNull
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final String fullName;
    private final List<Command<?>> subCommands;

    private Method method = null;

    public Command(@NotNull String name, @NotNull String description, @NotNull Class<T> clazz, String commandParentName, CommandManager manager) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.name = name;
        this.description = description;
        this.clazz = clazz;
        this.options = new ArrayList<>();
        this.commandManager = manager;
        this.subCommands = new ArrayList<>();
        this.object = clazz.getDeclaredConstructor().newInstance();
        if (commandParentName == null) {
            fullName = name;
        } else {
            fullName = commandParentName + "." + name;
        }

        // finds subclasses annotated with @ApplicationCommand and creates a subcommand
        for (Class<?> c : clazz.getClasses()) {
            System.out.println(c.getName());
            if (c.isAnnotationPresent(ApplicationCommand.class)) {
                ApplicationCommand applicationCommand = c.getAnnotation(ApplicationCommand.class);
                String n = applicationCommand.name();
                String d = applicationCommand.description();

                subCommands.add(new Command<>(n, d, c, name, manager));
            }
        }

        // locates the method annotated with @ApplicationCommandMethod
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(ApplicationCommandMethod.class)) {
                if (method == null) {
                    method = m;

                    for (Parameter p : m.getParameters()) {
                        if (p.isAnnotationPresent(Option.class)) {
                            options.add(new OptionData(commandManager.mapToOption(p.getType()), p.getName(), p.getAnnotation(Option.class).description(), p.getAnnotation(Option.class).isRequired()));
                        }
                    }
                }
            }
        }

        // locates the field annotated with @ApplicationCommandPermission and sets the command permissions
        for (Field f : clazz.getDeclaredFields()) {
            if(f.isAnnotationPresent(ApplicationCommandPermission.class)) {
                if(Collection.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);

                    Collection<?> collection = (Collection<?>) f.get(object);

                    if(collection != null) {
                        if(!collection.isEmpty()) {
                            if(collection.iterator().next() instanceof Permission) {
                                permissions.addAll((Collection<? extends Permission>) collection);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }
                    }
                }
            }
        }
    }

    public void performCommand(SlashCommandInteractionEvent event) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (method == null) return;

        if (event.getSubcommandName() != null) {
            for (Command<?> c : subCommands) {
                if(c.name.equals(event.getSubcommandName())) {
                    try {
                        c.performCommand(event);
                        return;
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (options.isEmpty()) {
            method.invoke(object, commandManager.bot, event);
        } else {
            Object[] params = new Object[options.size() + 2];
            params[0] = commandManager.bot;
            params[1] = event;

            for (int i = 2; i < (options.size() + 2); i++) {
                params[i] = commandManager.mapOption(event.getOption(options.get(i - 2).getName()));
            }
            method.invoke(object, params);
        }
    }

    public void upsertCommand(Guild guild) {
        CommandCreateAction action = guild.upsertCommand(clazz.getAnnotation(ApplicationCommand.class).name(),
                clazz.getAnnotation(ApplicationCommand.class).description()
        );
        action = action.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        action = action.addOptions(options);
        for (Command<?> subCommand : subCommands) {
            action = action.addSubcommands(new SubcommandData(subCommand.name, subCommand.getDescription())
                    .addOptions(subCommand.options)
            );
        }
        action.queue();
    }
}