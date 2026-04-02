package io.github.gvn2012.post_service.entities.composite_keys;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostTagId implements Serializable {
    private UUID postId;
    @jakarta.persistence.Column(name = "tag_id", columnDefinition = "INT")
    private Integer tagId;
}
