package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Entity
@NoArgsConstructor
@Table(name = "StarProfile")
public class StarProfile {
    @Transient
    private long timestamp;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private Long memberId;

    @Column
    private int stars;

    @Column
    private float shares;

    @Column(nullable = false)
    private int pickaxeCount = 0;

    public StarProfile(long guildId, long memberId, int stars, int shares, Integer pickaxeCount) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.stars = stars;
        this.shares = shares;
        if (pickaxeCount != null) this.pickaxeCount = pickaxeCount;
        if (pickaxeCount == null) this.pickaxeCount = 0;

        this.timestamp = System.currentTimeMillis();
    }

    public static StarProfileTable getTable() {
        return Main.INSTANCE.starProfileTable;
    }

    public void addStars(int amount) {
        this.stars += amount;
        if (this.stars < 0) this.stars = 0;
        getTable().update(this);
    }

    public boolean isExpired(long ttl) {
        // is expired after 20min
        return System.currentTimeMillis() - timestamp > ttl;
    }
}
