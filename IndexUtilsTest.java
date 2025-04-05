import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IndexUtilsTest {

    @Test
    void getInverseDocumentFrequencies() {
    }

    @Test
    void testGetUniqueWords() {
        Map<String, List<String>> docs = Map.ofEntries(
                Map.entry("d1", Arrays.asList("hello", "world", "how", "are", "you")),
                Map.entry("d2", Arrays.asList("hello", "you", "there")),
                Map.entry("d3", Arrays.asList("goodbye", "world", "see", "you", "later"))
        );

        Set<String> uniqueWords = IndexUtils.getUniqueWords(docs);
        assertEquals(9, uniqueWords.size());

        assertTrue(uniqueWords.contains("hello"));
        assertTrue(uniqueWords.contains("world"));
        assertTrue(uniqueWords.contains("how"));
        assertTrue(uniqueWords.contains("are"));
        assertTrue(uniqueWords.contains("you"));
        assertTrue(uniqueWords.contains("there"));
        assertTrue(uniqueWords.contains("goodbye"));
        assertTrue(uniqueWords.contains("see"));
        assertTrue(uniqueWords.contains("later"));
    }

    @Test
    void testGetInverseDocumentFrequencies() {
        Map<String, List<String>> docs = Map.ofEntries(
                Map.entry("d1", Arrays.asList("hello", "world", "how", "are", "you")),
                Map.entry("d2", Arrays.asList("hello", "you", "there")),
                Map.entry("d3", Arrays.asList("goodbye", "world", "see", "you", "later"))
        );

        Map<String, Double> inverseDocumentFrequencies = IndexUtils.getInverseDocumentFrequencies(docs);
        assertEquals(9, inverseDocumentFrequencies.size());

        // IDF("hello") = log(3/2)
        assertTrue(inverseDocumentFrequencies.containsKey("hello"));
        assertEquals(0.405, inverseDocumentFrequencies.get("hello"), 0.001);

        // IDF("goodbye") = log(3/1)
        assertTrue(inverseDocumentFrequencies.containsKey("goodbye"));
        assertEquals(1.098, inverseDocumentFrequencies.get("goodbye"), 0.001);
    }

    @Test
    void testGetTFIDF() {
        Map<String, List<String>> docs = Map.ofEntries(
                Map.entry("d1", Arrays.asList("hello", "world", "how", "are", "you")),
                Map.entry("d2", Arrays.asList("hello", "you", "there")),
                Map.entry("d3", Arrays.asList("goodbye", "world", "see", "you", "later"))
        );

        Map<String, Map<String, Double>> tfidf = IndexUtils.calculateTFIDF(docs);
        assertEquals(3, tfidf.size());
        assertTrue(tfidf.containsKey("d1"));

        // 0/5 * ln(3/1)
        assertEquals(0.0, tfidf.get("d1").get("goodbye"), 0.001);

        // 1/5 * ln(3/2)
        assertEquals(0.2 * 0.405, tfidf.get("d1").get("hello"), 0.001);
    }
}