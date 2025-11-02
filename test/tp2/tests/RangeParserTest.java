package tp2.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tp2.logic.RangeParser;

import java.util.List;

public class RangeParserTest {

    @Test
    public void testValidFormat() {
        assertTrue(RangeParser.isBasicFormat("AA,KK,AKs,AQo"));
        assertTrue(RangeParser.isBasicFormat("JJ+"));
    }

    @Test
    public void testInvalidFormat() {
        assertFalse(RangeParser.isBasicFormat("AA;KK"));
        assertFalse(RangeParser.isBasicFormat("XYZ"));
        assertFalse(RangeParser.isBasicFormat(""));
    }

    @Test
    public void testParseSimpleList() {
        List<String> manos = RangeParser.parse("AA, KK, AKo");
        assertTrue(manos.contains("AA"));
        assertTrue(manos.contains("KK"));
        assertTrue(manos.contains("AKO")); // el parser convierte a mayúsculas
    }
}
