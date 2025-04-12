import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IndexBuilderTest {
    public static final String UPENN_RSS_URL = "https://www.cis.upenn.edu/~cit5940/sample_rss_feed.xml";

    private static String getUpennPageUrl(int pageNum) {
        return "https://www.seas.upenn.edu/~cit5940/page" + pageNum + ".html";
    }

    @Test
    public void testParseFeedWithValidRssUrl() {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> result = ib.parseFeed(feeds);
        assertEquals(5, result.size(), "There should have been 5 links parsed");

        int[] pageNums = {1, 2, 3, 4, 5};
        for (int pageNum : pageNums) {
            assertTrue(result.containsKey(getUpennPageUrl(pageNum)), "Page " + pageNum + " should have been parsed");
            assertFalse(result.get(getUpennPageUrl(pageNum)).isEmpty(), "Page " + pageNum + " should have words");
        }

        assertEquals(10, result.get(getUpennPageUrl(1)).size());

        List<String> expectedWords = new ArrayList<>(Arrays.asList(
            "data",
            "structures",
            "linear",
            "data",
            "structures",
            "lists",
            "arraylist",
            "linkedlist",
            "stacks",
            "queues"
        ));
        assertEquals(expectedWords, result.get(getUpennPageUrl(1)));
    }

    @Test
    public void testParseFeedWithEmptyFeedList() {
        List<String> feeds = new ArrayList<>();
        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> result = ib.parseFeed(feeds);
        assertEquals(0, result.size());
    }

    @Test
    public void testParseFeedWithInvalidRssUrl() {
        List<String> feeds = new ArrayList<>();
        feeds.add("https://does.not.exist/feed.xml");
        IndexBuilder ib = new IndexBuilder();

        try {
            Map<String, List<String>> result = ib.parseFeed(feeds);
            fail("Should have thrown an exception when an invalid rss url is present");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("UnknownHostException"));
        }
    }

    @Test
    public void testBuildIndex() {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> docs = ib.parseFeed(feeds);
        Map<String, Map<String, Double>> forwardIndex = ib.buildIndex(docs);

        // Testing known values for this set of documents, as defined in the spec
        Map<String, Double> page1 = forwardIndex.get(getUpennPageUrl(1));
        assertTrue(page1.containsKey("data"));
        assertEquals(0.1021, page1.get("data"), 0.001);
        assertTrue(page1.containsKey("structures"));
        assertEquals(0.183, page1.get("structures"), 0.001);

        Map<String, Double> page2 = forwardIndex.get(getUpennPageUrl(2));
        assertTrue(page2.containsKey("data"));
        assertEquals(0.046, page2.get("data"), 0.001);
        assertTrue(page2.containsKey("structures"));
        assertEquals(0.0666, page2.get("structures"), 0.001);

        Map<String, Double> page3 = forwardIndex.get(getUpennPageUrl(3));
        assertTrue(page3.containsKey("trees"));
        assertEquals(0.0832, page3.get("trees"), 0.001);
        assertTrue(page3.containsKey("treeset"));
        assertEquals(0.0487, page3.get("treeset"), 0.001);

        Map<String, Double> page4 = forwardIndex.get(getUpennPageUrl(4));
        assertTrue(page4.containsKey("mallarme"));
        assertEquals(0.0731, page4.get("mallarme"), 0.001);
        assertTrue(page4.containsKey("others"));
        assertEquals(0.0731, page4.get("others"), 0.001);

        Map<String, Double> page5 = forwardIndex.get(getUpennPageUrl(5));
        assertTrue(page5.containsKey("files"));
        assertEquals(0.0509, page5.get("files"), 0.001);
        assertTrue(page5.containsKey("completely"));
        assertEquals(0.0894, page5.get("completely"), 0.001);
    }

    @Test
    public void testBuildInvertedIndex() {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> docs = ib.parseFeed(feeds);
        Map<String, Map<String, Double>> forwardIndex = ib.buildIndex(docs);

        Map<?,?> map = ib.buildInvertedIndex(forwardIndex);
        // Check the map keys and values are the right type before casting
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            assertInstanceOf(String.class, entry.getKey(), "The map key should be a string");
            assertInstanceOf(List.class, entry.getValue(), "The map value should be a list");
            assertInstanceOf(AbstractMap.SimpleEntry.class, ((List<?>) entry.getValue()).getFirst(), "The list elements should be SimpleEntry");
        }

        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> invertedIndex
                = (Map<String, List<AbstractMap.SimpleEntry<String, Double>>>) map;

        // Each term should map to a list of 5 document-TFIDF tuples
        assertEquals(5, invertedIndex.get("structures").size(), "Each term should have a list of 5 documents with TFIDF scores");

        // The list should be sorted by the TFIDF of the tuple
        assertEquals(getUpennPageUrl(1), invertedIndex.get("structures").get(0).getKey(), "Entries in the list should be sorted in descending order of TFIDF");
        assertEquals(0.183, invertedIndex.get("structures").get(0).getValue(), 0.001);
    }

    @Test
    public void testBuildHomePage() {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> docs = ib.parseFeed(feeds);
        Map<String, Map<String, Double>> forwardIndex = ib.buildIndex(docs);
        Map<?,?> map = ib.buildInvertedIndex(forwardIndex);
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> invertedIndex
                = (Map<String, List<AbstractMap.SimpleEntry<String, Double>>>) map;

        Collection<Map.Entry<String, List<String>>> h = ib.buildHomePage(invertedIndex);
        Map.Entry<String, List<String>> first = h.iterator().next();

        // Should be sorted by the term that has the most non-zero documents
        assertEquals("data", first.getKey(), "The term 'data' should have the most relevant articles of all terms");
        assertEquals(3, first.getValue().size());

        // The documents should be sorted by TFIDF scores in descending order
        assertEquals(getUpennPageUrl(1), first.getValue().getFirst());
    }

    @Test
    public void testSearchArticles() {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> docs = ib.parseFeed(feeds);
        Map<String, Map<String, Double>> forwardIndex = ib.buildIndex(docs);
        Map<?,?> map = ib.buildInvertedIndex(forwardIndex);

        List<String> matches = ib.searchArticles("data", map);
        assertEquals(3, matches.size(), "There should be 3 articles with non-zero TFIDF scores");

        // Check they are in order from most relevant to least
        assertEquals(getUpennPageUrl(1), matches.get(0), "Page 1 should be the most relevant to the term 'data'");
        assertEquals(getUpennPageUrl(2), matches.get(1));
        assertEquals(getUpennPageUrl(3), matches.get(2));
    }

    @Test
    public void testCreateAutocompleteFile()
    {
        List<String> feeds = new ArrayList<>();
        feeds.add(UPENN_RSS_URL);

        IndexBuilder ib = new IndexBuilder();
        Map<String, List<String>> docs = ib.parseFeed(feeds);
        Map<String, Map<String, Double>> forwardIndex = ib.buildIndex(docs);
        Map<?,?> map = ib.buildInvertedIndex(forwardIndex);
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> invertedIndex
                = (Map<String, List<AbstractMap.SimpleEntry<String, Double>>>) map;

        Collection<Map.Entry<String, List<String>>> h = ib.buildHomePage(invertedIndex);

        ib.createAutocompleteFile(h);
        assertTrue(Files.exists(Path.of("autocomplete.txt")));

        BufferedReader br;
        boolean isFirstLine = true;
        try {
            br = new BufferedReader(new FileReader("autocomplete.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (isFirstLine) {
                    isFirstLine = false;
                    assertEquals(h.size(), Integer.parseInt(line));
                    continue;
                }
                String[] parts = line.split("\\s+");
                assertEquals(2, parts.length);
                assertEquals(0, Integer.parseInt(parts[0]));
                assertFalse(parts[1].isEmpty());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
