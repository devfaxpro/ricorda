package com.fabio.ricorda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.WeakHashMap;

public final class ThemeUi {
    private ThemeUi(){}
    private static final WeakHashMap<View,String> applied=new WeakHashMap<>();
    public static ThemePalette palette(Context c){
        boolean dark=(Preferences.localized(c).getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        return ThemePalette.of(Preferences.get(c,"color","sage"),dark);
    }
    public static void applyTheme(Context c){
        ThemePalette p=palette(c);
        int style=c.getResources().getIdentifier("ThemeColor_"+p.id,"style",c.getPackageName());
        if(style!=0)c.getTheme().applyStyle(style,true);
    }
    public static void systemBars(android.view.Window window,ThemePalette p,boolean dark){
        window.setStatusBarColor(p.background);
        // Android 8.0 only supports light navigation icons, so retain a dark surface there.
        window.setNavigationBarColor(Build.VERSION.SDK_INT>=27||dark?p.background:p.ink);
        int flags=dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(!dark&&Build.VERSION.SDK_INT>=27)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        window.getDecorView().setSystemUiVisibility(flags);
    }
    private static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    public static GradientDrawable shape(Context c,int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d;}
    public static void button(Button b,ThemePalette p,boolean primary,int radius){
        b.setBackgroundTintList(null);
        b.setTextColor(primary?p.onAccent:p.action);
        GradientDrawable fill=shape(b.getContext(),primary?p.accent:p.soft,radius);
        if(primary&&p.white)fill.setStroke(dp(b.getContext(),1),p.border);
        b.setBackground(fill);
        b.setBackgroundTintList(null);
    }
    public static void switchStyle(Switch control,ThemePalette p){
        Context c=control.getContext();
        control.setTrackTintList(null);control.setThumbTintList(null);
        control.setTrackDrawable(new SwitchArtwork(c,p,false));control.setThumbDrawable(new SwitchArtwork(c,p,true));
        control.setSplitTrack(false);control.setShowText(false);control.setSwitchMinWidth(dp(c,56));control.setMinHeight(dp(c,48));
    }
    public static void applyNative(View root){applyNative(root,palette(root.getContext()));}
    private static void applyNative(View v,ThemePalette p){
        String signature=p.id+":"+p.surface;
        if(!signature.equals(applied.get(v))){
            int control=p.white?p.action:p.accent;
            if(v instanceof EditText){
                EditText e=(EditText)v;e.setTextColor(p.ink);e.setHintTextColor(p.muted);
                e.setBackgroundTintList(e.getBackground() instanceof GradientDrawable?null:ColorStateList.valueOf(p.action));
                e.setHighlightColor(((p.white?p.action:p.accent)&0x00ffffff)|0x55000000);
                if(Build.VERSION.SDK_INT>=29&&e.getTextCursorDrawable()!=null){android.graphics.drawable.Drawable d=e.getTextCursorDrawable().mutate();d.setTint(p.action);e.setTextCursorDrawable(d);}
            }else if(v instanceof Switch){switchStyle((Switch)v,p);}
            else if(v instanceof CompoundButton){((CompoundButton)v).setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{control,p.muted}));}
            else if(v instanceof CheckedTextView){((CheckedTextView)v).setCheckMarkTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{control,p.muted}));}
            if(v instanceof Button&&(v.getId()==android.R.id.button1||v.getId()==android.R.id.button2||v.getId()==android.R.id.button3))((Button)v).setTextColor(p.action);
            applied.put(v,signature);
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)applyNative(g.getChildAt(i),p);}
    }
}
