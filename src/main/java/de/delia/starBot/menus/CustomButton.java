package de.delia.starBot.menus;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.function.BiConsumer;

public class CustomButton extends ListenerAdapter {
    private final Button button;
    private BiConsumer<ButtonInteractionEvent, String> clickMethod = null;

    public CustomButton(Button button, BiConsumer<ButtonInteractionEvent, String> clickMethod) {
        this.button = button;
        this.clickMethod = clickMethod;
    }

    public CustomButton addEventListener(JDA jda) {
        jda.addEventListener(this);
        return this;
    }

    public CustomButton removeEventListener(JDA jda) {
        jda.removeEventListener(this);
        return this;
    }

    public void handle(ButtonInteractionEvent event, String metaData) {
        if (clickMethod != null) clickMethod.accept(event, metaData);
    }

    public Button getButton() {
        return button;
    }

    public Button getButton(String metaData) {
        return button.withId(button.getId() + "_" + metaData);
    }

    public String getButtonId() {
        return button.getId();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().split("_")[0].equals(button.getId())) {
            String metaData = event.getButton().getId().replaceFirst(event.getButton().getId().split("_")[0], "");
            handle(event, metaData);
        }
    }
}