package de.delia.starBot.features.birthday;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Birthday")
public class Birthday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long guildId;

    @Column
    private Long memberId;

    @Column
    private LocalDate birthday;

    @Column
    private int timesChanged;

    public Birthday(Long guildId, Long memberId, LocalDate birthday, int timesChanged) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.birthday = birthday;
        this.timesChanged = timesChanged;
    }

    public boolean hasBirthday() {
        LocalDate now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

        return now.getMonth() == birthday.getMonth() && now.getDayOfMonth() == birthday.getDayOfMonth();
    }
}
