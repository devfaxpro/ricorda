package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
public class AlertPolicyTest {
 private Reminder reminder(String style,boolean alarm,boolean vibration){Reminder r=new Reminder();r.notice=style;r.alarm=alarm;r.vibrate=vibration;return r;}
 @Test public void discreetSuppressesSoundIdentityAndVibration(){Reminder r=reminder("discreet",false,true);assertTrue(AlertPolicy.quiet(r));assertFalse(AlertPolicy.vibrates(r));assertEquals(AlertPolicy.channel(r,"sound1"),AlertPolicy.channel(r,"sound2"));}
 @Test public void turningOnAlarmOverridesDiscreetMode(){Reminder r=reminder("discreet",true,true);assertFalse(AlertPolicy.quiet(r));assertTrue(AlertPolicy.vibrates(r));assertEquals("alarms_v1",AlertPolicy.channel(r,"default"));}
 @Test public void legacyChannelIdsRemainStable(){assertEquals("reminders_v1",AlertPolicy.channel(reminder("legacy",false,false),"default"));assertEquals("alarms_v1",AlertPolicy.channel(reminder("legacy",true,false),"default"));}
 @Test public void bannerVibrationCanBeEnabledAndDisabled(){Reminder r=reminder("banner",false,false);String before=AlertPolicy.channel(r,"default");r.vibrate=true;assertTrue(AlertPolicy.vibrates(r));assertNotEquals(before,AlertPolicy.channel(r,"default"));}
 @Test public void alarmVibrationCanBeDisabled(){Reminder r=reminder("banner",true,false);assertFalse(AlertPolicy.vibrates(r));assertNotEquals("alarms_v1",AlertPolicy.channel(r,"default"));}
 @Test public void sameOptionsReuseSameChannel(){Reminder a=reminder("banner",false,true),b=reminder("banner",false,true);assertEquals(AlertPolicy.channel(a,"tone"),AlertPolicy.channel(b,"tone"));}
 @Test public void differentTonesDoNotOverwriteEachOthersChannel(){Reminder a=reminder("banner",false,true);assertNotEquals(AlertPolicy.channel(a,"tone1"),AlertPolicy.channel(a,"tone2"));}
}
