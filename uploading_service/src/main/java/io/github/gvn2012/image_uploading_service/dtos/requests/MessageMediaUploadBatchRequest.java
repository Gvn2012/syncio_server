package io.github.gvn2012.image_uploading_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMediaUploadBatchRequest {
    private List<MessageMediaUploadRequest> requests;
}
