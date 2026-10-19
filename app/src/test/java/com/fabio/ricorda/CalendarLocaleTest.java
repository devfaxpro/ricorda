package com.fabio.ricorda;
import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;
public class CalendarLocaleTest {
 @Test public void respectsFirstWeekday(){assertEquals(DayOfWeek.MONDAY,CalendarLocale.day(Locale.ITALY,0));assertEquals(DayOfWeek.SUNDAY,CalendarLocale.day(Locale.US,0));}
 @Test public void languageOnlyItalianKeepsMonday(){assertEquals(DayOfWeek.MONDAY,CalendarLocale.day(Locale.ITALIAN,0));assertEquals("TW",CalendarLocale.regional(Locale.forLanguageTag("zh-Hant")).getCountry());}
 @Test public void allDaysAppearOnce(){for(String tag:LanguageCatalog.TAGS){Locale locale=Locale.forLanguageTag(tag);Set<DayOfWeek> days=new HashSet<>();for(int i=0;i<7;i++)days.add(CalendarLocale.day(locale,i));assertEquals(7,days.size());}}
 @Test public void monthOffsetsAndWeekdayHeadersAgree(){for(String tag:LanguageCatalog.TAGS){Locale locale=Locale.forLanguageTag(tag);for(int month=1;month<=12;month++){LocalDate first=LocalDate.of(2026,month,1);int offset=CalendarLocale.offset(locale,first);assertEquals(first.getDayOfWeek(),CalendarLocale.day(locale,offset));}}}
 @Test public void mirroredMiniatureColumnsKeepTheSameDay(){for(int i=0;i<7;i++){assertEquals(i,CalendarLocale.visualColumn(i,false));assertEquals(6-i,CalendarLocale.visualColumn(i,true));}}
}
