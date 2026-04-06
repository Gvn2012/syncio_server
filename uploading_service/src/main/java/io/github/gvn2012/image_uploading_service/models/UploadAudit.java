package io.github.gvn2012.image_uploading_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "upload_audits")
public class UploadAudit {
    @Id
    private String id;
    private String imageId;
    private String fileName;
    private String contentType;
    private String status;
    private String uploadUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
