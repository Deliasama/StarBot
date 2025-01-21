package de.delia.starBot.guildConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "config", joinColumns = @JoinColumn(name = "guildConfig_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    Map<String, String> configs = new HashMap<>();
    @Id
    private Long guildId;

    public GuildConfig(Long guildId) {
        this.guildId = guildId;
        /*

        // Stock
        this.configs.put("enableStock", String.valueOf(false));

        // StarDrop
        this.configs.put("enableStarDrop", String.valueOf(true));
        this.configs.put("starDropMessageMin", String.valueOf(30));
        this.configs.put("starDropMessageMax", String.valueOf(60));
        try {
            this.configs.put("starDropBlacklistedChannel", objectMapper.writeValueAsString(new ArrayList<>()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static GuildConfig getGuildConfig(long guildId) {
        if (bufferedConfigs.containsKey(guildId)) {
            return bufferedConfigs.get(guildId);
        } else {
            GuildConfig config = Main.INSTANCE.guildConfigTable.get(guildId);
            bufferedConfigs.put(guildId, config);
            return config;
        }
    }

    @Deprecated
    public <T> List<T> getConfigList(String key, Class<T> type) {
        if (!configs.containsKey(key)) return null;
        try {
            return objectMapper.readValue(configs.get(key), objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<?> getConfigList(Configs config) {
        if (!configs.containsKey(config.id)) {
            return (List<?>) config.defaultValue;
        }

        try {
            return objectMapper.readValue(configs.get(config.id), objectMapper.getTypeFactory().constructCollectionType(List.class, config.type));
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

    @Deprecated
    public void setConfigList(Configs config, List<?> value) {
        try {
            configs.put(config.id, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public <T> T getConfig(String key, Class<T> type) {
        if (!configs.containsKey(key)) return null;
        return objectMapper.convertValue(configs.get(key), type);
    }

    public Object getConfig(Configs config) {
        if (!configs.containsKey(config.id)) {
            return config.defaultValue;
        }
        return objectMapper.convertValue(configs.get(config.id), config.type);
    }

    public boolean getConfigAsBoolean(Configs config) {
        if (config.type != boolean.class && config.type != Boolean.class) return false;
        return (boolean) getConfig(config);
    }

    public void setConfig(String key, String value) {
        configs.put(key, value);
    }

    public void setConfig(Configs config, String value) {
        configs.put(config.id, value);
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
                if (guildConfigs.isEmpty()) {
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
