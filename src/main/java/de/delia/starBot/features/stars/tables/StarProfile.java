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

    public StarProfile(long guildId, long memberId, int stars, int shares) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.stars = stars;
        this.shares = shares;
    }

    public static StarProfileTable getTable() {
        return Main.INSTANCE.starProfileTable;
    }

    public void addStars(int amount) {
        this.stars += amount;
        getTable().update(this);
    }
}
