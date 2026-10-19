package com.fabio.ricorda;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Explicit offline almanac dates: movable celebrations are generated per year. */
public final class SaintCalendar {
 public static final LocalDate FIRST=LocalDate.of(2026,1,1),LAST=LocalDate.of(2036,12,31);
 public static final class Entry {
  public final LocalDate date;public final String type,name,note;public final boolean movable;
  private Entry(String[] fields){date=LocalDate.parse(fields[0]);type=fields[1];name=fields[2];movable=fields[3].equals("SI");note=fields[4];}
 }
 private final SortedMap<LocalDate,Entry> entries;
 private SaintCalendar(SortedMap<LocalDate,Entry> data){entries=Collections.unmodifiableSortedMap(data);}
 public static SaintCalendar empty(){return new SaintCalendar(new TreeMap<>());}
 public static SaintCalendar read(Reader source)throws IOException {
  BufferedReader reader=new BufferedReader(source);String header=reader.readLine();if(!"data|tipo|voce_originale|contiene_ricorrenza_mobile|nota_verifica".equals(header))throw new IOException("Invalid calendar header");
  SortedMap<LocalDate,Entry> data=new TreeMap<>();String line;
  try{while((line=reader.readLine())!=null){if(line.trim().isEmpty())continue;String[] parts=line.split("\\|",-1);if(parts.length!=5||parts[2].isEmpty()||!(parts[3].equals("SI")||parts[3].equals("NO")))throw new IOException("Invalid calendar row");Entry entry=new Entry(parts);if(entry.date.isBefore(FIRST)||entry.date.isAfter(LAST)||data.put(entry.date,entry)!=null)throw new IOException("Invalid or duplicated date");}}catch(java.time.DateTimeException e){throw new IOException("Invalid calendar date",e);}
  if(data.size()!=java.time.temporal.ChronoUnit.DAYS.between(FIRST,LAST)+1)throw new IOException("Incomplete calendar");return new SaintCalendar(data);
 }
 public String name(LocalDate date){Entry entry=entries.get(date);return entry==null?"":entry.name;}
 public List<Entry> search(String query){List<Entry> result=new ArrayList<>();for(Entry entry:entries.values())if(SaintText.matchesKeyword(entry.name,query))result.add(entry);return result;}
 public List<Entry> search(String query,int year){List<Entry> result=new ArrayList<>();for(Entry entry:entries.values())if(entry.date.getYear()==year&&SaintText.matchesKeyword(entry.name,query))result.add(entry);return result;}
 public int size(){return entries.size();}
 public static boolean visible(String language){return "it".equals(language);}
}
