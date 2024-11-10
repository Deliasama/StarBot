package de.delia.starBot.features.stars.tables;

import de.delia.starBot.database.Table;
import de.delia.starBot.main.Main;

import java.util.ArrayList;
import java.util.List;

public class StarProfileTable extends Table<StarProfile> {
    public StarProfileTable() {
        super(StarProfile.class, Main.INSTANCE.entityManagerFactory);
    }

    public StarProfile get(long guildId, long memberId) {
        return getEntityManager(m -> {
            List<StarProfile> starProfiles = m.createQuery("SELECT u from StarProfile u where u.guildId = ?1 and u.memberId = ?2", StarProfile.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList();
            if (starProfiles.isEmpty()) {
                StarProfile starProfile = new StarProfile(guildId, memberId, 0, 0);
                save(starProfile);
                return starProfile;
            }
            return starProfiles.get(0);
        });
    }

    public List<StarProfile> getSorted(long guildId, int limit, String sort) {
        return getEntityManager(m -> {
            List<StarProfile> starProfiles = m.createQuery("SELECT u from StarProfile u where u.guildId = ?1 ORDER BY u.stars DESC LIMIT ?2", StarProfile.class)
                    .setParameter(1, guildId)
                    .setParameter(2, limit)
                    .getResultList();
            return starProfiles == null ? new ArrayList<>() : starProfiles;
        });
    }

    public Double getSumShares(long guildId) {
        return getEntityManager(m -> m.createQuery("SELECT SUM(u.shares) from StarProfile u where u.guildId = ?1", Double.class)
                .setParameter(1, guildId)
                .getSingleResult());
    }

    public List<StarProfile> getShareHolder(long guildId, int limit) {
        return getEntityManager(m -> {
            List<StarProfile> starProfiles = m.createQuery("SELECT u from StarProfile u where u.guildId = ?1 and u.shares>0", StarProfile.class)
                    .setParameter(1, guildId)
                    .getResultList();
            return starProfiles == null ? new ArrayList<>() : starProfiles;
        });
    }

    public StarProfile update(StarProfile starProfile) {
        return getEntityManager(m -> {
            m.getTransaction().begin();
            StarProfile u = m.find(StarProfile.class, starProfile.getId());
            if (u == null) return null;

            u.setStars(starProfile.getStars());
            u.setShares(starProfile.getShares());

            m.persist(u);
            m.getTransaction().commit();
            return u;
        });
    }
}
