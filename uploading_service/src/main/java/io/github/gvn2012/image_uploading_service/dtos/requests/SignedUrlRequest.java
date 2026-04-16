package io.github.gvn2012.image_uploading_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlRequest {
    private Map<String, String> objectPathsWithContentType;
}
