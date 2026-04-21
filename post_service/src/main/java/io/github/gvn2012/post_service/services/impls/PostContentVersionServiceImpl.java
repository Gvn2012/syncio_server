package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostContentVersion;
import io.github.gvn2012.post_service.entities.enums.DiffAlgorithm;
import io.github.gvn2012.post_service.repositories.PostContentVersionRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.utils.diff.DiffStrategyFactory;
import io.github.gvn2012.post_service.utils.diff.IDiffStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostContentVersionServiceImpl implements IPostContentVersionService {

    private final PostContentVersionRepository versionRepository;
    private final DiffStrategyFactory diffFactory;

    @Override
    @Transactional
    public PostContentVersion captureNewVersion(Post post, UUID editorId, String newContentStr) {
        if (newContentStr == null)
            newContentStr = "";

        List<PostContentVersion> versions = versionRepository.findByPostIdOrderByVersionNumberDesc(post.getId());
        int versionNum = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;

        String previousContent = retrieveFullTextAtVersion(post.getId(), versionNum - 1);

        DiffAlgorithm bestAlgo = diffFactory.determineBestAlgorithm(previousContent, newContentStr);

        PostContentVersion newVersion = new PostContentVersion();
        newVersion.setPost(post);
        newVersion.setCreatedBy(editorId);
        newVersion.setVersionNumber(versionNum);
        newVersion.setDiffAlgorithm(bestAlgo);

        IDiffStrategy strategy = diffFactory.getStrategy(bestAlgo);
        byte[] diffBytes = strategy.computeDiff(previousContent, newContentStr);

        newVersion.setContentDiff(diffBytes);
        newVersion.setOriginalSize(previousContent.length());
        newVersion.setCompressedSize(diffBytes.length);

        if (versionNum % 10 == 0) {
            newVersion.setIsSnapshot(true);
            newVersion.setContentSnapshot(newContentStr);
        }

        return versionRepository.save(newVersion);
    }

    @Override
    public String retrieveFullTextAtVersion(UUID postId, int targetVersion) {
        if (targetVersion == 0)
            return "";

        List<PostContentVersion> versions = versionRepository.findByPostIdOrderByVersionNumberAsc(postId);

        if (versions.isEmpty())
            return "";

        String currentText = "";
        for (PostContentVersion v : versions) {
            if (v.getVersionNumber() > targetVersion)
                break;
            if (Boolean.TRUE.equals(v.getIsSnapshot())) {
                currentText = v.getContentSnapshot();
            } else {
                IDiffStrategy strategy = diffFactory.getStrategy(v.getDiffAlgorithm());
                currentText = strategy.applyDiff(currentText, v.getContentDiff());
            }
        }

        return currentText;
    }
}
