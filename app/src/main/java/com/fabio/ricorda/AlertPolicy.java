package com.fabio.ricorda;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
/** Immutable channel identities reflect explicit user choices, never a random refresh. */
public final class AlertPolicy {
 public static boolean quiet(Reminder r){return !r.alarm&&"discreet".equals(r.notice);}
 public static boolean vibrates(Reminder r){return !quiet(r)&&("legacy".equals(r.notice)?r.alarm:r.vibrate);}
 public static String channel(Reminder r,String effectiveSound){
  String base=r.alarm?"alarms_v1":"reminders_v1";
  if("legacy".equals(r.notice)||(!quiet(r)&&vibrates(r)==r.alarm))return r.sound.isEmpty()?base:base+"_sound_"+UUID.nameUUIDFromBytes(r.sound.getBytes(StandardCharsets.UTF_8));
  String sound=quiet(r)?"silent":effectiveSound;
  return "alerts_v8_"+(r.alarm?"alarm":quiet(r)?"discreet":"banner")+"_"+(vibrates(r)?"v1":"v0")+"_"+UUID.nameUUIDFromBytes(sound.getBytes(StandardCharsets.UTF_8));
 }
 private AlertPolicy(){}
}
