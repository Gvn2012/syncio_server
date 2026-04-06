package io.github.gvn2012.image_uploading_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadConfirmResponse {
    private String imageId;
    private String status;
}
