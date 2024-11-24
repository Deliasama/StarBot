package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "StarProfile")
public class StarProfile {
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
    }

    public static StarProfileTable getTable() {
        return Main.INSTANCE.starProfileTable;
    }

    public void addStars(int amount) {
        this.stars += amount;
        getTable().update(this);
    }
}
