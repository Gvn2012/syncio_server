package io.github.gvn2012.post_service.utils.diff;

import io.github.gvn2012.post_service.entities.enums.DiffAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiffStrategyFactory {

    private final MyersDiffStrategy myersDiffStrategy;
    private final PatienceDiffStrategy patienceDiffStrategy;
    private final HistogramDiffStrategy histogramDiffStrategy;

    public IDiffStrategy getStrategy(DiffAlgorithm algorithm) {
        if (algorithm == null) return myersDiffStrategy;
        
        return switch (algorithm) {
            case MYERS -> myersDiffStrategy;
            case PATIENCE -> patienceDiffStrategy;
            case HISTOGRAM -> histogramDiffStrategy;
            default -> myersDiffStrategy;
        };
    }
}
