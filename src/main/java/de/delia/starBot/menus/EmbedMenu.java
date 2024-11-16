package de.delia.starBot.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EmbedMenu extends ListenerAdapter {
    @Getter
    private final String name;
    private final EmbedMenu parent;
    private final List<EmbedMenu> subMenus = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final Map<String, BiConsumer<GenericSelectMenuInteractionEvent<?, ? extends SelectMenu>, EmbedMenu>> selectMenuEvents = new HashMap<>();
    private final Map<String, BiConsumer<ButtonInteractionEvent, EmbedMenu>> buttonEvents = new HashMap<>();
    @Getter
    @Setter
    private SelectMenu selectMenu = null;
    private Function<OpenEmbedMenuEvent, EmbedBuilder> embedFunction;
    private List<Modal> modals = new ArrayList<>();
    private Map<String, BiConsumer<ModalInteractionEvent, EmbedMenu>> modalEvents = new HashMap<>();

    public EmbedMenu(String name, EmbedMenu parent) {
        this.name = name;
        this.parent = parent;
    }

    public EmbedMenuResponse generate(Member member, Guild guild, Channel channel) {
        onMenuGenerate(new OpenEmbedMenuEvent(member, guild, channel));

        MessageEmbed embed = getEmbed(member, guild, channel).orElse(new EmbedBuilder().setTitle("404 No menu found")).build();
        List<ActionRow> actionRows = getComponents();
        StringSelectMenu navigator = getNavigator();
        if (navigator != null) actionRows.add(ActionRow.of(getNavigator()));
        return new EmbedMenuResponse(embed, actionRows, channel, member, guild);
    }

    public void onMenuGenerate(OpenEmbedMenuEvent event) {

    }

    public EmbedMenu addSubMenu(EmbedMenu subMenu) {
        subMenus.add(subMenu);
        return this;
    }

    public EmbedMenu addButton(Button button, BiConsumer<ButtonInteractionEvent, EmbedMenu> event) {
        buttons.add(button.withId(getId() + ":" + button.getId()));
        buttonEvents.put("embedMenu:" + (parent == null ? name : (parent.getName() + ":" + name)) + ":" + button.getId(), event);
        return this;
    }

    public EmbedMenu addBackButton() {
        buttons.add(Button.danger((getId() + ":back"), Emoji.fromUnicode("U+25C0")));
        buttonEvents.put(getId() + ":back", (e, m) -> {
            EmbedMenuResponse embedMenuResponse = getParent().generate(e.getMember(), e.getGuild(), e.getChannel());
            e.editMessageEmbeds(embedMenuResponse.embed).setComponents(embedMenuResponse.actionRows).queue();
        });
        return this;
    }

    public EmbedMenu addModal(Modal modal, BiConsumer<ModalInteractionEvent, EmbedMenu> event) {
        modals.add(modal.createCopy().setId(getId() + ":" + modal.getId()).build());
        modalEvents.put(getId() + ":" + modal.getId(), event);
        return this;
    }

    public EmbedMenu addStringSelectMenu(StringSelectMenu selectMenu, BiConsumer<GenericSelectMenuInteractionEvent<?, ? extends SelectMenu>, EmbedMenu> event) {
        this.selectMenu = selectMenu.createCopy().setId(getId() + ":" + selectMenu.getId()).build();
        this.selectMenuEvents.put(getId() + ":" + selectMenu.getId(), event);
        return this;
    }

    public EmbedMenu addEntitySelectMenu(EntitySelectMenu selectMenu, BiConsumer<GenericSelectMenuInteractionEvent<?, ? extends SelectMenu>, EmbedMenu> event) {
        this.selectMenu = selectMenu.createCopy().setId(getId() + ":" + selectMenu.getId()).build();
        this.selectMenuEvents.put(getId() + ":" + selectMenu.getId(), event);
        return this;
    }

    public EmbedMenu addEmbedFunction(Function<OpenEmbedMenuEvent, EmbedBuilder> embedSupplier) {
        this.embedFunction = embedSupplier;
        return this;
    }

    public Optional<EmbedBuilder> getEmbed(Member member, Guild guild, Channel channel) {
        if (embedFunction != null)
            return Optional.ofNullable(embedFunction.apply(new OpenEmbedMenuEvent(member, guild, channel)));
        return Optional.empty();
    }

    public StringSelectMenu getNavigator() {
        if (parent == null && subMenus.isEmpty()) return null;
        if (subMenus.isEmpty()) return getPrimaryMenu().getNavigator();

        StringSelectMenu.Builder stringSelectMenuBuilder = StringSelectMenu.create(getId() + ":" + name);
        for (EmbedMenu subMenu : subMenus) stringSelectMenuBuilder.addOption(subMenu.name, subMenu.name);
        stringSelectMenuBuilder.setMaxValues(1);
        stringSelectMenuBuilder.setPlaceholder("Navigate");
        return stringSelectMenuBuilder.build();
    }

    public List<ActionRow> getComponents() {
        List<ActionRow> components = new ArrayList<>();

        if (!buttons.isEmpty()) components.add(ActionRow.of(buttons));
        if (selectMenu != null) components.add(ActionRow.of(selectMenu));
        return new ArrayList<>(components);
    }

    public Optional<Modal> getModal(String id) {
        return modals.stream().filter(m -> m.getId().split(":")[m.getId().split(":").length-1].equals(id)).findAny();
    }

    public void addEventListener(JDA jda) {
        jda.addEventListener(this);
    }

    public void removeEventListener(JDA jda) {
        jda.removeEventListener(this);
    }

    public String getId() {
        return "embedMenu:" + (parent == null ? "" : (parent.getName() + ":")) + name;
    }

    // get the SubMenu with the corresponding id
    public EmbedMenu getSubMenuWithId(String id) {
        if (!id.startsWith(getId())) return null;
        if (id.equals(getId())) return this;
        if (subMenus.isEmpty()) return this;

        for (EmbedMenu subMenu : subMenus) {
            if (id.startsWith(subMenu.getId())) {
                return subMenu.getSubMenuWithId(id);
            }
        }
        return null;
    }

    public EmbedMenu getParent() {
        if (parent == null) return this;
        return parent;
    }

    /**
     * Returns the highest parent in the hierarchy
     *
     * @return
     */
    public EmbedMenu getPrimaryMenu() {
        if (parent == null) return this;
        return parent.getPrimaryMenu();
    }

    public void onMenuButtonInteraction(MenuButtonInteractionEvent event) {
    }

    public void onMenuModalInteraction(ModalInteractionEvent event) {

    }

    @Override
    public void onGenericSelectMenuInteraction(@NotNull GenericSelectMenuInteractionEvent event) {
        if (!event.getSelectMenu().getId().startsWith(getId())) return;

        EmbedMenu menu = getSubMenuWithId(event.getSelectMenu().getId());

        if (menu == null) return;
        if (menu.selectMenuEvents.get(event.getSelectMenu().getId()) == null) return;
        menu.selectMenuEvents.get(event.getSelectMenu().getId()).accept(event, menu);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        String menuId = buttonId.substring(0, buttonId.lastIndexOf(':'));
        EmbedMenu menu = getSubMenuWithId(menuId);

        if (menu == null) return;

        // maybe make own function for that :3
        for (Button button : menu.buttons) {
            if (button.getId().equals(buttonId)) {
                MenuButtonInteractionEvent menuButtonInteractionEvent = new MenuButtonInteractionEvent(event);
                onMenuButtonInteraction(menuButtonInteractionEvent);
                // cancel if event.isCanceled is true
                if (menuButtonInteractionEvent.isCanceled()) return;

                if (menu.buttonEvents.containsKey(button.getId())) {
                    menu.buttonEvents.get(button.getId()).accept(event, menu);
                }
                return;
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getSelectMenu().getId().startsWith(getId())) return;
        String[] idArgs = event.getSelectMenu().getId().split(":");
        if (!(idArgs.length > 1 && idArgs[1].equals(name))) return;

        String value = event.getSelectedOptions().get(0).getValue();

        for (EmbedMenu subMenu : subMenus) {
            if (value.equals(subMenu.name)) {
                EmbedMenuResponse embedMenuResponse = subMenu.generate(event.getMember(), event.getGuild(), event.getChannel());
                event.editMessageEmbeds(embedMenuResponse.embed).setComponents(embedMenuResponse.actionRows).queue();
                return;
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith(getId())) return;
        String modalId = event.getModalId();

        String menuId = modalId.substring(0, modalId.lastIndexOf(':'));
        EmbedMenu menu = getSubMenuWithId(menuId);
        if (menu == null) return;

        onMenuModalInteraction(event);

        for (Modal modal : menu.modals) {
            if (modal.getId().equals(modalId)) {
                if(menu.modalEvents.containsKey(modal.getId())) {
                    menu.modalEvents.get(modal.getId()).accept(event, menu);
                }
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

    @Getter
    public static class MenuButtonInteractionEvent {
        private final ButtonInteractionEvent event;
        @Setter
        private boolean canceled = false;

        public MenuButtonInteractionEvent(ButtonInteractionEvent event) {
            this.event = event;
        }
    }

    @Getter
    @AllArgsConstructor
    /**
     * An Object containing every necessary Element to reply an Embed Menu, returned by the generate function
     */
    public static class EmbedMenuResponse {
        MessageEmbed embed;
        List<ActionRow> actionRows = new ArrayList<>();
        Channel channel;
        Member member;
        Guild guild;
    }
}
