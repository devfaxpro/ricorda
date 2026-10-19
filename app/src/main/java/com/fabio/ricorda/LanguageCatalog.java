package com.fabio.ricorda;
import java.util.*;
public final class LanguageCatalog {
 public static final String[] TAGS={"it","en","es","pt-BR","fr","de","zh-Hans","ja","ko","ru","nl","pl","uk","cs","sk","hu","ro","bg","el","tr","sv","da","nb","fi","ar","he","fa","hi","bn","ur","id","ms","vi","th","fil","ta","te","mr","sw","zh-Hant"};
 public static final String[] NAMES={"Italiano","English","Español","Português (Brasil)","Français","Deutsch","简体中文","日本語","한국어","Русский","Nederlands","Polski","Українська","Čeština","Slovenčina","Magyar","Română","Български","Ελληνικά","Türkçe","Svenska","Dansk","Norsk bokmål","Suomi","العربية","עברית","فارسی","हिन्दी","বাংলা","اردو","Bahasa Indonesia","Bahasa Melayu","Tiếng Việt","ไทย","Filipino","தமிழ்","తెలుగు","मराठी","Kiswahili","繁體中文"};
 /** Sort the visible native names without changing stored language identifiers. */
 public static int[] alphabeticalOrder(){
  java.text.Collator collator=java.text.Collator.getInstance(Locale.ROOT);collator.setStrength(java.text.Collator.PRIMARY);collator.setDecomposition(java.text.Collator.CANONICAL_DECOMPOSITION);
  Integer[] order=new Integer[NAMES.length];for(int i=0;i<order.length;i++)order[i]=i;
  Arrays.sort(order,(a,b)->{int compared=collator.compare(NAMES[a],NAMES[b]);return compared!=0?compared:TAGS[a].compareTo(TAGS[b]);});
  int[] result=new int[order.length];for(int i=0;i<order.length;i++)result[i]=order[i];return result;
 }
 private static String supported(String tag){
  if(tag==null||tag.isEmpty())return null;
  Locale locale=Locale.forLanguageTag(tag.replace('_','-'));
  String language=locale.getLanguage();
  if(language.equals("pt"))return "pt-BR";
  if(language.equals("zh")){
   String script=locale.getScript(),region=locale.getCountry();
   if(script.equals("Hant"))return "zh-Hant";
   if(script.equals("Hans"))return "zh-Hans";
   return Arrays.asList("TW","HK","MO").contains(region)?"zh-Hant":"zh-Hans";
  }
  if(language.equals("iw"))language="he";
  if(language.equals("in"))language="id";
  if(language.equals("no"))language="nb";
  if(language.equals("tl"))language="fil";
  return Arrays.asList(TAGS).contains(language)?language:null;
 }
 public static String resolve(String tag){String found=supported(tag);return found==null?"en":found;}
 public static String select(String saved,String... systemTags){
  if(saved!=null&&!saved.isEmpty()&&!saved.equals("system"))return resolve(saved);
  if(systemTags!=null)for(String tag:systemTags){String found=supported(tag);if(found!=null)return found;}
  return "en";
 }
 public static boolean isRtl(String tag){return Arrays.asList("ar","he","fa","ur").contains(resolve(tag));}
 public static Locale displayLocale(String selected,String... systemTags){
  String chosen=resolve(selected);Locale base=Locale.forLanguageTag(chosen);
  if(systemTags!=null)for(String system:systemTags){
   if(system==null)continue;Locale current=Locale.forLanguageTag(system.replace('_','-'));
   if(!current.getCountry().isEmpty()&&chosen.equals(supported(system)))return new Locale.Builder().setLocale(base).setRegion(current.getCountry()).build();
  }
  return base;
 }
 private LanguageCatalog(){}
}
