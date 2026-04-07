package ru.footballticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "stadium_sectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StadiumSector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "North", "South", "East", "West", "NorthEast", "SouthWest" и т.д.

    private String color; // для отображения на схеме стадиона (#FF0000)

    @Column(nullable = false)
    private Double priceMultiplier = 1.0; // множитель цены (VIP дороже)

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "sector")
    private List<Ticket> tickets;
}