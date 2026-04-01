package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "post_task_assignees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_task_user", columnNames = {"task_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostTaskAssignee {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private PostTask task;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}