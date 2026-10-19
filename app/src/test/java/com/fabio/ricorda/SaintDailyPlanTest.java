package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;

public class SaintDailyPlanTest {
 private final ZoneId zone=ZoneId.of("Europe/Rome");
 private final LocalTime time=LocalTime.of(9,0);
 private Instant at(String value){return LocalDateTime.parse(value).atZone(zone).toInstant();}
 private ZonedDateTime next(String now,String last,long pending){return SaintDailyPlan.next(at(now),zone,time,true,"it",last,pending);}
 @Test public void disabledDoesNotSchedule(){assertNull(SaintDailyPlan.next(at("2026-10-10T08:00"),zone,time,false,"it","",0));}
 @Test public void otherLanguagesDoNotSchedule(){for(String lang:LanguageCatalog.TAGS)if(!lang.equals("it"))assertNull(SaintDailyPlan.next(at("2026-10-10T08:00"),zone,time,true,lang,"",0));}
 @Test public void upcomingToday(){assertEquals(at("2026-10-10T09:00"),next("2026-10-10T08:00","",0).toInstant());}
 @Test public void newSettingDoesNotSendPastTime(){assertEquals(at("2026-10-11T09:00"),next("2026-10-10T10:00","",0).toInstant());}
 @Test public void asleepTodayKeepsPendingAlarm(){long p=at("2026-10-10T09:00").toEpochMilli();assertEquals(p,next("2026-10-10T10:00","",p).toInstant().toEpochMilli());assertTrue(SaintDailyPlan.due(at("2026-10-10T10:00"),zone,time,true,"it","",p));}
 @Test public void rebootAfterDeliveryDoesNotRepeat(){long p=at("2026-10-10T09:00").toEpochMilli();assertFalse(SaintDailyPlan.due(at("2026-10-10T10:00"),zone,time,true,"it","2026-10-10",p));assertEquals(at("2026-10-11T09:00"),next("2026-10-10T10:00","2026-10-10",p).toInstant());}
 @Test public void oldAlarmDoesNotSendWrongSaint(){long p=at("2026-10-09T09:00").toEpochMilli();assertFalse(SaintDailyPlan.due(at("2026-10-10T08:00"),zone,time,true,"it","",p));assertEquals(at("2026-10-10T09:00"),next("2026-10-10T08:00","",p).toInstant());}
 @Test public void movingTimeInvalidatesPending(){long p=at("2026-10-10T08:00").toEpochMilli();assertFalse(SaintDailyPlan.due(at("2026-10-10T08:30"),zone,time,true,"it","",p));}
 @Test public void languageChangeSuppressesAlreadyScheduledDelivery(){assertFalse(SaintDailyPlan.due(at("2026-10-10T09:00"),zone,time,true,"en","",at("2026-10-10T09:00").toEpochMilli()));}
 @Test public void disablingSuppressesAlreadyScheduledDelivery(){assertFalse(SaintDailyPlan.due(at("2026-10-10T09:00"),zone,time,false,"it","",at("2026-10-10T09:00").toEpochMilli()));}
 @Test public void earlyBroadcastDoesNotNotify(){assertFalse(SaintDailyPlan.due(at("2026-10-10T08:59"),zone,time,true,"it","",at("2026-10-10T09:00").toEpochMilli()));}
 @Test public void clockTurnedBackDoesNotRepeatSameDay(){assertEquals(at("2026-10-11T09:00"),next("2026-10-10T08:00","2026-10-10",0).toInstant());}
 @Test public void springTimeUsesLocalHour(){ZonedDateTime n=next("2026-03-28T10:00","",0);assertEquals(LocalTime.of(9,0),n.toLocalTime());assertEquals(ZoneOffset.ofHours(2),n.getOffset());}
 @Test public void autumnTimeUsesLocalHour(){ZonedDateTime n=next("2026-10-24T10:00","",0);assertEquals(LocalTime.of(9,0),n.toLocalTime());assertEquals(ZoneOffset.ofHours(1),n.getOffset());}
 @Test public void missingHourUsesFirstResolvedTime(){ZonedDateTime n=SaintDailyPlan.next(at("2026-03-29T01:00"),zone,LocalTime.of(2,30),true,"it","",0);assertEquals(LocalTime.of(3,30),n.toLocalTime());}
 @Test public void repeatedHourOnlyNotifiesOnce(){LocalTime t=LocalTime.of(2,30);ZonedDateTime first=LocalDate.of(2026,10,25).atTime(t).atZone(zone);Instant second=first.withLaterOffsetAtOverlap().toInstant();assertFalse(SaintDailyPlan.due(second,zone,t,true,"it","2026-10-25",first.toInstant().toEpochMilli()));}
 @Test public void changedZoneUsesNewLocalHour(){Instant now=at("2026-10-10T08:00");ZoneId other=ZoneId.of("America/New_York");ZonedDateTime n=SaintDailyPlan.next(now,other,time,true,"it","",at("2026-10-10T09:00").toEpochMilli());assertEquals(LocalTime.of(9,0),n.toLocalTime());assertEquals(other,n.getZone());}
 @Test public void cannotExceed2036Calendar(){assertNull(next("2036-12-31T10:00","",0));assertNull(next("2037-01-01T08:00","",0));assertFalse(SaintDailyPlan.available(LocalDate.of(2037,1,1)));}
 @Test public void beforeDatasetStartsOnJanuaryFirst(){assertEquals(at("2026-01-01T09:00"),next("2025-12-30T08:00","",0).toInstant());}
 @Test public void finalDayRemainsDeliverable(){assertTrue(SaintDailyPlan.due(at("2036-12-31T10:00"),zone,time,true,"it","",at("2036-12-31T09:00").toEpochMilli()));}
 @Test public void yearlyBoundaryDoesNotStopDailyNotifications(){assertEquals(at("2027-01-01T09:00"),next("2026-12-31T10:00","",0).toInstant());assertEquals(at("2032-02-29T09:00"),next("2032-02-28T10:00","",0).toInstant());}
}
