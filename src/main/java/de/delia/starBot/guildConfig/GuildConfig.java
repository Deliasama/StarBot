package de.delia.starBot.guildConfig;

import de.delia.starBot.features.stars.tables.Dividend;
import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@Table(name = "GuildConfig")
public class GuildConfig {
    public static final Map<Long, GuildConfig> bufferedConfigs = new HashMap<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long guildId;

    // Enable Stock system
    @Column(nullable = false)
    private boolean enableStock = false;

    // Star Drops
    @Column(nullable = false)
    private boolean enableStarDrop = true;

    @Column(nullable = false)
    private int starDropMessageMin = 30;

    @Column(nullable = false)
    private int starDropMessageMax = 60;

    public GuildConfig(Long guildId) {
        this.guildId = guildId;
    }

    public static GuildConfig getConfig(long guildId) {
        if(bufferedConfigs.containsKey(guildId)) {
            return bufferedConfigs.get(guildId);
        } else {
            GuildConfig config = Main.INSTANCE.guildConfigTable.get(guildId);
            bufferedConfigs.put(guildId, config);
            return config;
        }
    }

    public GuildConfig update() {
        return Main.INSTANCE.guildConfigTable.update(this);
    }

    public static class GuildConfigTable extends de.delia.starBot.database.Table<GuildConfig> {

        public GuildConfigTable() {
            super(GuildConfig.class, Main.INSTANCE.entityManagerFactory);
        }

        public GuildConfig get(long guildId) {
            return getEntityManager(m -> {
                List<GuildConfig> guildConfigs = m.createQuery("SELECT c from GuildConfig c where c.guildId = ?1", GuildConfig.class)
                        .setParameter(1, guildId)
                        .getResultList();
                if(guildConfigs.isEmpty()) {
                    GuildConfig config = new GuildConfig(guildId);
                    save(config);
                    return config;
                }
                return guildConfigs.get(0);
            });
        }

        public GuildConfig update(GuildConfig guildConfig) {
            return getEntityManager(m -> {
                m.getTransaction().begin();
                GuildConfig c = m.find(GuildConfig.class, guildConfig.id);

                c.enableStock = guildConfig.enableStock;
                c.enableStarDrop = guildConfig.enableStarDrop;
                c.starDropMessageMin = guildConfig.starDropMessageMin;
                c.starDropMessageMax = guildConfig.starDropMessageMax;

                m.persist(c);
                m.getTransaction().commit();
                return c;
            });
        }
    }
}
