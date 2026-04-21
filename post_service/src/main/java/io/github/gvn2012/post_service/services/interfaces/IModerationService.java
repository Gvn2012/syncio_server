package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.Post;
import java.util.UUID;

public interface IModerationService {

    void moderatePost(UUID userId, Post post);
}
