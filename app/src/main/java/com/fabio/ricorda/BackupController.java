package com.fabio.ricorda;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;

/** The Android document picker controls where the user reads and writes backups. */
public final class BackupController {
 private static final int EXPORT=61,IMPORT=62;
 private final Activity activity;private final Runnable changed;
 public BackupController(Activity activity,Runnable changed){this.activity=activity;this.changed=changed;}
 private String s(String key){return Preferences.s(activity,key);}
 private void toast(String key){Toast.makeText(activity,s(key),Toast.LENGTH_LONG).show();}
 private void ui(Runnable work){activity.runOnUiThread(()->{if(!activity.isFinishing()&&!activity.isDestroyed())work.run();});}
 private void background(Runnable work){Thread thread=new Thread(work,"Ricorda-backup");thread.start();}
 private String origin(){String id=Preferences.get(activity,"backup_origin","");if(id.isEmpty()){id=UUID.randomUUID().toString();Preferences.put(activity,"backup_origin",id);}return id;}
 public void exportFile(){try{int count=Store.read(activity).size();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(Intent.EXTRA_TITLE,BackupCodec.fileName(OffsetDateTime.now(),count,s("backup_filename")));activity.startActivityForResult(i,EXPORT);}catch(Exception e){toast("backup_export_error");}}
 public void importFile(){try{activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"),IMPORT);}catch(Exception e){toast("backup_import_error");}}
 public boolean result(int request,int result,Intent data){
  if(request!=EXPORT&&request!=IMPORT)return false;if(result!=Activity.RESULT_OK||data==null||data.getData()==null)return true;Uri uri=data.getData();
  if(request==EXPORT){try{List<Reminder> snapshot=Store.read(activity);OffsetDateTime created=OffsetDateTime.now();background(()->{try{String encoded=BackupCodec.encode(snapshot,created,Build.MANUFACTURER+" "+Build.MODEL,origin());BackupCodec.decode(encoded);try(OutputStream output=activity.getContentResolver().openOutputStream(uri,"wt")){if(output==null)throw new IOException("No output");output.write(encoded.getBytes(StandardCharsets.UTF_8));output.flush();}ui(()->toast("backup_exported"));}catch(Exception e){ui(()->toast("backup_export_error"));}});}catch(Exception e){toast("backup_export_error");}}
  else background(()->{try(InputStream input=activity.getContentResolver().openInputStream(uri)){BackupCodec.Snapshot snapshot=BackupCodec.read(input);ui(()->preview(snapshot));}catch(Exception e){ui(()->toast("backup_import_error"));}});
  return true;
 }
 private void preview(BackupCodec.Snapshot snapshot){
  Locale locale=Preferences.locale(activity);String date=snapshot.created.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,FormatStyle.SHORT).withLocale(locale));
  String summary=String.format(locale,s("backup_summary"),date,snapshot.device.isEmpty()?"—":snapshot.device,snapshot.active(),snapshot.items.size()-snapshot.active())+"\n\n"+s("backup_merge_help");
  AppFonts.show(new AlertDialog.Builder(activity).setTitle(s("backup_import")).setMessage(summary).setPositiveButton(s("backup_add"),(d,w)->apply(snapshot,false)).setNeutralButton(s("backup_replace"),(d,w)->AppFonts.show(new AlertDialog.Builder(activity).setTitle(s("backup_replace")).setMessage(s("backup_replace_warning")).setPositiveButton(s("backup_replace"),(x,y)->apply(snapshot,true)).setNegativeButton(s("cancel"),null))).setNegativeButton(s("cancel"),null));
 }
 private void apply(BackupCodec.Snapshot snapshot,boolean replace){
  boolean saved=false;
  try{
   List<Reminder> before=Store.read(activity);Set<String> existing=new HashSet<>();for(Reminder r:before)existing.add(r.id);
   List<Reminder> after=BackupCodec.combine(before,snapshot.items,replace,LocalDateTime.now());List<Reminder> added=new ArrayList<>();
   for(Reminder r:after)if(replace||!existing.contains(r.id)){restoreSound(r,snapshot.origin.equals(origin()));added.add(r);}
   // Commit first: cancellation must never destroy current schedules if storage is full.
   Store.write(activity,after);saved=true;if(!after.isEmpty())Preferences.prefs(activity).edit().putBoolean("has_created",true).apply();
   if(replace)for(Reminder r:before)Scheduler.cancel(activity,r);
   for(Reminder r:added)Scheduler.schedule(activity,r);
   changed.run();toast("backup_imported");
  }catch(Exception e){if(saved)changed.run();toast(saved?"backup_schedule_error":"backup_import_error");}
 }
 private void restoreSound(Reminder r,boolean sameInstallation){
  if(r.sound.isEmpty()||r.sound.equals("silent"))return;
  try{Uri uri=Uri.parse(r.sound);if(RingtoneManager.isDefault(uri))return;if(!sameInstallation){r.sound="";return;}
   // External backups cannot grant permission to arbitrary content providers.
   if(!"content".equals(uri.getScheme())||!"media".equals(uri.getAuthority())){r.sound="";return;}
   try(android.content.res.AssetFileDescriptor fd=activity.getContentResolver().openAssetFileDescriptor(uri,"r")){if(fd==null)r.sound="";}
  }catch(Exception e){r.sound="";}
 }
}
