package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostContentVersion;
import io.github.gvn2012.post_service.entities.enums.DiffAlgorithm;
import io.github.gvn2012.post_service.repositories.PostContentVersionRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostContentVersionService;
import io.github.gvn2012.post_service.utils.diff.DiffStrategyFactory;
import io.github.gvn2012.post_service.utils.diff.IDiffStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostContentVersionServiceImpl implements IPostContentVersionService {

    public record PostVersionEvent(UUID postId, UUID editorId, String content) {}

    private final PostContentVersionRepository versionRepository;
    private final PostRepository postRepository;
    private final DiffStrategyFactory diffFactory;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostVersionEvent(PostVersionEvent event) {
        log.info("Processing async version capture for post: {}", event.postId());
        postRepository.findById(event.postId()).ifPresent(post -> {
            captureNewVersion(post, event.editorId(), event.content());
        });
    }

    @Override
    @Transactional
    public void captureNewVersion(Post post, UUID editorId, String newContentStr) {
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

        versionRepository.save(newVersion);
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
