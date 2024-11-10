package de.delia.starBot.guildConfig;

import java.util.ArrayList;
import java.util.List;

public enum Configs {
    ENABLE_STOCK("enableStock", Boolean.class, false),

    ENABLE_STAR_DROP("enableStarDrop", Boolean.class, true),
    STAR_DROP_MESSAGE_MAX("starDropMessageMax", Integer.class, 60),
    STAR_DROP_MESSAGE_MIN("starDropMessageMin", Integer.class, 30),
    STAR_DROP_BLACKLISTED_CHANNEL("starDropBlacklistedChannel", Long.class, new ArrayList<>()),

    ENABLE_LOG("enableLog", Boolean.class, false),
    LOG_CHANNEL("logChannel", Long.class, null),
    ;

    public final String id;
    public final Class<?> type;
    public final Object defaultValue;

    Configs(String id, Class<?> type, Object defaultValue) {
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;
    }
}
