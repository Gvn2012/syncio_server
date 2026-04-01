package io.github.gvn2012.post_service.utils.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class PatienceDiffStrategy implements IDiffStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final MyersDiffStrategy fallbackStrategy = new MyersDiffStrategy();

    public record Edit(String op, String text) {}

    @Override
    public byte[] computeDiff(String originalText, String modifiedText) {
        if (originalText == null) originalText = "";
        if (modifiedText == null) modifiedText = "";
        
        List<String> a = Arrays.asList(originalText.split("(?<=\n)"));
        List<String> b = Arrays.asList(modifiedText.split("(?<=\n)"));

        List<Edit> edits = computePatienceDiff(a, b);
        try {
            return MAPPER.writeValueAsBytes(edits);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization error", e);
        }
    }

    private List<Edit> computePatienceDiff(List<String> a, List<String> b) {
        int start = 0;
        int endA = a.size();
        int endB = b.size();

        while (start < endA && start < endB && a.get(start).equals(b.get(start))) start++;
        while (endA > start && endB > start && a.get(endA - 1).equals(b.get(endB - 1))) {
            endA--;
            endB--;
        }

        List<Edit> edits = new ArrayList<>();
        for (int i = 0; i < start; i++) {
            edits.add(new Edit("EQUAL", a.get(i)));
        }

        List<String> sliceA = a.subList(start, endA);
        List<String> sliceB = b.subList(start, endB);

        if (!sliceA.isEmpty() || !sliceB.isEmpty()) {
            Map<String, Integer> countsA = new HashMap<>();
            Map<String, Integer> countsB = new HashMap<>();
            
            for (String line : sliceA) countsA.put(line, countsA.getOrDefault(line, 0) + 1);
            for (String line : sliceB) countsB.put(line, countsB.getOrDefault(line, 0) + 1);

            List<String> uniqueCommon = new ArrayList<>();
            for (String line : sliceA) {
                if (countsA.getOrDefault(line, 0) == 1 && countsB.getOrDefault(line, 0) == 1) {
                    uniqueCommon.add(line);
                }
            }

            if (uniqueCommon.isEmpty()) {
                fallbackToMyers(edits, sliceA, sliceB);
            } else {
                List<String> lcs = computeLCS(uniqueCommon, sliceB);
                int aIdx = 0;
                int bIdx = 0;

                for (String lcsLine : lcs) {
                    int nextA = sliceA.indexOf(lcsLine);
                    int nextB = sliceB.indexOf(lcsLine);

                    fallbackToMyers(edits, sliceA.subList(aIdx, nextA), sliceB.subList(bIdx, nextB));
                    edits.add(new Edit("EQUAL", lcsLine));

                    aIdx = nextA + 1;
                    bIdx = nextB + 1;
                }
                fallbackToMyers(edits, sliceA.subList(aIdx, sliceA.size()), sliceB.subList(bIdx, sliceB.size()));
            }
        }

        for (int i = endA; i < a.size(); i++) {
            edits.add(new Edit("EQUAL", a.get(i)));
        }

        return edits;
    }

    private void fallbackToMyers(List<Edit> edits, List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty()) return;
        if (a.isEmpty()) {
            for (String s : b) edits.add(new Edit("INSERT", s));
            return;
        }
        if (b.isEmpty()) {
            for (String s : a) edits.add(new Edit("DELETE", s));
            return;
        }
        
        try {
            byte[] myersDiff = fallbackStrategy.computeDiff(String.join("", a), String.join("", b));
            List<Edit> subEdits = MAPPER.readValue(myersDiff, new TypeReference<>() {});
            edits.addAll(subEdits);
        } catch (Exception e) {
            for (String s : a) edits.add(new Edit("DELETE", s));
            for (String s : b) edits.add(new Edit("INSERT", s));
        }
    }

    private List<String> computeLCS(List<String> seq1, List<String> seq2) {
        int m = seq1.size();
        int n = seq2.size();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (seq1.get(i - 1).equals(seq2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<String> lcs = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (seq1.get(i - 1).equals(seq2.get(j - 1))) {
                lcs.add(0, seq1.get(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return lcs;
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
