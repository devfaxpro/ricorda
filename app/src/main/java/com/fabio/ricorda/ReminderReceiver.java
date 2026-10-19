package com.fabio.ricorda;
import android.app.*;
import android.content.*;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import java.time.LocalDateTime;
import java.util.List;
public final class ReminderReceiver extends BroadcastReceiver {
 public static final String NORMAL="reminders_v1",ALARM="alarms_v1",PLAYBACK="alarm_playback_v8";
 public static void channels(Context c){
  NotificationManager nm=c.getSystemService(NotificationManager.class);
  nm.createNotificationChannel(new NotificationChannel(NORMAL,Preferences.s(c,"reminders"),NotificationManager.IMPORTANCE_HIGH));
  NotificationChannel alarm=new NotificationChannel(ALARM,Preferences.s(c,"alarms"),NotificationManager.IMPORTANCE_HIGH);
  alarm.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());alarm.enableVibration(true);nm.createNotificationChannel(alarm);
  NotificationChannel playing=new NotificationChannel(PLAYBACK,Preferences.s(c,"ringing"),NotificationManager.IMPORTANCE_HIGH);playing.setSound(null,null);playing.enableVibration(false);nm.createNotificationChannel(playing);
 }
 public static boolean enabled(Context c,String id){NotificationManager nm=c.getSystemService(NotificationManager.class);NotificationChannel ch=nm.getNotificationChannel(id);return nm.areNotificationsEnabled()&&ch!=null&&ch.getImportance()!=NotificationManager.IMPORTANCE_NONE;}
 public static boolean playbackEnabled(Context c){NotificationChannel old=c.getSystemService(NotificationManager.class).getNotificationChannel("alarm_playback_v5");return enabled(c,PLAYBACK)&&(old==null||old.getImportance()!=NotificationManager.IMPORTANCE_NONE);}
 public static String soundChannel(Context c,Reminder r){
  NotificationManager nm=c.getSystemService(NotificationManager.class);String base=r.alarm?ALARM:NORMAL;NotificationChannel baseChannel=nm.getNotificationChannel(base);
  Uri sound=AlertPolicy.quiet(r)||r.sound.equals("silent")?null:r.sound.isEmpty()?(baseChannel==null?RingtoneManager.getDefaultUri(r.alarm?RingtoneManager.TYPE_ALARM:RingtoneManager.TYPE_NOTIFICATION):baseChannel.getSound()):Uri.parse(r.sound);
  String id=AlertPolicy.channel(r,sound==null?"silent":sound.toString());if(nm.getNotificationChannel(id)==null){
   String name=Preferences.s(c,r.alarm?"alarm":AlertPolicy.quiet(r)?"notice_discreet":"notice_banner");
   NotificationChannel channel=new NotificationChannel(id,name,AlertPolicy.quiet(r)?NotificationManager.IMPORTANCE_LOW:NotificationManager.IMPORTANCE_HIGH);
   channel.setDescription(Preferences.s(c,"notice_android_help"));channel.setSound(sound,new AudioAttributes.Builder().setUsage(r.alarm?AudioAttributes.USAGE_ALARM:AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());channel.enableVibration(AlertPolicy.vibrates(r));channel.setShowBadge(true);channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);nm.createNotificationChannel(channel);
  }if(!id.equals(base)){
   NotificationChannel existing=nm.getNotificationChannel(id);String label=Preferences.s(c,r.alarm?"alarm":AlertPolicy.quiet(r)?"notice_discreet":"notice_banner");
   if(!AlertPolicy.quiet(r)){String toneName=Preferences.s(c,sound==null?"silent":"base_sound");if(!r.sound.isEmpty()&&sound!=null)try{android.media.Ringtone tone=RingtoneManager.getRingtone(c,sound);if(tone!=null)toneName=tone.getTitle(c);}catch(Exception ignored){}label+=" · "+toneName;if(AlertPolicy.vibrates(r))label+=" · "+Preferences.s(c,"vibration");}
   existing.setName(label);existing.setDescription(Preferences.s(c,"notice_android_help"));nm.createNotificationChannel(existing);
  }return id;
 }
 @Override public void onReceive(Context c,Intent i){
  if("STOP_ALL".equals(i.getAction())||"SNOOZE_ALL".equals(i.getAction())){try{if("SNOOZE_ALL".equals(i.getAction()))AlarmState.snoozeAll(c);else AlarmState.dismissAll(c);}catch(Exception e){android.util.Log.e("Ricorda","Alarm action failed",e);}return;}
  String id=i.getStringExtra("id");if(id==null)return;
  if("STOP".equals(i.getAction())||"SNOOZE".equals(i.getAction())){try{if("SNOOZE".equals(i.getAction()))AlarmState.snooze(c,id);else AlarmState.dismiss(c,id);}catch(Exception e){android.util.Log.e("Ricorda","Alarm action failed",e);}return;}
  try{
   List<Reminder> list=Store.read(c);Reminder r=null;for(Reminder x:list)if(x.id.equals(id))r=x;
   boolean snooze="SNOOZE_FIRE".equals(i.getAction());
   if(r==null||r.deleted||(!snooze&&r.delivered))return;
   if(snooze){if(!r.alarm||!AlarmState.consumeSnooze(c,id))return;}else if(r.millis()>System.currentTimeMillis()){Scheduler.schedule(c,r);return;}
   channels(c);String channel=soundChannel(c,r);if(!enabled(c,r.alarm?ALARM:NORMAL)||!enabled(c,channel))return;
   if(r.alarm){AlarmState.cancelSnooze(c,id);AlarmState.markActive(c,id);}
   boolean started=false;
   if(r.alarm&&Scheduler.exact(c)&&playbackEnabled(c))try{c.startForegroundService(new Intent(c,AlarmService.class).putExtra("id",id).putExtra("name",r.name).putExtra("soundChannel",channel).putExtra("volume",r.volume));started=true;}catch(RuntimeException e){android.util.Log.w("Ricorda","Using notification fallback",e);}
   if(!started)notifyReminder(c,r);
   r.recordDelivery(LocalDateTime.now(),snooze);
   if(!snooze){Store.write(c,list);Scheduler.schedule(c,r);}
  }catch(Exception e){android.util.Log.e("Ricorda","Reminder delivery failed",e);}
 }
 private void notifyReminder(Context c,Reminder r){
  PendingIntent open=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class).setData(Uri.parse("ricorda://show/"+r.id)).putExtra("show",r.id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent stop=PendingIntent.getBroadcast(c,0,new Intent(c,ReminderReceiver.class).setAction("STOP").setData(Uri.parse("ricorda://stop/"+r.id)).putExtra("id",r.id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String message=r.notes.isEmpty()?Preferences.s(c,"reminder_now"):r.notes;
  Notification.Builder builder=new Notification.Builder(c,soundChannel(c,r)).setSmallIcon(R.drawable.ic_notification).setContentTitle(r.name).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message))
   .setContentIntent(open).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).setCategory(r.alarm?Notification.CATEGORY_ALARM:Notification.CATEGORY_REMINDER)
   .setShowWhen(true).setWhen(System.currentTimeMillis()).addAction(new Notification.Action.Builder(null,Preferences.s(c,r.alarm?"stop_alarm":"ok"),stop).build());
  if(r.alarm){PendingIntent snooze=PendingIntent.getBroadcast(c,0,new Intent(c,ReminderReceiver.class).setAction("SNOOZE").setData(Uri.parse("ricorda://snooze-action/"+r.id)).putExtra("id",r.id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);builder.setContentIntent(AlarmActivity.pending(c)).setDeleteIntent(stop).setAutoCancel(false).setTimeoutAfter(AlarmTiming.TEN_MINUTES).addAction(new Notification.Action.Builder(null,Preferences.s(c,"snooze_alarm"),snooze).build());if(AlarmActivity.fullScreenAllowed(c))builder.setFullScreenIntent(AlarmActivity.pending(c),true);}
  Notification n=builder.build();if(r.alarm)n.flags|=Notification.FLAG_INSISTENT;c.getSystemService(NotificationManager.class).notify(r.id,1,n);
 }
}
