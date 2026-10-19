package com.fabio.ricorda;
import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;
public class ReminderDeliveryTest {
 private Reminder reminder(String repeat){Reminder r=new Reminder();r.repeat=repeat;r.anchor=LocalDateTime.parse("2026-10-09T09:00");r.next=r.anchor;return r;}
 @Test public void dailySnoozePreservesNextDayAndOriginalTime(){Reminder r=reminder("daily");r.recordDelivery(r.next,false);assertEquals(LocalDateTime.parse("2026-10-10T09:00"),r.next);r.recordDelivery(LocalDateTime.parse("2026-10-09T09:10"),true);assertEquals(LocalDateTime.parse("2026-10-10T09:00"),r.next);assertEquals(LocalDateTime.parse("2026-10-09T09:00"),r.anchor);assertFalse(r.delivered);}
 @Test public void selectedDatesSurviveSnooze(){Reminder r=reminder("calendar");r.dates.add("2026-10-09");r.dates.add("2026-10-11");r.recordDelivery(r.next,false);r.recordDelivery(LocalDateTime.parse("2026-10-09T09:10"),true);assertEquals(LocalDateTime.parse("2026-10-11T09:00"),r.next);assertEquals(2,r.dates.size());assertFalse(r.delivered);}
 @Test public void snoozeDoesNotRearmCompletedOneOff(){Reminder r=reminder("none");r.recordDelivery(r.next,false);assertTrue(r.delivered);r.recordDelivery(r.next.plusMinutes(10),true);assertTrue(r.delivered);assertEquals(r.anchor,r.next);}
 @Test public void lastSelectedDateCanBeSnoozedWithoutNewCalendarDates(){Reminder r=reminder("calendar");r.dates.add("2026-10-09");r.recordDelivery(r.next,false);r.recordDelivery(r.next.plusMinutes(10),true);assertTrue(r.delivered);assertEquals(1,r.dates.size());assertEquals("2026-10-09",r.dates.first());}
}
