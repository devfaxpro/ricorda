package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.*;
public class WidgetAgendaTest {
 private final LocalDate today=LocalDate.of(2026,10,10);
 private Reminder r(String name,String anchor,String repeat){Reminder r=new Reminder();r.name=name;r.anchor=LocalDateTime.parse(anchor);r.next=r.anchor;r.repeat=repeat;return r;}
 @Test public void excludesOtherDaysAndTrash(){Reminder deleted=r("Deleted","2026-10-10T08:00","none");deleted.deleted=true;Reminder kept=r("Today","2026-10-10T09:00","none");assertEquals(List.of(kept),WidgetAgenda.today(List.of(deleted,kept,r("Tomorrow","2026-10-11T08:00","none")),today));}
 @Test public void chronologicalOrder(){Reminder first=r("First","2026-10-10T08:00","none"),last=r("Last","2026-10-10T19:30","none");assertEquals(List.of(first,last),WidgetAgenda.today(List.of(last,first),today));}
 @Test public void recurringDeliveryKeepsTodaysEntry(){Reminder daily=r("Daily","2026-10-01T09:00","daily");daily.recordDelivery(today.atTime(10,0),false);assertEquals(today.plusDays(1),daily.next.toLocalDate());assertEquals(List.of(daily),WidgetAgenda.today(List.of(daily),today));}
 @Test public void deliveredOneOffRemainsInTodaysAgenda(){Reminder past=r("Past","2026-10-10T07:00","none");past.delivered=true;assertEquals(List.of(past),WidgetAgenda.today(List.of(past),today));}
 @Test public void respectsSelectedDates(){Reminder custom=r("Dates","2026-10-01T09:00","calendar");custom.dates.add("2026-10-10");assertEquals(List.of(custom),WidgetAgenda.today(List.of(custom),today));assertTrue(WidgetAgenda.today(List.of(custom),today.plusDays(1)).isEmpty());}
 @Test public void emptyData(){assertTrue(WidgetAgenda.today(Collections.emptyList(),today).isEmpty());}
}
