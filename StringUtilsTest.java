import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class StringUtilsTest {

    @Test
    void testRemovePunctuation() {
        String input = "Hello, world!; 'Good::bye.'";
        String result = StringUtils.removePunctuation(input);
        assertEquals("Hello world Goodbye", result);
    }

    @Test
    void tokenize() {
        String input = "Hello, world! 'Good::bye.'";
        List<String> tokens = StringUtils.tokenize(input);
        assertEquals(3, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
        assertEquals("goodbye", tokens.get(2));
    }
}