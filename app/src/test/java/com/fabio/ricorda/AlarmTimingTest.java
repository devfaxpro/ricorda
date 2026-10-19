package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
public class AlarmTimingTest {
 @Test public void snoozeAddsExactlyTenMinutesAcrossMidnight(){long now=java.time.Instant.parse("2026-10-09T23:55:00Z").toEpochMilli();assertEquals(java.time.Instant.parse("2026-10-10T00:05:00Z").toEpochMilli(),AlarmTiming.snoozeAt(now));}
 @Test public void snoozeCrossesDstByDuration(){long now=java.time.Instant.parse("2026-10-25T00:55:00Z").toEpochMilli();assertEquals(600000L,AlarmTiming.snoozeAt(now)-now);}
 @Test public void expiredAlarmIsInactive(){assertFalse(AlarmTiming.active(1234,1234));assertFalse(AlarmTiming.active(1234,1235));assertTrue(AlarmTiming.active(1234,1233));}
}
