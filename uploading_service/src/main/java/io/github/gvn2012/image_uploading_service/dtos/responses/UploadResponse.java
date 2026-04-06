package io.github.gvn2012.image_uploading_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
    private String imageId;
    private String uploadUrl;
    private String method;
    private Map<String, String> headers;
    private int expiresIn;
}