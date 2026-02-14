package com.example.kihan.domain.user;

import com.example.kihan.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Table(name = "users")
@Audited
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private LocalDateTime lastLoginAt;

    public static User register(
            final String email,
            final String password,
            final String name
    ) {
        User user = new User();
        user.email = requireNonNull(email);
        user.password = requireNonNull(password);
        user.name = requireNonNull(name);
        user.role = UserRole.USER;
        return user;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

}