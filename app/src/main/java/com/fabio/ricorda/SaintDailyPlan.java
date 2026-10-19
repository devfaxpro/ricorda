package com.fabio.ricorda;

import java.time.*;

/** Calendar-day scheduling: no 24-hour intervals across daylight-saving changes. */
public final class SaintDailyPlan {
    public static final LocalDate FIRST=SaintCalendar.FIRST, LAST=SaintCalendar.LAST;
    public static boolean available(LocalDate day){return !day.isBefore(FIRST)&&!day.isAfter(LAST);}
    public static boolean active(boolean enabled,String language){return enabled&&SaintCalendar.visible(language);}
    public static ZonedDateTime next(Instant now,ZoneId zone,LocalTime time,boolean enabled,String language,String delivered,long pending) {
        if(!active(enabled,language))return null;
        LocalDate today=now.atZone(zone).toLocalDate();
        LocalDate day=today.isBefore(FIRST)?FIRST:today;
        if(day.isAfter(LAST))return null;
        ZonedDateTime target=day.atTime(time).atZone(zone);
        // Preserve an alarm due today while Android was asleep or the phone rebooted.
        boolean waiting=pending==target.toInstant().toEpochMilli()&&day.equals(today);
        if(day.toString().equals(delivered)||(!target.toInstant().isAfter(now)&&!waiting))target=day.plusDays(1).atTime(time).atZone(zone);
        return available(target.toLocalDate())?target:null;
    }
    public static boolean due(Instant now,ZoneId zone,LocalTime time,boolean enabled,String language,String delivered,long pending) {
        LocalDate today=now.atZone(zone).toLocalDate();
        return active(enabled,language)&&available(today)&&!today.toString().equals(delivered)
            && pending==today.atTime(time).atZone(zone).toInstant().toEpochMilli()&&!now.isBefore(Instant.ofEpochMilli(pending));
    }
    private SaintDailyPlan(){}
}
