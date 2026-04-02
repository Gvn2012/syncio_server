package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.TagTrending;
import io.github.gvn2012.post_service.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl {

    private final PostAnnouncementRepository announcementRepository;
    private final PostPollRepository pollRepository;
    private final FeedItemRepository feedItemRepository;
    private final PostTagRepository postTagRepository;
    private final TagTrendingRepository tagTrendingRepository;

    private static final int FEED_RETENTION_DAYS = 30;

    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanupExpiredAnnouncements() {
        log.info("Starting maintenance: Cleanup expired announcements");
        announcementRepository.unpinExpired(LocalDateTime.now());
    }

    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanupExpiredPolls() {
        log.info("Starting maintenance: Close expired polls");
        pollRepository.closeExpired(LocalDateTime.now());
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void purgeOldFeedItems() {
        log.info("Starting maintenance: Purging feed items older than {} days", FEED_RETENTION_DAYS);
        feedItemRepository.purgeOldItems(LocalDateTime.now().minusDays(FEED_RETENTION_DAYS));
    }

    @Transactional
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 mins
    public void recalculateTrendingTags() {
        log.info("Starting maintenance: Recalculating trending tags");
        tagTrendingRepository.deleteAllInBatch();

        List<Object[]> topTags = postTagRepository.findTopTags(LocalDateTime.now().minusHours(24),
                PageRequest.of(0, 10));

        List<TagTrending> trending = topTags.stream()
                .map(row -> {
                    io.github.gvn2012.post_service.entities.Tag tag = (io.github.gvn2012.post_service.entities.Tag) row[0];
                    Long count = (Long) row[1];
                    return TagTrending.builder()
                            .tagId(tag.getId())
                            .tag(tag)
                            .postCount24h(count)
                            .trendingScore(count.doubleValue())
                            .lastCalculatedAt(LocalDateTime.now())
                            .isTrending(true)
                            .build();
                })
                .toList();

        tagTrendingRepository.saveAll(Objects.requireNonNull(trending));
    }
}
