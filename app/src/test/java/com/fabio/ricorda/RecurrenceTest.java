package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDateTime;

public class RecurrenceTest {
    private LocalDateTime at(String s){return LocalDateTime.parse(s);}
    @Test public void monthlyKeepsOriginalDay(){
        LocalDateTime anchor=at("2026-01-31T09:00");
        assertEquals(at("2026-02-28T09:00"),Recurrence.nextAfter(anchor,"monthly",anchor));
        assertEquals(at("2026-03-31T09:00"),Recurrence.nextAfter(anchor,"monthly",at("2026-02-28T09:00")));
    }
    @Test public void leapBirthdayReturnsToFebruary29(){
        LocalDateTime anchor=at("2024-02-29T10:00");
        assertEquals(at("2025-02-28T10:00"),Recurrence.nextAfter(anchor,"yearly",anchor));
        assertEquals(at("2028-02-29T10:00"),Recurrence.nextAfter(anchor,"yearly",at("2027-03-01T12:00")));
    }
    @Test public void missedOccurrencesAdvanceToFuture(){
        assertEquals(at("2026-10-06T09:00"),Recurrence.nextAfter(at("2020-01-01T09:00"),"daily",at("2026-10-05T15:00")));
    }
    @Test public void nonRepeatingIsFinished(){assertNull(Recurrence.nextAfter(at("2026-10-05T09:00"),"none",at("2026-10-05T09:00")));}
    @Test public void futureDateIsPreserved(){assertEquals(at("2026-12-05T09:00"),Recurrence.nextAfter(at("2026-12-05T09:00"),"yearly",at("2026-10-05T09:00")));}
    @Test public void weeklyAdvancesAcrossYear(){assertEquals(at("2027-01-06T09:00"),Recurrence.nextAfter(at("2026-12-30T09:00"),"weekly",at("2026-12-30T09:00")));}
    @Test public void weeklySkipsMissedWeeks(){assertEquals(at("2026-10-30T09:00"),Recurrence.nextAfter(at("2026-10-09T09:00"),"weekly",at("2026-10-24T12:00")));}
    @Test public void selectedDatesAreSortedAndPastDatesSkipped(){assertEquals(at("2026-12-01T09:00"),Recurrence.nextSelected(java.util.Arrays.asList("2027-01-01","2026-12-01","2026-01-01"),java.time.LocalTime.of(9,0),at("2026-10-09T12:00")));}
    @Test public void lastSelectedDateFinishes(){assertNull(Recurrence.nextSelected(java.util.Arrays.asList("2026-10-09"),java.time.LocalTime.of(9,0),at("2026-10-09T09:00")));}
    @Test public void calendarMonthEndUsesAnchor(){assertTrue(Recurrence.occurs(java.time.LocalDate.of(2026,2,28),at("2026-01-31T09:00"),"monthly",java.util.Collections.emptyList()));assertTrue(Recurrence.occurs(java.time.LocalDate.of(2026,3,31),at("2026-01-31T09:00"),"monthly",java.util.Collections.emptyList()));assertFalse(Recurrence.occurs(java.time.LocalDate.of(2026,3,28),at("2026-01-31T09:00"),"monthly",java.util.Collections.emptyList()));}
    @Test public void calendarLeapDaySelected(){assertTrue(Recurrence.occurs(java.time.LocalDate.of(2028,2,29),at("2026-10-09T09:00"),"calendar",java.util.Arrays.asList("2028-02-29")));}
    @Test public void noOccurrenceBeforeAnchor(){assertFalse(Recurrence.occurs(java.time.LocalDate.of(2026,10,8),at("2026-10-09T09:00"),"daily",java.util.Collections.emptyList()));}
}
