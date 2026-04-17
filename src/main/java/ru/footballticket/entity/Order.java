package ru.footballticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber; // для отслеживания (например ORD-20260407-001)

    private LocalDateTime orderDate;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PAID;

    private String customerEmail; // для отслеживания без регистрации

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // может быть null (гость)

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    public enum OrderStatus {
        PAID, PROCESSING, SHIPPED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = "ORD-" + System.currentTimeMillis();
        }
    }
}