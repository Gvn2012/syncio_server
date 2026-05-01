package io.github.gvn2012.logging_service.services;

import io.github.gvn2012.logging_service.dtos.CentralizedLogSearchResponse;
import io.github.gvn2012.logging_service.entities.CentralizedLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CentralizedLogQueryService {

    private final MongoTemplate mongoTemplate;

    public CentralizedLogSearchResponse search(
            String service,
            String severity,
            String traceId,
            String requestId,
            String userId,
            String action,
            String outcome,
            Instant from,
            Instant to,
            int page,
            int size) {
        List<Criteria> criteria = new ArrayList<>();
        addEquals(criteria, "service", service);
        addEquals(criteria, "severity", severity);
        addEquals(criteria, "traceId", traceId);
        addEquals(criteria, "requestId", requestId);
        addEquals(criteria, "userId", userId);
        addEquals(criteria, "action", action);
        addEquals(criteria, "outcome", outcome);

        if (from != null || to != null) {
            Criteria timestampCriteria = Criteria.where("timestamp");
            if (from != null) {
                timestampCriteria = timestampCriteria.gte(from);
            }
            if (to != null) {
                timestampCriteria = timestampCriteria.lte(to);
            }
            criteria.add(timestampCriteria);
        }

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, CentralizedLog.class);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.skip((long) page * size);
        query.limit(size);

        List<CentralizedLog> items = mongoTemplate.find(query, CentralizedLog.class);
        return CentralizedLogSearchResponse.builder()
                .items(items)
                .totalElements(total)
                .page(page)
                .size(size)
                .build();
    }

    private void addEquals(List<Criteria> criteria, String field, String value) {
        if (value != null && !value.isBlank()) {
            criteria.add(Criteria.where(field).is(value));
        }
    }
}
