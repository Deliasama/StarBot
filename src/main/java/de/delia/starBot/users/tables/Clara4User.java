package de.delia.starBot.users.tables;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Clara4User")
public class Clara4User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private Long memberId;

    @Column
    private Long untisId;

    @Column
    private Long starId;

    public Clara4User(long guildId, long memberId, @Nullable Long untisId, @Nullable Long starId) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.untisId = untisId;
        this.starId = starId;
    }
}
