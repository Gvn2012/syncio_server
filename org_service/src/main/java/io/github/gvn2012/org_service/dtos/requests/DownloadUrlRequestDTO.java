package io.github.gvn2012.org_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadUrlRequestDTO {
    private Set<String> objectPaths;
}
