package de.delia.starBot.users.tables;

import de.delia.starBot.database.Table;
import de.delia.starBot.main.Main;

import java.util.List;

public class Clara4UserTable extends Table<Clara4User> {

    public Clara4UserTable() {
        super(Clara4User.class, Main.INSTANCE.entityManagerFactory);
    }

    public Clara4User get(long guildId, long memberId) {
        return getEntityManager(m -> {
            List<Clara4User> users = m.createQuery("SELECT u from Clara4User u where u.guildId = ?1 and u.memberId = ?2", Clara4User.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList();
            if(users.isEmpty()) {
                Clara4User clara4User = new Clara4User(guildId, memberId, null, null);
                save(clara4User);
                return clara4User;
            }
            return users.get(0);
        });
    }

    public Clara4User update(Clara4User user) {
        return getEntityManager(m -> {
            m.getTransaction().begin();
            Clara4User u = m.find(Clara4User.class, user.getId());
            if (u == null) return null;

            u.setUntisId(user.getUntisId());
            u.setStarId(user.getStarId());

            m.persist(u);
            m.getTransaction().commit();
            return u;
        });
    }
}
