package de.delia.starBot.menus;

import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CacheableEmbedMenu extends EmbedMenu {
    public static final long DEFAULT_CACHE_TTL = 10 * 60 * 1000;
    public Map<String, EmbedMenuInstance<? extends CacheableEmbedMenu>> instanceCache = new HashMap<>();

    public CacheableEmbedMenu(String name, EmbedMenu parent) {
        super(name, parent);
        startCacheCleaner();
    }

    public EmbedMenuInstance<? extends CacheableEmbedMenu> getInstance(String id) {
        return instanceCache.get(id);
    }

    public boolean ifInstanceNotExpired(String id, long ttl, Consumer<EmbedMenuInstance<? extends CacheableEmbedMenu>> callback) {
        EmbedMenuInstance<? extends CacheableEmbedMenu> instance = getInstance(id);
        if (instance == null || instance.isExpired(ttl)) return false;
        callback.accept(instance);
        return true;
    }

    @Override
    public EmbedMenuResponse generate(Member member, Guild guild, Channel channel) {
        String id = generateCacheKey(member, guild);
        EmbedMenuInstance<? extends CacheableEmbedMenu> instance = instanceCache.get(id);

        if (instance != null && !instance.isExpired(DEFAULT_CACHE_TTL)) {
            return new EmbedMenuResponse(instance.getEmbedBuilder().build(), instance.getActionRows(), channel, member, guild);
        }

        // Generate new Instance
        EmbedBuilder embed = getEmbed(member, guild, channel).orElse(new EmbedBuilder().setTitle("404 No menu found"));
        List<ActionRow> actionRows = getComponents();
        StringSelectMenu navigator = getNavigator();
        if (navigator != null) actionRows.add(ActionRow.of(getNavigator()));
        EmbedMenuResponse embedMenuResponse = new EmbedMenuResponse(embed.build(), actionRows, channel, member, guild);

        newInstance(id, new EmbedMenuInstance<>(this, member, guild, embed, actionRows, System.currentTimeMillis() / 1000 * 1000));
        return embedMenuResponse;
    }

    public EmbedMenuResponse regenerate(Member member, Guild guild, Channel channel) {
        String id = generateCacheKey(member, guild);
        EmbedMenuInstance<? extends CacheableEmbedMenu> instance = instanceCache.get(id);
        if (instance != null) instanceCache.remove(id);

        return generate(member, guild, channel);
    }

    public String generateCacheKey(Member member, Guild guild) {
        return guild.getId() + ":" + member.getUser().getId();
    }

    public void newInstance(String id, EmbedMenuInstance<? extends CacheableEmbedMenu> instance) {
        instanceCache.put(id, instance);
    }

    private void startCacheCleaner() {
        ScheduledExecutorService executor = Main.scheduler;
        executor.scheduleAtFixedRate(() -> {
            instanceCache.entrySet().removeIf(entry -> entry.getValue().isExpired(DEFAULT_CACHE_TTL));
        }, 10, 10, TimeUnit.MINUTES);
    }
}
