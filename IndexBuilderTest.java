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

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            assertTrue(entry.getKey().contains("https://www.seas.upenn.edu/~cit5940/page"));
            assertFalse(entry.getValue().isEmpty(), "Each web page should have some text");
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

}
