package de.delia.starBot.menus;

import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class DropdownMenu extends ListenerAdapter {
    List<Option> options = new ArrayList<>();
    final String id;

    public DropdownMenu(Bot bot, String id) {
        this.id = id;

        bot.jda.addEventListener(this);

        Class<? extends DropdownMenu> clazz = this.getClass();

        for(Method method : clazz.getDeclaredMethods()) {
            if(method.isAnnotationPresent(MyStringSelectOption.class)) {
                MyStringSelectOption annotation = method.getAnnotation(MyStringSelectOption.class);
                options.add(new Option(annotation.value(), SelectOption.of(annotation.label(), annotation.value()), method));
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(event.getComponentId().equals(id)) {
            onInteraction(event);
            for(SelectOption o : event.getSelectedOptions()) {
                for(Option option : options) {
                    if(o.getValue().equals(option.value)) {
                        try {
                            option.method.invoke(this, event);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public abstract void onInteraction(StringSelectInteractionEvent event);

    public ItemComponent getItemComponent() {
        return StringSelectMenu.create(id)
                .addOptions(options.stream().map(o -> o.option).toList())
                .setMaxValues(1)
                .build();
    }

    class Option {
        String value;
        SelectOption option;
        Method method;

        public Option(String value, SelectOption option, Method method) {
            this.value = value;
            this.option = option;
            this.method = method;
        }
    }
}
