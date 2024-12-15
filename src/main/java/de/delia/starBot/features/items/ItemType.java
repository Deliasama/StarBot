package de.delia.starBot.features.items;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum ItemType {
    PICKAXE("pickaxe", "Pickaxe", ":pick:", 5);

    final String itemId;
    final String name;
    final String emoji;
    final int defaultStackSize;

    ItemType(String itemId, String name, String emoji, int defaultStackSize) {
        this.itemId = itemId;
        this.name = name;
        this.emoji = emoji;
        this.defaultStackSize = defaultStackSize;
    }
}
