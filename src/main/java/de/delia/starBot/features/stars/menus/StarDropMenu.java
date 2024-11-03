package de.delia.starBot.features.stars.menus;

import com.iwebpp.crypto.TweetNaclFast;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Telescope;
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

public class StarDropMenu extends ButtonMenu {
    private final Map<Long, Boolean> collected = new HashMap<>();
    Random random = new Random();

    public StarDropMenu(JDA jda) {
        super(jda);
    }

    @Override
    public void init() {
        buttons.add(Button.secondary("1", Emoji.fromUnicode("⭐")));
        buttons.add(Button.secondary("2", Emoji.fromUnicode("\uD83C\uDF1F")));
        buttons.add(Button.secondary("3", Emoji.fromUnicode("✨")));
    }

    @Override
    public void buttonInteraction(ButtonInteractionEvent event) {
        // checks if the starDrop is already claimed
        if(collected.getOrDefault(Objects.requireNonNull(event.getGuild()).getIdLong(), false)) {
            event.reply("to slow!").setEphemeral(true).queue();
            return;
        }

        // checks if the clicked button and then claims the drop
        if(event.getButton().getId().split(":")[1].equals("X")) {
            collected.put(event.getGuild().getIdLong(), true);
            int starsEarned = random.nextInt(21)+10;

            Telescope telescope = (Telescope) Building.loadBuilding(Telescope.class, event.getGuild().getIdLong(), event.getMember().getIdLong());
            if(telescope != null) starsEarned = ((int) (((long) starsEarned) * (1.0 + ((long) telescope.getLevel() * 0.5))));

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Shooting Star!")
                    .setColor(Color.magenta)
                    .setDescription(event.getMember().getEffectiveName() + " has collected the shooting star and received **" + starsEarned + "** stars!");

            event.editMessageEmbeds(embedBuilder.build()).setActionRow(event.getMessage().getActionRows().get(0).asDisabled().getComponents()).queue((m) -> {
                collected.put(event.getGuild().getIdLong(), false);
            });

            StarProfile.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong()).addStars(starsEarned);


        } else {
            event.reply("Wrong Star!").setEphemeral(true).queue();
        }
    }
}
