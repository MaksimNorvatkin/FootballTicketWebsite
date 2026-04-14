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

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    private String description;

    @Column(name = "tickets_sold")
    private Integer ticketsSold = 0;

    @Column(name = "popularity_score")
    private Integer popularityScore = 0; // для популярных матчей

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Ticket> tickets;
}