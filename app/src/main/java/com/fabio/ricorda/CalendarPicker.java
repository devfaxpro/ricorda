package com.fabio.ricorda;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class CalendarPicker extends Dialog {
 private final TreeSet<String> selected;
 private final Runnable changed;
 private final int accent,ink,muted,surface,soft,onAccent;
 private final Locale locale;
 private final ThemePalette theme;
 private int year,month=-1;
 private LinearLayout frame;
 private LocalDate focused;
 private boolean single,restoreDayFocus;
 private java.util.function.Consumer<YearMonth> monthChosen;
 private java.util.function.Predicate<LocalDate> hasEvent;
 public CalendarPicker(Context c,TreeSet<String> dates,LocalDate start,int accent,int ink,int muted,int surface,int soft,Runnable changed){
  super(c);AppFonts.applyTheme(getContext());this.selected=dates;this.changed=changed;this.accent=accent;this.ink=ink;this.muted=muted;this.surface=surface;this.soft=soft;
  theme=ThemeUi.palette(c);onAccent=theme.onAccent;
  year=start.getYear();focused=start;locale=Preferences.locale(c);
 }
 public CalendarPicker singleDate(){single=true;month=focused.getMonthValue()-1;return this;}
 public CalendarPicker browseMonths(java.util.function.Predicate<LocalDate> hasEvent,java.util.function.Consumer<YearMonth> chosen){this.hasEvent=hasEvent;this.monthChosen=chosen;return this;}
 private String s(String k){return Preferences.s(getContext(),k);}
 private int dp(float n){return Math.round(n*getContext().getResources().getDisplayMetrics().density);}
 private float sp(float n){return n*getContext().getResources().getDisplayMetrics().scaledDensity;}
 @Override protected void onCreate(android.os.Bundle b){super.onCreate(b);getWindow().setBackgroundDrawableResource(android.R.color.transparent);render();}
 @Override public void show(){super.show();getWindow().setLayout(-1,-1);}
 private LinearLayout col(){LinearLayout l=new LinearLayout(getContext());l.setOrientation(LinearLayout.VERTICAL);return l;}
 private TextView text(String t,int size){TextView v=new TextView(getContext());v.setText(t);v.setTextSize(size);v.setTypeface(AppFonts.face(getContext(),false));v.setTextColor(ink);v.setPadding(0,dp(5),0,dp(5));return v;}
 private GradientDrawable shape(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private Button button(String t,boolean primary,Runnable action){Button b=new Button(getContext());b.setText(t);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(AppFonts.face(getContext(),false));b.setTextColor(primary?onAccent:theme.action);b.setMinHeight(dp(50));b.setMinimumWidth(0);b.setMinWidth(0);b.setPadding(dp(14),dp(9),dp(14),dp(9));b.setBackgroundTintList(null);ThemeUi.button(b,theme,primary,14);b.setOnClickListener(v->action.run());return b;}
 private Button arrow(String symbol,String label,Runnable action){if(LanguageCatalog.isRtl(Preferences.language(getContext())))symbol=symbol.equals("‹")?"›":"‹";Button b=button(symbol,false,action);b.setTextSize(27);b.setBackgroundColor(Color.TRANSPARENT);b.setContentDescription(label);b.setPadding(0,0,0,0);return b;}
 private void gap(LinearLayout target,int height){target.addView(new Space(getContext()),new LinearLayout.LayoutParams(1,dp(height)));}
 private void footer(String title,Runnable action){gap(frame,12);frame.addView(button(title,true,action),new LinearLayout.LayoutParams(-1,dp(52)));}
 private void render(){
  frame=col();frame.setLayoutDirection(LanguageCatalog.isRtl(Preferences.language(getContext()))?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);frame.setBackgroundColor(surface);frame.setPadding(dp(10),dp(12),dp(10),dp(12));
  frame.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(10)+i.getSystemWindowInsetLeft(),dp(12)+i.getSystemWindowInsetTop(),dp(10)+i.getSystemWindowInsetRight(),dp(12)+i.getSystemWindowInsetBottom());return i;});setContentView(frame);frame.requestApplyInsets();
  LinearLayout head=new LinearLayout(getContext());head.setGravity(Gravity.CENTER_VERTICAL);head.addView(arrow("‹",s("back"),()->{if(month>=0){month=-1;render();}else dismiss();}),new LinearLayout.LayoutParams(dp(44),dp(48)));
  head.addView(text(s(monthChosen!=null?"choose_month":single?"choose_single_date":"choose_dates"),22),new LinearLayout.LayoutParams(0,-2,1));frame.addView(head);
  if(month<0)renderYear();else renderMonth();if(android.os.Build.VERSION.SDK_INT>=28)frame.setAccessibilityPaneTitle(s(month<0?"all_months":single?"choose_single_date":"choose_dates"));
 }
 private void renderYear(){
  LinearLayout nav=new LinearLayout(getContext());nav.setGravity(Gravity.CENTER_VERTICAL);
  nav.addView(arrow("‹",s("previous_year"),()->{if(year>1900)year--;render();}),new LinearLayout.LayoutParams(dp(44),dp(44)));
  TextView title=text(String.valueOf(year),23);title.setGravity(Gravity.CENTER);nav.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  nav.addView(arrow("›",s("next_year"),()->{if(year<2200)year++;render();}),new LinearLayout.LayoutParams(dp(44),dp(44)));frame.addView(nav);
  ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(true);LinearLayout grid=col();grid.setMinimumHeight(dp(480));scroll.addView(grid);
  for(int row=0;row<4;row++){LinearLayout line=new LinearLayout(getContext());for(int column=0;column<3;column++){final int m=row*3+column;Tile tile=new Tile(getContext(),m);StringBuilder description=new StringBuilder(DateLabels.format(YearMonth.of(year,m+1).atDay(1),locale,"yMMMM"));for(String date:selected){LocalDate chosen=LocalDate.parse(date);if(chosen.getYear()==year&&chosen.getMonthValue()==m+1)description.append(" · ").append(s("selected")).append(" ").append(DateLabels.format(chosen,locale,"MMMMd"));}AccessibilityUi.button(tile,description.toString(),false);tile.setOnClickListener(v->{if(monthChosen!=null){monthChosen.accept(YearMonth.of(year,m+1));dismiss();}else{month=m;focused=LocalDate.of(year,m+1,1);render();}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(2),dp(4),dp(2),dp(4));line.addView(tile,lp);}grid.addView(line,new LinearLayout.LayoutParams(-1,0,1));}frame.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(monthChosen==null){if(!single)frame.addView(text(Preferences.selectedDates(getContext(),selected.size()),13));footer(s("confirm_dates"),()->{changed.run();dismiss();});}else footer(s("close"),this::dismiss);
 }
 private void renderMonth(){
  TextView monthTitle=text(DateLabels.format(YearMonth.of(year,month+1).atDay(1),locale,"yMMMM"),24);monthTitle.setPadding(dp(8),dp(4),dp(8),dp(12));frame.addView(monthTitle);
  ScrollView scroll=new ScrollView(getContext());LinearLayout inner=col();scroll.addView(inner);frame.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));inner.addView(text(s(single?"choose_date_help":"choose_days_help"),13));
  YearMonth ym=YearMonth.of(year,month+1);int offset=CalendarLocale.offset(locale,ym.atDay(1));LinearLayout week=new LinearLayout(getContext());
  for(int i=1;i<=7;i++){TextView day=text(CalendarLocale.day(locale,i-1).getDisplayName(java.time.format.TextStyle.NARROW,locale),12);day.setGravity(Gravity.CENTER);day.setTextColor(muted);week.addView(day,new LinearLayout.LayoutParams(0,dp(35),1));}inner.addView(week);
  for(int row=0;row<(offset+ym.lengthOfMonth()+6)/7;row++){LinearLayout line=new LinearLayout(getContext());for(int column=0;column<7;column++){int n=row*7+column-offset+1;if(n<1||n>ym.lengthOfMonth()){line.addView(new Space(getContext()),new LinearLayout.LayoutParams(0,dp(68),1));continue;}LocalDate d=ym.atDay(n);LinearLayout cell=col();cell.setGravity(Gravity.CENTER);TextView number=text(String.valueOf(n),17);number.setGravity(Gravity.CENTER);TextView saint=text(Saints.shortName(getContext(),d),11);saint.setSingleLine();saint.setGravity(Gravity.CENTER);saint.setTextColor(muted);cell.addView(number);if(Saints.hasData(getContext(),d))cell.addView(saint,new LinearLayout.LayoutParams(-1,-2));
    GradientDrawable shape=shape(selected.contains(d.toString())?soft:surface,18);if(selected.contains(d.toString()))shape.setStroke(dp(2),theme.white?theme.border:accent);cell.setBackground(shape);cell.setContentDescription(DateLabels.format(d,locale,"yMMMMd")+Saints.suffix(getContext(),d));AccessibilityUi.button(cell,cell.getContentDescription(),selected.contains(d.toString()));if(restoreDayFocus&&focused.equals(d)){AccessibilityUi.restoreFocus(cell);restoreDayFocus=false;}cell.setOnClickListener(v->{focused=d;restoreDayFocus=true;if(single){selected.clear();selected.add(d.toString());}else if(!selected.add(d.toString()))selected.remove(d.toString());changed.run();render();});line.addView(cell,new LinearLayout.LayoutParams(0,dp(68),1));}inner.addView(line);}
  gap(inner,14);inner.addView(text(DateLabels.format(focused,locale,"MMMMd")+Saints.suffix(getContext(),focused),16));if(!single)inner.addView(text(Preferences.selectedDates(getContext(),selected.size()),13));footer(s("all_months"),()->{month=-1;render();});
 }
 @Override public void onBackPressed(){if(month>=0){month=-1;render();}else super.onBackPressed();}
 private final class Tile extends View {
  private final int m;Tile(Context c,int month){super(c);m=month;setFocusable(true);setClickable(true);setBackground(shape(soft,12));}
  @Override protected void onDraw(Canvas canvas){
   super.onDraw(canvas);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);YearMonth ym=YearMonth.of(year,m+1);p.setColor(ink);p.setTypeface(AppFonts.face(getContext(),true));p.setTextSize(Math.min(sp(12),getWidth()*.12f));String title=MonthLabels.format(ym.atDay(1),locale,"LLLL");if(p.measureText(title)>getWidth()-dp(10))title=MonthLabels.format(ym.atDay(1),locale,"LLL");boolean rtl=LanguageCatalog.isRtl(locale.toLanguageTag());p.setTextAlign(rtl?Paint.Align.RIGHT:Paint.Align.LEFT);canvas.drawText(title,rtl?getWidth()-dp(5):dp(5),dp(18),p);
   float cell=(getWidth()-dp(6))/7f,rowHeight=(getHeight()-dp(40))/6f;p.setTypeface(AppFonts.face(getContext(),false));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.min(sp(9),cell*.76f));p.setColor(muted);
   for(int i=1;i<=7;i++)canvas.drawText(CalendarLocale.day(locale,i-1).getDisplayName(java.time.format.TextStyle.NARROW,locale),dp(3)+cell*(CalendarLocale.visualColumn(i-1,LanguageCatalog.isRtl(locale.toLanguageTag()))+.5f),dp(31),p);
   p.setTextSize(Math.min(sp(11),cell*.83f));int offset=CalendarLocale.offset(locale,ym.atDay(1));
   for(int d=1;d<=ym.lengthOfMonth();d++){int at=offset+d-1;float x=dp(3)+cell*(CalendarLocale.visualColumn(at%7,LanguageCatalog.isRtl(locale.toLanguageTag()))+.5f),centerY=dp(38)+rowHeight*(at/7+.5f),baseline=centerY-(p.ascent()+p.descent())/2;
    if(selected.contains(ym.atDay(d).toString())||(hasEvent!=null&&hasEvent.test(ym.atDay(d)))){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(theme.white?theme.border:accent);canvas.drawCircle(x,centerY,Math.min(cell,rowHeight)*.46f,p);p.setStyle(Paint.Style.FILL);}p.setColor(ink);canvas.drawText(String.valueOf(d),x,baseline,p);
   }
  }
 }
}
