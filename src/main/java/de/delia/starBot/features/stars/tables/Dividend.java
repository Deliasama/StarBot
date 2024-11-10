package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Dividend")
public class Dividend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private int value = 150;

    @Column
    private int trendDay = 0;

    @Column
    private int trendWeek = 0;

    public Dividend(long guildId, int value, int trendDay, int trendWeek) {
        this.guildId = guildId;
        this.value = value;
        this.trendDay = trendDay;
        this.trendWeek = trendWeek;
    }


    public static class DividendTable extends de.delia.starBot.database.Table<Dividend> {
        public DividendTable() {
            super(Dividend.class, Main.INSTANCE.entityManagerFactory);
        }

        public Dividend get(long guildId) {
            return getEntityManager(m -> {
                List<Dividend> dividends = m.createQuery("SELECT d from Dividend d where d.guildId = ?1", Dividend.class)
                        .setParameter(1, guildId)
                        .getResultList();
                if (dividends.isEmpty()) {
                    Dividend dividend = new Dividend(guildId, 150, 0, 0);
                    save(dividend);
                    return dividend;
                }
                return dividends.get(0);
            });
        }

        public Dividend update(Dividend dividend) {
            return getEntityManager(m -> {
                m.getTransaction().begin();
                Dividend d = m.find(Dividend.class, dividend.getId());

                if (d == null) return null;

                d.setValue(dividend.getValue());
                d.setTrendDay(dividend.getTrendDay());
                d.setTrendWeek(dividend.getTrendWeek());

                m.persist(d);
                m.getTransaction().commit();
                return d;
            });
        }
    }
}
