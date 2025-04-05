import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class IndexBuilder implements IIndexBuilder {
    @Override
    public Map<String, List<String>> parseFeed(List<String> feeds) {
        Map<String, List<String>> docs = new HashMap<>();
        for (String rssUrl : feeds) {
            Document rssDoc;
            try {
                // Fetch the RSS document by URL
                rssDoc = Jsoup.connect(rssUrl).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Extract all the <link> tags in the RSS feed
            Elements links = rssDoc.getElementsByTag("link");
            for (Element link : links) {
                String htmlUrl = link.text();
                Document htmlDoc;
                try {
                    htmlDoc = Jsoup.connect(htmlUrl).get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Elements bodies = htmlDoc.getElementsByTag("body");

                List<String> words = new ArrayList<>();
                for (Element body : bodies) {
                    String contents = body.text();
                    List<String> tokens = StringUtils.tokenize(contents);
                    words.addAll(tokens);
                }
                docs.put(htmlUrl, words);
            }
        }

        return docs;
    }

    @Override
    public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
        return IndexUtils.calculateTFIDF(docs);
    }

    @Override
    public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
        return Map.of();
    }

    @Override
    public Collection<Map.Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
        return List.of();
    }

    @Override
    public Collection<?> createAutocompleteFile(Collection<Map.Entry<String, List<String>>> homepage) {
        return List.of();
    }

    @Override
    public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
        return List.of();
    }
}
