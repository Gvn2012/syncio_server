package io.github.gvn2012.user_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfilePictureResponse {
    private String id;
    private Long fileSize;
    private Integer height;
    private Integer width;
    private String url;
    private String mimeType;
    private Boolean deleted;
    private Boolean primary;
}
