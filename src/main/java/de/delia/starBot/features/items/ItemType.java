package de.delia.starBot.features.items;

public enum ItemType {
    PICKAXE("pickaxe", "Pickaxe", ":pick:", 5);

    public final String name;
    public final String emoji;
    public final int defaultStackSize;
    final String itemId;

    ItemType(String itemId, String name, String emoji, int defaultStackSize) {
        this.itemId = itemId;
        this.name = name;
        this.emoji = emoji;
        this.defaultStackSize = defaultStackSize;
    }
}
