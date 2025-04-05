package de.delia.starBot.features.stars.menus;

import de.delia.starBot.features.stars.StarProfileManager;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Telescope;
import de.delia.starBot.main.Main;
import de.delia.starBot.menus.ButtonMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class GazeMenu extends ButtonMenu {
    private final Map<Long, Boolean> clicked = new HashMap<>();

    public GazeMenu(JDA jda) {
        super(jda);
    }

    @Override
    public void init() {
        buttons.add(Button.secondary("gazeButton1", Emoji.fromUnicode("⭐")));
        buttons.add(Button.secondary("gazeButton2", Emoji.fromUnicode("\uD83C\uDF1F")));
        buttons.add(Button.secondary("gazeButton3", Emoji.fromUnicode("✨")));
    }

    @Override
    public void buttonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().split(":")[1].equals(event.getMember().getId())) {
            event.reply("You are not allowed to interact with this button!").queue();
            return;
        }


        if (clicked.getOrDefault(Objects.requireNonNull(event.getGuild()).getIdLong(), false)) {
            event.reply("to fast!!").setEphemeral(true).queue();
            return;
        }
        clicked.put(event.getMember().getIdLong(), true);

        int collected = Integer.parseInt(event.getButton().getId().split(":")[2]);

        Telescope telescope = (Telescope) Building.loadBuilding(Telescope.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
        if (telescope != null) collected = ((int) (((long) collected) * (1.0 + ((long) telescope.getLevel() * 0.5))));

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Gaze!")
                .setColor(Color.magenta)
                .setDescription("You collected **" + collected + "** stars!");

        StarProfile starProfile = Main.INSTANCE.starProfileManager.getProfile(event.getGuild().getIdLong(), event.getMember().getIdLong());
        starProfile.setStars(starProfile.getStars() + collected);
        Main.INSTANCE.starProfileManager.updateProfile(starProfile);

        event.editMessageEmbeds(embedBuilder.build()).setActionRow(event.getMessage().getActionRows().get(0).asDisabled().getComponents()).queue((m) -> {
            clicked.put(event.getMember().getIdLong(), false);
        });
    }
}
