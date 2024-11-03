package de.delia.starBot.guildConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.delia.starBot.features.stars.tables.Dividend;
import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@Table(name = "GuildConfig")
public class GuildConfig {
    public static final Map<Long, GuildConfig> bufferedConfigs = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private Long guildId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "config", joinColumns = @JoinColumn(name = "guildConfig_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    Map<String, String> configs = new HashMap<>();

    public GuildConfig(Long guildId) {
        this.guildId = guildId;

        this.configs.put("enableStock", String.valueOf(false));

        this.configs.put("enableStarDrop", String.valueOf(true));
        this.configs.put("starDropMessageMin", String.valueOf(30));
        this.configs.put("starDropMessageMax", String.valueOf(60));
        try {
            this.configs.put("starDropBlacklistedChannel", objectMapper.writeValueAsString(new ArrayList<>()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static GuildConfig getGuildConfig(long guildId) {
        if(bufferedConfigs.containsKey(guildId)) {
            return bufferedConfigs.get(guildId);
        } else {
            GuildConfig config = Main.INSTANCE.guildConfigTable.get(guildId);
            bufferedConfigs.put(guildId, config);
            return config;
        }
    }

    public <T> List<T> getConfigList(String key, Class<T> type) {
        if(!configs.containsKey(key))return null;
        try {
            return objectMapper.readValue(configs.get(key), objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfigList(String key, List<?> value) {
        try {
            configs.put(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getConfig(String key, Class<T> type) {
        if(!configs.containsKey(key))return null;
        return objectMapper.convertValue(configs.get(key), type);
    }

    public void setConfig(String key, String value) {
        configs.put(key, value);
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
                GuildConfig c = m.find(GuildConfig.class, guildConfig.guildId);

                c.configs = guildConfig.configs;

                m.persist(c);
                m.getTransaction().commit();
                return c;
            });
        }
    }
}
