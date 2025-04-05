import java.util.Comparator;

public class ByReverseWeightOrder implements Comparator<ITerm> {

    @Override
    public int compare(ITerm o1, ITerm o2) {
        // Note that we are comparing o2 to o1 because
        // we want reverse order.
        return Long.compare(o2.getWeight(), o1.getWeight());
    }
}
