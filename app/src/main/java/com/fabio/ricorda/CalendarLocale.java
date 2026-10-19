package com.fabio.ricorda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

/** Shared weekday ordering for the home calendar and date picker. */
public final class CalendarLocale {
 public static Locale regional(Locale locale){
  if(!locale.getCountry().isEmpty())return locale;
  String[] tags={"it","en","es","pt-BR","fr","de","zh-Hans","ja","ko","ru","nl","pl","uk","cs","sk","hu","ro","bg","el","tr","sv","da","nb","fi","ar","he","fa","hi","bn","ur","id","ms","vi","th","fil","ta","te","mr","sw","zh-Hant"};
  String[] regions={"IT","US","ES","BR","FR","DE","CN","JP","KR","RU","NL","PL","UA","CZ","SK","HU","RO","BG","GR","TR","SE","DK","NO","FI","EG","IL","IR","IN","BD","PK","ID","MY","VN","TH","PH","IN","IN","IN","KE","TW"};
  String tag=LanguageCatalog.resolve(locale.toLanguageTag());
  for(int i=0;i<tags.length;i++)if(tags[i].equals(tag))return new Locale.Builder().setLocale(locale).setRegion(regions[i]).build();
  return locale;
 }
 public static DayOfWeek day(Locale locale,int column){
  if(column<0||column>6)throw new IllegalArgumentException("Weekday column");
  return WeekFields.of(regional(locale)).getFirstDayOfWeek().plus(column);
 }
 public static int offset(Locale locale,LocalDate first){
  return Math.floorMod(first.getDayOfWeek().getValue()-day(locale,0).getValue(),7);
 }
 public static int visualColumn(int logical,boolean rtl){return rtl?6-logical:logical;}
 private CalendarLocale(){}
}
