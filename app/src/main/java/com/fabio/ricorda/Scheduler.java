package com.fabio.ricorda;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;

public final class Scheduler {
    private Scheduler() {}
    static PendingIntent pending(Context c, Reminder r) {
        Intent i=new Intent(c,ReminderReceiver.class).setAction("REMIND")
            .setData(Uri.parse("ricorda://reminder/"+r.id)).putExtra("id",r.id);
        return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static boolean exact(Context c) {
        return Build.VERSION.SDK_INT<31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }
    public static void cancel(Context c,Reminder r) {
        AlarmState.cancel(c,r.id);
        c.getSystemService(AlarmManager.class).cancel(pending(c,r));
        c.getSystemService(NotificationManager.class).cancel(r.id,1);
    }
    public static void schedule(Context c,Reminder r) {
        AlarmManager am=c.getSystemService(AlarmManager.class);
        PendingIntent p=pending(c,r);
        if(r.deleted || r.delivered) { am.cancel(p); return; }
        long time=Math.max(System.currentTimeMillis()+1500,r.millis());
        try {
            if(exact(c)) {
                if(r.alarm) {
                    Intent show=new Intent(c,MainActivity.class).setData(Uri.parse("ricorda://show/"+r.id)).putExtra("show",r.id);
                    PendingIntent info=PendingIntent.getActivity(c,0,show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                    am.setAlarmClock(new AlarmManager.AlarmClockInfo(time,info),p);
                } else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,p);
            } else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,p);
        } catch(SecurityException e) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,p); }
    }
    public static void reschedule(Context c) { for(Reminder r:Store.read(c)) schedule(c,r); AlarmState.reschedule(c); }
}
