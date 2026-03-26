package io.github.gvn2012.relationship_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_restrictions", indexes = {
        @Index(name = "ix_restrict_restrictor", columnList = "restrictor_user_id"),
        @Index(name = "ix_restrict_restricted", columnList = "restricted_user_id")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_restriction",
                columnNames = {"restrictor_user_id", "restricted_user_id"}
        )
})
public class UserRestriction extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "restrictor_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID restrictorUserId;

    @NotNull
    @Column(name = "restricted_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID restrictedUserId;

    // Restriction settings
    @Column(name = "hide_comments", nullable = false)
    private Boolean hideComments = true; // their comments only visible to them

    @Column(name = "hide_read_receipts", nullable = false)
    private Boolean hideReadReceipts = true; // don't show when you read their messages

    @Column(name = "hide_online_status", nullable = false)
    private Boolean hideOnlineStatus = true;

    @Column(name = "move_messages_to_requests", nullable = false)
    private Boolean moveMessagesToRequests = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "unrestricted_at")
    private LocalDateTime unrestrictedAt;
}
