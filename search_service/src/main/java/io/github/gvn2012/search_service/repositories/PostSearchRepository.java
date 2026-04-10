package io.github.gvn2012.search_service.repositories;

import io.github.gvn2012.search_service.documents.PostIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostIndex, String> {
}
