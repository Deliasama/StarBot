package de.delia.starBot.features.items;

import de.delia.starBot.features.stars.tables.Daily;
import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Item")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long guildId;
    @Column
    private Long memberId;
    @Column
    String itemId;
    @Column
    Integer stackSize;
    @Column
    int amount;

    public ItemEntity(Long guildId, Long memberId, String itemId, Integer stackSize, int amount) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.itemId = itemId;
        this.stackSize = stackSize;
        this.amount = amount;
    }

    public static class ItemTable extends de.delia.starBot.database.Table<ItemEntity> {
        public ItemTable() {
            super(ItemEntity.class, Main.INSTANCE.entityManagerFactory);
        }

        public ItemEntity get(long guildId, long memberId, String itemId) {
            return getEntityManager(m -> {
                List<ItemEntity> itemEntities = m.createQuery("SELECT u from ItemEntity u where u.guildId = ?1 and u.memberId = ?2 and u.itemId = ?3", ItemEntity.class)
                        .setParameter(1, guildId)
                        .setParameter(2, memberId)
                        .setParameter(3, itemId)
                        .getResultList();
                if (itemEntities.isEmpty()) {
                    return null;
                }
                return itemEntities.get(0);
            });
        }

        public List<ItemEntity> getItems(long guildId, long memberId) {
            return getEntityManager(m -> m.createQuery("SELECT u from ItemEntity u where u.guildId = ?1 and u.memberId = ?2", ItemEntity.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList());
        }

        public ItemEntity update(ItemEntity itemEntity) {
            return getEntityManager(m -> {
                m.getTransaction().begin();
                ItemEntity u = m.find(ItemEntity.class, itemEntity.getId());
                if (u == null) return null;

                u.setStackSize(itemEntity.getStackSize());
                u.setAmount(itemEntity.getAmount());

                m.persist(u);
                m.getTransaction().commit();
                return u;
            });
        }
    }
}
