package ru.footballticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber; // Уникальный номер билета для отслеживания

    @Column(nullable = false)
    private Integer rowNumber;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private StadiumSector sector;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public enum TicketStatus {
        AVAILABLE, RESERVED, PAID, CANCELLED // ДОСТУПНО, ЗАБРОНИРОВАНО, ОПЛАЧЕНО, ОТМЕНЕНО
    }
}