package io.github.gvn2012.post_service.utils;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SimilarityUtilsTest {

    @Test
    public void testIdenticalStrings() {
        String text = "The quick brown fox jumps over the lazy dog";
        double similarity = SimilarityUtils.calculateCosineSimilarity(text, text);
        assertEquals(similarity, 1.0, 0.001);
    }

    @Test
    public void testCompletelyDifferentStrings() {
        String text1 = "Apple Banana Orange";
        String text2 = "Computer Keyboard Mouse";
        double similarity = SimilarityUtils.calculateCosineSimilarity(text1, text2);
        assertEquals(similarity, 0.0, 0.001);
    }

    @Test
    public void testPartialSimilarity() {
        String text1 = "Java Spring Boot Microservices";
        String text2 = "Java Spring Data JPA";
        double similarity = SimilarityUtils.calculateCosineSimilarity(text1, text2);
        // "Java" and "Spring" are common. "Boot", "Microservices", "Data", "JPA" are unique.
        assertTrue(similarity > 0.4 && similarity < 0.6);
    }

    @Test
    public void testStopWordsFiltering() {
        String text1 = "This is a test";
        String text2 = "That was a test";
        // After stop word removal, both should just be "test"
        double similarity = SimilarityUtils.calculateCosineSimilarity(text1, text2);
        assertEquals(similarity, 1.0, 0.001);
    }

    @Test
    public void testHtmlTagRemoval() {
        String text1 = "<p>Hello World</p>";
        String text2 = "Hello World";
        double similarity = SimilarityUtils.calculateCosineSimilarity(text1, text2);
        assertEquals(similarity, 1.0, 0.001);
    }

    @Test
    public void testNullAndEmpty() {
        assertEquals(SimilarityUtils.calculateCosineSimilarity(null, "test"), 0.0);
        assertEquals(SimilarityUtils.calculateCosineSimilarity("test", ""), 0.0);
    }
}
