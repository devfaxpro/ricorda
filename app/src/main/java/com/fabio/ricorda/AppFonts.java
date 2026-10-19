package com.fabio.ricorda;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;
import java.util.Map;

/** Bundled fonts, with Android's fallback for characters absent from a family. */
public final class AppFonts {
 public static final String[] IDS={"nunito","lato","lora","caveat"};
 public static final String[] NAMES={"Nunito","Lato","Lora","Caveat"};
 public static final String[] DESCRIPTIONS={"font_soft","font_clean","font_elegant","font_handwritten"};
 private static final Map<String,Typeface> cache=new HashMap<>();
 private static final Map<android.widget.TextView,String> styled=new java.util.WeakHashMap<>();
 public static String selected(Context c){String id=Preferences.get(c,"font","nunito");for(String known:IDS)if(known.equals(id))return id;return "nunito";}
 public static void applyTheme(Context c){String id=selected(c);int style=id.equals("lato")?R.style.FontLato:id.equals("lora")?R.style.FontLora:id.equals("caveat")?R.style.FontCaveat:R.style.FontNunito;c.getTheme().applyStyle(style,true);ThemeUi.applyTheme(c);}
 public static Typeface face(Context c,boolean bold){return face(c,selected(c),bold);}
 public static void apply(android.view.View view){if(view instanceof android.widget.TextView){android.widget.TextView text=(android.widget.TextView)view;Typeface current=text.getTypeface();boolean bold=current!=null&&current.isBold();String key=selected(view.getContext())+(bold?"-bold":"-regular");if(!key.equals(styled.get(text))){text.setTypeface(face(view.getContext(),bold));styled.put(text,key);}}if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)apply(group.getChildAt(i));}}
 public static void watch(android.app.Dialog dialog){android.view.View decor=dialog.getWindow().getDecorView();decor.setLayoutDirection(LanguageCatalog.isRtl(Preferences.language(dialog.getContext()))?android.view.View.LAYOUT_DIRECTION_RTL:android.view.View.LAYOUT_DIRECTION_LTR);apply(decor);ThemeUi.applyNative(decor);decor.getViewTreeObserver().addOnGlobalLayoutListener(()->{apply(decor);ThemeUi.applyNative(decor);});}
 public static android.app.AlertDialog show(android.app.AlertDialog.Builder builder){android.app.AlertDialog dialog=builder.create();dialog.setOnShowListener(d->watch(dialog));dialog.show();return dialog;}
 public static synchronized Typeface face(Context c,String id,boolean bold){String key=id+(bold?"-bold":"-regular");Typeface cached=cache.get(key);if(cached!=null)return cached;int resource=id.equals("lato")?R.font.lato:id.equals("lora")?R.font.lora:id.equals("caveat")?R.font.caveat:R.font.nunito;Typeface family=c.getResources().getFont(resource);Typeface result=Typeface.create(family,bold?Typeface.BOLD:Typeface.NORMAL);cache.put(key,result);return result;}
 private AppFonts(){}
}
