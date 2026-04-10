package io.github.gvn2012.image_uploading_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlRequest {
    private Set<String> objectPaths;
}
