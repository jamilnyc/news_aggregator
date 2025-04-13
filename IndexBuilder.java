import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class IndexBuilder implements IIndexBuilder {

    @Override
    public Map<String, List<String>> parseFeed(List<String> feeds) {
        Map<String, List<String>> docs = new HashMap<>();
        for (String rssUrl : feeds) {
            Document rssDoc;
            try {
                // Fetch the RSS document by URL
                System.out.println("Fetching RSS Feed: " + rssUrl);
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
                    System.out.println("Fetching HTML Document: " + htmlUrl);
                    htmlDoc = Jsoup.connect(htmlUrl).get();
                } catch (IOException e) {
                    if (e instanceof java.net.SocketException
                    || e instanceof java.net.SocketTimeoutException) {
                        // Skip articles that time out.
                        System.err.println("Skipping document due to a socket error");
                        continue;
                    }

                    if (e instanceof HttpStatusException) {
                        List<Integer> skippableCodes = new ArrayList<>();
                        skippableCodes.add(403);
                        skippableCodes.add(404);
                        int statusCode = ((HttpStatusException) e).getStatusCode();
                        if (skippableCodes.contains(statusCode)) {
                            System.err.println("Skipping document due HTTP status code: " + statusCode);
                            continue;
                        }

                    }
                    throw new RuntimeException(e);
                }

                Elements bodies = htmlDoc.getElementsByTag("body");

                List<String> words = new ArrayList<>();
                for (Element body : bodies) {
                    String contents = body.text();
                    List<String> tokens = StringUtils.tokenize(contents);
                    words.addAll(tokens);
                }
                System.out.println("Found " + words.size() + " words");
                docs.put(htmlUrl, words);
            }
            System.out.println("✅ Finished parsing RSS Feed: " + rssUrl);
        }

        return docs;
    }

    @Override
    public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
        System.out.println("Starting to build an index with " + docs.size() + " documents");
        return IndexUtils.calculateTFIDF(docs);
    }

    @Override
    public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
        System.out.println("Starting to build an Inverted Index");
        // Word -> List<Entry<DocumentName, TFIDF>>
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> invertedIndex = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry : index.entrySet()) {
            String documentName = entry.getKey();
            Map<String, Double> documentTFIDF = entry.getValue();
            System.out.println("  Words: " + documentTFIDF.size() + " | " + documentName);

            for (Map.Entry<String, Double> tfidfEntry : documentTFIDF.entrySet()) {
                String word = tfidfEntry.getKey();
                double tfidf = tfidfEntry.getValue();

                if (!invertedIndex.containsKey(word)) {
                    invertedIndex.put(word, new ArrayList<>());
                }

                if (tfidf > 0) {
                    invertedIndex.get(word).add(new AbstractMap.SimpleEntry<>(documentName, tfidf));
                }
            }
        }

        // Sort the lists so that the documents with the highest TFIDF are at the top
        for (Map.Entry<String, List<AbstractMap.SimpleEntry<String, Double>>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            List<AbstractMap.SimpleEntry<String, Double>> tfidfValues = entry.getValue();

            System.out.println("  Sorting results for term " + word);
            Collections.sort(tfidfValues, new Comparator<AbstractMap.SimpleEntry>() {
                public int compare(AbstractMap.SimpleEntry o1, AbstractMap.SimpleEntry o2) {
                    double v1 = ((Number) o1.getValue()).doubleValue();
                    double v2 = ((Number) o2.getValue()).doubleValue();

                    return Double.compare(v2, v1);
                }
            });
        }

        return invertedIndex;
    }

    @Override
    public Collection<Map.Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
        System.out.println("Starting to build home page");
        // Remove terms that are stop words
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> trueInvertedIndex = (Map<String, List<AbstractMap.SimpleEntry<String, Double>>>) invertedIndex;
        Arrays.asList(STOPW).forEach(trueInvertedIndex.keySet()::remove);

        // For the remaining terms, remove documents that have a 0 TFIDF score
        for (Map.Entry<String, List<AbstractMap.SimpleEntry<String, Double>>> entry : trueInvertedIndex.entrySet()) {
            String word = entry.getKey();
            List<AbstractMap.SimpleEntry<String, Double>> tfidf = entry.getValue();
            tfidf.removeIf(t -> t.getValue() <= 0.0);
        }

        // Terms to list of associated articles
        List<Map.Entry<String, List<String>>> cleaned = new ArrayList<>();
        for (Map.Entry<String, List<AbstractMap.SimpleEntry<String, Double>>> entry : trueInvertedIndex.entrySet()) {
            String word = entry.getKey();
            List<AbstractMap.SimpleEntry<String, Double>> tfidf = entry.getValue();
            List<String> documents = new ArrayList<>();
            for (AbstractMap.SimpleEntry<String, Double> tfidfEntry : tfidf) {
                documents.add(tfidfEntry.getKey());
            }
            cleaned.add(new AbstractMap.SimpleEntry<>(word, documents));
        }

        // Tag terms are sorted by the number of articles (descending), then by reverse lexicographic order
        Collections.sort(cleaned, new Comparator<>() {
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                if (o1.getValue().size() == o2.getValue().size()) {
                    return o2.getKey().compareTo(o1.getKey());
                }
                return o2.getValue().size() - o1.getValue().size();
            }
        });

        return cleaned;
    }

    @Override
    public Collection<?> createAutocompleteFile(Collection<Map.Entry<String, List<String>>> homepage) {
        System.out.println("Starting creating the autocomplete file");
        File autocompleteFile;
        List<String> words = new ArrayList<>();
        try {
            autocompleteFile = new File("autocomplete.txt");
            Files.deleteIfExists(autocompleteFile.toPath());
            if (!autocompleteFile.createNewFile()) {
                System.err.println("Could not create autocomplete file.");
                return null;
            }

            for (Map.Entry<String, List<String>> entry : homepage) {
                String word = entry.getKey();
                words.add(word);
            }
            Collections.sort(words);

            FileWriter fw = new FileWriter(autocompleteFile);
            fw.write(homepage.size() + "\n");
            for (String word : words) {
                fw.write("    " + 0 + " " + word + "\n");
            }
            fw.close();
            System.out.println("📝 Finished writing to autocomplete.txt");
        } catch (IOException e) {
            System.err.println("Could not create autocomplete file.");
            throw new RuntimeException(e);
        }

        return words;
    }

    @Override
    public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> trueInvertedIndex = (Map<String, List<AbstractMap.SimpleEntry<String, Double>>>) invertedIndex;
        List<String> matchingArticles = new ArrayList<>();
        List<AbstractMap.SimpleEntry<String, Double>> termsToArticles = trueInvertedIndex.get(queryTerm);
        if (termsToArticles == null) {
            System.err.println("Term " + queryTerm + " not found. Did you build the whole index?");
            return matchingArticles;
        }
        for(AbstractMap.SimpleEntry<String, Double> entry : termsToArticles) {
            // Only match articles that are actually relevant
            if (entry.getValue() > 0) {
                matchingArticles.add(entry.getKey());
            }
        }

        System.out.println("🔎 Search for '" + queryTerm + "': " + matchingArticles.size() + " results");
        return matchingArticles;
    }
}
