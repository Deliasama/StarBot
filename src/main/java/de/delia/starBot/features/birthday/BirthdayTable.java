package de.delia.starBot.features.birthday;

import de.delia.starBot.database.Table;
import de.delia.starBot.features.stars.tables.Daily;
import de.delia.starBot.main.Main;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BirthdayTable extends Table<Birthday> {
    public BirthdayTable() {
        super(Birthday.class, Main.INSTANCE.entityManagerFactory);
    }

    public Birthday get(long guildId, long memberId) {
        return getEntityManager(m -> {
            List<Birthday> birthdayList = m.createQuery("SELECT u from Birthday u where u.guildId = ?1 and u.memberId = ?2", Birthday.class)
                    .setParameter(1, guildId)
                    .setParameter(2, memberId)
                    .getResultList();
            if (birthdayList.isEmpty()) {
                Birthday birthday = new Birthday(guildId, memberId, null, 0);
                save(birthday);
                return birthday;
            }
            return birthdayList.get(0);
        });
    }

    public List<Birthday> getBirthdays(long guildId) {
        return getEntityManager(m -> m.createQuery("SELECT b from Birthday b where b.guildId = ?1 ORDER BY EXTRACT(MONTH FROM b.birthday), EXTRACT(DAY FROM b.birthday) ASC", Birthday.class)
                .setParameter(1, guildId)
                .getResultList());
    }

    public List<Birthday> findAllWithBirthdayAtDate(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        return getEntityManager(m -> m.createQuery("SELECT b from Birthday b WHERE MONTH(b.birthday) = ?1 AND DAY(b.birthday) = ?2", Birthday.class)
                .setParameter(1, month)
                .setParameter(2, day)
                .getResultList());
    }

    public Birthday update(Birthday birthday) {
        return getEntityManager(m -> {
            m.getTransaction().begin();
            Birthday u = m.find(Birthday.class, birthday.getId());
            if (u == null) return null;

            u.setBirthday(birthday.getBirthday());
            u.setTimesChanged(birthday.getTimesChanged());

            m.persist(u);
            m.getTransaction().commit();
            return u;
        });
    }
}
