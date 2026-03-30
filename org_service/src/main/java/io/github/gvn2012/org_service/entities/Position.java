package io.github.gvn2012.org_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "positions",
        indexes = {
                @Index(name = "ix_positions_org", columnList = "organization_id"),
                @Index(name = "ix_positions_dept", columnList = "department_id"),
                @Index(name = "ix_positions_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_positions_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
public class Position extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "title", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String title;

    @Column(name = "code", nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_salary", precision = 15, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 15, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "requirements", columnDefinition = "JSON")
    private String requirements;
}
