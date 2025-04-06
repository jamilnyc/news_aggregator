import java.nio.DoubleBuffer;
import java.util.*;

public class IndexUtils {
    public static Map<String, Double> getInverseDocumentFrequencies(Map<String, List<String>> docs) {
        int totalNumberOfDocuments = docs.size();

        // Number of documents with term `t` in it
        Map<String, Integer> termDocumentCounts = new HashMap<>();
        Set<String> uniqueWords = getUniqueWords(docs);

        for (String uniqueWord : uniqueWords) {
            // Initialize the number of documents that contain this word to 0
            termDocumentCounts.put(uniqueWord, 0);

            // Loop over every document and check if it contains the word
            for (Map.Entry<String, List<String>> entry : docs.entrySet()) {
                if (entry.getValue().contains(uniqueWord)) {
                    termDocumentCounts.put(uniqueWord, termDocumentCounts.get(uniqueWord) + 1);
                }
            }
        }

        Map<String, Double> inverseDocumentFrequencies = new HashMap<>();
        for (Map.Entry<String, Integer> entry : termDocumentCounts.entrySet()) {
            String uniqueWord = entry.getKey();
            double numberOfDocumentsWithTerm = (double) entry.getValue();
            double idf = Math.log(totalNumberOfDocuments / numberOfDocumentsWithTerm);
            inverseDocumentFrequencies.put(uniqueWord, idf);
        }

        return inverseDocumentFrequencies;
    }

    public static Map<String, Map<String, Double>> calculateTFIDF(Map<String, List<String>> docs) {
        Map<String, Double> inverseDocumentFrequencies = getInverseDocumentFrequencies(docs);
        Map<String, Map<String, Double>> tfidf = new HashMap<>();

        Set<String> uniqueWords = getUniqueWords(docs);

        for (Map.Entry<String, List<String>> entry : docs.entrySet()) {
            String documentName = entry.getKey();
            List<String> documentWords = entry.getValue();
            tfidf.put(documentName, new TreeMap<String, Double>());

            for (String uniqueWord : uniqueWords) {
                // Number of times `t` appears in document `d`
                int appearances = Collections.frequency(documentWords, uniqueWord);

                // Total number of words in the document `d`
                int totalWords = documentWords.size();

                double termFrequency = (double) appearances / (double) totalWords;
                double idf = inverseDocumentFrequencies.get(uniqueWord);
                tfidf.get(documentName).put(uniqueWord, termFrequency * idf);
            }
        }

        return tfidf;
    }

    public static Set<String> getUniqueWords(Map<String, List<String>> docs) {
        Set<String> uniqueWords = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : docs.entrySet()) {
            uniqueWords.addAll(entry.getValue());
        }
        return uniqueWords;
    }
}
