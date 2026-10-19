package com.fabio.ricorda;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.util.LinkedHashMap;

/** Active only during audible alarm playback, never while awaiting an alarm. */
public final class AlarmService extends Service {
 private static final int ID=7101;
 private static AlarmService active;
 private final LinkedHashMap<String,String> ringing=new LinkedHashMap<>();
 private final LinkedHashMap<String,String> soundChannels=new LinkedHashMap<>();
 private final LinkedHashMap<String,Integer> volumes=new LinkedHashMap<>();
 private float gain=.4f;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private MediaPlayer player;
 private PowerManager.WakeLock wake;
 private Vibrator vibrator;
 private AudioFocusRequest focus;
 public static void stopReminder(String id){if(active==null)return;boolean removed=active.ringing.remove(id)!=null;active.soundChannels.remove(id);active.volumes.remove(id);if(active.ringing.isEmpty())active.stopSelf();else if(removed){active.getSystemService(NotificationManager.class).notify(ID,active.notification(false));active.playLatest();}}
 public static void stopAll(Context c){c.stopService(new Intent(c,AlarmService.class));}
 @Override public void onCreate(){super.onCreate();active=this;ReminderReceiver.channels(this);}
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int startId){
  if(i==null||i.getStringExtra("id")==null||i.getStringExtra("name")==null){stopSelf();return START_NOT_STICKY;}
  ringing.put(i.getStringExtra("id"),i.getStringExtra("name"));
  soundChannels.remove(i.getStringExtra("id"));soundChannels.put(i.getStringExtra("id"),i.getStringExtra("soundChannel"));
  volumes.put(i.getStringExtra("id"),Math.max(10,Math.min(100,i.getIntExtra("volume",40))));
  if(Build.VERSION.SDK_INT>=29)startForeground(ID,notification(true),ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(ID,notification(true));
  if(wake==null){wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Ricorda:active-alarm");wake.acquire(600000L);handler.postDelayed(this::stopSelf,600000L);}playLatest();
  return START_NOT_STICKY;
 }
 private Notification notification(boolean announce){
  String title=ringing.size()==1?ringing.values().iterator().next():Preferences.s(this,"ringing");
  PendingIntent open=AlarmActivity.pending(this);
  PendingIntent stop=PendingIntent.getBroadcast(this,ID,new Intent(this,ReminderReceiver.class).setAction("STOP_ALL"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent snooze=PendingIntent.getBroadcast(this,ID,new Intent(this,ReminderReceiver.class).setAction("SNOOZE_ALL"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder builder=new Notification.Builder(this,ReminderReceiver.PLAYBACK).setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(Preferences.s(this,"stop_alarm"))
   .setStyle(new Notification.BigTextStyle().bigText(String.join("\n",ringing.values()))).setContentIntent(open).setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PRIVATE)
   .setOngoing(true).setOnlyAlertOnce(!announce).setWhen(System.currentTimeMillis()).setShowWhen(true)
   .addAction(new Notification.Action.Builder(null,Preferences.s(this,ringing.size()>1?"stop_all_alarms":"stop_alarm"),stop).build())
   .addAction(new Notification.Action.Builder(null,Preferences.s(this,ringing.size()>1?"snooze_all_alarms":"snooze_alarm"),snooze).build());
  if(announce&&AlarmActivity.fullScreenAllowed(this))builder.setFullScreenIntent(open,true);return builder.build();
 }
 private void playLatest(){
  if(player!=null){player.release();player=null;}if(vibrator!=null)vibrator.cancel();if(focus!=null){getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);focus=null;}
  String channel=null;for(String id:soundChannels.keySet()){channel=soundChannels.get(id);gain=volumes.getOrDefault(id,40)/100f;}play(channel);
 }
 private void play(String channel){
  NotificationChannel ch=getSystemService(NotificationManager.class).getNotificationChannel(channel==null?ReminderReceiver.ALARM:channel);
  Uri sound=ch==null?RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM):ch.getSound();
  AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
  if(ch!=null&&ch.shouldVibrate()){vibrator=getSystemService(Vibrator.class);if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,600,600},0),attrs);}
  if(sound==null)return;
  focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs).setOnAudioFocusChangeListener(change->{if(player==null)return;try{if(change<0&&player.isPlaying())player.pause();else if(change>0)player.start();}catch(IllegalStateException ignored){}}).build();
  if(getSystemService(AudioManager.class).requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED)startPlayer(sound,attrs,true);
 }
 private void startPlayer(Uri uri,AudioAttributes attrs,boolean fallback){
  try{player=new MediaPlayer();player.setAudioAttributes(attrs);player.setVolume(gain,gain);player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);player.setDataSource(this,uri);player.setLooping(true);player.setOnPreparedListener(MediaPlayer::start);
   player.setOnErrorListener((p,w,e)->{p.release();player=null;if(fallback)startPlayer(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),attrs,false);return true;});player.prepareAsync();
  }catch(Exception e){if(player!=null){player.release();player=null;}if(fallback)startPlayer(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),attrs,false);}
 }
 @Override public void onDestroy(){for(String id:ringing.keySet())try{AlarmState.clearActive(this,id);}catch(Exception ignored){}handler.removeCallbacksAndMessages(null);if(player!=null){player.release();player=null;}if(vibrator!=null)vibrator.cancel();if(focus!=null)getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);if(wake!=null&&wake.isHeld())wake.release();stopForeground(STOP_FOREGROUND_REMOVE);active=null;super.onDestroy();}
}
