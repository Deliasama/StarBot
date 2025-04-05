package de.delia.starBot.features.items;

import de.delia.starBot.main.Bot;

import java.util.*;
import java.util.stream.Collectors;

public interface Item {
    static Map<ItemType, Item> getItems(Bot bot, long guildId, long memberId) {
        Map<ItemType, Item> items = bot.itemTable.getItems(guildId, memberId).stream()
                .collect(Collectors.toMap(
                        item -> ItemType.getItemType(item.getItemId()), // Now recognized
                        item -> newItemInstance(bot, item),
                        (existing, replacement) -> existing
                ));

        for (ItemType itemType : ItemType.values()) {
            if (!items.containsKey(itemType)) {
                ItemEntity itemEntity = new ItemEntity(guildId, memberId, itemType.itemId, itemType.defaultStackSize, 0);
                bot.itemTable.save(itemEntity);
                items.put(itemType, newItemInstance(bot, itemEntity));
            }
        }
        return items;
    }

    static Item getItem(Bot bot, long guildId, long memberId, ItemType itemType) {
        if (bot == null) return null;
        ItemEntity itemEntity = bot.itemTable.get(guildId, memberId, itemType.itemId);
        int maxStackSize = itemType.defaultStackSize;
        int amount = 0;
        if (itemEntity != null) {
            amount = itemEntity.getAmount();
            maxStackSize = itemEntity.getStackSize();
        }
        if (itemEntity == null) {
            itemEntity = new ItemEntity(guildId, memberId, itemType.itemId, maxStackSize, amount);
            bot.itemTable.save(itemEntity);
        }

        final ItemEntity finalItemEntity = itemEntity;

        return newItemInstance(bot, finalItemEntity);
    }

    private static Item newItemInstance(Bot bot, ItemEntity itemEntity) {
        ItemType itemType = ItemType.getItemType(itemEntity.getItemId());
        return new Item() {
            private final ItemType type = itemType;
            private ItemEntity entity = itemEntity;
            private int amount = itemEntity.getAmount();
            private int stackSize = itemEntity.getStackSize();

            @Override
            public void update() {
                entity.setAmount(amount);
                entity.setStackSize(stackSize);
                entity = bot.itemTable.update(entity);
            }

            @Override
            public void use() {
                // Leave this empty for now
            }

            @Override
            public boolean canUse() {
                return false;
            }

            @Override
            public ItemType getItemType() {
                return type;
            }

            @Override
            public int getAmount() {
                return amount;
            }

            @Override
            public void setAmount(int amount) {
                this.amount = Math.min(amount, stackSize);
            }

            @Override
            public int getStackSize() {
                return stackSize;
            }

            @Override
            public void setStackSize(int stackSize) {
                this.stackSize = stackSize;
            }

            @Override
            public String getName() {
                return type.name();
            }

            @Override
            public String getEmoji() {
                return type.emoji;
            }
        };
    }

    void update();

    void use();

    boolean canUse();

    ItemType getItemType();

    int getAmount();

    void setAmount(int amount);

    int getStackSize();

    void setStackSize(int stackSize);

    String getName();

    String getEmoji();
}
