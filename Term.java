public class Term implements ITerm {

    String term;
    long weight;

    /**
     * Initialize a Term with a given query String and weight
     */
    public Term(String term, long weight) {
        if (term == null || weight < 0) {
            throw new IllegalArgumentException("Invalid term argument");
        }

        this.term = term;
        this.weight = weight;
    }

    @Override
    public int compareTo(ITerm that) {
        if (this.term.equals(that.getTerm())) {
            return Long.compare(this.weight, that.getWeight());
        }

        return term.compareTo(that.getTerm());
    }

    @Override
    public String toString() {
        // The GUI expects the string form of the term to be formatted this way
        return weight + "\t" + term;
    }

    @Override
    public long getWeight() {
        return weight;
    }

    @Override
    public String getTerm() {
        return term;
    }

    @Override
    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public String setTerm(String term) {
        this.term = term;
        return term;
    }

}
