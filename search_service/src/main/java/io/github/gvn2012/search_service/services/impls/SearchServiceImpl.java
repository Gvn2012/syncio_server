package io.github.gvn2012.search_service.services.impls;

import io.github.gvn2012.search_service.documents.PostIndex;
import io.github.gvn2012.search_service.documents.UserIndex;
import io.github.gvn2012.search_service.dtos.responses.UniversalSearchResponse;
import io.github.gvn2012.search_service.services.interfaces.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

        private final ElasticsearchOperations elasticsearchOperations;
        private final MediaEnrichmentService mediaEnrichmentService;
        private final RestTemplate restTemplate = new RestTemplate();

        @org.springframework.beans.factory.annotation.Value("${services.user.url}")
        private String userServiceUrl;

        @org.springframework.beans.factory.annotation.Value("${services.post.url}")
        private String postServiceUrl;

        @Override
        public UniversalSearchResponse search(String keyword, int page, int size, String currentUserId) {
                long startTime = System.currentTimeMillis();
                log.info("Performing universal fuzzy search for keyword: '{}', requester: {}", keyword, currentUserId);

                try {
                        boolean isUsernameSearch = keyword.startsWith("@");
                        String finalKeyword = isUsernameSearch ? keyword.substring(1) : keyword;

                        Query userQuery = NativeQuery.builder()
                                        .withQuery(q -> q
                                                        .bool(b -> {
                                                                b.must(m -> m
                                                                                .match(mm -> mm
                                                                                                .field(isUsernameSearch
                                                                                                                ? "username"
                                                                                                                : "fullName")
                                                                                                .query(finalKeyword)
                                                                                                .fuzziness("AUTO")));
                                                                if (currentUserId != null) {
                                                                        b.mustNot(mn -> mn
                                                                                        .term(t -> t
                                                                                                        .field("_id")
                                                                                                        .value(currentUserId)));
                                                                }
                                                                return b;
                                                        }))
                                        .withPageable(PageRequest.of(page, size))
                                        .build();

                        SearchHits<UserIndex> userHits = elasticsearchOperations.search(userQuery, UserIndex.class);
                        List<UserIndex> users = userHits.getSearchHits().stream()
                                        .map(hit -> hit.getContent())
                                        .collect(Collectors.toList());

                        Query postQuery = NativeQuery.builder()
                                        .withQuery(q -> q
                                                        .match(m -> m
                                                                        .field("content")
                                                                        .query(keyword)
                                                                        .fuzziness("AUTO")))
                                        .withPageable(PageRequest.of(page, size))
                                        .build();

                        SearchHits<PostIndex> postHits = elasticsearchOperations.search(postQuery, PostIndex.class);
                        List<PostIndex> posts = postHits.getSearchHits().stream()
                                        .map(hit -> hit.getContent())
                                        .collect(Collectors.toList());

                        mediaEnrichmentService.enrichUserMediaUrls(users);

                        return UniversalSearchResponse.builder()
                                        .people(users)
                                        .posts(posts)
                                        .totalPeople(userHits.getTotalHits())
                                        .totalPosts(postHits.getTotalHits())
                                        .processingTimeMs(System.currentTimeMillis() - startTime)
                                        .build();
                } catch (Exception e) {
                        log.error("Search failed due to index or cluster issue. This may happen if the persistent storage was purged. Error: {}",
                                        e.getMessage());
                        return UniversalSearchResponse.builder()
                                        .people(Collections.emptyList())
                                        .posts(Collections.emptyList())
                                        .totalPeople(0L)
                                        .totalPosts(0L)
                                        .processingTimeMs(System.currentTimeMillis() - startTime)
                                        .build();
                }
        }

        @Override
        public void triggerReindexing() {
                log.info("Triggering re-indexing across all services...");

                try {
                        restTemplate.postForLocation(userServiceUrl + "/internal/reindex", null);
                        log.info("Triggered re-indexing for User Service");
                } catch (Exception e) {
                        log.error("Failed to trigger re-indexing for User Service: {}", e.getMessage());
                }

                try {
                        restTemplate.postForLocation(postServiceUrl + "/internal/reindex", null);
                        log.info("Triggered re-indexing for Post Service");
                } catch (Exception e) {
                        log.error("Failed to trigger re-indexing for Post Service: {}", e.getMessage());
                }
        }
}
