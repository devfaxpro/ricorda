package com.fabio.ricorda;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SaintCalendarTest {
 private SaintCalendar calendar;
 private String raw;
 @Before public void loadShippedCalendar()throws Exception {
  raw=new String(Files.readAllBytes(Paths.get("src/main/assets/saints_2026_2036.txt")),StandardCharsets.UTF_8);
  calendar=SaintCalendar.read(new StringReader(raw));
 }
 private List<String> dates(String query){return calendar.search(query,2026).stream().map(e->e.date.toString()).collect(Collectors.toList());}
 @Test public void everyCivilDayThrough2036HasAnEntry(){assertEquals(4018,calendar.size());for(LocalDate d=SaintCalendar.FIRST;!d.isAfter(SaintCalendar.LAST);d=d.plusDays(1))assertFalse(d.toString(),calendar.name(d).isEmpty());}
 @Test public void packagedNamesAreReadVerbatim(){for(String line:raw.split("\\R")){if(!line.matches("[0-9]{4}-.*"))continue;String[] fields=line.split("\\|",-1);assertEquals(fields[2],calendar.name(LocalDate.parse(fields[0])));}}
 @Test public void datesOutsideSupportedRangeHaveNoData(){assertEquals("",calendar.name(LocalDate.of(2037,1,1)));assertEquals("",calendar.name(LocalDate.of(2025,12,31)));}
 @Test public void paoloMatchesAllFourWholeNameOccurrences(){assertEquals(Arrays.asList("2026-01-25","2026-02-06","2026-06-26","2026-06-29"),dates("Paolo"));}
 @Test public void caseAndSurroundingSpacesDoNotChangeResults(){assertEquals(dates("Paolo"),dates("paolo"));assertEquals(dates("Paolo"),dates("  PAOLO  "));assertEquals(dates("Paolo"),dates("pAoLo"));}
 @Test public void partialNamesDoNotMatchPaolo(){assertTrue(dates("paol").isEmpty());assertFalse(dates("Paolo").contains("2026-06-22"));assertFalse(dates("Paolo").contains("2026-04-02"));assertFalse(dates("Paolo").contains("2026-09-27"));}
 @Test public void multipleKeywordsMatchTheCompleteStoredName(){assertEquals(Collections.singletonList("2026-02-06"),dates("paolo miki"));assertEquals(Collections.singletonList("2026-02-06"),dates("MIKI PAOLO"));}
 @Test public void saintPrefixesAndPunctuationAreIgnored(){assertEquals(dates("Paolo"),dates("San Paolo"));assertEquals(dates("Paolo"),dates("S. Paolo!"));assertEquals(dates("Paolo"),dates("Ss. Paolo"));}
 @Test public void emptyAndOnlyPrefixesReturnNoResults(){assertTrue(dates("").isEmpty());assertTrue(dates("   ").isEmpty());assertTrue(dates("San").isEmpty());assertTrue(dates("...").isEmpty());}
 @Test public void accentsAreOptional(){assertTrue(SaintText.matchesKeyword("S. André", "ANDRE"));assertTrue(SaintText.matchesKeyword("Sant’Agata", "AGATA"));}
 @Test public void saintsAreVisibleOnlyInItalian(){for(String tag:LanguageCatalog.TAGS)assertEquals(tag,tag.equals("it"),SaintCalendar.visible(tag));}
 @Test public void incompleteCalendarIsRejected()throws Exception{try{SaintCalendar.read(new StringReader(raw.substring(0,raw.indexOf("2026-12-31"))));fail("Incomplete data accepted");}catch(IOException expected){assertTrue(expected.getMessage().contains("Incomplete"));}}
 @Test public void duplicatedDateIsRejected()throws Exception{try{SaintCalendar.read(new StringReader(raw+"\n2026-01-01|SANTI|Duplicate|NO|-\n"));fail("Duplicate accepted");}catch(IOException expected){assertTrue(expected.getMessage().contains("duplicated"));}}
 @Test public void abbreviatedNewSourceNamesKeepTwoDots(){assertEquals("Paolo",SaintText.abbreviated("S. Paolo",7));assertEquals("Pietr..",SaintText.abbreviated("Ss. Pietro e Paolo",7));}

 @Test public void easterAndItalianMovableDatesMatchCeiTable(){int[][] dates={{2026,4,5},{2027,3,28},{2028,4,16},{2029,4,1},{2030,4,21},{2031,4,13},{2032,3,28},{2033,4,17},{2034,4,9},{2035,3,25},{2036,4,13}};for(int[] d:dates){LocalDate e=LocalDate.of(d[0],d[1],d[2]);assertTrue(calendar.name(e).startsWith("Pasqua ·"));assertTrue(calendar.name(e.minusDays(46)).startsWith("Mercoledì delle Ceneri ·"));assertTrue(calendar.name(e.plusDays(42)).startsWith("Ascensione del Signore ·"));assertTrue(calendar.name(e.plusDays(49)).startsWith("Pentecoste ·"));assertTrue(calendar.name(e.plusDays(63)).startsWith("Corpus Domini ·"));}}
 @Test public void threeLeapDaysAreCovered(){for(int y:new int[]{2028,2032,2036})assertTrue(calendar.name(LocalDate.of(y,2,29)).contains("Ilaro"));}
 @Test public void movingFeastsAreNotFrozenOn2026Dates(){assertFalse(calendar.name(LocalDate.of(2027,4,5)).contains("Pasqua"));assertFalse(calendar.name(LocalDate.of(2027,2,18)).contains("Ceneri"));assertTrue(calendar.name(LocalDate.of(2027,4,5)).contains("Vincenzo Ferrer"));}
 @Test public void knownSourceMovableErrorsAreCorrected(){assertFalse(calendar.name(LocalDate.of(2026,10,25)).contains("Re dell"));assertTrue(calendar.name(LocalDate.of(2026,11,22)).contains("Re dell"));assertFalse(calendar.name(LocalDate.of(2026,12,26)).contains("Sacra Famiglia"));assertTrue(calendar.name(LocalDate.of(2026,12,27)).contains("Sacra Famiglia"));assertTrue(calendar.name(LocalDate.of(2033,12,30)).contains("Sacra Famiglia"));}
 @Test public void searchIsScopedToSelectedYearAndKeepsFixedSaints(){assertEquals(4,calendar.search("Paolo",2036).size());for(SaintCalendar.Entry e:calendar.search("Paolo",2036))assertEquals(2036,e.date.getYear());assertTrue(calendar.name(LocalDate.of(2026,12,13)).contains("Santa Lucia"));assertTrue(calendar.name(LocalDate.of(2036,12,29)).contains("Tommaso Becket"));}
 @Test public void saintsAndAlertPlanShareTheSameCoverage(){assertEquals(SaintCalendar.FIRST,SaintDailyPlan.FIRST);assertEquals(SaintCalendar.LAST,SaintDailyPlan.LAST);}
}
