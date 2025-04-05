import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

}
