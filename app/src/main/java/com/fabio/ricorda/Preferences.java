package com.fabio.ricorda;
import android.content.*;
import android.content.res.Configuration;
import java.util.Locale;
public final class Preferences {
 public static final String[] LANGS=LanguageCatalog.TAGS, LANGUAGE_NAMES=LanguageCatalog.NAMES;
 public static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("appearance",0);}
 public static String get(Context c,String k,String fallback){return prefs(c).getString(k,fallback);}
 public static void put(Context c,String k,String v){prefs(c).edit().putString(k,v).apply();if(k.equals("language")){SaintDailyReceiver.schedule(c);WidgetUpdater.refresh(c);}else if(k.equals("color")||k.equals("mode"))WidgetUpdater.refresh(c);}
 public static String language(Context c){android.os.LocaleList system=android.content.res.Resources.getSystem().getConfiguration().getLocales();String[] tags=new String[system.size()];for(int i=0;i<tags.length;i++)tags[i]=system.get(i).toLanguageTag();return LanguageCatalog.select(get(c,"language","system"),tags);}
 public static Locale locale(Context c){android.os.LocaleList system=android.content.res.Resources.getSystem().getConfiguration().getLocales();String[] tags=new String[system.size()];for(int i=0;i<tags.length;i++)tags[i]=system.get(i).toLanguageTag();return LanguageCatalog.displayLocale(language(c),tags);}
 public static Context localized(Context c){Configuration cfg=new Configuration(c.getResources().getConfiguration());cfg.setLocale(Locale.forLanguageTag(language(c)));cfg.setLayoutDirection(Locale.forLanguageTag(language(c)));String mode=get(c,"mode","system");if(!mode.equals("system"))cfg.uiMode=(cfg.uiMode&~Configuration.UI_MODE_NIGHT_MASK)|(mode.equals("dark")?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);return c.createConfigurationContext(cfg);}
 public static String s(Context c,String key){Context local=localized(c);int id=local.getResources().getIdentifier(key,"string",c.getPackageName());if(id==0)throw new IllegalArgumentException(key);return local.getString(id);}
 public static String selectedDates(Context c,int count){return localized(c).getResources().getQuantityString(R.plurals.selected_date_count,count,count);}
 private Preferences(){}
}
