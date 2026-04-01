package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostDailyAnalytics;
import io.github.gvn2012.post_service.entities.composite_keys.PostDailyAnalyticsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostDailyAnalyticsRepository extends JpaRepository<PostDailyAnalytics, PostDailyAnalyticsId> {

}
