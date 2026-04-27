package io.github.gvn2012.search_service.services.impls;

import io.github.gvn2012.search_service.clients.UploadClient;
import io.github.gvn2012.search_service.documents.PostIndex;
import io.github.gvn2012.search_service.documents.UserIndex;
import io.github.gvn2012.search_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.search_service.dtos.responses.DownloadUrlResponseDTO;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final UploadClient uploadClient;

    @Override
    public UniversalSearchResponse search(String keyword, int page, int size, String currentUserId) {
        long startTime = System.currentTimeMillis();
        log.info("Performing universal fuzzy search for keyword: '{}', requester: {}", keyword, currentUserId);

        boolean isUsernameSearch = keyword.startsWith("@");
        String finalKeyword = isUsernameSearch ? keyword.substring(1) : keyword;

        // 1. Search Users
        Query userQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            b.must(m -> m
                                    .match(mm -> mm
                                            .field(isUsernameSearch ? "username" : "fullName")
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

        // 2. Search Posts
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

        // 3. Enrich URLs
        enrichMediaUrls(users);

        return UniversalSearchResponse.builder()
                .people(users)
                .posts(posts)
                .totalPeople(userHits.getTotalHits())
                .totalPosts(postHits.getTotalHits())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    private void enrichMediaUrls(List<UserIndex> users) {
        if (users == null || users.isEmpty()) return;

        Set<String> pathsToSign = users.stream()
                .map(UserIndex::getAvatarPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pathsToSign.isEmpty()) return;

        DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
        Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();

        for (UserIndex user : users) {
            if (user.getAvatarPath() != null) {
                user.setAvatarUrl(signedUrls.getOrDefault(user.getAvatarPath(), user.getAvatarUrl()));
            }
        }
    }
}
