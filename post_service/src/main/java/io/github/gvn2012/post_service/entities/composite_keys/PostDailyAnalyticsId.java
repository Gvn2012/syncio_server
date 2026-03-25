package io.github.gvn2012.post_service.entities.composite_keys;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostDailyAnalyticsId implements Serializable {
    private UUID postId;
    private LocalDate analyticsDate;
}
