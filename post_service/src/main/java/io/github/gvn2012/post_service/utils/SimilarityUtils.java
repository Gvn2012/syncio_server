package io.github.gvn2012.post_service.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for calculating text similarity using Cosine Similarity
 * algorithm.
 * Uses Bag-of-Words (BoW) approach with basic normalization and stop-word
 * filtering.
 */
@Slf4j
public class SimilarityUtils {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when", "at", "by", "from",
            "for", "with", "in", "on", "to", "of", "is", "am", "are", "was", "were", "be", "been",
            "this", "that", "these", "those", "it", "its", "i", "me", "my", "you", "your", "he",
            "him", "his", "she", "her", "we", "us", "our", "they", "them", "their"));

    /**
     * Calculates the cosine similarity between two strings.
     * 
     * @return a value between 0.0 (no similarity) and 1.0 (identical)
     */
    public static double calculateCosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> vector1 = getTermFrequencyVector(text1);
        Map<String, Integer> vector2 = getTermFrequencyVector(text2);

        Set<String> bothTerms = new HashSet<>(vector1.keySet());
        bothTerms.addAll(vector2.keySet());

        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        for (String term : bothTerms) {
            int freq1 = vector1.getOrDefault(term, 0);
            int freq2 = vector2.getOrDefault(term, 0);

            dotProduct += (double) freq1 * freq2;
            magnitude1 += Math.pow(freq1, 2);
            magnitude2 += Math.pow(freq2, 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    private static Map<String, Integer> getTermFrequencyVector(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Integer> vector = new HashMap<>();
        for (String token : tokens) {
            vector.put(token, vector.getOrDefault(token, 0) + 1);
        }
        return vector;
    }

    private static List<String> tokenize(String text) {
        if (text == null)
            return Collections.emptyList();

        String cleanText = text.replaceAll("<[^>]*>", " ");

        cleanText = cleanText.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");

        return Arrays.stream(cleanText.split("\\s+"))
                .filter(s -> !s.isEmpty() && !STOP_WORDS.contains(s))
                .collect(Collectors.toList());
    }
}
