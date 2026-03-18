package io.github.gvn2012.user_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "ix_users_active", columnList = "is_active"),
                @Index(name = "ix_users_soft_deleted", columnList = "is_soft_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_users_username", columnNames = "username")
        }
)
public class User extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 64)
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Column(name = "password_hash", length = 512)
    @Size(min = 8, max = 512)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_soft_deleted", nullable = false)
    private Boolean softDeleted = false;

    @Column(name = "is_hard_deleted", nullable = false)
    private Boolean hardDeleted = false;

    @Column(name = "is_banned", nullable = false)
    private Boolean banned = false;

    @Column(name = "is_suspended", nullable = false)
    private Boolean suspended = false;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Column(name = "locale", length = 16)
    private String locale = "en";

    @Column(name = "timezone", length = 64)
    private String timezone;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserEmail> emails = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserPhone> phones = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPasswordReset> passwordResets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();
}