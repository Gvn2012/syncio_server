package io.github.gvn2012.post_service.utils.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class HistogramDiffStrategy implements IDiffStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final MyersDiffStrategy fallbackStrategy = new MyersDiffStrategy();

    public record Edit(String op, String text) {}

    @Override
    public byte[] computeDiff(String originalText, String modifiedText) {
        if (originalText == null) originalText = "";
        if (modifiedText == null) modifiedText = "";

        List<String> a = Arrays.asList(originalText.split("(?<=\n)"));
        List<String> b = Arrays.asList(modifiedText.split("(?<=\n)"));

        List<Edit> edits = computeHistogramDiff(a, b);
        try {
            return MAPPER.writeValueAsBytes(edits);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization error", e);
        }
    }

    private List<Edit> computeHistogramDiff(List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty()) return new ArrayList<>();
        if (a.isEmpty()) {
            return b.stream().map(s -> new Edit("INSERT", s)).toList();
        }
        if (b.isEmpty()) {
            return a.stream().map(s -> new Edit("DELETE", s)).toList();
        }

        Map<String, Integer> counts = new HashMap<>();
        for (String line : a) counts.put(line, counts.getOrDefault(line, 0) + 1);
        for (String line : b) counts.put(line, counts.getOrDefault(line, 0) + 1);

        String bestSplit = null;
        int minCount = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() < minCount && a.contains(entry.getKey()) && b.contains(entry.getKey())) {
                minCount = entry.getValue();
                bestSplit = entry.getKey();
            }
        }

        List<Edit> edits = new ArrayList<>();
        if (bestSplit != null && minCount < 10) {
            int aIdx = a.indexOf(bestSplit);
            int bIdx = b.indexOf(bestSplit);
            
            List<Edit> left = computeHistogramDiff(a.subList(0, aIdx), b.subList(0, bIdx));
            List<Edit> right = computeHistogramDiff(a.subList(aIdx + 1, a.size()), b.subList(bIdx + 1, b.size()));
            
            edits.addAll(left);
            edits.add(new Edit("EQUAL", bestSplit));
            edits.addAll(right);
        } else {
            fallbackToMyers(edits, a, b);
        }

        return edits;
    }

    private void fallbackToMyers(List<Edit> edits, List<String> a, List<String> b) {
        try {
            byte[] myersDiff = fallbackStrategy.computeDiff(String.join("", a), String.join("", b));
            List<Edit> subEdits = MAPPER.readValue(myersDiff, new TypeReference<>() {});
            edits.addAll(subEdits);
        } catch (Exception e) {
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
            throw new RuntimeException("Failed to apply diff", e);
        }
    }
}
