package de.delia.starBot.features.items;

import de.delia.starBot.main.Bot;

public interface Item {
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

        return new Item() {
            private final ItemType type = itemType;
            private ItemEntity entity = finalItemEntity;
            private int amount = finalItemEntity.getAmount();
            private int stackSize = finalItemEntity.getStackSize();

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
                return itemType;
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
