package de.delia.starBot.features.stars;

import de.delia.starBot.features.items.Item;
import de.delia.starBot.features.items.ItemType;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.main.Main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StarProfileManager {
    public Map<String, StarProfile> cachedStarProfiles = new ConcurrentHashMap<>();
    private final long ttl = 60*60*1000;

    public StarProfileManager() {
        Main.scheduler.scheduleAtFixedRate(() -> {
            for (StarProfile starProfile : cachedStarProfiles.values()) {
                if (starProfile.isExpired(ttl)) cachedStarProfiles.remove(String.valueOf(starProfile.getGuildId()) + String.valueOf(starProfile.getMemberId()));
            }
        }, 0, 20*60*1000, TimeUnit.MILLISECONDS);
    }

    public StarProfile getProfile(Long guildId, Long memberId) {
        String id = guildId.toString() + memberId.toString();
        StarProfile starProfile = cachedStarProfiles.get(id);
        if (starProfile != null) {
            if (!starProfile.isExpired(ttl)) {
                return starProfile;
            }
            cachedStarProfiles.remove(id);
        }
        starProfile = StarProfile.getTable().get(guildId, memberId);
        cachedStarProfiles.put(id, starProfile);

        // Load Items!
        for (ItemType itemType : ItemType.values()) {
            starProfile.getItems().put(itemType, Item.getItem(Main.INSTANCE, guildId, memberId, itemType));

            // Set pickaxes to the old amount, remove this in a few updates!!
            if (starProfile.getItems().get(ItemType.PICKAXE).getAmount() == 0 && itemType == ItemType.PICKAXE) {
                starProfile.getItems().get(ItemType.PICKAXE).setAmount(starProfile.getPickaxeCount());
                starProfile.getItems().get(ItemType.PICKAXE).update();
            }
        }

        return starProfile;
    }

    public StarProfile updateProfile(StarProfile starProfile) {
        StarProfile sp = Main.INSTANCE.starProfileTable.update(starProfile);
        sp.setTimestamp(starProfile.getTimestamp());
        sp.setItems(starProfile.getItems());

        cachedStarProfiles.put(String.valueOf(starProfile.getGuildId()) + String.valueOf(starProfile.getMemberId()), sp);
        return sp;
    }

    public void addStars(StarProfile starProfile, int stars) {
        stars += starProfile.getStars();
        if (stars < 0) stars = 0;
        starProfile.setStars(stars);
        updateProfile(starProfile);
    }
}
