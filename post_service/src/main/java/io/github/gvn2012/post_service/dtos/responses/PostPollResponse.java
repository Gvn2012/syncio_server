package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPollResponse {
    private UUID id;
    private UUID postId;
    private String question;
    private Boolean allowMultipleAnswers;
    private Integer maxOptionsSelected;
    private LocalDateTime expiresAt;
    private Boolean isClosed;
    private List<PollOptionResponse> options;
}
