package com.fabio.ricorda;
import android.content.*;

public final class RescheduleReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i) {
        try { if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))AlarmState.afterBoot(c);Scheduler.reschedule(c); }
        catch(Exception e) { android.util.Log.e("Ricorda","Ripristino degli avvisi non riuscito",e); }
        SaintDailyReceiver.schedule(c);WidgetUpdater.refresh(c);
    }
}
