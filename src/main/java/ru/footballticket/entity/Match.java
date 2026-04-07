package ru.footballticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String stadium;

    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    private String description;

    @Column(name = "popularity_score")
    private Integer popularityScore = 0; // для популярных матчей

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Ticket> tickets;
}