package com.fabio.ricorda;

import java.time.LocalDateTime;

/** Pure calendar arithmetic: all occurrences are based on the original date. */
public final class Recurrence {
    private Recurrence() {}
    public static LocalDateTime nextAfter(LocalDateTime anchor, String repeat, LocalDateTime now) {
        if (anchor.isAfter(now)) return anchor;
        if ("none".equals(repeat)) return null;
        long n;
        switch (repeat) {
            case "daily":
                n = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(anchor.toLocalDate(), now.toLocalDate()));
                while (!anchor.plusDays(n).isAfter(now)) n++;
                return anchor.plusDays(n);
            case "weekly":
                n = Math.max(1, java.time.temporal.ChronoUnit.WEEKS.between(anchor.toLocalDate(), now.toLocalDate()));
                while (!anchor.plusWeeks(n).isAfter(now)) n++;
                return anchor.plusWeeks(n);
            case "monthly":
                n = Math.max(1, java.time.temporal.ChronoUnit.MONTHS.between(java.time.YearMonth.from(anchor), java.time.YearMonth.from(now)));
                while (!anchor.plusMonths(n).isAfter(now)) n++;
                return anchor.plusMonths(n);
            case "yearly":
                n = Math.max(1, now.getYear() - anchor.getYear());
                while (!anchor.plusYears(n).isAfter(now)) n++;
                return anchor.plusYears(n);
            default: throw new IllegalArgumentException("Ripetizione sconosciuta");
        }
    }
    public static LocalDateTime nextSelected(java.util.Collection<String> dates, java.time.LocalTime time, LocalDateTime now) {
        return dates.stream().map(java.time.LocalDate::parse).map(d -> d.atTime(time)).filter(d -> d.isAfter(now)).min(LocalDateTime::compareTo).orElse(null);
    }
    public static boolean occurs(java.time.LocalDate day, LocalDateTime anchor, String repeat, java.util.Collection<String> dates) {
        if ("calendar".equals(repeat)) return dates.contains(day.toString());
        if (day.isBefore(anchor.toLocalDate())) return false;
        LocalDateTime next=nextAfter(anchor,repeat,day.atStartOfDay().minusNanos(1));
        return next!=null && next.toLocalDate().equals(day);
    }
}
