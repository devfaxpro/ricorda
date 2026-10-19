package com.fabio.ricorda;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.time.LocalTime;
import java.util.Locale;
import java.util.function.Consumer;

/** A small native time dialog, following the app's current palette and typeface. */
public final class SoftTimePicker {
    private final Activity context;
    private final ThemePalette theme;
    private final int accent,onAccent,ink,muted,surface,soft;
    private SoftTimePicker(Activity c,int a,int on,int i,int m,int s,int tint){context=c;theme=ThemeUi.palette(c);accent=a;onAccent=on;ink=i;muted=m;surface=s;soft=tint;}
    private int dp(float n){return Math.round(n*context.getResources().getDisplayMetrics().density);}
    private String s(String key){return Preferences.s(context,key);}
    private GradientDrawable shape(int color,int radius,boolean stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke)d.setStroke(dp(2),theme.white?theme.border:accent);return d;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(context);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setTypeface(AppFonts.face(context,bold));return t;}
    private Button button(String value,boolean primary){Button b=new Button(context);b.setText(value);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(AppFonts.face(context,true));ThemeUi.button(b,theme,primary,16);b.setMinHeight(dp(48));b.setMinimumWidth(0);b.setMinWidth(0);b.setPadding(dp(12),dp(12),dp(12),dp(12));return b;}
    private EditText field(int value,String label,int action){
        EditText e=new EditText(context);e.setText(String.format(Locale.ROOT,"%02d",value));e.setTextSize(34);e.setTextColor(ink);e.setGravity(Gravity.CENTER);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});e.setSingleLine();e.setSelectAllOnFocus(true);e.setMinimumWidth(0);e.setMinWidth(0);e.setPadding(dp(8),dp(8),dp(8),dp(8));
        e.setTypeface(AppFonts.face(context,true));e.setHint(label);e.setImeOptions(action|EditorInfo.IME_FLAG_NO_EXTRACT_UI);e.setBackground(shape(soft,18,false));
        android.graphics.Paint.FontMetrics metrics=e.getPaint().getFontMetrics();
        e.setMinHeight(Math.max(dp(72),(int)Math.ceil(metrics.bottom-metrics.top)+dp(16)));
        e.setBackgroundTintList(null);e.setOnFocusChangeListener((v,focused)->e.setBackground(shape(soft,18,focused)));
        if(Build.VERSION.SDK_INT>=29&&e.getTextCursorDrawable()!=null){android.graphics.drawable.Drawable cursor=e.getTextCursorDrawable().mutate();cursor.setTint(theme.action);e.setTextCursorDrawable(cursor);}
        ThemeUi.applyNative(e);return e;
    }
    private LinearLayout cell(String label,EditText field){LinearLayout cell=new LinearLayout(context);cell.setOrientation(LinearLayout.VERTICAL);cell.addView(field,new LinearLayout.LayoutParams(-1,-2));TextView caption=text(label,14,muted,false);caption.setGravity(Gravity.CENTER);caption.setPadding(0,dp(8),0,0);AccessibilityUi.label(caption,field);cell.addView(caption);return cell;}
    private Dialog build(LocalTime initial,Consumer<LocalTime> selected){
        Dialog dialog=new Dialog(context);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int horizontalPadding=dp(context.getResources().getConfiguration().screenWidthDp<=360?20:24);
        LinearLayout panel=new LinearLayout(context);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(horizontalPadding,dp(24),horizontalPadding,dp(24));panel.setBackground(shape(surface,28,false));panel.setFocusableInTouchMode(true);
        TextView title=text(s("time_pick_title"),25,ink,true);if(Build.VERSION.SDK_INT>=28)title.setAccessibilityHeading(true);panel.addView(title);
        TextView hint=text(s("time_pick_hint"),15,muted,false);hint.setPadding(0,dp(10),0,dp(22));panel.addView(hint);
        EditText hour=field(initial.getHour(),s("time_hours"),EditorInfo.IME_ACTION_NEXT),minute=field(initial.getMinute(),s("time_minutes"),EditorInfo.IME_ACTION_DONE);
        hour.setId(View.generateViewId());minute.setId(View.generateViewId());hour.setNextFocusForwardId(minute.getId());
        // Keep the approved compact row, allowing more width for enlarged Android text.
        LinearLayout digits=new LinearLayout(context){
            @Override protected void onMeasure(int widthSpec,int heightSpec){
                int desired=dp(236*Math.max(1f,getResources().getConfiguration().fontScale));
                int available=View.MeasureSpec.getMode(widthSpec)==View.MeasureSpec.UNSPECIFIED?desired:View.MeasureSpec.getSize(widthSpec);
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(Math.min(desired,available),View.MeasureSpec.EXACTLY),heightSpec);
            }
        };
        digits.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);digits.setGravity(Gravity.TOP);digits.setOrientation(LinearLayout.HORIZONTAL);
        digits.addView(cell(s("time_hours"),hour),new LinearLayout.LayoutParams(0,-2,1));
        TextView colon=text(":",28,theme.action,true);colon.setGravity(Gravity.CENTER);colon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        digits.addView(colon,new LinearLayout.LayoutParams(dp(24),hour.getMinHeight()));
        digits.addView(cell(s("time_minutes"),minute),new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams digitParams=new LinearLayout.LayoutParams(-2,-2);digitParams.gravity=Gravity.CENTER_HORIZONTAL;panel.addView(digits,digitParams);
        TextView error=text("",13,muted,false);error.setPadding(0,dp(14),0,0);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);error.setVisibility(View.GONE);panel.addView(error);
        TextWatcher clearError=new TextWatcher(){
            public void beforeTextChanged(CharSequence value,int start,int count,int after){}
            public void onTextChanged(CharSequence value,int start,int before,int count){error.setVisibility(View.GONE);}
            public void afterTextChanged(Editable value){}
        };
        hour.addTextChangedListener(clearError);minute.addTextChangedListener(clearError);
        LinearLayout actions=new LinearLayout(context);actions.setOrientation(LinearLayout.VERTICAL);
        Button cancel=button(s("cancel"),false),confirm=button(s("time_set"),true);
        actions.addView(confirm,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams below=new LinearLayout.LayoutParams(-1,-2);below.topMargin=dp(9);actions.addView(cancel,below);
        LinearLayout.LayoutParams actionParams=new LinearLayout.LayoutParams(-1,-2);actionParams.topMargin=dp(24);panel.addView(actions,actionParams);
        Runnable accept=()->{
            LocalTime value=TimeEntry.parse(hour.getText().toString(),minute.getText().toString());
            if(value==null){boolean invalidHour=TimeEntry.number(hour.getText().toString(),23)<0;error.setText(s(invalidHour?"time_hour_error":"time_minute_error"));error.setVisibility(View.VISIBLE);EditText input=invalidHour?hour:minute;input.requestFocus();input.selectAll();return;}
            InputMethodManager keyboard=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);keyboard.hideSoftInputFromWindow(hour.getWindowToken(),0);dialog.dismiss();selected.accept(value);
        };
        cancel.setOnClickListener(v->dialog.cancel());confirm.setOnClickListener(v->accept.run());
        hour.setOnEditorActionListener((view,action,event)->{if(action==EditorInfo.IME_ACTION_NEXT){minute.requestFocus();minute.selectAll();return true;}return false;});
        minute.setOnEditorActionListener((view,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){accept.run();return true;}return false;});
        ScrollView scroll=new ScrollView(context);scroll.setFillViewport(false);scroll.setBackground(shape(surface,28,false));scroll.addView(panel);dialog.setContentView(scroll);panel.requestFocus();
        Window window=dialog.getWindow();window.setBackgroundDrawableResource(android.R.color.transparent);window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);window.setDimAmount(.28f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setOnShowListener(d->{
            int width=Math.min(dp(350),context.getResources().getDisplayMetrics().widthPixels-dp(32));window.setLayout(width,WindowManager.LayoutParams.WRAP_CONTENT);window.setGravity(Gravity.CENTER);
        });dialog.show();return dialog;
    }
    public static Dialog show(Activity c,LocalTime initial,int accent,int onAccent,int ink,int muted,int surface,int soft,Consumer<LocalTime> selected){return new SoftTimePicker(c,accent,onAccent,ink,muted,surface,soft).build(initial,selected);}
}
