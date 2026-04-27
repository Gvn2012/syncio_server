package io.github.gvn2012.image_uploading_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadUrlResponse {
    private Map<String, String> downloadUrls;
}
