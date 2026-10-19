package com.fabio.ricorda;
import java.time.LocalDate;
import java.util.*;

public final class WidgetAgenda {
    public static List<Reminder> today(List<Reminder> all,LocalDate day){
        List<Reminder> result=new ArrayList<>();
        for(Reminder r:all)if(r.occurs(day))result.add(r);
        result.sort(Comparator.comparing((Reminder r)->r.anchor.toLocalTime()).thenComparing(r->r.name,String.CASE_INSENSITIVE_ORDER).thenComparing(r->r.id));
        return result;
    }
    private WidgetAgenda(){}
}
