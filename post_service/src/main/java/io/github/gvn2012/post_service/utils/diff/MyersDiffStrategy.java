package io.github.gvn2012.post_service.utils.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MyersDiffStrategy implements IDiffStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        return computeDiffFromChunks(a, b);
    }

    public byte[] computeDiffFromChunks(String[] a, String[] b) {
        int n = a.length;
        int m = b.length;
        int max = n + m;
        int[] v = new int[2 * max + 1];
        List<int[]> trace = new ArrayList<>();

        v[max + 1] = 0;
        for (int d = 0; d <= max; d++) {
            trace.add(v.clone());
            for (int k = -d; k <= d; k += 2) {
                int index = k + max;
                int x;
                if (k == -d || (k != d && v[index - 1] < v[index + 1])) {
                    x = v[index + 1];
                } else {
                    x = v[index - 1] + 1;
                }
                int y = x - k;
                while (x < n && y < m && a[x].equals(b[y])) {
                    x++;
                    y++;
                }
                v[index] = x;
                if (x >= n && y >= m) {
                    return buildEdits(trace, a, b, max);
                }
            }
        }
        return serialize(List.of(new Edit("EQUAL", String.join("", a))));
    }

    private byte[] buildEdits(List<int[]> trace, String[] a, String[] b, int max) {
        int x = a.length;
        int y = b.length;
        List<Edit> edits = new ArrayList<>();

        for (int d = trace.size() - 1; d > 0; d--) {
            int[] vPrev = trace.get(d - 1);
            int k = x - y;
            int index = k + max;
            int prevK;

            if (k == -d || (k != d && vPrev[index - 1] < vPrev[index + 1])) {
                prevK = k + 1;
            } else {
                prevK = k - 1;
            }
            int prevX = vPrev[prevK + max];
            int prevY = prevX - prevK;

            while (x > prevX && y > prevY) {
                x--;
                y--;
                edits.add(0, new Edit("EQUAL", a[x]));
            }
            if (x > prevX) {
                x--;
                edits.add(0, new Edit("DELETE", a[x]));
            } else if (y > prevY) {
                y--;
                edits.add(0, new Edit("INSERT", b[y]));
            }
        }
        while (x > 0 && y > 0) {
            x--;
            y--;
            edits.add(0, new Edit("EQUAL", a[x]));
        }
        return serialize(edits);
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
            log.error("Failed to apply diff", e);
            throw new RuntimeException("Failed to apply diff", e);
        }
    }

    private byte[] serialize(List<Edit> edits) {
        try {
            return MAPPER.writeValueAsBytes(edits);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize diff", e);
            throw new RuntimeException("Failed to serialize diff", e);
        }
    }
}
