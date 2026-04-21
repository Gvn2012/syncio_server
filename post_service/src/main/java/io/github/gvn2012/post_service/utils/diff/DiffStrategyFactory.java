package io.github.gvn2012.post_service.utils.diff;

import io.github.gvn2012.post_service.entities.enums.DiffAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DiffStrategyFactory {

    private final MyersDiffStrategy myersDiffStrategy;
    private final PatienceDiffStrategy patienceDiffStrategy;
    private final HistogramDiffStrategy histogramDiffStrategy;

    private static final String CHUNK_REGEX = "(?<=\n)|(?=<)|(?<=>)|(?<=\\s)|(?=\\s)|(?<=@\\w+)|(?=@)";
    private static final Pattern CHUNK_PATTERN = Pattern.compile(CHUNK_REGEX);

    public IDiffStrategy getStrategy(DiffAlgorithm algorithm) {
        if (algorithm == null)
            return myersDiffStrategy;

        return switch (algorithm) {
            case MYERS -> myersDiffStrategy;
            case PATIENCE -> patienceDiffStrategy;
            case HISTOGRAM -> histogramDiffStrategy;
            default -> myersDiffStrategy;
        };
    }

    public DiffAlgorithm determineBestAlgorithm(String original, String modified) {
        if (original == null || original.isEmpty() || modified == null || modified.isEmpty()) {
            return DiffAlgorithm.MYERS;
        }

        String[] chunksA = splitIntoChunks(original);
        String[] chunksB = splitIntoChunks(modified);

        if (chunksA.length < 50 || original.length() < 1000) {
            return DiffAlgorithm.MYERS;
        }

        double uniqueRatio = calculateUniqueCommonRatio(chunksA, chunksB);

        if (uniqueRatio > 0.3) {
            return DiffAlgorithm.PATIENCE;
        }

        return DiffAlgorithm.HISTOGRAM;
    }

    public String[] splitIntoChunks(String text) {
        if (text == null)
            return new String[0];
        return CHUNK_PATTERN.split(text);
    }

    private double calculateUniqueCommonRatio(String[] a, String[] b) {
        Set<String> setA = new HashSet<>(Arrays.asList(a));
        Set<String> setB = new HashSet<>(Arrays.asList(b));

        long commonCount = setA.stream().filter(setB::contains).count();
        if (a.length == 0)
            return 0;

        return (double) commonCount / a.length;
    }
}
