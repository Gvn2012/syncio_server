package io.github.gvn2012.search_service.controllers;

import io.github.gvn2012.search_service.documents.PostIndex;
import io.github.gvn2012.search_service.documents.UserIndex;
import io.github.gvn2012.search_service.dtos.responses.UniversalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ElasticsearchOperations elasticsearchOperations;
    @org.springframework.beans.factory.annotation.Value("${syncio.gateway.host:http://localhost:8080}")
    private String gatewayHost;

    @GetMapping
    public ResponseEntity<UniversalSearchResponse> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {
            
        long startTime = System.currentTimeMillis();
        log.info("Performing universal fuzzy search for keyword: '{}', requester: {}", keyword, currentUserId);
        boolean isUsernameSearch = keyword.startsWith("@");
        String finalKeyword = isUsernameSearch ? keyword.substring(1) : keyword;

        // 1. Search People (Fuzzy match based on @ prefix, excluding self)
        Query userQuery = NativeQuery.builder()
                .withQuery(q -> q
                    .bool(b -> {
                        b.must(m -> m
                            .match(mm -> mm
                                .field(isUsernameSearch ? "username" : "fullName")
                                .query(finalKeyword)
                                .fuzziness("AUTO")
                            )
                        );
                        if (currentUserId != null) {
                            b.mustNot(mn -> mn
                                .term(t -> t
                                    .field("_id")
                                    .value(currentUserId)
                                )
                            );
                        }
                        return b;
                    })
                )
                .withPageable(org.springframework.data.domain.PageRequest.of(page, size))
                .build();

        SearchHits<UserIndex> userHits = elasticsearchOperations.search(userQuery, UserIndex.class);
        List<UserIndex> users = userHits.getSearchHits().stream()
                .map(hit -> {
                    UserIndex content = hit.getContent();
                    if (content.getAvatarPath() != null) {
                        content.setAvatarUrl(String.format("%s/api/v1/upload/view?path=%s", gatewayHost, content.getAvatarPath()));
                    }
                    return content;
                })
                .toList();

        // 2. Search Posts (Fuzzy match on content)
        Query postQuery = NativeQuery.builder()
                .withQuery(q -> q
                    .match(m -> m
                        .field("content")
                        .query(keyword)
                        .fuzziness("AUTO")
                    )
                )
                .withPageable(org.springframework.data.domain.PageRequest.of(page, size))
                .build();

        SearchHits<PostIndex> postHits = elasticsearchOperations.search(postQuery, PostIndex.class);
        List<PostIndex> posts = postHits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .toList();

        UniversalSearchResponse response = UniversalSearchResponse.builder()
                .people(users)
                .posts(posts)
                .totalPeople(userHits.getTotalHits())
                .totalPosts(postHits.getTotalHits())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();

        return ResponseEntity.ok(response);
    }
}
