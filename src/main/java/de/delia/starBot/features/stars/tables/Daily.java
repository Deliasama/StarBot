package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Daily")
public class Daily {
    @Column
    Instant lastCalled;
    @Column
    int streak;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long guildId;
    @Column
    private Long memberId;

    public Daily(Long guildId, Long memberId, Instant lastCalled, int streak) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.lastCalled = lastCalled;
        this.streak = streak;
    }

    public static DailyTable getTable() {
        return Main.INSTANCE.dailyTable;
    }
}
