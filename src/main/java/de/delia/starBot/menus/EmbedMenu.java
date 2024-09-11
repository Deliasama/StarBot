package de.delia.starBot.menus;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EmbedMenu extends ListenerAdapter {
    @Getter
    private final String name;
    @Getter
    private final EmbedMenu parent;
    private final List<EmbedMenu> subMenus = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final Map<String, BiConsumer<ButtonInteractionEvent, EmbedMenu>> buttonEvents = new HashMap<>();
    private Function<OpenEmbedMenuEvent, EmbedBuilder> embedFunction;

    public EmbedMenu(String name, EmbedMenu parent) {
        this.name = name;
        this.parent = parent;
    }

    public EmbedMenu addSubMenu(EmbedMenu subMenu) {
        subMenus.add(subMenu);
        return this;
    }

    public EmbedMenu addButton(Button button, BiConsumer<ButtonInteractionEvent, EmbedMenu> event) {
        buttons.add(button.withId("embedMenu:" + (parent == null ? name : (parent.getName() + ":" + name)) + ":" + button.getId()));
        buttonEvents.put("embedMenu:" + (parent == null ? name : (parent.getName() + ":" + name)) + ":" + button.getId(), event);
        return this;
    }

    public EmbedMenu addEmbedFunction(Function<OpenEmbedMenuEvent, EmbedBuilder> embedSupplier) {
        this.embedFunction = embedSupplier;
        return this;
    }

    public Optional<EmbedBuilder> getEmbed(Member member, Guild guild, Channel channel) {
        if(embedFunction != null)return Optional.ofNullable(embedFunction.apply(new OpenEmbedMenuEvent(member, guild, channel)));
        return Optional.empty();
    }

    public StringSelectMenu getNavigator() {
        if(subMenus.isEmpty())return null;

        StringSelectMenu.Builder stringSelectMenuBuilder = StringSelectMenu.create("embedMenu:" + (parent == null ? name : (parent.getName() + ":" + name)));
        for (EmbedMenu subMenu : subMenus) stringSelectMenuBuilder.addOption(subMenu.name, subMenu.name);
        stringSelectMenuBuilder.setMaxValues(1);
        return stringSelectMenuBuilder.build();
    }

    public List<ItemComponent> getComponents() {
        return new ArrayList<>(buttons);
    }

    public void addEventListener(JDA jda) {
        jda.addEventListener(this);
    }

    public void removeEventListener(JDA jda) {
        jda.removeEventListener(this);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getButton().getId().startsWith("embedMenu:" + (parent == null ? name : (parent.getName()))))return;
        String[] idArgs = event.getButton().getId().split(":");

        EmbedMenu menu = null;
        if(parent == null)menu = this;
        if(idArgs.length < 4)return;
        for(EmbedMenu subMenu : subMenus) {
            if(idArgs[2].equals(subMenu.name)) {
                menu = subMenu;
                break;
            }
        }
        if(menu == null)return;

        // maybe make own function for that :3
        for(Button button : menu.buttons) {
            if(button.getId().equals(event.getButton().getId())) {
                if(menu.buttonEvents.containsKey(button.getId())) {
                    menu.buttonEvents.get(button.getId()).accept(event, menu);
                }
                return;
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if(!event.getSelectMenu().getId().startsWith("embedMenu:" + name))return;
        String[] idArgs = event.getSelectMenu().getId().split(":");
        if(!(idArgs.length > 1 && idArgs[1].equals(name))) return;

        String value = event.getSelectedOptions().get(0).getValue();

        for(EmbedMenu subMenu : subMenus) {
            if(value.equals(subMenu.name)) {
                Optional<EmbedBuilder> optionalEmbed = subMenu.getEmbed(event.getMember(), event.getGuild(), event.getChannel());
                if(optionalEmbed.isPresent()) {
                    // maybe make own function for that :3

                    List<ActionRow> actionRows = new ArrayList<>();
                    List<ItemComponent> components = subMenu.getComponents();
                    actionRows.add(components != null ? ActionRow.of(components) : null);
                    StringSelectMenu navigator = getNavigator();
                    actionRows.add(navigator != null ? ActionRow.of(navigator) : null);

                    actionRows.removeIf(Objects::isNull);

                    event.editMessageEmbeds(optionalEmbed.get().build()).setComponents(actionRows).queue();

                    // ..

                } else {
                    event.reply("Error!").setEphemeral(true).queue();
                }
                return;
            }
        }
    }

    @Getter
    public static class OpenEmbedMenuEvent {
        private final Member member;
        private final Guild guild;
        private final Channel channel;

        public OpenEmbedMenuEvent(Member member, Guild guild, Channel channel) {
            this.member = member;
            this.guild = guild;
            this.channel = channel;
        }
    }
}
