package ru.footballticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data              // Геттеры/сеттеры/toString
@NoArgsConstructor // Пустой конструктор
@AllArgsConstructor // Конструктор со всеми полями
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    public enum Role {
        USER, MANAGER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // Автоматически ставит текущее время
        if (role == null) role = Role.USER;  // Если роль не указана - ставит USER
    }
}