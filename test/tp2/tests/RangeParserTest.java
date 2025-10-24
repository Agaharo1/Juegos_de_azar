package tp2.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tp2.logic.RangeParser;

import java.util.List;

public class RangeParserTest {

    @Test
    public void testValidFormat() {
        assertTrue(RangeParser.isBasicFormat("AA,KK,AKs,AQo"));
        assertTrue(RangeParser.isBasicFormat("AA"));
    }

    @Test
    public void testInvalidFormat() {
        assertFalse(RangeParser.isBasicFormat("AA;KK"));
        assertFalse(RangeParser.isBasicFormat("1234"));
        assertFalse(RangeParser.isBasicFormat(""));
    }

    @Test
    public void testParseSimpleList() {
        List<String> manos = RangeParser.parse("AA, KK, AKo");
        assertEquals(3, manos.size());
        assertEquals("AA", manos.get(0));
        assertEquals("KK", manos.get(1));
        assertEquals("AKO", manos.get(2)); // parser convierte a mayúsculas
    }
}
