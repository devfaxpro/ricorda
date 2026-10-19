package com.fabio.ricorda;

import android.app.*;
import android.content.Context;
import android.text.*;
import android.view.Gravity;
import android.widget.*;
import java.time.LocalDate;
import java.util.function.Consumer;

public final class SaintSearchDialog {
 public static void show(Context c,int accent,int ink,int soft,int initialYear,Consumer<LocalDate> picked){
  if(!Saints.enabled(c))return;
  float density=c.getResources().getDisplayMetrics().density;int pad=Math.round(18*density);
  int[] year={Math.max(SaintCalendar.FIRST.getYear(),Math.min(SaintCalendar.LAST.getYear(),initialYear))};
  LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(pad,pad,pad,pad);
  LinearLayout nav=new LinearLayout(c);nav.setGravity(Gravity.CENTER_VERTICAL);
  Button previous=new Button(c),next=new Button(c);previous.setText("‹");next.setText("›");
  previous.setPadding(0,0,0,0);next.setPadding(0,0,0,0);previous.setMinWidth(0);next.setMinWidth(0);
  previous.setContentDescription(Preferences.s(c,"previous_year"));next.setContentDescription(Preferences.s(c,"next_year"));
  ThemeUi.button(previous,ThemeUi.palette(c),false,12);ThemeUi.button(next,ThemeUi.palette(c),false,12);
  TextView yearLabel=new TextView(c);yearLabel.setGravity(Gravity.CENTER);yearLabel.setTextSize(21);yearLabel.setTextColor(ink);AccessibilityUi.heading(yearLabel);
  nav.addView(previous,new LinearLayout.LayoutParams(Math.round(48*density),Math.round(48*density)));nav.addView(yearLabel,new LinearLayout.LayoutParams(0,-2,1));nav.addView(next,new LinearLayout.LayoutParams(Math.round(48*density),Math.round(48*density)));body.addView(nav);
  EditText search=new EditText(c);search.setSingleLine();search.setTextSize(16);search.setTextColor(ink);search.setHint(Preferences.s(c,"saint_search_hint"));body.addView(search);
  TextView help=new TextView(c);help.setText(Preferences.s(c,"saint_search_help"));help.setTextColor(ink);help.setPadding(0,pad/2,0,pad/2);body.addView(help);
  ScrollView scroll=new ScrollView(c);LinearLayout results=new LinearLayout(c);results.setOrientation(LinearLayout.VERTICAL);scroll.addView(results);body.addView(scroll,new LinearLayout.LayoutParams(-1,Math.round(310*density)));
  AlertDialog dialog=new AlertDialog.Builder(c).setTitle(Preferences.s(c,"saint_search")).setView(body).setNegativeButton(Preferences.s(c,"close"),null).create();
  Runnable render=()->{
   yearLabel.setText(String.valueOf(year[0]));previous.setEnabled(year[0]>SaintCalendar.FIRST.getYear());next.setEnabled(year[0]<SaintCalendar.LAST.getYear());results.removeAllViews();
   String query=search.getText().toString();if(SaintText.searchable(query).length()<2)return;
   for(SaintCalendar.Entry entry:Saints.search(c,query,year[0])){
    Button choice=new Button(c);choice.setAllCaps(false);choice.setTextColor(ink);choice.setTextSize(14);choice.setTypeface(AppFonts.face(c,false));choice.setText(DateLabels.format(entry.date,Preferences.locale(c),"yMMMMd")+"\n"+entry.name);choice.setMinHeight(Math.round(64*density));ThemeUi.button(choice,ThemeUi.palette(c),false,12);
    LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,-2);size.bottomMargin=Math.round(8*density);results.addView(choice,size);choice.setOnClickListener(v->{picked.accept(entry.date);dialog.dismiss();});
   }
   if(results.getChildCount()==0){TextView no=new TextView(c);no.setTextColor(ink);no.setText(Preferences.s(c,"saint_no_results"));results.addView(no);}
  };
  previous.setOnClickListener(v->{if(year[0]>SaintCalendar.FIRST.getYear()){year[0]--;render.run();}});
  next.setOnClickListener(v->{if(year[0]<SaintCalendar.LAST.getYear()){year[0]++;render.run();}});
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int n,int f){}public void afterTextChanged(Editable e){render.run();}public void onTextChanged(CharSequence q,int a,int b,int n){}});
  render.run();dialog.setOnShowListener(d->AppFonts.watch(dialog));dialog.show();
 }
 private SaintSearchDialog(){}
}
