package io.github.gvn2012.search_service.services.interfaces;

import io.github.gvn2012.search_service.dtos.responses.UniversalSearchResponse;

public interface ISearchService {
    UniversalSearchResponse search(String keyword, int page, int size, String currentUserId);
}
