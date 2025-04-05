import java.util.Comparator;

public class ByPrefixOrder implements Comparator<ITerm> {
    // Length of the prefix
    private final int r;

    public ByPrefixOrder(int r) {
        this.r = r;
    }

    @Override
    public int compare(ITerm o1, ITerm o2) {
        // If the specified prefix r is longer than the actual term,
        // we need to adjust the length accordingly
        int prefixLength1 = Math.min(o1.getTerm().length(), r);
        int prefixLength2 = Math.min(o2.getTerm().length(), r);

        // Only consider characters up to the length of the prefix
        String firstString = o1.getTerm().substring(0, prefixLength1);
        String secondString = o2.getTerm().substring(0, prefixLength2);

        return firstString.compareTo(secondString);
    }
}
