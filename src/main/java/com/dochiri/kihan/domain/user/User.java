package com.dochiri.kihan.domain.user;

import com.dochiri.kihan.domain.BaseEntity;
import com.dochiri.kihan.domain.user.exception.UserAccessDeniedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private LocalDateTime lastLoginAt;

    public static User register(String email, String passwordHash, String name) {
        User user = new User();
        user.email = requireNonNull(email);
        user.passwordHash = requireNonNull(passwordHash);
        user.name = requireNonNull(name);
        user.role = UserRole.USER;
        return user;
    }

    public void updateLastLoginAt(LocalDateTime now) {
        this.lastLoginAt = requireNonNull(now);
    }

    public void verifyAccessBy(Long requestUserId, UserRole requestUserRole) {
        boolean owner = getId().equals(requireNonNull(requestUserId));
        boolean admin = requireNonNull(requestUserRole) == UserRole.ADMIN;

        if (!owner && !admin) {
            throw UserAccessDeniedException.forUser(getId());
        }
    }

}