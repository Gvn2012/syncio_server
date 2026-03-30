package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "org_policies",
        indexes = {
                @Index(name = "ix_org_policies_org", columnList = "organization_id"),
                @Index(name = "ix_org_policies_category", columnList = "category"),
                @Index(name = "ix_org_policies_active", columnList = "organization_id, is_active")
        }
)
public class OrgPolicy extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "title", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String content;

    @Column(name = "version", nullable = false, length = 32)
    @Size(max = 32)
    private String version = "1.0";

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private PolicyCategory category;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    /** References User.id in user_service */
    @Column(name = "approved_by_id", columnDefinition = "BINARY(16)")
    private UUID approvedById;
}
