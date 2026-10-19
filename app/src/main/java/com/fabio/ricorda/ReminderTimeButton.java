package com.fabio.ricorda;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** One accessible button with the clock and digits centered together. */
final class ReminderTimeButton extends LinearLayout {
    private final TextView digits;
    ReminderTimeButton(Context context,LocalTime time){
        super(context);ThemePalette palette=ThemeUi.palette(context);
        setOrientation(HORIZONTAL);setLayoutDirection(LAYOUT_DIRECTION_LTR);setGravity(Gravity.CENTER);
        setPadding(dp(18),dp(14),dp(18),dp(14));setMinimumHeight(dp(72));
        setBackground(ThemeUi.shape(context,palette.soft,16));
        ImageView clock=new ImageView(context);clock.setImageResource(R.drawable.ic_time_soft);clock.setImageTintList(android.content.res.ColorStateList.valueOf(palette.action));
        addView(clock,new LayoutParams(dp(28),dp(28)));
        digits=new TextView(context);digits.setTextSize(28);digits.setTextColor(palette.ink);digits.setTypeface(AppFonts.face(context,false));digits.setFontFeatureSettings("tnum");digits.setLetterSpacing(.025f);digits.setTextDirection(TEXT_DIRECTION_LTR);
        LayoutParams size=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);size.leftMargin=dp(16);addView(digits,size);
        setClickable(true);setTime(time);
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    void setTime(LocalTime time){
        String value=time.format(DateTimeFormatter.ofPattern("HH:mm",Preferences.locale(getContext())));digits.setText(value);
        AccessibilityUi.button(this,Preferences.s(getContext(),"time_label")+" · "+value,false);
    }
}
