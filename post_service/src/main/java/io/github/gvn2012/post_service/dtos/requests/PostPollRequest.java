package io.github.gvn2012.post_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPollRequest {
    private String question;
    private Boolean allowMultipleAnswers;
    private Integer maxOptionsSelected;
    private LocalDateTime expiresAt;
    private List<PollOptionRequest> options;
}
