import java.util.Arrays;
import java.util.List;

public class StringUtils {
    public static String removePunctuation(String input) {
        return input.replaceAll("[;,'!?.:\"]", "");
    }

    public static List<String> tokenize(String input) {
        input = removePunctuation(input).toLowerCase();
        String[] tokens = input.split("\\s+");
        return Arrays.asList(tokens);
    }
}
