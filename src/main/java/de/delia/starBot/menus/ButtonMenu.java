package de.delia.starBot.menus;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ButtonMenu extends ListenerAdapter {

    public List<Button> buttons = new ArrayList<>();

    public ButtonMenu(JDA jda) {
        init();
        jda.addEventListener(this);
    }

    public abstract void init();

    public abstract void buttonInteraction(ButtonInteractionEvent event);

    public List<ItemComponent> getActionRow() {
        return buttons.stream().map(b -> (ItemComponent) b).toList();
    }

    public List<ItemComponent> getActionRow(List<String> metaData) {
        if(metaData.size() != buttons.size())return null;

        List<Button> buttonsWithCustomMetaData = new ArrayList<>();

        for (int i = 0; i< buttons.size(); i++) {
            buttonsWithCustomMetaData.add(buttons.get(i).withId(buttons.get(i).getId() + ":" + metaData.get(i)));
        }
        return buttonsWithCustomMetaData.isEmpty() ? null : buttonsWithCustomMetaData.stream().map(b -> (ItemComponent) b).toList();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(buttons.stream().anyMatch(b -> Objects.equals(b.getId(), event.getComponent().getId().split(":")[0]))) {
            buttonInteraction(event);
        }
    }
}
