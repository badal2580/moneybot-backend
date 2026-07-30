package com.badal.moneybot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(
            nullable = false,
            length = 30
    )
    private String role;

    @PrePersist
    @PreUpdate
    public void normalizeUserData() {

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        if (name != null) {
            name = name.trim();
        }

        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }
    }
}
