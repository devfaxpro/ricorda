package com.fabio.ricorda;

import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.*;

public class ReminderSearchTest {
    @Test public void turkishCaseIsFoldedBeforeRemovingLatinAccents() {
        Locale tr = Locale.forLanguageTag("tr");
        assertEquals(ReminderSearch.normalized("istanbul", tr), ReminderSearch.normalized("İSTANBUL", tr));
        assertEquals(ReminderSearch.normalized("ışık", tr), ReminderSearch.normalized("IŞIK", tr));
    }
    @Test public void latinSearchIgnoresCaseAndAccents() {
        assertEquals("caffe paolo", ReminderSearch.normalized("CAFFÈ Paolo", Locale.ITALIAN));
        assertEquals("reunion", ReminderSearch.normalized("RÉUNION", Locale.FRENCH));
    }
    @Test public void indicVowelsAndViramaAreNotErased() {
        assertNotEquals(ReminderSearch.normalized("कल", Locale.forLanguageTag("hi")), ReminderSearch.normalized("कला", Locale.forLanguageTag("hi")));
        assertEquals("தமிழ்", ReminderSearch.normalized("தமிழ்", Locale.forLanguageTag("ta")));
    }
    @Test public void japaneseVoicingAndCanonicalEquivalenceArePreserved() {
        assertNotEquals(ReminderSearch.normalized("か", Locale.JAPANESE), ReminderSearch.normalized("が", Locale.JAPANESE));
        assertEquals(ReminderSearch.normalized("が", Locale.JAPANESE), ReminderSearch.normalized("か\u3099", Locale.JAPANESE));
    }
}
