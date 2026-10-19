package com.fabio.ricorda;

import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public final class Reminder {
    public String id = UUID.randomUUID().toString();
    public String name = "", notes = "", category = "Personale", repeat = "none";
    public String sound = ""; // Empty means the default channel sound; silent means no sound.
    public int volume=40;
    public String notice="legacy";
    public boolean vibrate;
    public java.util.TreeSet<String> dates=new java.util.TreeSet<>();
    public LocalDateTime nextAfter(LocalDateTime now){return repeat.equals("calendar")?Recurrence.nextSelected(dates,anchor.toLocalTime(),now):Recurrence.nextAfter(anchor,repeat,now);}
    public boolean occurs(java.time.LocalDate day){return !deleted && Recurrence.occurs(day,anchor,repeat,dates);}
    public void recordDelivery(LocalDateTime now,boolean snoozed){
        if(snoozed)return;
        LocalDateTime future=nextAfter(now);
        if(future==null)delivered=true;else next=future;
    }
    public LocalDateTime anchor, next;
    public boolean alarm, deleted, delivered;
    public long millis() { return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(); }
    public JSONObject json() throws JSONException {
        return new JSONObject().put("id",id).put("name",name).put("notes",notes)
            .put("sound",sound).put("volume",volume).put("notice",notice).put("vibrate",vibrate).put("dates",new org.json.JSONArray(dates)).put("category",category).put("repeat",repeat).put("anchor",anchor.toString())
            .put("next",next.toString()).put("alarm",alarm).put("deleted",deleted).put("delivered",delivered);
    }
    public static Reminder from(JSONObject o) throws JSONException {
        Reminder r = new Reminder();
        r.id=o.getString("id"); r.name=o.getString("name"); r.notes=o.optString("notes");
        r.sound=o.optString("sound",""); r.category=o.optString("category","Personale"); r.repeat=o.optString("repeat","none");
        r.volume=Math.max(10,Math.min(100,o.optInt("volume",40)));org.json.JSONArray dates=o.optJSONArray("dates");if(dates!=null)for(int i=0;i<dates.length();i++)r.dates.add(java.time.LocalDate.parse(dates.getString(i)).toString());
        r.anchor=LocalDateTime.parse(o.getString("anchor")); r.next=LocalDateTime.parse(o.getString("next"));
        r.alarm=o.optBoolean("alarm"); r.deleted=o.optBoolean("deleted"); r.delivered=o.optBoolean("delivered");
        r.notice=o.optString("notice","legacy");r.vibrate=o.optBoolean("vibrate",r.alarm);
        return r;
    }
}
