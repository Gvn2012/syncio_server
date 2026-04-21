package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.PostMapper;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.ISimilarityService;
import io.github.gvn2012.post_service.utils.SimilarityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimilarityServiceImpl implements ISimilarityService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    private static final double DUPLICATE_THRESHOLD = 0.85;
    private static final int MAX_CANDIDATES = 100;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> findSimilarPosts(UUID postId, int limit) {
        Post sourcePost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        List<Post> candidates = postRepository.findByStatus(PostStatus.PUBLISHED, PageRequest.of(0, MAX_CANDIDATES));

        return candidates.stream()
                .filter(p -> !p.getId().equals(postId))
                .map(p -> {
                    double score = SimilarityUtils.calculateCosineSimilarity(sourcePost.getContent(), p.getContent());
                    return new ScoredPost(p, score);
                })
                .filter(sp -> sp.score > 0.1)
                .sorted(Comparator.comparingDouble((ScoredPost sp) -> sp.score).reversed())
                .limit(limit)
                .map(sp -> postMapper.toResponse(sp.post))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicate(String content) {
        if (content == null || content.trim().isEmpty())
            return false;

        List<Post> recentPosts = postRepository.findByStatus(PostStatus.PUBLISHED, PageRequest.of(0, 50));

        for (Post post : recentPosts) {
            double score = SimilarityUtils.calculateCosineSimilarity(content, post.getContent());
            if (score > DUPLICATE_THRESHOLD) {
                log.warn("Near-duplicate post detected via Cosine Similarity (score: {})", score);
                return true;
            }
        }
        return false;
    }

    private record ScoredPost(Post post, double score) {
    }
}
