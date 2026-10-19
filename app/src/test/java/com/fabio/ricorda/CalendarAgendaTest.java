package com.fabio.ricorda;
import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class CalendarAgendaTest {
 private Reminder reminder(String name,String date,String repeat){Reminder r=new Reminder();r.name=name;r.anchor=LocalDateTime.parse(date);r.next=r.anchor;r.repeat=repeat;return r;}
 @Test public void emptyCalendarHasNoMarkersOrItems(){assertFalse(CalendarAgenda.hasReminders(Collections.emptyList(),LocalDate.of(2026,10,19)));assertTrue(CalendarAgenda.forDay(Collections.emptyList(),LocalDate.of(2026,10,19)).isEmpty());}
 @Test public void markersAndDailyItemsAgreeIncludingRecurringReminders(){Reminder a=reminder("Daily","2026-10-19T09:00","daily"),b=reminder("Birthday","2026-10-20T12:00","yearly"),c=reminder("Selected","2026-10-19T18:00","calendar");c.dates.add("2026-10-21");List<Reminder> all=Arrays.asList(a,b,c);for(int n=18;n<25;n++){LocalDate day=LocalDate.of(2026,10,n);assertEquals(!CalendarAgenda.forDay(all,day).isEmpty(),CalendarAgenda.hasReminders(all,day));}assertEquals(Arrays.asList(a,c),CalendarAgenda.forDay(all,LocalDate.of(2026,10,21)));assertEquals(Arrays.asList(a,b),CalendarAgenda.forDay(all,LocalDate.of(2027,10,20)));}
 @Test public void deletedRemindersCannotCreateCalendarMarkers(){Reminder r=reminder("Deleted","2026-10-19T12:00","daily");r.deleted=true;assertFalse(CalendarAgenda.hasReminders(Arrays.asList(r),LocalDate.of(2026,10,20)));assertTrue(CalendarAgenda.forDay(Arrays.asList(r),LocalDate.of(2026,10,20)).isEmpty());}
 @Test public void selectedDayItemsAreSortedByTimeWithoutChangingSourceOrder(){Reminder late=reminder("Later","2026-10-19T18:00","none"),early=reminder("First","2026-10-19T08:00","none");List<Reminder> all=Arrays.asList(late,early);assertEquals(Arrays.asList(early,late),CalendarAgenda.forDay(all,LocalDate.of(2026,10,19)));assertEquals(late,all.get(0));assertTrue(CalendarAgenda.forDay(all,LocalDate.of(2026,10,20)).isEmpty());}
 @Test public void calendarDraftUsesTheSelectedDateIncludingNearMidnight(){LocalDate chosen=LocalDate.of(2026,12,29);assertEquals(LocalDateTime.of(2026,12,29,9,30),CalendarAgenda.draftStart(chosen,LocalDateTime.of(2026,10,19,8,30)));LocalDateTime late=LocalDateTime.of(2026,10,19,23,30);assertEquals(LocalDateTime.of(2026,10,19,23,59),CalendarAgenda.draftStart(late.toLocalDate(),late));}
 @Test public void ordinaryDraftStillUsesTheNextHourAndKeepsFutureDayRollover(){assertEquals(LocalDateTime.of(2026,10,20,0,30),CalendarAgenda.draftStart(null,LocalDateTime.of(2026,10,19,23,30,25)));}
}
