package de.delia.starBot.features.stars.tables;

import de.delia.starBot.database.Table;
import de.delia.starBot.main.Main;

import java.util.List;

public class DailyTable extends Table<Daily> {

    public DailyTable() {
        super(Daily.class, Main.INSTANCE.entityManagerFactory);
    }

    public Daily get(long guildId, long memberId) {
        return getEntityManager(m -> {
            List<Daily> dailyList = m.createQuery("SELECT u from Daily u where u.guildId = ?1 and u.memberId = ?2", Daily.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList();
            if(dailyList.isEmpty()) {
                Daily daily = new Daily(guildId, memberId, null, 0);
                save(daily);
                return daily;
            }
            return dailyList.get(0);
        });
    }

    public Daily update(Daily daily) {
        return getEntityManager(m -> {
            m.getTransaction().begin();
            Daily u = m.find(Daily.class, daily.getId());
            if (u == null) return null;

            u.setLastCalled(daily.getLastCalled());
            u.setStreak(daily.getStreak());

            m.persist(u);
            m.getTransaction().commit();
            return u;
        });
    }
}
