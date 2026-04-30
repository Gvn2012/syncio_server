package io.github.gvn2012.messaging_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {
    private String conversationId;
    private String name;
    private String description;
    private String groupAvatar;
}
