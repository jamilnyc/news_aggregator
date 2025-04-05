/**
 * @author Harry Smith
 */

public class Node {

    public static final int NUM_CHILDREN = 26;

    private Term term;

    // Indicates whether this Node represents a complete word
    // If all input words are unique, then this is essentially a boolean
    // 0 if it is not a complete word, 1 if it is
    private int words;

    // How many words have the prefix of the node
    private int prefixes;

    // References to this Node's children
    // For simple words, there will be up to 26 children
    // for each letter of the alphabet
    private Node[] references;

    /**
     * Initialize a Node with an empty string and 0 weight; useful for
     * writing tests.
     */
    public Node() {
        // This is used for nodes that do not correspond to a complete word
        this.term = new Term("", 0);
        this.references = new Node[NUM_CHILDREN];
    }

    /**
     * Initialize a Node with the given query string and weight.
     * @throws IllegalArgumentException if query is null or if weight is negative.
     */
    public Node (String query, long weight) {
        this.term = new Term(query, weight);
        this.references = new Node[NUM_CHILDREN];
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    public int getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(int prefixes) {
        this.prefixes = prefixes;
    }

    public Node[] getReferences() {
        return references;
    }

    public void setReferences(Node[] references) {
        this.references = references;
    }

    public void incrementPrefixes() {
        prefixes += 1;
    }

    public boolean isCompleteWord() {
        return words > 0;
    }

    public ITerm getCopyTerm() {
        return new Term(term.getTerm(), term.getWeight());
    }
}
