package com.fabio.ricorda;

import java.time.*;
import java.util.*;

/** The calendar markers and the selected-day list use the same occurrence rules. */
public final class CalendarAgenda {
    private CalendarAgenda(){}
    public static List<Reminder> forDay(List<Reminder> reminders,LocalDate day){
        List<Reminder> result=new ArrayList<>();
        for(Reminder reminder:reminders)if(reminder.occurs(day))result.add(reminder);
        result.sort(Comparator.comparing((Reminder r)->r.anchor.toLocalTime()).thenComparing(r->r.name,String.CASE_INSENSITIVE_ORDER));
        return result;
    }
    public static boolean hasReminders(List<Reminder> reminders,LocalDate day){
        for(Reminder reminder:reminders)if(reminder.occurs(day))return true;
        return false;
    }
    public static LocalDateTime draftStart(LocalDate day,LocalDateTime now){
        LocalDateTime suggested=now.plusHours(1).withSecond(0).withNano(0);
        if(day==null)return suggested;
        LocalTime time=suggested.toLocalTime();
        // A late-night suggestion must not silently move a calendar draft to tomorrow.
        if(day.equals(now.toLocalDate())&&!suggested.toLocalDate().equals(day))time=LocalTime.of(23,59);
        return day.atTime(time);
    }
}
