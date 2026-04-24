package io.github.gvn2012.post_service.utils;

import io.github.gvn2012.post_service.services.interfaces.IBannedWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationScanner {

    private final IBannedWordService bannedWordService;

    public static class ScanResult {
        private final String sanitizedContent;
        private final List<String> detectedWords;

        public ScanResult(String sanitizedContent, List<String> detectedWords) {
            this.sanitizedContent = sanitizedContent;
            this.detectedWords = detectedWords;
        }

        public String getSanitizedContent() {
            return sanitizedContent;
        }

        public List<String> getDetectedWords() {
            return detectedWords;
        }

        public boolean hasViolations() {
            return !detectedWords.isEmpty();
        }
    }

    public ScanResult scanAndCensor(String content) {
        if (content == null || content.isEmpty()) {
            return new ScanResult(content, new ArrayList<>());
        }

        List<String> bannedWords = bannedWordService.getAllBannedWords();
        if (bannedWords == null || bannedWords.isEmpty()) {
            return new ScanResult(content, new ArrayList<>());
        }

        String regex = bannedWords.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|", "(?i)\\b(", ")\\b"));

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        List<String> detected = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            detected.add(matcher.group());
            sb.append(content, lastEnd, matcher.start());

            // Replace with stars of same length
            String stars = "*".repeat(matcher.group().length());
            sb.append(stars);

            lastEnd = matcher.end();
        }
        sb.append(content.substring(lastEnd));

        return new ScanResult(sb.toString(), detected);
    }

    /**
     * Specifically handles HTML content by stripping tags before scanning,
     * but we want to return the censored HTML.
     * Note: A robust version would parse HTML to avoid censoring tags themselves.
     * For now, we perform a simple replacement which is usually safe for \b
     * matches.
     */
    public ScanResult scanAndCensorHtml(String htmlContent) {
        // We reuse the same logic. \b (word boundaries) usually protects HTML tags
        // like <p> from being matched unless the bad word is "p".
        return scanAndCensor(htmlContent);
    }
}
