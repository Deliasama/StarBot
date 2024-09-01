package de.delia.starBot.features.stars.menus;

import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.menus.ButtonMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Random;

public class StarDropMenu extends ButtonMenu {
    private boolean collected = false;
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
        if(collected) {
            event.reply("zu langsam!").setEphemeral(true).queue();
            return;
        }

        if(event.getButton().getId().split(":")[1].equals("X")) {
            collected = true;
            int starsEarned = random.nextInt(20)+10;

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Sternschnuppe!")
                    .setColor(Color.magenta)
                    .setDescription(event.getMember().getEffectiveName() + " hat die Sternschnuppe eingesammelt und erhält **" + starsEarned + "** Sterne!");

            event.editMessageEmbeds(embedBuilder.build()).setActionRow(event.getMessage().getActionRows().get(0).asDisabled().getComponents()).queue((m) -> {
                collected = false;
            });

            StarProfile.getTable().get(event.getGuild().getIdLong(), event.getUser().getIdLong()).addStars(starsEarned);


        } else {
            event.reply("Stupid!").setEphemeral(true).queue();
        }
    }
}
