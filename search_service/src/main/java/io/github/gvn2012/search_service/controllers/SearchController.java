package io.github.gvn2012.search_service.controllers;

import io.github.gvn2012.search_service.dtos.responses.UniversalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final io.github.gvn2012.search_service.services.interfaces.ISearchService searchService;

    @GetMapping
    public ResponseEntity<UniversalSearchResponse> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId) {

        return ResponseEntity.ok(searchService.search(keyword, page, size, currentUserId));
    }
}
