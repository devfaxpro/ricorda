package com.fabio.ricorda;
import android.appwidget.*;
import android.content.*;
import android.os.Bundle;
public final class AgendaWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids){WidgetUpdater.refresh(c);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager manager,int id,Bundle options){WidgetUpdater.refresh(c);}
    @Override public void onDisabled(Context c){WidgetUpdater.refresh(c);}
}
