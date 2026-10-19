package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.io.*;
import java.time.*;
import java.util.*;

public class BackupCodecTest {
 private final OffsetDateTime created=OffsetDateTime.parse("2026-10-10T18:30:05+02:00");
 private final LocalDateTime now=LocalDateTime.parse("2026-10-10T19:00");
 private Reminder reminder(){Reminder r=new Reminder();r.name="Compleanno di Giulia";r.notes="Torta, caffè e 日本語 🎂";r.anchor=LocalDateTime.parse("2026-10-11T09:00");r.next=r.anchor;r.alarm=true;r.notice="banner";r.vibrate=false;r.volume=35;return r;}
 private String file(Reminder... reminders)throws Exception{return BackupCodec.encode(Arrays.asList(reminders),created,"Telefono di prova","11111111-1111-1111-1111-111111111111");}
 private void invalid(String file)throws Exception{try{BackupCodec.decode(file);fail("Invalid file accepted");}catch(JSONException expected){}}
 @Test public void roundTripPreservesAllReminderFieldsAndMetadata()throws Exception{Reminder r=reminder();r.repeat="calendar";r.dates.add("2026-10-11");r.dates.add("2026-12-25");r.sound="content://media/internal/audio/media/12";BackupCodec.Snapshot backup=BackupCodec.decode(file(r));Reminder copy=backup.items.get(0);assertEquals(r.json().toString(),copy.json().toString());assertEquals(created,backup.created);assertEquals("Telefono di prova",backup.device);assertEquals(36,backup.origin.length());}
 @Test public void trashAndDeliveryStatusArePreserved()throws Exception{Reminder r=reminder();r.deleted=true;r.delivered=true;BackupCodec.Snapshot b=BackupCodec.decode(file(r));assertEquals(0,b.active());assertTrue(b.items.get(0).deleted);assertTrue(b.items.get(0).delivered);}
 @Test public void emptyBackupIsValid()throws Exception{assertTrue(BackupCodec.decode(file()).items.isEmpty());}
 @Test public void filenameIdentifiesDateTimeAndCount(){assertEquals("Ricorda_2026-10-10_18-30-05_12-promemoria.json",BackupCodec.fileName(created,12,"promemoria"));}
 @Test public void foreignFilesAndFutureVersionsAreRejected()throws Exception{invalid("{}");JSONObject obj=new JSONObject(file(reminder()));obj.put("version",2);invalid(obj.toString());obj.put("version",1);obj.put("format","other-app");invalid(obj.toString());}
 @Test public void duplicateIdsWithinFileAreRejected()throws Exception{Reminder r=reminder();invalid(file(r,r));}
 @Test public void truncatedFilesAndTrailingGarbageAreRejected()throws Exception{String valid=file(reminder());invalid(valid.substring(0,valid.length()-4));invalid(valid+"garbage");}
 @Test public void oneInvalidRowRejectsEntireBackup()throws Exception{JSONObject obj=new JSONObject(file(reminder(),reminder()));obj.getJSONArray("items").getJSONObject(1).put("anchor","2026-02-30T10:00");invalid(obj.toString());}
 @Test public void arbitrarySoundProtocolsAreRejected()throws Exception{Reminder r=reminder();r.sound="https://example.org/sound";invalid(file(r));}
 @Test public void wrongTypesAndInvalidVolumesAreRejected()throws Exception{JSONObject obj=new JSONObject(file(reminder()));obj.getJSONArray("items").getJSONObject(0).put("alarm","true");invalid(obj.toString());obj.getJSONArray("items").getJSONObject(0).put("alarm",true).put("volume",0);invalid(obj.toString());}
 @Test public void unknownRecurrenceAndMissingCustomDatesAreRejected()throws Exception{Reminder r=reminder();r.repeat="minute";invalid(file(r));r.repeat="calendar";invalid(file(r));}
 @Test public void unicodeTextAndBracesInsideNotesRemainData()throws Exception{Reminder r=reminder();r.notes="{test} [data] \"quoted\" È 日本語 🎂";assertEquals(r.notes,BackupCodec.decode(file(r)).items.get(0).notes);}
 @Test public void hugeAndDeeplyNestedFilesAreRejected()throws Exception{char[] huge=new char[BackupCodec.MAX_BYTES+1];Arrays.fill(huge,' ');invalid(new String(huge));invalid("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[0]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");}
 @Test public void invalidUtf8IsRejected()throws Exception{try{BackupCodec.read(new ByteArrayInputStream(new byte[]{(byte)0xc3,0x28}));fail();}catch(IOException expected){}}
 @Test public void importingTwiceDoesNotCreateDuplicates()throws Exception{Reminder r=reminder();List<Reminder> first=BackupCodec.combine(Collections.emptyList(),Arrays.asList(r),false,now);assertEquals(1,BackupCodec.combine(first,Arrays.asList(r),false,now).size());}
 @Test public void mergeKeepsCurrentVersionOfExistingReminder()throws Exception{Reminder old=reminder(),current=Reminder.from(old.json());current.notes="La nota aggiornata";List<Reminder> result=BackupCodec.combine(Arrays.asList(current),Arrays.asList(old),false,now);assertSame(current,result.get(0));assertEquals("La nota aggiornata",result.get(0).notes);}
 @Test public void separateRemindersWithSameNameRemainSeparate()throws Exception{assertEquals(2,BackupCodec.combine(Arrays.asList(reminder()),Arrays.asList(reminder()),false,now).size());}
 @Test public void replaceUsesOnlyImportedItems()throws Exception{Reminder incoming=reminder();List<Reminder> result=BackupCodec.combine(Arrays.asList(reminder()),Arrays.asList(incoming),true,now);assertEquals(1,result.size());assertEquals(incoming.id,result.get(0).id);}
 @Test public void pastOneOffDoesNotRingOnImport()throws Exception{Reminder r=reminder();r.anchor=now.minusDays(1);r.next=r.anchor;Reminder copy=BackupCodec.prepare(r,now);assertTrue(copy.delivered);assertFalse(r.delivered);}
 @Test public void pastRepeatingAlarmMovesToNextOccurrence()throws Exception{Reminder r=reminder();r.repeat="daily";r.anchor=now.minusDays(2);r.next=r.anchor;Reminder copy=BackupCodec.prepare(r,now);assertEquals(now.plusDays(1),copy.next);assertEquals(r.anchor,copy.anchor);assertFalse(copy.delivered);}
 @Test public void selectedDatesKeepOnlyFutureNextOccurrence()throws Exception{Reminder r=reminder();r.repeat="calendar";r.anchor=now.minusDays(1);r.next=r.anchor;r.dates.add("2026-10-09");r.dates.add("2026-10-12");assertEquals(LocalDateTime.parse("2026-10-12T19:00"),BackupCodec.prepare(r,now).next);}
 @Test public void importDoesNotRearmDeletedOrDeliveredItems()throws Exception{Reminder r=reminder();r.deleted=true;r.next=now.minusDays(2);assertEquals(r.next,BackupCodec.prepare(r,now).next);r.deleted=false;r.delivered=true;assertTrue(BackupCodec.prepare(r,now).delivered);}
 @Test public void legacyRemindersGetCompatibleNotificationDefaults()throws Exception{JSONObject old=reminder().json();old.remove("notice");old.remove("vibrate");Reminder restored=Reminder.from(old);assertEquals("legacy",restored.notice);assertTrue(AlertPolicy.vibrates(restored));}
}
