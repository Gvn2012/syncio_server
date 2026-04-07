package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event published when an image is successfully uploaded to GCS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadedEvent implements Serializable {
    private String imageId;
    private String objectPath;
    private String bucketName;
    private String contentType;
    private Long size;
}
