package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.Locale;

public class ReminderDateSearchTest {
 private Reminder reminder(String repeat){Reminder r=new Reminder();r.name="Bollo auto";r.anchor=LocalDateTime.of(2026,10,19,9,30);r.next=r.anchor;r.repeat=repeat;return r;}
 @Test public void noDateDoesNotRestrictResults(){assertTrue(ReminderSearch.matchesDay(reminder("none"),null));}
 @Test public void exactDayIncludesAlreadyDeliveredReminder(){Reminder r=reminder("none");r.delivered=true;assertTrue(ReminderSearch.matchesDay(r,LocalDate.of(2026,10,19)));assertFalse(ReminderSearch.matchesDay(r,LocalDate.of(2026,10,20)));}
 @Test public void repeatSearchDoesNotOnlyCheckNextNotification(){Reminder r=reminder("weekly");r.next=r.anchor.plusWeeks(3);assertTrue(ReminderSearch.matchesDay(r,LocalDate.of(2026,10,26)));assertFalse(ReminderSearch.matchesDay(r,LocalDate.of(2026,10,27)));assertFalse(ReminderSearch.matchesDay(r,LocalDate.of(2026,10,12)));}
 @Test public void chosenCalendarDatesAreSearchable(){Reminder r=reminder("calendar");r.dates.add("2036-02-29");r.dates.add("2036-04-13");assertTrue(ReminderSearch.matchesDay(r,LocalDate.of(2036,2,29)));assertFalse(ReminderSearch.matchesDay(r,LocalDate.of(2036,2,28)));}
 @Test public void textAndDateCanBothMatch(){Reminder r=reminder("yearly");assertTrue(ReminderSearch.matchesDay(r,LocalDate.of(2036,10,19))&&ReminderSearch.normalized(r.name,Locale.ITALIAN).contains(ReminderSearch.normalized("BOLLO",Locale.ITALIAN)));assertFalse(ReminderSearch.matchesDay(r,LocalDate.of(2036,10,20)));}
}
