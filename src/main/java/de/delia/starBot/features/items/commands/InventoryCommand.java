package de.delia.starBot.features.items.commands;

import de.delia.starBot.commands.ApplicationCommand;
import de.delia.starBot.commands.ApplicationCommandMethod;
import de.delia.starBot.features.items.Item;
import de.delia.starBot.features.items.ItemType;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.Map;

@ApplicationCommand(name = "inventory", description = "Open your Inventory!")
public class InventoryCommand {
    @ApplicationCommandMethod
    public void onCommand(Bot bot, SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) return;
        StarProfile starProfile = bot.starProfileManager.getProfile(event.getGuild().getIdLong(), event.getMember().getIdLong());
        if (starProfile == null) return;

        Map<ItemType, Item> items = starProfile.getItems();

        String mineItems = ItemType.PICKAXE.emoji + " " + ItemType.PICKAXE.name + ": **" + items.get(ItemType.PICKAXE).getAmount() + "/" + items.get(ItemType.PICKAXE).getStackSize() + "**\n" +
                "\n(More is coming soon!)";

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getMember().getUser().getName(), null, event.getMember().getUser().getAvatarUrl())
                .setTitle("Inventory")
                .addField("Mine Items:", mineItems, false)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
