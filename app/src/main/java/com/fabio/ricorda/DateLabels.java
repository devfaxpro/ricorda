package com.fabio.ricorda;

import android.text.format.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateLabels {
    private DateLabels() {}

    public static String format(LocalDate date, Locale locale, String skeleton) {
        String pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
        return MonthLabels.format(date,locale,pattern);
    }
}
