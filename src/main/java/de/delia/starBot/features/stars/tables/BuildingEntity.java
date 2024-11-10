package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Buildings")
public class BuildingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private Long memberId;

    @Column
    private String type;

    @Column
    private Integer level;

    @Column(length = 512)
    private String metadata;

    public BuildingEntity(Long memberId, Long guildId, String type, Integer level, String metadata) {
        this.memberId = memberId;
        this.guildId = guildId;
        this.type = type;
        this.level = level;
        this.metadata = metadata;
    }

    public static class BuildingTable extends de.delia.starBot.database.Table<BuildingEntity> {

        public BuildingTable() {
            super(BuildingEntity.class, Main.INSTANCE.entityManagerFactory);
        }

        public Optional<BuildingEntity> get(long guildId, long memberId, String type) {
            return getEntityManager(m -> {
                List<BuildingEntity> buildingEntities = m.createQuery("SELECT b from BuildingEntity b where b.guildId = ?1 and b.memberId = ?2 and b.type = ?3", BuildingEntity.class)
                        .setParameter(1, guildId)
                        .setParameter(2, memberId)
                        .setParameter(3, type)
                        .getResultList();
                if (buildingEntities.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(buildingEntities.get(0));
            });
        }

        public List<BuildingEntity> getBuildings(long guildId, long memberId) {
            return getEntityManager(m -> m.createQuery("SELECT b from BuildingEntity b where b.guildId = ?1 and b.memberId = ?2", BuildingEntity.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList());
        }

        public BuildingEntity update(BuildingEntity buildingEntity) {
            return getEntityManager(m -> {
                m.getTransaction().begin();
                BuildingEntity b = m.find(BuildingEntity.class, buildingEntity.getId());
                if (b == null) return null;

                b.setLevel(buildingEntity.getLevel());
                b.setMetadata(buildingEntity.getMetadata());

                m.persist(b);
                m.getTransaction().commit();
                return b;
            });
        }
    }
}
