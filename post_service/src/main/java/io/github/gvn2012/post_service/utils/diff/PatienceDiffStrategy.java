package io.github.gvn2012.post_service.utils.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class PatienceDiffStrategy implements IDiffStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final MyersDiffStrategy fallbackStrategy;

    public record Edit(String op, String text) {
    }

    @Override
    public byte[] computeDiff(String originalText, String modifiedText) {
        if (originalText == null)
            originalText = "";
        if (modifiedText == null)
            modifiedText = "";

        String[] a = originalText.split("(?<=\n)");
        String[] b = modifiedText.split("(?<=\n)");

        List<Edit> edits = computePatienceDiff(Arrays.asList(a), Arrays.asList(b));
        return serialize(edits);
    }

    public List<Edit> computePatienceDiff(List<String> a, List<String> b) {
        int start = 0;
        int endA = a.size();
        int endB = b.size();

        while (start < endA && start < endB && a.get(start).equals(b.get(start))) {
            start++;
        }

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
        processSection(edits, sliceA, sliceB);

        for (int i = endA; i < a.size(); i++) {
            edits.add(new Edit("EQUAL", a.get(i)));
        }

        return edits;
    }

    private void processSection(List<Edit> edits, List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty())
            return;
        if (a.isEmpty()) {
            for (String s : b)
                edits.add(new Edit("INSERT", s));
            return;
        }
        if (b.isEmpty()) {
            for (String s : a)
                edits.add(new Edit("DELETE", s));
            return;
        }

        Map<String, Integer> countsA = countOccurrences(a);
        Map<String, Integer> countsB = countOccurrences(b);

        List<Integer> uniqueA = new ArrayList<>();
        List<Integer> uniqueB = new ArrayList<>();
        Map<String, Integer> lineToPosB = new HashMap<>();

        for (int i = 0; i < b.size(); i++) {
            String line = b.get(i);
            if (countsB.get(line) == 1) {
                lineToPosB.put(line, i);
            }
        }

        for (int i = 0; i < a.size(); i++) {
            String line = a.get(i);
            if (countsA.get(line) == 1 && lineToPosB.containsKey(line)) {
                uniqueA.add(i);
                uniqueB.add(lineToPosB.get(line));
            }
        }

        if (uniqueA.isEmpty()) {

            fallbackToMyers(edits, a, b);
        } else {

            List<Integer> lcsIdx = computeLCSIndices(uniqueB);

            int lastA = 0;
            int lastB = 0;

            for (int idx : lcsIdx) {
                int currA = uniqueA.get(idx);
                int currB = uniqueB.get(idx);

                processSection(edits, a.subList(lastA, currA), b.subList(lastB, currB));

                edits.add(new Edit("EQUAL", a.get(currA)));

                lastA = currA + 1;
                lastB = currB + 1;
            }

            processSection(edits, a.subList(lastA, a.size()), b.subList(lastB, b.size()));
        }
    }

    private Map<String, Integer> countOccurrences(List<String> list) {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : list) {
            counts.put(s, counts.getOrDefault(s, 0) + 1);
        }
        return counts;
    }

    private List<Integer> computeLCSIndices(List<Integer> bIndices) {
        if (bIndices.isEmpty())
            return Collections.emptyList();

        List<Integer> piles = new ArrayList<>();
        int[] prev = new int[bIndices.size()];
        int[] pileIdx = new int[bIndices.size()];

        for (int i = 0; i < bIndices.size(); i++) {
            int val = bIndices.get(i);
            int pos = Collections.binarySearch(piles, val);
            if (pos < 0)
                pos = -(pos + 1);

            if (pos == piles.size()) {
                piles.add(val);
            } else {
                piles.set(pos, val);
            }

            pileIdx[pos] = i;
            if (pos > 0) {
                prev[i] = pileIdx[pos - 1];
            } else {
                prev[i] = -1;
            }
        }

        List<Integer> result = new ArrayList<>();
        int curr = pileIdx[piles.size() - 1];
        while (curr != -1) {
            result.add(0, bIndices.indexOf(bIndices.get(curr)));
            curr = prev[curr];
        }

        List<Integer> correctedResult = new ArrayList<>();
        int c = pileIdx[piles.size() - 1];
        while (c != -1) {
            correctedResult.add(0, c);
            c = prev[c];
        }
        return correctedResult;
    }

    private void fallbackToMyers(List<Edit> edits, List<String> a, List<String> b) {
        try {
            byte[] myersDiff = fallbackStrategy.computeDiffFromChunks(a.toArray(new String[0]),
                    b.toArray(new String[0]));
            List<MyersDiffStrategy.Edit> subEdits = MAPPER.readValue(myersDiff, new TypeReference<>() {
            });
            for (MyersDiffStrategy.Edit se : subEdits) {
                edits.add(new Edit(se.op(), se.text()));
            }
        } catch (Exception e) {
            log.error("Patience fallback to Myers failed", e);
            for (String s : a)
                edits.add(new Edit("DELETE", s));
            for (String s : b)
                edits.add(new Edit("INSERT", s));
        }
    }

    @Override
    public String applyDiff(String originalText, byte[] diff) {
        if (diff == null || diff.length == 0)
            return originalText;
        try {
            List<Edit> edits = MAPPER.readValue(new String(diff, StandardCharsets.UTF_8), new TypeReference<>() {
            });
            StringBuilder sb = new StringBuilder();
            for (Edit edit : edits) {
                if ("INSERT".equals(edit.op()) || "EQUAL".equals(edit.op())) {
                    sb.append(edit.text());
                }
            }
            return sb.toString();
        } catch (JsonProcessingException e) {
            log.error("Failed to apply patience diff", e);
            throw new RuntimeException("Failed to apply diff", e);
        }
    }

    private byte[] serialize(List<Edit> edits) {
        try {
            return MAPPER.writeValueAsBytes(edits);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize patience diff", e);
            throw new RuntimeException("Failed to serialize diff", e);
        }
    }
}
