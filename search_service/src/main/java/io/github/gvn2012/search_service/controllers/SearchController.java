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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ElasticsearchOperations elasticsearchOperations;

    @GetMapping
    public ResponseEntity<UniversalSearchResponse> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
            
        long startTime = System.currentTimeMillis();
        log.info("Performing universal fuzzy search for keyword: '{}'", keyword);

        // 1. Search People (Fuzzy match on username and fullName)
        Query userQuery = NativeQuery.builder()
                .withQuery(q -> q
                    .multiMatch(m -> m
                        .fields("username", "fullName")
                        .query(keyword)
                        .fuzziness("AUTO")
                    )
                )
                .withPageable(org.springframework.data.domain.PageRequest.of(page, size))
                .build();

        SearchHits<UserIndex> userHits = elasticsearchOperations.search(userQuery, UserIndex.class);
        List<UserIndex> users = userHits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
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
