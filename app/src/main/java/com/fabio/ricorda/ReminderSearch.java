package com.fabio.ricorda;

import java.text.Normalizer;
import java.util.Locale;

public final class ReminderSearch {
    private ReminderSearch() {}

    public static boolean matchesDay(Reminder reminder,java.time.LocalDate day){
        return day==null||Recurrence.occurs(day,reminder.anchor,reminder.repeat,reminder.dates);
    }

    public static String normalized(String text, Locale locale) {
        // Lowercase first: Turkish dotted I must stay distinct from dotless I.
        String decomposed = Normalizer.normalize(text.toLowerCase(locale), Normalizer.Form.NFD);
        StringBuilder result = new StringBuilder();
        boolean latinBase = false;
        for (int i = 0; i < decomposed.length();) {
            int cp = decomposed.codePointAt(i);
            i += Character.charCount(cp);
            int type = Character.getType(cp);
            boolean mark = type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK;
            // Accent-tolerant Latin search must not erase Indic vowels or Japanese voicing.
            if (mark && latinBase) continue;
            if (!mark) latinBase = Character.UnicodeScript.of(cp) == Character.UnicodeScript.LATIN;
            result.appendCodePoint(cp);
        }
        return Normalizer.normalize(result, Normalizer.Form.NFC);
    }
}
