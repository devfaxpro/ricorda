package com.fabio.ricorda;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Only a user-created alarm notification may open this non-exported lock-screen UI. */
public final class AlarmActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());private String shown="";private ThemePalette palette;
 private final Runnable update=new Runnable(){public void run(){refresh();if(!isFinishing())handler.postDelayed(this,1000);}};
 public static boolean fullScreenAllowed(Context c){return Build.VERSION.SDK_INT<34||c.getSystemService(NotificationManager.class).canUseFullScreenIntent();}
 public static PendingIntent pending(Context c){Intent i=new Intent(c,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);Bundle options=null;if(Build.VERSION.SDK_INT>=35){ActivityOptions o=ActivityOptions.makeBasic();o.setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);options=o.toBundle();}return PendingIntent.getActivity(c,7102,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE,options);}
 @Override protected void attachBaseContext(Context c){super.attachBaseContext(Preferences.localized(c));}
 private String s(String k){return Preferences.s(this,k);}private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle b){boolean dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;setTheme(dark?R.style.AppTheme_Dark:R.style.AppTheme);AppFonts.applyTheme(this);ThemeUi.applyTheme(this);super.onCreate(b);palette=ThemeUi.palette(this);
  if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}else getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);ThemeUi.systemBars(getWindow(),palette,dark);refresh();
 }
 @Override protected void onResume(){super.onResume();handler.post(update);}@Override protected void onPause(){handler.removeCallbacks(update);super.onPause();}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);shown="";refresh();}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}private void gap(LinearLayout l,int n){l.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(n)));}
 private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(palette.ink);t.setTextSize(size);t.setTypeface(AppFonts.face(this,true));return t;}
 private GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));return d;}
 private Button button(String name,boolean primary,Runnable action){Button b=new Button(this);b.setText(name);b.setTextSize(16);b.setTypeface(AppFonts.face(this,false));b.setAllCaps(false);b.setMinHeight(dp(54));ThemeUi.button(b,palette,primary,16);b.setOnClickListener(v->{try{action.run();shown="";refresh();}catch(Exception e){Toast.makeText(this,s("retry"),Toast.LENGTH_LONG).show();}});return b;}
 private void refresh(){try{List<String> active=AlarmState.activeIds(this);if(active.isEmpty()){finish();return;}String signature=String.join(",",active);if(signature.equals(shown))return;shown=signature;LinearLayout body=column();body.setPadding(dp(24),dp(32),dp(24),dp(28));body.setBackgroundColor(palette.background);LinearLayout brand=new LinearLayout(this);brand.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);brand.setGravity(Gravity.CENTER_VERTICAL);TextView logo=text("ricorda",27);logo.setTextColor(palette.action);brand.addView(logo);View dot=new View(this);dot.setBackground(shape(palette.white?palette.action:palette.accent));LinearLayout.LayoutParams dotp=new LinearLayout.LayoutParams(dp(5),dp(5));dotp.setMargins(dp(3),dp(7),0,0);brand.addView(dot,dotp);body.addView(brand);gap(body,32);body.addView(text(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),48));gap(body,8);body.addView(text(s("ringing"),18));gap(body,30);
   for(Reminder r:Store.read(this))if(active.contains(r.id)){body.addView(text(r.name,26));if(!r.notes.isEmpty()){gap(body,10);body.addView(text(r.notes,16));}gap(body,24);body.addView(button(s("stop_alarm"),true,()->AlarmState.dismiss(this,r.id)));gap(body,12);body.addView(button(s("snooze_alarm"),false,()->{if(AlarmState.snooze(this,r.id))Toast.makeText(this,s("snoozed"),Toast.LENGTH_SHORT).show();}));gap(body,32);}
   ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(palette.background);scroll.addView(body);scroll.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});setContentView(scroll);scroll.requestApplyInsets();
  }catch(Exception e){Toast.makeText(this,s("retry"),Toast.LENGTH_LONG).show();finish();}}
}
