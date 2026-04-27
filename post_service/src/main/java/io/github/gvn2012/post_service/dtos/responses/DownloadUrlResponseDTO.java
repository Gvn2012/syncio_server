package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadUrlResponseDTO {
    private Map<String, String> downloadUrls;
}
