package com.fabio.ricorda;
import java.text.Normalizer;
import java.util.Locale;
public final class SaintText {
 public static String searchable(String value){return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replace('’','\'').trim();}
 public static String abbreviated(String value,int max){if(max<3)throw new IllegalArgumentException("At least three characters required");String label=value.replaceFirst("(?i)^(ss?\\.\\s*|san |santa |santi |santo |sante |sant[’']|beato |beata |beati |beate )","");return label.length()<=max?label:label.substring(0,max-2)+"..";}
 private static java.util.Set<String> words(String value){java.util.Set<String> words=new java.util.HashSet<>();java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("[\\p{L}\\p{N}]+").matcher(searchable(value));while(matcher.find())words.add(matcher.group());return words;}
 public static boolean matchesKeyword(String name,String query){java.util.Set<String> wanted=words(query);wanted.removeAll(java.util.Arrays.asList("s","ss","san","santa","santo","santi","sante","sant"));return !wanted.isEmpty()&&words(name).containsAll(wanted);}
 private SaintText(){}
}
