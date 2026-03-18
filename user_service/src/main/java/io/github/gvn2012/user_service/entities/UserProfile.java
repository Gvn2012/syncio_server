package io.github.gvn2012.user_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Entity
@Table(
        name = "user_profiles",
        indexes = {
                @Index(name = "ix_user_profiles_department_id", columnList = "department_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_user_profiles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "ux_user_profiles_employee_id", columnNames = "employee_id")
        }
)
public class UserProfile extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    @Past
    private LocalDate dateOfBirth;

    @Column(name = "job_title", length = 255)
    private String jobTitle;

    @Column(name = "department_id", columnDefinition = "BINARY(16)")
    private UUID departmentId;

    @Column(name = "employee_id", length = 64)
    private String employeeId;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "skills", columnDefinition = "json")
    private String skills;

    @Column(name = "contact_info", columnDefinition = "json")
    private String contactInfo;

    @Column(name = "profile_completed_score")
    @Min(0)
    @Max(100)
    private Short profileCompletedScore = 0;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProfilePicture> pictures = new LinkedHashSet<>();
}