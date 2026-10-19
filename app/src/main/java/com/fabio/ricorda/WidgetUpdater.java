package com.fabio.ricorda;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.util.SizeF;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class WidgetUpdater extends BroadcastReceiver {
    static PendingIntent open(Context c,String action,String value){
        Intent i=new Intent(c,MainActivity.class).setData(new Uri.Builder().scheme("ricorda").authority("widget").appendPath(action).appendPath(value).build())
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(action.equals("show"))i.putExtra("show",value);else if(action.equals("saint"))i.putExtra("saint_day",value);else i.putExtra("widget_action",action);
        return PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static String s(Context c,String key){return Preferences.s(c,key);}
    private static RemoteViews base(Context c,int layout,LocalDate today,ThemePalette p){
        RemoteViews v=new RemoteViews(c.getPackageName(),layout);
        v.setInt(R.id.widget_root,"setLayoutDirection",LanguageCatalog.isRtl(Preferences.language(c))?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        v.setInt(R.id.widget_brand,"setTextDirection",View.TEXT_DIRECTION_LTR);
        v.setInt(R.id.widget_background,"setColorFilter",p.surface);v.setTextColor(R.id.widget_brand,p.action);
        v.setTextColor(R.id.widget_date,p.muted);v.setTextColor(R.id.widget_title,p.ink);
        v.setTextViewText(R.id.widget_date,DateLabels.format(today,Preferences.locale(c),"EMMMd"));
        return v;
    }
    private interface SizedView { RemoteViews build(int width,int height); }
    private static RemoteViews variants(Bundle options,SizedView factory){
        if(Build.VERSION.SDK_INT>=31){
            ArrayList<SizeF> sizes=options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
            if(sizes!=null&&!sizes.isEmpty()){
                Map<SizeF,RemoteViews> layouts=new LinkedHashMap<>();
                for(SizeF size:sizes){
                    if(size!=null&&Float.isFinite(size.getWidth())&&Float.isFinite(size.getHeight())&&size.getWidth()>0&&size.getHeight()>0){
                        layouts.put(size,factory.build(Math.round(size.getWidth()),Math.round(size.getHeight())));if(layouts.size()==16)break;
                    }
                }
                if(!layouts.isEmpty())return new RemoteViews(layouts);
            }
        }
        // Older launchers report ranges: portrait is narrow/tall, landscape wide/short.
        int narrow=Math.max(1,options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,250));
        int shortSide=Math.max(1,options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,180));
        int wide=Math.max(narrow,options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,narrow));
        int tall=Math.max(shortSide,options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,shortSide));
        return new RemoteViews(factory.build(wide,shortSide),factory.build(narrow,tall));
    }
    private static int px(Context c,int dp){return Math.round(dp*c.getResources().getDisplayMetrics().density);}
    private static WidgetLayout layout(Context c,int width,int height){return WidgetLayout.of(width,height,c.getResources().getConfiguration().fontScale);}
    private static void fit(Context c,RemoteViews v,WidgetLayout layout){
        int pad=px(c,layout.padding);v.setViewPadding(R.id.widget_content,pad,pad,pad,pad);
        v.setViewVisibility(R.id.widget_brand,layout.brand?View.VISIBLE:View.GONE);
        v.setViewVisibility(R.id.widget_date,layout.date?View.VISIBLE:View.GONE);
    }
    private static RemoteViews agenda(Context c,LocalDate today,ThemePalette p,List<Reminder> list,boolean failed,int width,int height){
        WidgetLayout layout=layout(c,width,height);int count=Math.min(list.size(),layout.rows);
        RemoteViews v=base(c,R.layout.widget_agenda,today,p);fit(c,v,layout);
        v.setViewVisibility(R.id.widget_meta,layout.brand||layout.date?View.VISIBLE:View.GONE);
        v.setTextViewText(R.id.widget_title,s(c,"today")+(list.isEmpty()?"":" · "+list.size()));
        v.setTextViewTextSize(R.id.widget_title,TypedValue.COMPLEX_UNIT_SP,layout.titleSp);
        v.setOnClickPendingIntent(R.id.widget_header,open(c,"today",""));v.setOnClickPendingIntent(R.id.widget_root,open(c,"today",""));
        v.setTextViewText(R.id.widget_empty,s(c,failed?"read_error":"no_day_events"));v.setTextColor(R.id.widget_empty,p.muted);
        v.setTextViewTextSize(R.id.widget_empty,TypedValue.COMPLEX_UNIT_SP,layout.bodySp);
        v.setViewVisibility(R.id.widget_empty,list.isEmpty()&&layout.bodyHeight>=32?View.VISIBLE:View.GONE);
        int[] rows={R.id.widget_row1,R.id.widget_row2,R.id.widget_row3,R.id.widget_row4,R.id.widget_row5,R.id.widget_row6};
        for(int n=0;n<rows.length;n++){
            v.setViewVisibility(rows[n],n<count?View.VISIBLE:View.GONE);
            if(n<count){Reminder r=list.get(n);String detail=r.anchor.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))+"  "+r.name;
                v.setTextViewText(rows[n],detail);v.setTextColor(rows[n],p.ink);v.setTextViewTextSize(rows[n],TypedValue.COMPLEX_UNIT_SP,layout.bodySp);
                v.setInt(rows[n],"setHeight",px(c,layout.rowHeight));v.setContentDescription(rows[n],detail);v.setOnClickPendingIntent(rows[n],open(c,"show",r.id));}
        }
        v.setViewVisibility(R.id.widget_footer,layout.footer?View.VISIBLE:View.GONE);
        v.setTextViewText(R.id.widget_all,s(c,"widget_view_today"));v.setTextColor(R.id.widget_all,p.action);v.setInt(R.id.widget_all,"setHeight",px(c,layout.footerHeight));v.setOnClickPendingIntent(R.id.widget_all,open(c,"today",""));
        v.setImageViewResource(R.id.widget_add_background,p.white?R.drawable.widget_add_white:R.drawable.widget_add_shape);
        // Transparent SRC_ATOP also neutralizes a tint left by a previously selected color.
        v.setInt(R.id.widget_add_background,"setColorFilter",p.white?Color.TRANSPARENT:p.accent);
        v.setTextColor(R.id.widget_add,p.onAccent);v.setContentDescription(R.id.widget_add,s(c,"new_reminder"));v.setOnClickPendingIntent(R.id.widget_add,open(c,"new",""));
        return v;
    }
    private static RemoteViews saint(Context c,LocalDate today,ThemePalette p,int width,int height){
        WidgetLayout layout=layout(c,width,height);RemoteViews v=base(c,R.layout.widget_saint,today,p);fit(c,v,layout);
        boolean italian=Saints.enabled(c);String name=Saints.name(c,today);String detail=italian?(name.isEmpty()?s(c,"saint_data_unavailable"):name):s(c,"widget_italian_only");
        v.setTextViewText(R.id.widget_title,s(c,"saint_daily_title"));v.setTextColor(R.id.widget_saint_name,p.action);
        v.setViewVisibility(R.id.widget_title,layout.saintTitle?View.VISIBLE:View.GONE);
        v.setTextViewText(R.id.widget_saint_name,detail);v.setTextViewTextSize(R.id.widget_saint_name,TypedValue.COMPLEX_UNIT_SP,layout.saintSp);
        v.setInt(R.id.widget_saint_name,"setMaxLines",layout.saintLines);
        v.setTextViewText(R.id.widget_saint_link,s(c,italian?"calendar_view":"settings"));v.setTextColor(R.id.widget_saint_link,p.action);v.setViewVisibility(R.id.widget_saint_link,layout.saintLink?View.VISIBLE:View.GONE);
        v.setContentDescription(R.id.widget_root,s(c,"saint_daily_title")+" · "+today+" · "+detail);
        v.setOnClickPendingIntent(R.id.widget_root,italian?open(c,"saint",today.toString()):open(c,"settings",""));return v;
    }
    public static void refresh(Context c){
        try {
            AppWidgetManager manager=AppWidgetManager.getInstance(c);
            int[] agendaIds=manager.getAppWidgetIds(new ComponentName(c,AgendaWidget.class));
            int[] saintIds=manager.getAppWidgetIds(new ComponentName(c,SaintWidget.class));
            if(agendaIds.length+saintIds.length==0){midnight(c,false);return;}
            LocalDate today=LocalDate.now();ThemePalette p=ThemeUi.palette(c);
            if(agendaIds.length>0){
                List<Reminder> read;boolean readFailed=false;
                try{read=WidgetAgenda.today(Store.read(c),today);}catch(RuntimeException e){read=Collections.emptyList();readFailed=true;}
                final List<Reminder> list=read;final boolean failed=readFailed;
                for(int id:agendaIds)manager.updateAppWidget(id,variants(manager.getAppWidgetOptions(id),(w,h)->agenda(c,today,p,list,failed,w,h)));
            }
            for(int id:saintIds)manager.updateAppWidget(id,variants(manager.getAppWidgetOptions(id),(w,h)->saint(c,today,p,w,h)));
            midnight(c,true);
        }catch(RuntimeException e){android.util.Log.e("Ricorda","Widget update failed",e);}
    }
    private static void midnight(Context c,boolean active){
        PendingIntent pending=PendingIntent.getBroadcast(c,0,new Intent(c,WidgetUpdater.class).setAction("com.fabio.ricorda.WIDGET_MIDNIGHT"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager=c.getSystemService(AlarmManager.class);
        if(!active){manager.cancel(pending);return;}
        // A visual refresh does not need to wake the phone or use an exact alarm.
        long next=LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        manager.set(AlarmManager.RTC,next,pending);
    }
    @Override public void onReceive(Context c,Intent i){refresh(c);}
}
