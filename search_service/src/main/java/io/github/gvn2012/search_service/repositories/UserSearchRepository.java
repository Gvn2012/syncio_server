package io.github.gvn2012.search_service.repositories;

import io.github.gvn2012.search_service.documents.UserIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserIndex, String> {
}
