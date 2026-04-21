package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.Post;

import java.util.UUID;

public interface IPostContentVersionService {
    void captureNewVersion(Post post, UUID editorId, String newContentStr);
    String retrieveFullTextAtVersion(UUID postId, int versionNumber);
}
