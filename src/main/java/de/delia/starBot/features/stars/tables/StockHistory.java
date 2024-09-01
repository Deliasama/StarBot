package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "StockHistory")
public class StockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private int value = 0;

    @Column
    private Instant timestamp;

    public static StockHistoryTable getTable() {
        return Main.INSTANCE.stockHistoryTable;
    }

    public StockHistory(Long guildId, int value, Instant timestamp) {
        this.guildId = guildId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public static class StockHistoryTable extends de.delia.starBot.database.Table<StockHistory> {
        public StockHistoryTable() {
            super(StockHistory.class, Main.INSTANCE.entityManagerFactory);
        }

        public List<StockHistory> getSorted(long guildId, int limit) {
            return getEntityManager(m -> {
                List<StockHistory> stockHistories = m.createQuery("SELECT u from StockHistory u where u.guildId = ?1 ORDER BY u.timestamp DESC LIMIT ?2", StockHistory.class)
                        .setParameter(1, guildId)
                        .setParameter(2, limit)
                        .getResultList();
                return stockHistories==null ? new ArrayList<>() : stockHistories;
            });
        }
    }
}
