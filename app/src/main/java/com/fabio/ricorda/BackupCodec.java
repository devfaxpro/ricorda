package com.fabio.ricorda;

import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Portable data only. No permissions, executable content, or active alarm state. */
public final class BackupCodec {
 public static final int MAX_BYTES=8*1024*1024,MAX_ITEMS=10000;
 public static final class Snapshot {
  public final OffsetDateTime created;public final String device,origin;public final List<Reminder> items;
  private Snapshot(OffsetDateTime created,String device,String origin,List<Reminder> items){this.created=created;this.device=device;this.origin=origin;this.items=Collections.unmodifiableList(items);}
  public int active(){int count=0;for(Reminder r:items)if(!r.deleted)count++;return count;}
 }
 public static String encode(List<Reminder> items,OffsetDateTime created,String device)throws JSONException {
  return encode(items,created,device,"");
 }
 public static String encode(List<Reminder> items,OffsetDateTime created,String device,String origin)throws JSONException {
  JSONArray rows=new JSONArray();for(Reminder r:items)rows.put(r.json());
  return new JSONObject().put("format","ricorda-backup").put("version",1).put("appVersion","8.0").put("createdAt",created.toString()).put("device",device).put("origin",origin).put("items",rows).toString(2);
 }
 public static Snapshot read(InputStream input)throws IOException,JSONException {
  if(input==null)throw new IOException("No input");ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
  while((n=input.read(buffer))!=-1){if(bytes.size()+n>MAX_BYTES)throw new IOException("File too large");bytes.write(buffer,0,n);}return decode(StandardCharsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(bytes.toByteArray())).toString());
 }
 private static void require(boolean ok)throws JSONException {if(!ok)throw new JSONException("Invalid backup");}
 private static String text(JSONObject o,String key,int max,boolean required)throws JSONException {if(!o.has(key)){require(!required);return "";}Object value=o.get(key);require(value instanceof String&&((String)value).length()<=max);return (String)value;}
 private static void flag(JSONObject o,String key)throws JSONException {if(o.has(key))require(o.get(key) instanceof Boolean);}
 private static void dateRange(LocalDate date)throws JSONException {require(date.getYear()>=1900&&date.getYear()<=2200);}
 public static Snapshot decode(String value)throws JSONException {
  require(value.getBytes(StandardCharsets.UTF_8).length<=MAX_BYTES);
  int depth=0;boolean quoted=false,escaped=false;for(int i=0;i<value.length();i++){char ch=value.charAt(i);if(quoted){if(escaped)escaped=false;else if(ch=='\\')escaped=true;else if(ch=='"')quoted=false;}else if(ch=='"')quoted=true;else if(ch=='{'||ch=='['){require(++depth<=16);}else if(ch=='}'||ch==']')depth--;}
  try{
   JSONTokener tokener=new JSONTokener(value.startsWith("\uFEFF")?value.substring(1):value);Object parsed=tokener.nextValue();require(parsed instanceof JSONObject&&tokener.nextClean()==0);JSONObject document=(JSONObject)parsed;
   require("ricorda-backup".equals(document.getString("format")));require(document.get("version") instanceof Number&&document.getDouble("version")==1);
   OffsetDateTime created=OffsetDateTime.parse(text(document,"createdAt",64,true));String device=text(document,"device",200,false),origin=text(document,"origin",36,false);
   JSONArray rows=document.getJSONArray("items");require(rows.length()<=MAX_ITEMS);List<Reminder> items=new ArrayList<>();Set<String> ids=new HashSet<>();
   for(int i=0;i<rows.length();i++){
    JSONObject row=rows.getJSONObject(i);String id=text(row,"id",36,true);require(UUID.fromString(id).toString().equals(id)&&ids.add(id));require(!text(row,"name",120,true).trim().isEmpty());text(row,"notes",600,false);text(row,"category",120,false);
    String sound=text(row,"sound",4096,false);require(sound.isEmpty()||sound.equals("silent")||sound.startsWith("content://")||sound.startsWith("android.resource://"));
    String repeat=text(row,"repeat",20,true);require(Arrays.asList("none","daily","weekly","monthly","yearly","calendar").contains(repeat));
    String notice=text(row,"notice",20,false);require(notice.isEmpty()||Arrays.asList("legacy","discreet","banner").contains(notice));
    if(row.has("volume")){Object volume=row.get("volume");require(volume instanceof Number&&((Number)volume).doubleValue()==((Number)volume).intValue()&&row.getInt("volume")>=10&&row.getInt("volume")<=100);}
    for(String key:new String[]{"alarm","deleted","delivered","vibrate"})flag(row,key);
    dateRange(LocalDateTime.parse(text(row,"anchor",40,true)).toLocalDate());dateRange(LocalDateTime.parse(text(row,"next",40,true)).toLocalDate());
    JSONArray dates=row.optJSONArray("dates");require(!row.has("dates")||dates!=null);if(dates!=null){require(dates.length()<=10000);Set<String> unique=new HashSet<>();for(int d=0;d<dates.length();d++){require(dates.get(d) instanceof String);String day=dates.getString(d);require(day.length()==10&&unique.add(day));dateRange(LocalDate.parse(day));}}if(repeat.equals("calendar"))require(dates!=null&&dates.length()>0);
    items.add(Reminder.from(row));
   }
   return new Snapshot(created,device,origin,items);
  }catch(IllegalArgumentException|DateTimeException e){throw new JSONException("Invalid backup: "+e.getClass().getSimpleName());}
 }
 public static String fileName(OffsetDateTime time,int count,String label){return "Ricorda_"+time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss",Locale.ROOT))+"_"+count+"-"+label.replaceAll("[^\\p{L}\\p{N}_-]","")+".json";}
 public static Reminder prepare(Reminder original,LocalDateTime now)throws JSONException {
  Reminder r=Reminder.from(original.json());if(!r.deleted&&!r.delivered&&!r.next.isAfter(now)){LocalDateTime future=r.nextAfter(now);if(future==null)r.delivered=true;else r.next=future;}return r;
 }
 public static List<Reminder> combine(List<Reminder> current,List<Reminder> imported,boolean replace,LocalDateTime now)throws JSONException {
  LinkedHashMap<String,Reminder> result=new LinkedHashMap<>();if(!replace)for(Reminder r:current)result.put(r.id,r);
  for(Reminder r:imported)if(!result.containsKey(r.id))result.put(r.id,prepare(r,now));return new ArrayList<>(result.values());
 }
 private BackupCodec(){}
}
