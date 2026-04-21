package io.github.gvn2012.post_service.utils.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementation of the Histogram Diff algorithm.
 * An improvement over Patience Diff that uses low-frequency lines as anchors.
 * Provides a great balance between diff quality and performance.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HistogramDiffStrategy implements IDiffStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final MyersDiffStrategy fallbackStrategy;

    public record Edit(String op, String text) {}

    @Override
    public byte[] computeDiff(String originalText, String modifiedText) {
        if (originalText == null) originalText = "";
        if (modifiedText == null) modifiedText = "";

        String[] a = originalText.split("(?<=\n)");
        String[] b = modifiedText.split("(?<=\n)");

        List<Edit> edits = computeHistogramDiff(Arrays.asList(a), Arrays.asList(b));
        return serialize(edits);
    }

    public List<Edit> computeHistogramDiff(List<String> a, List<String> b) {
        int start = 0;
        int endA = a.size();
        int endB = b.size();

        // Match common prefix
        while (start < endA && start < endB && a.get(start).equals(b.get(start))) {
            start++;
        }
        // Match common suffix
        while (endA > start && endB > start && a.get(endA - 1).equals(b.get(endB - 1))) {
            endA--;
            endB--;
        }

        List<Edit> edits = new ArrayList<>();
        // Prefix
        for (int i = 0; i < start; i++) {
            edits.add(new Edit("EQUAL", a.get(i)));
        }

        // Sectioned middle
        processSection(edits, a.subList(start, endA), b.subList(start, endB));

        // Suffix
        for (int i = endA; i < a.size(); i++) {
            edits.add(new Edit("EQUAL", a.get(i)));
        }

        return edits;
    }

    private void processSection(List<Edit> edits, List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty()) return;
        if (a.isEmpty()) {
            for (String s : b) edits.add(new Edit("INSERT", s));
            return;
        }
        if (b.isEmpty()) {
            for (String s : a) edits.add(new Edit("DELETE", s));
            return;
        }

        // Use Histogram to find the best pivot
        Map<String, Integer> countsA = countOccurrences(a);
        Map<String, Integer> countsB = countOccurrences(b);

        String pivot = null;
        int minFreq = Integer.MAX_VALUE;
        int bestAIdx = -1;
        int bestBIdx = -1;

        for (int i = 0; i < a.size(); i++) {
            String line = a.get(i);
            Integer countB = countsB.get(line);
            if (countB != null) {
                int freq = countsA.get(line) + countB;
                if (freq < minFreq) {
                    minFreq = freq;
                    pivot = line;
                    bestAIdx = i;
                    bestBIdx = b.indexOf(line);
                } else if (freq == minFreq && freq != Integer.MAX_VALUE) {
                    // Tie-breaker: pick something closer to the middle
                    if (Math.abs(i - a.size() / 2) < Math.abs(bestAIdx - a.size() / 2)) {
                        pivot = line;
                        bestAIdx = i;
                        bestBIdx = b.indexOf(line);
                    }
                }
            }
        }

        if (pivot == null) {
            // No common lines, fallback to Myers
            fallbackToMyers(edits, a, b);
        } else {
            // Recurse using pivot as anchor
            processSection(edits, a.subList(0, bestAIdx), b.subList(0, bestBIdx));
            edits.add(new Edit("EQUAL", pivot));
            processSection(edits, a.subList(bestAIdx + 1, a.size()), b.subList(bestBIdx + 1, b.size()));
        }
    }

    private Map<String, Integer> countOccurrences(List<String> list) {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : list) {
            counts.put(s, counts.getOrDefault(s, 0) + 1);
        }
        return counts;
    }

    private void fallbackToMyers(List<Edit> edits, List<String> a, List<String> b) {
        try {
            byte[] myersDiff = fallbackStrategy.computeDiffFromChunks(a.toArray(new String[0]), b.toArray(new String[0]));
            List<MyersDiffStrategy.Edit> subEdits = MAPPER.readValue(myersDiff, new TypeReference<>() {});
            for (MyersDiffStrategy.Edit se : subEdits) {
                edits.add(new Edit(se.op(), se.text()));
            }
        } catch (Exception e) {
            log.error("Histogram fallback to Myers failed", e);
            for (String s : a) edits.add(new Edit("DELETE", s));
            for (String s : b) edits.add(new Edit("INSERT", s));
        }
    }

    @Override
    public String applyDiff(String originalText, byte[] diff) {
        if (diff == null || diff.length == 0) return originalText;
        try {
            List<Edit> edits = MAPPER.readValue(new String(diff, StandardCharsets.UTF_8), new TypeReference<>() {});
            StringBuilder sb = new StringBuilder();
            for (Edit edit : edits) {
                if ("INSERT".equals(edit.op()) || "EQUAL".equals(edit.op())) {
                    sb.append(edit.text());
                }
            }
            return sb.toString();
        } catch (JsonProcessingException e) {
            log.error("Failed to apply histogram diff", e);
            throw new RuntimeException("Failed to apply diff", e);
        }
    }

    private byte[] serialize(List<Edit> edits) {
        try {
            return MAPPER.writeValueAsBytes(edits);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize histogram diff", e);
            throw new RuntimeException("Failed to serialize diff", e);
        }
    }
}

