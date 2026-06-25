package org.home.currencies.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringCleanerTest {

    @Test
    void cleanString_leadingTrailingSpaces_returnsStripped() {
        String input = " EUR   ";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EUR", cleaned);
    }


    @Test
    void cleanString_blank_returnsNull() {
        String input = "   " ;
        String cleaned = StringCleaner.cleanString(input);
        assertNull(cleaned);
    }

    @Test
    void cleanString_htmlEntity_returnsUnescaped() {
        String input = "Eur&amp;Dollar";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("Eur&Dollar", cleaned);
    }

    @Test
    void cleanString_ligature_returnsNormalized() {
        String input = "ﬁnance";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("finance", cleaned);
    }

    @Test
    void cleanString_controlChar_returnsStripped() {
        String input = "EU\u0003R";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EUR", cleaned);
    }

    @Test
    void cleanString_carriageReturn_returnsStripped() {
        String input = "EU\rR";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EUR", cleaned);
    }

    @Test
    void cleanString_tab_returnsSpace() {
        String input = "EU\tR";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EU R", cleaned);
    }

    @Test
    void cleanString_zeroWidthChar_returnsStripped() {
        String input = "EU\u200bR";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EUR", cleaned);
    }

    @Test
    void cleanString_multipleSpaces_returnsSingleSpace() {
        String input = "EU   R";
        String cleaned = StringCleaner.cleanString(input);
        assertEquals("EU R", cleaned);
    }

    @Test
    void cleanString_onlyControlChars_returnsNull() {
        String input = "\u0003\u0004";
        String cleaned = StringCleaner.cleanString(input);
        assertNull(cleaned);
    }


}
