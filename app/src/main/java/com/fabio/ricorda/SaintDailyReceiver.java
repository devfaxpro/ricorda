package com.fabio.ricorda;

import android.app.*;
import android.content.*;
import android.net.Uri;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SaintDailyReceiver extends BroadcastReceiver {
    public static final String CHANNEL="saint_daily_v9", ENABLED="saint_daily_enabled", TIME="saint_daily_time";
    private static final String PENDING="saint_daily_pending", DELIVERED="saint_daily_delivered", TAG="saint_daily";
    public static boolean enabled(Context c){return Preferences.prefs(c).getBoolean(ENABLED,false);}
    public static LocalTime time(Context c){try{return LocalTime.parse(Preferences.get(c,TIME,"09:00")).withSecond(0).withNano(0);}catch(Exception e){return LocalTime.of(9,0);}}
    private static PendingIntent pending(Context c){return PendingIntent.getBroadcast(c,0,new Intent(c,SaintDailyReceiver.class).setAction("com.fabio.ricorda.SAINT_DAILY"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    public static void channel(Context c){
        if(!Saints.enabled(c))return;
        NotificationChannel channel=new NotificationChannel(CHANNEL,Preferences.s(c,"saint_daily_title"),NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(Preferences.s(c,"saint_daily_help"));channel.setShowBadge(false);
        c.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    public static void configure(Context c,boolean on,LocalTime at){
        Preferences.prefs(c).edit().putBoolean(ENABLED,on).putString(TIME,at.toString()).remove(PENDING).apply();
        schedule(c);
    }
    public static void schedule(Context c){
        try {
            SharedPreferences prefs=Preferences.prefs(c);AlarmManager alarms=c.getSystemService(AlarmManager.class);
            Instant now=Instant.now();ZoneId zone=ZoneId.systemDefault();
            ZonedDateTime next=SaintDailyPlan.next(now,zone,time(c),enabled(c),Preferences.language(c),prefs.getString(DELIVERED,""),prefs.getLong(PENDING,0));
            if(next==null||!Saints.hasData(c,next.toLocalDate())){
                alarms.cancel(pending(c));prefs.edit().remove(PENDING).apply();if(!SaintDailyPlan.active(enabled(c),Preferences.language(c)))c.getSystemService(NotificationManager.class).cancel(TAG,1);return;
            }
            channel(c);long target=next.toInstant().toEpochMilli();prefs.edit().putLong(PENDING,target).apply();
            long trigger=Math.max(System.currentTimeMillis()+1000,target);
            try{if(Scheduler.exact(c))alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pending(c));else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pending(c));}
            catch(SecurityException e){alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pending(c));}
        }catch(RuntimeException e){android.util.Log.e("Ricorda","Daily saint scheduling failed",e);}
    }
    @Override public void onReceive(Context c,Intent intent){
        try {
            SharedPreferences prefs=Preferences.prefs(c);Instant now=Instant.now();ZoneId zone=ZoneId.systemDefault();LocalDate today=now.atZone(zone).toLocalDate();
            if(!SaintDailyPlan.due(now,zone,time(c),enabled(c),Preferences.language(c),prefs.getString(DELIVERED,""),prefs.getLong(PENDING,0)))return;
            channel(c);String name=Saints.name(c,today);
            if(!name.isEmpty()&&ReminderReceiver.enabled(c,CHANNEL)){
                Intent open=new Intent(c,MainActivity.class).setData(Uri.parse("ricorda://saint/"+today)).putExtra("saint_day",today.toString()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent click=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                String date=DateLabels.format(today,Locale.ITALIAN,"EMMMMd");
                Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(Preferences.s(c,"saint_daily_title"))
                    .setContentText(name).setSubText(date).setStyle(new Notification.BigTextStyle().bigText(name)).setContentIntent(click)
                    .setAutoCancel(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_EVENT).setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setTimeoutAfter(Math.max(1000,Duration.between(now,today.plusDays(1).atStartOfDay(zone).toInstant()).toMillis())).build();
                c.getSystemService(NotificationManager.class).notify(TAG,1,n);
                prefs.edit().putString(DELIVERED,today.toString()).apply();
            }
            // Skip a blocked day's alert; re-enabling notifications will not flood old notices.
            prefs.edit().remove(PENDING).apply();
        }catch(RuntimeException e){android.util.Log.e("Ricorda","Daily saint notification failed",e);Preferences.prefs(c).edit().remove(PENDING).apply();}
        finally{schedule(c);WidgetUpdater.refresh(c);}
    }
}
