package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "post_tasks", indexes = {
        @Index(name = "ix_post_tasks_post_id", columnList = "post_id"),
        @Index(name = "ix_post_tasks_due_date", columnList = "due_date"),
        @Index(name = "ix_post_tasks_status", columnList = "status")
})
public class PostTask {

    @Id
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "priority")
    private Integer priority = 0; // 0=low,1=medium,2=high (or enum)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.OPEN;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}