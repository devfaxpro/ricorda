package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalTime;
public class TimeEntryTest {
 @Test public void acceptsAllMinutesOfDay(){for(int h=0;h<24;h++)for(int m=0;m<60;m++)assertEquals(LocalTime.of(h,m),TimeEntry.parse(Integer.toString(h),Integer.toString(m)));}
 @Test public void acceptsLeadingZeroes(){assertEquals(LocalTime.of(7,5),TimeEntry.parse("07","05"));}
 @Test public void acceptsLocalizedDigits(){assertEquals(LocalTime.of(9,30),TimeEntry.parse("０９","３０"));assertEquals(LocalTime.of(9,30),TimeEntry.parse("٠٩","٣٠"));}
 @Test public void rejectsInvalidHour(){for(String h:new String[]{"24","99","-1","1.5","xx","１２３"})assertNull(TimeEntry.parse(h,"00"));}
 @Test public void rejectsInvalidMinute(){for(String m:new String[]{"60","99","-1","0.5","xx","100"})assertNull(TimeEntry.parse("12",m));}
 @Test public void incompleteInputCannotReplacePreviousTime(){assertNull(TimeEntry.parse("","30"));assertNull(TimeEntry.parse("12",""));assertNull(TimeEntry.parse(null,"00"));assertNull(TimeEntry.parse("12",null));}
 @Test public void disallowsSpaceAndSigns(){assertNull(TimeEntry.parse(" 9","00"));assertNull(TimeEntry.parse("+9","00"));assertNull(TimeEntry.parse("09","5 "));}
}
