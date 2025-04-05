import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Autocomplete implements IAutocomplete {
    Node root;

    // Maximum number of suggestions
    // I'm not sure why we're required to store this as a member variable of this
    // class because it is not used. The AutoCompleteGUI actually sorts the list
    // and truncates it based on the value of k, so there's nothing to do with it here.
    // Probably an oversight on the part of whoever designed this assignment
    int k;

    public Autocomplete() {
        root = new Node();
    }

    @Override
    public void addWord(String word, long weight) {
        if (!isValidWord(word)) {
            return;
        }

        word = word.toLowerCase();
        Node currentNode = root;
        currentNode.incrementPrefixes();

        // Useful for debugging
        String partialWord = "";
        for (int i = 0; i < word.length(); i++) {
            // This gets the index into the Node's references array
            int currentCharacter = word.charAt(i) - 'a';
            partialWord += word.charAt(i);

            if (currentNode.getReferences()[currentCharacter] == null) {
                currentNode.getReferences()[currentCharacter] = new Node();
            }

            // If this is the last character of the word, that means we have a complete word
            if (i == word.length() - 1) {
                assert(currentNode.getReferences()[currentCharacter] != null);
                // Node already exists at this point, so mark it as a complete word
                Term t = new Term(word, weight);
                currentNode.getReferences()[currentCharacter].setTerm(t);
                currentNode.getReferences()[currentCharacter].setWords(1);
            }

            currentNode.getReferences()[currentCharacter].incrementPrefixes();
            currentNode = currentNode.getReferences()[currentCharacter];
        }
    }

    @Override
    public Node buildTrie(String filename, int k) {
        this.k = k;
        // Read from the file, one line at a time
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String line;
        try {
            while((line = br.readLine()) != null) {
                line = line.toLowerCase().trim();

                // Each line should be a numerical weight and the String value
                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    continue;
                }

                addWord(parts[1], Long.parseLong(parts[0]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }

    @Override
    public Node getSubTrie(String prefix) {
        prefix = prefix.toLowerCase();
        Node currentNode = root;

        for(int i = 0; i < prefix.length(); i++) {
            int currentCharacter = prefix.charAt(i) - 'a';
            currentNode = currentNode.getReferences()[currentCharacter];
            if (currentNode == null) {
                return null;
            }
        }
        return currentNode;
    }

    @Override
    public int countPrefixes(String prefix) {
        Node subTrie = getSubTrie(prefix);
        if (subTrie == null) {
            return 0;
        }
        return subTrie.getPrefixes();
    }

    @Override
    public List<ITerm> getSuggestions(String prefix) {
        List<ITerm> suggestions = new ArrayList<>();

        Node currentNode = getSubTrie(prefix);
        if (currentNode == null) {
            return suggestions;
        }

        getSuggestionsHelper(currentNode, suggestions);
        return suggestions;
    }

    /**
     * Recursively populate the list with complete words as we do a depth-first-search
     * of the Trie starting at the given node.
     *
     * @param currentNode The root of the subtrie
     * @param suggestions The running list of terms to suggest
     */
    private void getSuggestionsHelper(Node currentNode, List<ITerm> suggestions) {
        if (currentNode == null) {
            return;
        }

        if (currentNode.isCompleteWord()) {
            suggestions.add(currentNode.getCopyTerm());
        }

        for (Node child : currentNode.getReferences()) {
            if (child == null) {
                continue;
            }

            getSuggestionsHelper(child, suggestions);
        }
    }

    public static boolean isValidWord(String word) {
        // String is one or more alphabetic characters
        return word.matches("[a-zA-Z]+");
    }

}
