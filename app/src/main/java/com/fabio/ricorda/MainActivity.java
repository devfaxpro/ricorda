package com.fabio.ricorda;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class MainActivity extends Activity {
 private int ink,accent,muted,bg,soft,surface,onAccent,action;
 private ThemePalette theme;
 private String page="home",query="",editingId;
 private boolean expanded;
 private boolean restoreCalendarFocus;
 private LinearLayout body,listArea;
 private AlertDialog editor;
 private EditText nameInput,notesInput;
 private Spinner repeatInput;
 private String chosenSound="", retainedCategory="Personale";
 private Button soundButton;
 private Switch alarmInput,vibrationInput;
 private Spinner noticeInput;
 private BackupController backups;
 private int chosenVolume=40;
 private android.media.MediaPlayer previewPlayer;
 private boolean previewDragging;
 private TreeSet<String> chosenDates=new TreeSet<>();
 private Button calendarButton;
 private LocalDate selectedDay=LocalDate.now();
 private YearMonth visibleMonth=YearMonth.now();
 private int listFilter;
 private LocalDate searchDay;
 private final String[] filterKeys={"filter_all","filter_upcoming","filter_today","filter_alarms","filter_repeating","filter_past"};
 private LocalDate chosenDate;
 private LocalTime chosenTime;
 private final String[] categories={"Personale","Compleanno","Scadenza","Anniversario","Appuntamento"};
 private final String[] categoryKeys={"personal","birthday","deadline","anniversary","appointment"};
 private final String[] repeatKeys={"none","daily","weekly","monthly","yearly","calendar"};
 private final String[] colors=ThemePalette.IDS;
 private final String[] modes={"light","dark","system"};
 private final Handler refreshHandler=new Handler(Looper.getMainLooper());
 private final Runnable refresh= new Runnable(){public void run(){if((editor==null||!editor.isShowing())&&!AccessibilityUi.touchExploration(MainActivity.this))renderList();refreshHandler.postDelayed(this,30000);}};
 @Override protected void attachBaseContext(Context base){super.attachBaseContext(Preferences.localized(base));}
 private String s(String key){return Preferences.s(this,key);}
 private Locale locale(){return Preferences.locale(this);}
 private String date(LocalDate d){return DateLabels.format(d,locale(),"yMMMMd");}
 private String time(LocalTime t){return t.format(DateTimeFormatter.ofPattern("HH:mm",locale()));}
 @Override public void onCreate(Bundle state){
  boolean dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
  setTheme(dark?R.style.AppTheme_Dark:R.style.AppTheme);AppFonts.applyTheme(this);super.onCreate(state);palette(dark);ReminderReceiver.channels(this);backups=new BackupController(this,()->{page="home";render();});
  if(state!=null){page=state.getString("page","home");query=state.getString("query","");expanded=state.getBoolean("expanded");listFilter=state.getInt("filter",0);String savedSearchDay=state.getString("searchDay");if(savedSearchDay!=null)searchDay=LocalDate.parse(savedSearchDay);selectedDay=LocalDate.parse(state.getString("selectedDay",LocalDate.now().toString()));visibleMonth=YearMonth.parse(state.getString("visibleMonth",YearMonth.now().toString()));}
  render();
  if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->{if(!page.equals("home"))go("home");else finish();});
  if(state!=null&&state.getBoolean("editor"))try{openEditor(null,Reminder.from(new org.json.JSONObject(state.getString("draft"))));editingId=state.getString("editingId");}catch(Exception e){toast(s("retry"));}
  showLaunchIntent();askAtStartup();
 }
 @Override protected void onResume(){super.onResume();try{Scheduler.reschedule(this);render();}catch(Exception e){error(e);}SaintDailyReceiver.schedule(this);WidgetUpdater.refresh(this);refreshHandler.postDelayed(refresh,30000);}
 @Override protected void onPause(){refreshHandler.removeCallbacks(refresh);stopPreview();super.onPause();}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);showLaunchIntent();}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("page",page);out.putString("query",query);out.putBoolean("expanded",expanded);out.putInt("filter",listFilter);if(searchDay!=null)out.putString("searchDay",searchDay.toString());out.putString("selectedDay",selectedDay.toString());out.putString("visibleMonth",visibleMonth.toString());if(editor!=null&&editor.isShowing())try{out.putBoolean("editor",true);out.putString("draft",draft().json().toString());out.putString("editingId",editingId);}catch(Exception ignored){}}
 private void showLaunchIntent(){
  String action=getIntent().getStringExtra("widget_action");getIntent().removeExtra("widget_action");
  if(action!=null){
   if(action.equals("new")){if(editor==null||!editor.isShowing())openEditor(null,null);}
   else if(action.equals("today")){page="all";query="";listFilter=2;searchDay=null;render();}
   else if(action.equals("settings"))go("settings");
  }
  String saintDay=getIntent().getStringExtra("saint_day");getIntent().removeExtra("saint_day");
  if(saintDay!=null&&Saints.enabled(this))try{
   selectedDay=LocalDate.parse(saintDay);visibleMonth=YearMonth.from(selectedDay);page="calendar";render();
   getSystemService(NotificationManager.class).cancel("saint_daily",1);
   AppFonts.show(new AlertDialog.Builder(this).setTitle(date(selectedDay)).setMessage(Saints.hasData(this,selectedDay)?Saints.name(this,selectedDay):s("saint_data_unavailable")).setPositiveButton(s("ok"),null));
  }catch(java.time.format.DateTimeParseException ignored){}

  if(getIntent().getBooleanExtra("ringing",false)){getIntent().removeExtra("ringing");AppFonts.show(new AlertDialog.Builder(this).setTitle(s("ringing")).setMessage(s("alarm_help")).setPositiveButton(s("stop_alarm"),(d,w)->AlarmService.stopAll(this)).setNegativeButton(s("close"),null));}
  String id=getIntent().getStringExtra("show");if(id==null)return;getIntent().removeExtra("show");getSystemService(NotificationManager.class).cancel(id,1);
  try{for(Reminder r:Store.read(this))if(r.id.equals(id)&&!r.deleted){AppFonts.show(new AlertDialog.Builder(this).setTitle(r.name).setMessage(r.notes.isEmpty()?s("reminder_now"):r.notes).setPositiveButton(s("ok"),null).setNeutralButton(s("edit"),(d,w)->openEditor(r,null)));break;}}catch(Exception e){error(e);}
 }
 private void palette(boolean dark){
  theme=ThemePalette.of(Preferences.get(this,"color","sage"),dark);
  accent=theme.accent;onAccent=theme.onAccent;action=theme.action;
  bg=theme.background;surface=theme.surface;ink=theme.ink;muted=theme.muted;soft=theme.soft;
  ThemeUi.systemBars(getWindow(),theme,dark);
 }
 private int blend(int a,int b,float f){return Color.rgb((int)(Color.red(a)*(1-f)+Color.red(b)*f),(int)(Color.green(a)*(1-f)+Color.green(b)*f),(int)(Color.blue(a)*(1-f)+Color.blue(b)*f));}
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setTypeface(AppFonts.face(this,bold));t.setLineSpacing(dp(3),1);if(bold&&size>=24)AccessibilityUi.heading(t);return t;}
 private void gap(LinearLayout l,int n){l.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(n)));}
 private Button button(String label,boolean primary,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(AppFonts.face(this,false));ThemeUi.button(b,theme,primary,12);b.setPadding(dp(14),dp(10),dp(14),dp(10));b.setMinHeight(dp(48));b.setOnClickListener(v->action.run());if(label.startsWith("＋"))b.setContentDescription(s("new_reminder"));return b;}
 private void icon(LinearLayout row,int resource,String label,Runnable action){ImageButton b=new ImageButton(this);b.setImageResource(resource);b.setImageTintList(ColorStateList.valueOf(this.action));b.setContentDescription(label);b.setBackground(shape(soft,14));b.setPadding(dp(12),dp(12),dp(12),dp(12));b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(48),dp(48));p.setMarginStart(dp(8));row.addView(b,p);}
 private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_LONG).show();}
 private void error(Exception e){android.util.Log.e("Ricorda","Operation failed",e);toast(s("retry"));}
 private void go(String dest){page=dest;query="";expanded=false;render();}

 private String previousArrow(){return LanguageCatalog.isRtl(Preferences.language(this))?"›":"‹";}
 private String nextArrow(){return LanguageCatalog.isRtl(Preferences.language(this))?"‹":"›";}
 private void render(){
  listArea=null;LinearLayout root=column();root.setLayoutDirection(LanguageCatalog.isRtl(Preferences.language(this))?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);root.setBackgroundColor(bg);
  root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);body=column();body.setPadding(dp(20),dp(20),dp(20),dp(28));scroll.addView(body,new android.widget.FrameLayout.LayoutParams(getResources().getConfiguration().screenWidthDp>=600?dp(600):-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  setContentView(root);root.requestApplyInsets();
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);LinearLayout brand=new LinearLayout(this);brand.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);brand.setGravity(Gravity.CENTER_VERTICAL);brand.addView(text("ricorda",30,action,true));View dot=new View(this);dot.setBackground(shape(action,20));LinearLayout.LayoutParams dotp=new LinearLayout.LayoutParams(dp(6),dp(6));dotp.setMargins(dp(3),dp(7),0,0);brand.addView(dot,dotp);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  icon(top,R.drawable.ic_brush,s("appearance"),()->go("appearance"));fontIcon(top);icon(top,R.drawable.ic_settings,s("settings"),()->go("settings"));body.addView(top);gap(body,22);
  if(page.equals("privacy")){privacy();return;}if(page.equals("fonts")){fonts();return;}if(page.equals("settings")){settings();return;}if(page.equals("appearance")){appearance();return;}
  body.addView(text(DateLabels.format(LocalDate.now(),locale(),"EMMMMd"),15,muted,false));
  boolean calendarPage=page.equals("home")||page.equals("calendar");
  gap(body,12);
  if(!calendarPage){body.addView(text(s(page.equals("trash")?"trash_title":"agenda_title"),27,ink,true));gap(body,7);body.addView(text(s("agenda_subtitle"),14,muted,false));gap(body,18);body.addView(button("＋  "+s("new_reminder"),true,()->openEditor(null,null)));gap(body,18);}
  LinearLayout tabs=new LinearLayout(this);String[] dest={"home","all","trash"},labels={"calendar_view","list_view","trash"};for(int i=0;i<3;i++){final String target=dest[i];Button b=button(s(labels[i]),page.equals(target)||(page.equals("calendar")&&target.equals("home")),()->go(target));b.setSelected(page.equals(target)||(page.equals("calendar")&&target.equals("home")));b.setMinWidth(0);b.setMinimumWidth(0);b.setTextSize(13);b.setMaxLines(2);b.setPadding(dp(4),dp(8),dp(4),dp(8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(2),0,dp(2),0);tabs.addView(b,lp);}body.addView(tabs);gap(body,18);
  if(!page.equals("home")&&!page.equals("calendar")){EditText search=input(s("search"),120,false);search.setText(query);body.addView(search);gap(body,10);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence t,int a,int c,int f){}public void onTextChanged(CharSequence t,int a,int b,int c){query=t.toString();renderList();}public void afterTextChanged(Editable e){}});Spinner filters=spinner(filterKeys);filters.setSelection(listFilter);filters.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int i,long id){listFilter=i;renderList();}});body.addView(filters);gap(body,12);if(page.equals("all"))dateSearchControls();}
  listArea=column();body.addView(listArea);renderList();gap(body,22);body.addView(text(s("local_data"),12,muted,false));
 }
 private void renderList(){if(listArea==null)return;listArea.removeAllViews();try{List<Reminder> all=Store.read(this);all.sort(Comparator.comparing(r->r.next));if(page.equals("home")||page.equals("calendar")){renderCalendar(all);return;}LocalDate onDay=page.equals("all")?searchDay:null;if(onDay!=null)all.sort(Comparator.comparing(r->r.anchor.toLocalTime()));int count=0;for(Reminder r:all)if(r.deleted==page.equals("trash")&&matches(r)){if(onDay==null)card(r);else card(r,onDay);count++;}if(count==0)empty(s(page.equals("trash")?"empty_trash":"no_results"),s("search_help"));}catch(Exception e){empty(s("read_error"),s("retry"));}}
 private void dateSearchControls(){
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
  Button choose=button(searchDay==null?s("search_by_day"):s("search_by_day")+" · "+date(searchDay),false,this::pickSearchDay);
  row.addView(choose,new LinearLayout.LayoutParams(0,-2,1));
  if(searchDay!=null){Button clear=button("×",false,()->{searchDay=null;render();});clear.setTextSize(24);clear.setPadding(0,0,0,0);clear.setMinWidth(0);clear.setContentDescription(s("clear_date_filter"));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(dp(48),dp(48));size.setMarginStart(dp(8));row.addView(clear,size);}
  body.addView(row);gap(body,12);
 }
 private void pickSearchDay(){
  LocalDate start=searchDay==null?LocalDate.now():searchDay;TreeSet<String> selected=new TreeSet<>();selected.add(start.toString());
  CalendarPicker[] picker=new CalendarPicker[1];picker[0]=new CalendarPicker(this,selected,start,accent,ink,muted,surface,soft,()->{if(!selected.isEmpty()){searchDay=LocalDate.parse(selected.first());listFilter=0;picker[0].dismiss();render();}}).singleDate();picker[0].show();
 }
 private String normalized(String text){return ReminderSearch.normalized(text,locale());}
 private boolean matches(Reminder r){if(page.equals("all")&&!ReminderSearch.matchesDay(r,searchDay))return false;if(!normalized(r.name+" "+r.notes).contains(normalized(query)))return false;switch(listFilter){case 1:return !r.delivered&&!r.next.isBefore(LocalDateTime.now());case 2:return r.occurs(LocalDate.now())||r.next.toLocalDate().equals(LocalDate.now());case 3:return r.alarm;case 4:return !r.repeat.equals("none");case 5:return r.delivered||r.next.isBefore(LocalDateTime.now());default:return true;}}
 private void renderCalendar(List<Reminder> all){
  if(Saints.enabled(this)){TextView find=text(s("saint_search"),15,action,true);find.setPadding(dp(3),dp(8),dp(3),dp(8));find.setMinHeight(dp(48));find.setFocusable(true);find.setOnClickListener(v->SaintSearchDialog.show(this,accent,ink,soft,visibleMonth.getYear(),d->{selectedDay=d;visibleMonth=YearMonth.from(d);renderList();}));listArea.addView(find);}
  LinearLayout panel=column();panel.setBackground(shape(surface,18));panel.setPadding(dp(6),dp(12),dp(6),dp(12));
  LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
  Button previous=button(previousArrow(),false,()->{visibleMonth=visibleMonth.minusMonths(1);selectedDay=visibleMonth.atDay(1);renderList();});previous.setBackgroundColor(Color.TRANSPARENT);previous.setTextSize(28);previous.setMinWidth(0);previous.setPadding(0,0,0,0);previous.setContentDescription(s("previous_month"));nav.addView(previous,new LinearLayout.LayoutParams(dp(48),dp(48)));
  LinearLayout heading=column();heading.setGravity(Gravity.CENTER);TextView month=text(MonthLabels.format(visibleMonth.atDay(1),locale(),"LLLL"),20,ink,true),year=text(String.valueOf(visibleMonth.getYear()),13,muted,false);month.setGravity(Gravity.CENTER);year.setGravity(Gravity.CENTER);heading.addView(month);heading.addView(year);heading.setPadding(0,dp(4),0,dp(4));
  AccessibilityUi.button(heading,s("all_months")+" · "+month.getText()+" "+year.getText(),false);
  heading.setOnClickListener(v->new CalendarPicker(this,new TreeSet<>(),visibleMonth.atDay(1),accent,ink,muted,surface,soft,()->{}).browseMonths(d->CalendarAgenda.hasReminders(all,d),m->{visibleMonth=m;selectedDay=m.atDay(1);renderList();}).show());nav.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
  Button next=button(nextArrow(),false,()->{visibleMonth=visibleMonth.plusMonths(1);selectedDay=visibleMonth.atDay(1);renderList();});next.setBackgroundColor(Color.TRANSPARENT);next.setTextSize(28);next.setMinWidth(0);next.setPadding(0,0,0,0);next.setContentDescription(s("next_month"));nav.addView(next,new LinearLayout.LayoutParams(dp(48),dp(48)));panel.addView(nav);gap(panel,10);
  LinearLayout weekdays=new LinearLayout(this);for(int i=0;i<7;i++){TextView day=text(CalendarLocale.day(locale(),i).getDisplayName(java.time.format.TextStyle.NARROW,locale()),12,muted,false);day.setGravity(Gravity.CENTER);weekdays.addView(day,new LinearLayout.LayoutParams(0,dp(28),1));}panel.addView(weekdays);
  int offset=CalendarLocale.offset(locale(),visibleMonth.atDay(1));int cellHeight=Math.max(dp(56),Math.round(22*getResources().getDisplayMetrics().scaledDensity)+dp(26));
  for(int row=0;row<(offset+visibleMonth.lengthOfMonth()+6)/7;row++){
   LinearLayout line=new LinearLayout(this);
   for(int col=0;col<7;col++){
    int n=row*7+col-offset+1;LinearLayout.LayoutParams bounds=new LinearLayout.LayoutParams(0,cellHeight,1);bounds.setMargins(dp(1),dp(2),dp(1),dp(2));
    if(n<1||n>visibleMonth.lengthOfMonth()){line.addView(new Space(this),bounds);continue;}
    LocalDate day=visibleMonth.atDay(n);List<Reminder> events=CalendarAgenda.forDay(all,day);boolean selected=day.equals(selectedDay);
    LinearLayout cell=column();cell.setGravity(Gravity.CENTER);
    TextView number=text(String.valueOf(n),17,selected?onAccent:ink,true);number.setGravity(Gravity.CENTER);cell.addView(number,new LinearLayout.LayoutParams(-2,-2));
    View marker=new View(this);marker.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);marker.setBackground(shape(events.isEmpty()?Color.TRANSPARENT:selected?onAccent:action,8));LinearLayout.LayoutParams dot=new LinearLayout.LayoutParams(dp(5),dp(5));dot.topMargin=dp(3);cell.addView(marker,dot);
    GradientDrawable fill=shape(selected?accent:day.equals(LocalDate.now())?soft:surface,16);if(theme.white&&selected)fill.setStroke(dp(2),theme.border);cell.setBackground(fill);
    AccessibilityUi.button(cell,date(day)+(events.isEmpty()?"":" · "+events.size()+" "+s("reminders")),selected);
    cell.setOnClickListener(v->{selectedDay=day;restoreCalendarFocus=true;renderList();});
    if(restoreCalendarFocus&&selected){AccessibilityUi.restoreFocus(cell);restoreCalendarFocus=false;}
    line.addView(cell,bounds);
   }
   panel.addView(line);
  }
  listArea.addView(panel);gap(listArea,8);
  if(!selectedDay.equals(LocalDate.now())){listArea.addView(button(s("back_today"),false,()->{selectedDay=LocalDate.now();visibleMonth=YearMonth.from(selectedDay);renderList();}));gap(listArea,12);}
  TextView dateTitle=text(date(selectedDay),20,ink,true);AccessibilityUi.heading(dateTitle);listArea.addView(dateTitle);
  if(Saints.hasData(this,selectedDay))listArea.addView(text(Saints.name(this,selectedDay),14,action,true));
  gap(listArea,12);Button add=button("＋  "+s("new_reminder"),true,()->openEditor(null,null,selectedDay));add.setContentDescription(s("new_reminder")+" · "+date(selectedDay));listArea.addView(add,new LinearLayout.LayoutParams(-1,-2));gap(listArea,16);
  List<Reminder> events=CalendarAgenda.forDay(all,selectedDay);for(Reminder reminder:events)card(reminder,selectedDay);if(events.isEmpty())empty(s("no_day_events"),s("add_day_help"));
 }

 private void empty(String title,String message){LinearLayout box=column();box.setPadding(dp(20),dp(20),dp(20),dp(20));box.setBackground(shape(soft,15));box.addView(text("✧  "+title,15,ink,true));gap(box,8);box.addView(text(message,13,muted,false));listArea.addView(box);gap(listArea,12);}
 private void card(Reminder r){card(r,r.next.toLocalDate());}
 private void card(Reminder r,LocalDate occurrence){LinearLayout card=column();card.setPadding(dp(18),dp(16),dp(18),dp(12));card.setBackground(shape(surface,16));card.addView(text(r.name,17,ink,true));gap(card,7);
  card.addView(text((occurrence.equals(LocalDate.now())?s("today"):date(occurrence))+" · "+time(r.next.toLocalTime())+(r.alarm?" · "+s("alarm"):""),13,muted,false));
  if(!r.repeat.equals("none")||r.delivered)card.addView(text((r.repeat.equals("none")?"":s(r.repeat))+(r.delivered?" · "+s("delivered"):""),12,muted,false));gap(card,12);
  LinearLayout actions=new LinearLayout(this);
  if(r.deleted)actions.addView(button(s("restore"),false,()->changeDeleted(r,false)));
  else{actions.addView(button(s("edit"),false,()->openEditor(r,null)),new LinearLayout.LayoutParams(0,-2,1));actions.addView(new Space(this),new LinearLayout.LayoutParams(dp(10),1));actions.addView(button(s("delete"),false,()->changeDeleted(r,true)),new LinearLayout.LayoutParams(0,-2,1));}
  for(int i=0;i<actions.getChildCount();i++){View actionView=actions.getChildAt(i);if(actionView instanceof Button)actionView.setContentDescription(((Button)actionView).getText()+" · "+r.name);}card.addView(actions);listArea.addView(card);gap(listArea,12);
 }
 private void changeDeleted(Reminder r,boolean deleted){try{List<Reminder> data=Store.read(this);for(Reminder x:data)if(x.id.equals(r.id))x.deleted=deleted;Store.write(this,data);if(deleted)Scheduler.cancel(this,r);else Scheduler.reschedule(this);render();toast(s(deleted?"deleted":"restored"));}catch(Exception e){error(e);}}
 private TextView label(LinearLayout form,String key){gap(form,16);TextView label=text(s(key),13,ink,true);form.addView(label);gap(form,7);return label;}
 private EditText input(String hint,int limit,boolean multiline){EditText e=new EditText(this);e.setTextSize(16);e.setTypeface(AppFonts.face(this,false));e.setTextColor(ink);e.setHintTextColor(muted);e.setHint(hint);e.setPadding(dp(12),dp(12),dp(12),dp(12));e.setBackground(shape(soft,9));e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)});e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|(multiline?InputType.TYPE_TEXT_FLAG_MULTI_LINE:0));if(multiline)e.setMinLines(2);else e.setSingleLine();e.setBackgroundTintList(null);ThemeUi.applyNative(e);return e;}
 private Spinner spinner(String[] keys){String[] values=new String[keys.length];for(int i=0;i<keys.length;i++)values[i]=s(keys[i]);Spinner sp=new Spinner(this);ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){@Override public View getView(int position,View convert,ViewGroup parent){View view=super.getView(position,convert,parent);AppFonts.apply(view);return view;}@Override public View getDropDownView(int position,View convert,ViewGroup parent){View view=super.getDropDownView(position,convert,parent);AppFonts.apply(view);return view;}};a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);sp.setAdapter(a);sp.setMinimumHeight(dp(48));return sp;}
 private void openEditor(Reminder original,Reminder restored){openEditor(original,restored,null);}
 private void openEditor(Reminder original,Reminder restored,LocalDate suggestedDay){
  stopPreview();Reminder r=restored==null?original:restored;editingId=original==null?null:original.id;LocalDateTime initial=r==null?CalendarAgenda.draftStart(suggestedDay,LocalDateTime.now()):r.next;chosenDate=initial.toLocalDate();chosenTime=initial.toLocalTime();chosenSound=r==null?"":r.sound;chosenVolume=r==null?40:r.volume;chosenDates=new TreeSet<>(r==null?Collections.emptySet():r.dates);retainedCategory=r==null?"Personale":r.category;
  LinearLayout form=column();form.setPadding(dp(22),dp(4),dp(22),dp(18));TextView nameLabel=label(form,"name_label");nameInput=input(s("name_hint"),120,false);AccessibilityUi.label(nameLabel,nameInput);if(r!=null)nameInput.setText(r.name);form.addView(nameInput);
  gap(form,16);Button date=button(date(chosenDate),false,()->{});TextView saintDate=text(Saints.name(this,chosenDate),13,muted,false);date.setOnClickListener(v->{TreeSet<String> one=new TreeSet<>();one.add(chosenDate.toString());new CalendarPicker(this,one,chosenDate,accent,ink,muted,surface,soft,()->{chosenDate=LocalDate.parse(one.first());date.setText(date(chosenDate));saintDate.setText(Saints.name(this,chosenDate));saintDate.setVisibility(Saints.hasData(this,chosenDate)?View.VISIBLE:View.GONE);}).singleDate().show();});form.addView(date);saintDate.setVisibility(Saints.hasData(this,chosenDate)?View.VISIBLE:View.GONE);form.addView(saintDate);
  label(form,"time_label");ReminderTimeButton time=new ReminderTimeButton(this,chosenTime);time.setOnClickListener(v->SoftTimePicker.show(this,chosenTime,accent,onAccent,ink,muted,surface,soft,selected->{chosenTime=selected;time.setTime(chosenTime);}));form.addView(time,new LinearLayout.LayoutParams(-1,-2));
  TextView repeatLabel=label(form,"repeat_label");repeatInput=spinner(repeatKeys);AccessibilityUi.label(repeatLabel,repeatInput);int repeat=r==null?0:Math.max(0,Arrays.asList(repeatKeys).indexOf(r.repeat));repeatInput.setSelection(repeat);form.addView(repeatInput);calendarButton=button(s("choose_dates"),false,()->openDates());form.addView(calendarButton);calendarButton.setVisibility(repeat==5?View.VISIBLE:View.GONE);updateDatesLabel();final boolean[] initialSelection={true};repeatInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int i,long id){date.setEnabled(i!=5);saintDate.setVisibility(i!=5&&Saints.hasData(MainActivity.this,chosenDate)?View.VISIBLE:View.GONE);calendarButton.setVisibility(i==5?View.VISIBLE:View.GONE);if(i==5&&!initialSelection[0])openDates();initialSelection[0]=false;}});
  gap(form,18);alarmInput=toggle(s("alarm_label"),r==null?Preferences.get(this,"alert_style","banner").equals("alarm"):r.alarm);alarmInput.setTypeface(AppFonts.face(this,true));form.addView(alarmInput,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout noticeGroup=column();TextView noticeLabel=label(noticeGroup,"alert_style");noticeInput=spinner(new String[]{"notice_discreet","notice_banner"});AccessibilityUi.label(noticeLabel,noticeInput);String style=r==null?Preferences.get(this,"alert_style","banner"):r.notice;noticeInput.setSelection(style.equals("discreet")?0:1);noticeGroup.addView(noticeInput);form.addView(noticeGroup);
  LinearLayout vibrationRow=new LinearLayout(this);vibrationRow.setGravity(Gravity.CENTER_VERTICAL);vibrationInput=toggle(s("vibration"),r==null?Preferences.prefs(this).getBoolean("default_vibration",true):AlertPolicy.vibrates(r));vibrationRow.addView(vibrationInput,new LinearLayout.LayoutParams(-1,-2));form.addView(vibrationRow);
  TextView soundLabel=label(form,"event_sound");soundButton=button(eventSoundTitle(),false,this::pickSound);AccessibilityUi.label(soundLabel,soundButton);form.addView(soundButton);
  LinearLayout volume=column();gap(volume,16);TextView volumeLabel=text(s("alarm_volume")+" · "+chosenVolume+"%",14,ink,true);volume.addView(volumeLabel);SeekBar seek=new SeekBar(this);seek.setMin(10);seek.setMax(100);seek.setProgress(chosenVolume);seek.setContentDescription(s("alarm_volume"));seek.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setRangeInfo(android.view.accessibility.AccessibilityNodeInfo.RangeInfo.obtain(android.view.accessibility.AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT,0,100,seek.getProgress()));if(Build.VERSION.SDK_INT>=30)info.setStateDescription(java.text.NumberFormat.getPercentInstance(locale()).format(seek.getProgress()/100.0));}});seek.setProgressTintList(null);seek.setProgressBackgroundTintList(null);GradientDrawable track=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.WHITE,accent});track.setCornerRadius(dp(8));if(theme.white)track.setStroke(dp(1),theme.border);track.setSize(dp(240),dp(6));android.graphics.drawable.LayerDrawable trackLayer=new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{track});trackLayer.setLayerGravity(0,Gravity.CENTER_VERTICAL|Gravity.FILL_HORIZONTAL);trackLayer.setLayerHeight(0,dp(6));seek.setProgressDrawable(trackLayer);GradientDrawable volumeThumb=shape(accent,20);volumeThumb.setSize(dp(24),dp(24));volumeThumb.setStroke(dp(2),theme.white?theme.border:onAccent);seek.setThumbTintList(null);seek.setThumb(volumeThumb);volume.addView(seek,new LinearLayout.LayoutParams(-1,dp(48)));volume.addView(text(s("volume_help"),13,muted,false));volume.setVisibility(alarmInput.isChecked()?View.VISIBLE:View.GONE);form.addView(volume);
  seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int value,boolean user){chosenVolume=value;volumeLabel.setText(s("alarm_volume")+" · "+chosenVolume+"%");if(previewPlayer!=null)try{previewPlayer.setVolume(chosenVolume/100f,chosenVolume/100f);}catch(IllegalStateException ignored){}}public void onStartTrackingTouch(SeekBar b){startPreview();}public void onStopTrackingTouch(SeekBar b){stopPreview();}});
  Runnable updateAlert=()->{boolean quiet=!alarmInput.isChecked()&&noticeInput.getSelectedItemPosition()==0;noticeGroup.setVisibility(alarmInput.isChecked()?View.GONE:View.VISIBLE);volume.setVisibility(alarmInput.isChecked()?View.VISIBLE:View.GONE);vibrationRow.setVisibility(quiet?View.GONE:View.VISIBLE);soundButton.setEnabled(!quiet);soundButton.setAlpha(quiet?.6f:1f);soundButton.setText(quiet?s("silent"):eventSoundTitle());};
  alarmInput.setOnCheckedChangeListener((b,on)->{updateAlert.run();if(!on)stopPreview();else{if(getSystemService(android.media.AudioManager.class).getStreamVolume(android.media.AudioManager.STREAM_ALARM)==0)toast(s("system_volume_off"));offerFullScreen();}});
  noticeInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int i,long id){updateAlert.run();}});updateAlert.run();
  TextView notesLabel=label(form,"notes_label");notesInput=input(s("notes_hint"),600,true);AccessibilityUi.label(notesLabel,notesInput);if(r!=null)notesInput.setText(r.notes);form.addView(notesInput);
  ScrollView scroll=new ScrollView(this);scroll.setPadding(0,0,0,dp(20));scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(true);scroll.setScrollbarFadingEnabled(false);scroll.setScrollIndicators(View.SCROLL_INDICATOR_TOP|View.SCROLL_INDICATOR_BOTTOM);scroll.addView(form);editor=new AlertDialog.Builder(this).setTitle(s(original==null?"new_reminder":"edit")).setView(scroll).setNegativeButton(s("cancel"),null).setPositiveButton(s("save"),null).create();editor.setOnDismissListener(d->stopPreview());editor.setOnShowListener(d->{AppFonts.watch(editor);editor.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->saveEditor());editor.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);editor.getWindow().setLayout(Math.min(dp(560),getResources().getDisplayMetrics().widthPixels-dp(24)),-2);editor.getWindow().setBackgroundDrawable(shape(surface,24));});editor.show();if(alarmInput.isChecked())offerFullScreen();
 }
 private void updateDatesLabel(){if(calendarButton!=null)calendarButton.setText(chosenDates.isEmpty()?s("choose_dates"):Preferences.selectedDates(this,chosenDates.size()));}
 private void openDates(){new CalendarPicker(this,chosenDates,chosenDate,accent,ink,muted,surface,soft,this::updateDatesLabel).show();}
 private Reminder draft(){Reminder r=new Reminder();if(editingId!=null)r.id=editingId;r.name=nameInput.getText().toString().trim();r.notes=notesInput.getText().toString().trim();r.category=retainedCategory;r.sound=chosenSound;r.volume=chosenVolume;r.dates=new TreeSet<>(chosenDates);r.repeat=repeatKeys[repeatInput.getSelectedItemPosition()];r.alarm=alarmInput.isChecked();r.notice=noticeInput.getSelectedItemPosition()==0?"discreet":"banner";r.vibrate=vibrationInput.isChecked();r.anchor=LocalDateTime.of(chosenDate,chosenTime);r.next=r.anchor;return r;}
 private void saveEditor(){Reminder r=draft();if(r.name.isEmpty()){nameInput.setError(s("name_required"));return;}try{List<Reminder> list=Store.read(this);Reminder previous=null;for(Reminder x:list)if(x.id.equals(r.id))previous=x;
  if(r.repeat.equals("calendar")){if(r.dates.isEmpty()){toast(s("choose_one_date"));return;}r.anchor=LocalDate.parse(r.dates.first()).atTime(chosenTime);LocalDateTime next=r.nextAfter(LocalDateTime.now());if(next==null){if(previous!=null&&previous.repeat.equals("calendar")&&previous.dates.equals(r.dates)&&previous.anchor.toLocalTime().equals(chosenTime))next=previous.next;else{toast(s("future_required"));return;}}r.next=next;}
  boolean same=previous!=null&&previous.next.equals(r.next)&&previous.repeat.equals(r.repeat)&&previous.dates.equals(r.dates);if(!r.next.isAfter(LocalDateTime.now())&&!same){toast(s("future_required"));return;}if(same){r.anchor=previous.anchor;r.delivered=previous.delivered;}list.removeIf(x->x.id.equals(r.id));list.add(r);Store.write(this,list);Preferences.prefs(this).edit().putBoolean("has_created",true).apply();Scheduler.cancel(this,r);Scheduler.schedule(this,r);editor.dismiss();page="home";selectedDay=r.next.toLocalDate();visibleMonth=YearMonth.from(selectedDay);render();toast(s("saved"));}catch(Exception e){error(e);}}
 private void stopPreview(){previewDragging=false;if(previewPlayer!=null){previewPlayer.release();previewPlayer=null;}}
 private void startPreview(){stopPreview();if(chosenSound.equals("silent"))return;android.media.AudioManager audio=getSystemService(android.media.AudioManager.class);if(audio.getStreamVolume(android.media.AudioManager.STREAM_ALARM)==0){toast(s("system_volume_off"));return;}previewDragging=true;try{Uri uri;if(chosenSound.isEmpty()){NotificationChannel ch=getSystemService(NotificationManager.class).getNotificationChannel(ReminderReceiver.ALARM);uri=ch==null?RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM):ch.getSound();}else uri=Uri.parse(chosenSound);if(uri==null)return;android.media.MediaPlayer player=new android.media.MediaPlayer();previewPlayer=player;player.setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_ALARM).build());player.setVolume(chosenVolume/100f,chosenVolume/100f);player.setDataSource(this,uri);player.setLooping(true);player.setOnPreparedListener(p->{if(previewDragging&&previewPlayer==p)p.start();});player.setOnErrorListener((p,w,e)->{stopPreview();toast(s("preview_unavailable"));return true;});player.prepareAsync();refreshHandler.postDelayed(()->{if(previewPlayer==player)stopPreview();},15000);}catch(Exception e){stopPreview();toast(s("preview_unavailable"));}}
 private void settings(){
  body.addView(text(s("settings"),28,ink,true));label(body,"language");body.addView(button(languageLabel(),false,this::chooseLanguage));
  widgetSettings();if(Saints.enabled(this))saintSettings();
  label(body,"alert_style");body.addView(text(s("notice_defaults_help"),13,muted,false));gap(body,10);body.addView(button(s(defaultAlert().alarm?"alarm":AlertPolicy.quiet(defaultAlert())?"notice_discreet":"notice_banner"),false,this::chooseDefaultAlert));gap(body,10);
  Switch vibration=toggle(s("vibration"),Preferences.prefs(this).getBoolean("default_vibration",true));vibration.setText(s("vibration"));vibration.setTextColor(ink);vibration.setTypeface(AppFonts.face(this,false));vibration.setOnCheckedChangeListener((v,on)->Preferences.prefs(this).edit().putBoolean("default_vibration",on).apply());body.addView(vibration);
  gap(body,12);body.addView(text(s("notice_android_help"),13,muted,false));gap(body,10);body.addView(button(s("notice_channel_settings"),false,()->openChannel(ReminderReceiver.soundChannel(this,defaultAlert()))));gap(body,10);body.addView(button(s("notice_phone_settings"),false,()->launch(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()))));
  label(body,"backup_title");body.addView(text(s("backup_help"),13,muted,false));gap(body,10);body.addView(button(s("backup_export"),false,()->backups.exportFile()));gap(body,10);body.addView(button(s("backup_import"),false,()->backups.importFile()));
  label(body,"sounds");body.addView(text(s("sound_help"),13,muted,false));gap(body,10);
  body.addView(button(s("reminder_sound")+" · "+soundTitle(ReminderReceiver.NORMAL),false,()->openChannel(ReminderReceiver.NORMAL)));gap(body,10);
  body.addView(button(s("alarm_sound")+" · "+soundTitle(ReminderReceiver.ALARM),false,()->openChannel(ReminderReceiver.ALARM)));
  label(body,"help");body.addView(text(s("help_text"),13,muted,false));
  gap(body,20);body.addView(button(s("privacy_policy"),false,()->go("privacy")));
 }
 private void privacy(){
  body.addView(button(previousArrow()+" "+s("settings"),false,()->go("settings")));gap(body,16);
  body.addView(text(s("privacy_policy"),28,ink,true));gap(body,12);
  try(java.io.InputStream input=getAssets().open("privacy-"+Preferences.language(this)+".txt")){
   java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;
   while((count=input.read(buffer))!=-1)bytes.write(buffer,0,count);
   TextView content=text(new String(bytes.toByteArray(),java.nio.charset.StandardCharsets.UTF_8),16,ink,false);
   content.setTextIsSelectable(true);content.setLineSpacing(dp(4),1f);body.addView(content);
  }catch(java.io.IOException e){body.addView(text("Fabio Avitabile · fabiocorby@gmail.com",16,ink,false));}
  gap(body,16);body.addView(button("fabiocorby@gmail.com",false,()->launch(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:fabiocorby@gmail.com")))));
 }
 private void widgetSettings(){
  label(body,"widget_title");body.addView(text(s("widget_help"),13,muted,false));gap(body,10);
  body.addView(button(s("widget_add_agenda"),false,()->pinWidget(AgendaWidget.class)));
  if(Saints.enabled(this)){gap(body,10);body.addView(button(s("widget_add_saint"),false,()->pinWidget(SaintWidget.class)));}
 }
 private void pinWidget(Class<?> provider){
  android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(this);
  try{if(manager.isRequestPinAppWidgetSupported()&&manager.requestPinAppWidget(new ComponentName(this,provider),null,null))return;}catch(RuntimeException e){android.util.Log.w("Ricorda","Widget pin request unavailable",e);}
  AppFonts.show(new AlertDialog.Builder(this).setTitle(s("widget_title")).setMessage(s("widget_manual")).setPositiveButton(s("ok"),null));
 }
 private void saintSettings(){
  label(body,"saint_daily_title");body.addView(text(s("saint_daily_help"),13,muted,false));gap(body,10);
  Switch daily=toggle(s("saint_daily_enable"),SaintDailyReceiver.enabled(this));daily.setText(s("saint_daily_enable"));daily.setTextColor(ink);daily.setTypeface(AppFonts.face(this,false));
  daily.setOnCheckedChangeListener((v,on)->{if(on)pickSaintTime(true);else{SaintDailyReceiver.configure(this,false,SaintDailyReceiver.time(this));render();}});body.addView(daily);gap(body,10);
  body.addView(button(s("saint_daily_time")+" · "+time(SaintDailyReceiver.time(this)),false,()->pickSaintTime(SaintDailyReceiver.enabled(this))));gap(body,10);
  body.addView(text(s("saint_data_year"),12,muted,false));
  if(SaintDailyReceiver.enabled(this)){
   SaintDailyReceiver.channel(this);
   if(!Scheduler.exact(this)){gap(body,8);body.addView(text(s("saint_timing_help"),12,muted,false));}
   gap(body,10);body.addView(button(s("notice_channel_settings"),false,()->openChannel(SaintDailyReceiver.CHANNEL)));
  }
 }
 private void pickSaintTime(boolean enable){
  LocalTime at=SaintDailyReceiver.time(this);
  android.app.Dialog picker=SoftTimePicker.show(this,at,accent,onAccent,ink,muted,surface,soft,selected->{
   SaintDailyReceiver.configure(this,enable,selected);render();
   if(enable){SaintDailyReceiver.channel(this);if(!getSystemService(NotificationManager.class).areNotificationsEnabled())notificationPermission();else if(!ReminderReceiver.enabled(this,SaintDailyReceiver.CHANNEL))openChannel(SaintDailyReceiver.CHANNEL);else offerExact();}
  });picker.setOnCancelListener(d->render());
 }
 private Switch toggle(String description,boolean checked){Switch toggle=new Switch(this);toggle.setText(description);toggle.setTextSize(15);toggle.setTextColor(ink);toggle.setTypeface(AppFonts.face(this,false));toggle.setGravity(Gravity.CENTER_VERTICAL);toggle.setShowText(false);toggle.setSplitTrack(false);toggle.setMinHeight(dp(48));toggle.setSwitchPadding(dp(16));toggle.setPaddingRelative(0,dp(4),dp(2),dp(4));ThemeUi.switchStyle(toggle,theme);toggle.setChecked(checked);return toggle;}
 private Reminder defaultAlert(){Reminder r=new Reminder();String style=Preferences.get(this,"alert_style","banner");r.alarm=style.equals("alarm");r.notice=style.equals("discreet")?"discreet":"banner";r.vibrate=Preferences.prefs(this).getBoolean("default_vibration",true);return r;}
 private void chooseDefaultAlert(){String[] values={"discreet","banner","alarm"};String[] names={s("notice_discreet"),s("notice_banner"),s("alarm")};int selected=Math.max(0,Arrays.asList(values).indexOf(Preferences.get(this,"alert_style","banner")));final int[] choice={selected};AppFonts.show(new AlertDialog.Builder(this).setTitle(s("alert_style")).setSingleChoiceItems(names,selected,(d,n)->choice[0]=n).setPositiveButton(s("save"),(d,w)->{Preferences.put(this,"alert_style",values[choice[0]]);render();}).setNegativeButton(s("cancel"),null));}
 private String eventSoundTitle(){if(chosenSound.isEmpty())return s("base_sound");if(chosenSound.equals("silent"))return s("silent");try{android.media.Ringtone tone=RingtoneManager.getRingtone(this,Uri.parse(chosenSound));return tone==null?s("system_sound"):tone.getTitle(this);}catch(Exception e){return s("system_sound");}}
 private void pickSound(){stopPreview();int type=alarmInput.isChecked()?RingtoneManager.TYPE_ALARM:RingtoneManager.TYPE_NOTIFICATION;Uri current=chosenSound.isEmpty()?RingtoneManager.getDefaultUri(type):chosenSound.equals("silent")?null:Uri.parse(chosenSound);Intent picker=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,type).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,true).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,true).putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,RingtoneManager.getDefaultUri(type)).putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,current).putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,s("event_sound"));try{startActivityForResult(picker,40);}catch(ActivityNotFoundException e){toast(s("sound_picker_missing"));}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(backups!=null&&backups.result(request,result,data))return;if(request==40&&result==RESULT_OK&&data!=null){Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);chosenSound=uri==null?"silent":RingtoneManager.isDefault(uri)?"":uri.toString();if(soundButton!=null)soundButton.setText(eventSoundTitle());}}
 private String soundTitle(String channel){try{NotificationChannel ch=getSystemService(NotificationManager.class).getNotificationChannel(channel);if(ch==null||ch.getSound()==null)return s("silent");android.media.Ringtone tone=RingtoneManager.getRingtone(this,ch.getSound());return tone==null?s("system_sound"):tone.getTitle(this);}catch(Exception e){return s("system_sound");}}
 private void openChannel(String id){launch(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,id));}
 private void launch(Intent i){try{startActivity(i);}catch(ActivityNotFoundException e){try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){toast(s("retry"));}}}
 private void notificationPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED&&(!Preferences.prefs(this).getBoolean("asked_notifications",false)||shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))){Preferences.prefs(this).edit().putBoolean("asked_notifications",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);}else launch(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}
 private void exactPermission(){if(Build.VERSION.SDK_INT>=31)launch(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));else toast(s("active"));}
 @Override public void onRequestPermissionsResult(int request,String[] p,int[] g){super.onRequestPermissionsResult(request,p,g);render();try{Scheduler.reschedule(this);}catch(Exception e){error(e);}SaintDailyReceiver.schedule(this);offerExact();}
 private void offerFullScreen(){if(Build.VERSION.SDK_INT>=34&&!AlarmActivity.fullScreenAllowed(this)&&!Preferences.prefs(this).getBoolean("asked_full_screen",false)){Preferences.prefs(this).edit().putBoolean("asked_full_screen",true).apply();AppFonts.show(new AlertDialog.Builder(this).setTitle(s("wake_screen_title")).setMessage(s("wake_screen_help")).setPositiveButton(s("open_android_settings"),(d,w)->launch(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName())))).setNegativeButton(s("later"),null));}}
 private void askAtStartup(){if(!Preferences.prefs(this).getBoolean("intro_done",false)){Preferences.prefs(this).edit().putBoolean("intro_done",true).apply();AppFonts.show(new AlertDialog.Builder(this).setTitle(s("welcome_notifications")).setMessage(s("welcome_help")).setPositiveButton(s("continue_label"),(d,w)->{if(Build.VERSION.SDK_INT>=33&&!getSystemService(NotificationManager.class).areNotificationsEnabled())notificationPermission();else offerExact();}).setNegativeButton(s("later"),null));}}
 private void offerExact(){if(!Scheduler.exact(this))AppFonts.show(new AlertDialog.Builder(this).setTitle(s("exact_alarms")).setMessage(s("exact_help")).setPositiveButton(s("activate"),(d,w)->exactPermission()).setNegativeButton(s("later"),null));}
 private String languageLabel(){String actual=Preferences.LANGUAGE_NAMES[Arrays.asList(Preferences.LANGS).indexOf(Preferences.language(this))];return Preferences.get(this,"language","system").equals("system")?s("system_language")+" · "+actual:actual;}
 private void chooseLanguage(){
  String saved=Preferences.get(this,"language","system"),current=Preferences.language(this);int[] order=LanguageCatalog.alphabeticalOrder();int selected=0;
  CharSequence[] choices=new CharSequence[order.length+1];choices[0]=s("system_language");
  for(int position=0;position<order.length;position++){int index=order[position];SpannableString nativeName=new SpannableString(Preferences.LANGUAGE_NAMES[index]);nativeName.setSpan(new android.text.style.LocaleSpan(Locale.forLanguageTag(Preferences.LANGS[index])),0,nativeName.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);choices[position+1]=nativeName;if(!saved.equals("system")&&Preferences.LANGS[index].equals(current))selected=position+1;}
  final int[] choice={selected};AppFonts.show(new AlertDialog.Builder(this).setTitle(s("language")).setSingleChoiceItems(choices,selected,(d,n)->choice[0]=n).setPositiveButton(s("save"),(d,w)->{Preferences.put(this,"language",choice[0]==0?"system":Preferences.LANGS[order[choice[0]-1]]);recreate();}).setNegativeButton(s("cancel"),null));
 }
 private void fontIcon(LinearLayout row){Button b=button("Aa",false,()->go("fonts"));b.setContentDescription(s("fonts"));b.setTextSize(21);b.setTypeface(AppFonts.face(this,"nunito",true));b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(0,0,0,0);b.setBackground(shape(soft,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));lp.setMargins(dp(8),0,0,0);row.addView(b,lp);}
 private void fonts(){
  body.addView(text(s("fonts"),28,ink,true));gap(body,10);body.addView(text(s("font_help"),15,muted,false));gap(body,22);
  for(int i=0;i<AppFonts.IDS.length;i++){final String id=AppFonts.IDS[i];boolean selected=id.equals(AppFonts.selected(this));LinearLayout card=column();card.setPadding(dp(18),dp(16),dp(18),dp(16));GradientDrawable background=shape(selected?soft:surface,16);if(selected)background.setStroke(dp(2),theme.white?theme.border:accent);card.setBackground(background);TextView title=text(AppFonts.NAMES[i]+(selected?"  ✓":""),21,ink,true);title.setTypeface(AppFonts.face(this,id,true));card.addView(title);gap(card,4);TextView description=text(s(AppFonts.DESCRIPTIONS[i]),14,muted,false);card.addView(description);gap(card,12);TextView sample=text(s("home_title"),22,ink,false);sample.setTypeface(AppFonts.face(this,id,false));card.addView(sample);gap(card,6);TextView detail=text(s("font_sample"),16,muted,false);detail.setTypeface(AppFonts.face(this,id,false));card.addView(detail);card.setFocusable(true);card.setClickable(true);card.setSelected(selected);card.setContentDescription(AppFonts.NAMES[i]+" · "+s(AppFonts.DESCRIPTIONS[i]));AccessibilityUi.button(card,card.getContentDescription(),selected);card.setOnClickListener(v->{if(!id.equals(AppFonts.selected(this))){Preferences.put(this,"font",id);recreate();}});body.addView(card);gap(body,12);}
  gap(body,6);body.addView(button(s("back"),false,()->go("home")));
 }
 private void appearance(){
  body.addView(text(s("appearance"),28,ink,true));label(body,"mode_label");body.addView(text(s("mode_help"),13,muted,false));
  for(String mode:modes){gap(body,9);body.addView(button(s(mode),Preferences.get(this,"mode","system").equals(mode),()->{Preferences.put(this,"mode",mode);recreate();}));}
  label(body,"color_label");body.addView(text(s("color_help"),13,muted,false));
  for(String color:colors){
   gap(body,9);ThemePalette sample=ThemePalette.of(color,false);boolean chosen=color.equals(Preferences.get(this,"color","sage"));
   LinearLayout option=new LinearLayout(this);option.setGravity(Gravity.CENTER_VERTICAL);option.setPadding(dp(14),dp(12),dp(14),dp(12));option.setMinimumHeight(dp(66));
   GradientDrawable tile=shape(chosen?soft:surface,16);tile.setStroke(dp(chosen?2:1),chosen?(sample.white?theme.border:sample.accent):theme.border);option.setBackground(tile);
   View swatch=new View(this);GradientDrawable paint=shape(sample.accent,12);if(sample.white)paint.setStroke(dp(1),theme.border);swatch.setBackground(paint);option.addView(swatch,new LinearLayout.LayoutParams(dp(40),dp(40)));
   TextView name=text(s(color),16,ink,true);LinearLayout.LayoutParams labelParams=new LinearLayout.LayoutParams(0,-2,1);labelParams.setMarginStart(dp(14));option.addView(name,labelParams);
   TextView mark=text(chosen?"✓":"",21,ink,true);option.addView(mark,new LinearLayout.LayoutParams(dp(28),-2));
   option.setClickable(true);option.setFocusable(true);option.setSelected(chosen);option.setContentDescription(s(color));AccessibilityUi.button(option,option.getContentDescription(),chosen);option.setOnClickListener(v->{Preferences.put(this,"color",color);recreate();});body.addView(option);
  }
  label(body,"preview");LinearLayout preview=column();preview.setPadding(dp(18),dp(18),dp(18),dp(18));preview.setBackground(shape(surface,16));preview.addView(text(s("name_hint"),17,ink,true));gap(preview,12);preview.addView(button(s("new_reminder"),true,()->openEditor(null,null)));body.addView(preview);
 }
 @Override public void onBackPressed(){if(!page.equals("home"))go("home");else super.onBackPressed();}
}
