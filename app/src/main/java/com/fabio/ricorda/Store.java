package com.fabio.ricorda;

import android.content.Context;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public final class Store {
    private Store() {}
    public static synchronized List<Reminder> read(Context c) {
        try {
            JSONArray a=new JSONArray(c.getSharedPreferences("ricorda",0).getString("items","[]"));
            List<Reminder> list=new ArrayList<>();
            for(int i=0;i<a.length();i++) list.add(Reminder.from(a.getJSONObject(i)));
            return list;
        } catch(Exception e) { throw new IllegalStateException("Non riesco a leggere i promemoria salvati.", e); }
    }
    public static synchronized void write(Context c,List<Reminder> list) {
        try {
            JSONArray a=new JSONArray(); for(Reminder r:list) a.put(r.json());
            android.content.SharedPreferences prefs=c.getSharedPreferences("ricorda",0);
            String before=prefs.getString("items","[]");
            if(!prefs.edit().putString("items",a.toString()).commit()) {
                // A failed disk commit still updates SharedPreferences in memory.
                // Restore the previous contents before reporting failure to the user.
                prefs.edit().putString("items",before).commit();
                throw new IllegalStateException("Spazio insufficiente per salvare.");
            }
        } catch(Exception e) { throw new IllegalStateException("Salvataggio non riuscito. Riprova.",e); }
        WidgetUpdater.refresh(c);
    }
}
