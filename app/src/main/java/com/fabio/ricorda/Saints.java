package com.fabio.ricorda;
import android.content.Context;
import java.time.LocalDate;
import java.util.List;
public final class Saints {
 private static SaintCalendar calendar;
 private static synchronized SaintCalendar data(Context c){if(calendar==null)try(java.io.Reader source=new java.io.InputStreamReader(c.getAssets().open("saints_2026_2036.txt"),java.nio.charset.StandardCharsets.UTF_8)){calendar=SaintCalendar.read(source);}catch(java.io.IOException e){android.util.Log.e("Ricorda","Calendar data unavailable",e);calendar=SaintCalendar.empty();}return calendar;}
 public static boolean enabled(Context c){return SaintCalendar.visible(Preferences.language(c));}
 public static String name(Context c,LocalDate day){return enabled(c)?data(c).name(day):"";}
 public static boolean hasData(Context c,LocalDate day){return !name(c,day).isEmpty();}
 public static String suffix(Context c,LocalDate day){String value=name(c,day);return value.isEmpty()?"":" · "+value;}
 public static String shortName(Context c,LocalDate day){return SaintText.abbreviated(name(c,day),7);}
 public static List<SaintCalendar.Entry> search(Context c,String query){return enabled(c)?data(c).search(query):java.util.Collections.emptyList();}
 public static List<SaintCalendar.Entry> search(Context c,String query,int year){return enabled(c)?data(c).search(query,year):java.util.Collections.emptyList();}
 private Saints(){}
}
