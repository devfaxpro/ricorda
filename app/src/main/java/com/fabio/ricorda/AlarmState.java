package com.fabio.ricorda;

import android.app.*;
import android.content.*;
import android.net.Uri;
import java.util.*;

/** Snoozes are separate alarms: postponing one never alters its recurrence anchor. */
public final class AlarmState {
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("alarm_state",0);}
 private static void put(Context c,String key,long value){if(!prefs(c).edit().putLong(key,value).commit())throw new IllegalStateException("Alarm state could not be saved");}
 private static void remove(Context c,String key){if(!prefs(c).edit().remove(key).commit())throw new IllegalStateException("Alarm state could not be saved");}
 static long started(Context c,String id){return prefs(c).getLong("active:"+id,0);}
 public static void markActive(Context c,String id){put(c,"active:"+id,AlarmTiming.snoozeAt(System.currentTimeMillis()));}
 public static List<String> activeIds(Context c){List<String> ids=new ArrayList<>();long now=System.currentTimeMillis();for(Map.Entry<String,?> e:prefs(c).getAll().entrySet())if(e.getKey().startsWith("active:")&&e.getValue() instanceof Long&&AlarmTiming.active((Long)e.getValue(),now))ids.add(e.getKey().substring(7));ids.sort(Comparator.comparingLong(id->started(c,id)));return ids;}
 static void clearActive(Context c,String id){remove(c,"active:"+id);}
 public static void dismiss(Context c,String id){clearActive(c,id);c.getSystemService(NotificationManager.class).cancel(id,1);AlarmService.stopReminder(id);}
 public static void dismissAll(Context c){for(String id:activeIds(c))dismiss(c,id);AlarmService.stopAll(c);}
 private static Reminder find(Context c,String id){for(Reminder r:Store.read(c))if(r.id.equals(id)&&!r.deleted&&r.alarm)return r;return null;}
 public static boolean snooze(Context c,String id){if(!activeIds(c).contains(id)||find(c,id)==null)return false;long at=AlarmTiming.snoozeAt(System.currentTimeMillis());put(c,"snooze:"+id,at);try{schedule(c,id,at);}catch(RuntimeException e){remove(c,"snooze:"+id);throw e;}dismiss(c,id);return true;}
 public static void snoozeAll(Context c){for(String id:activeIds(c))snooze(c,id);}
 private static PendingIntent pending(Context c,String id){Intent i=new Intent(c,ReminderReceiver.class).setAction("SNOOZE_FIRE").setData(Uri.parse("ricorda://snooze/"+id)).putExtra("id",id);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 private static void schedule(Context c,String id,long at){AlarmManager am=c.getSystemService(AlarmManager.class);PendingIntent p=pending(c,id);long time=Math.max(at,System.currentTimeMillis()+1000);if(Scheduler.exact(c))try{am.setAlarmClock(new AlarmManager.AlarmClockInfo(time,AlarmActivity.pending(c)),p);return;}catch(SecurityException ignored){}am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,p);}
 public static boolean consumeSnooze(Context c,String id){long at=prefs(c).getLong("snooze:"+id,0);if(at==0)return false;if(at>System.currentTimeMillis()){schedule(c,id,at);return false;}remove(c,"snooze:"+id);return true;}
 public static void cancelSnooze(Context c,String id){remove(c,"snooze:"+id);c.getSystemService(AlarmManager.class).cancel(pending(c,id));}
 public static void cancel(Context c,String id){cancelSnooze(c,id);dismiss(c,id);}
 public static void reschedule(Context c){for(Map.Entry<String,?> e:prefs(c).getAll().entrySet())if(e.getKey().startsWith("snooze:")&&e.getValue() instanceof Long){String id=e.getKey().substring(7);if(find(c,id)==null)cancelSnooze(c,id);else schedule(c,id,(Long)e.getValue());}}
 public static void afterBoot(Context c){SharedPreferences.Editor edit=prefs(c).edit();for(String key:prefs(c).getAll().keySet())if(key.startsWith("active:"))edit.remove(key);edit.commit();}
 private AlarmState(){}
}
