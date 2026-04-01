package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostContentVersion;

import java.util.UUID;

public interface IPostContentVersionService {
    PostContentVersion captureNewVersion(Post post, UUID editorId, String newContentStr);
    String retrieveFullTextAtVersion(UUID postId, int versionNumber);
}
