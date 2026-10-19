package com.fabio.ricorda;

import java.time.LocalDate;
import java.time.format.*;
import java.util.Locale;

/** Capitalize month names, including inflected forms, without changing other date words. */
public final class MonthLabels {
    private MonthLabels(){}
    public static String initialUpper(String word,Locale locale){
        if(word.isEmpty())return word;
        int end=word.offsetByCodePoints(0,1);
        return word.substring(0,end).toUpperCase(locale)+word.substring(end);
    }
    public static String format(LocalDate day,Locale locale,String pattern){
        DateTimeFormatterBuilder result=new DateTimeFormatterBuilder();
        boolean quoted=false;int from=0;
        for(int i=0;i<pattern.length();){
            char c=pattern.charAt(i);
            if(c=='\''){
                if(i+1<pattern.length()&&pattern.charAt(i+1)=='\''){i+=2;continue;}
                quoted=!quoted;i++;continue;
            }
            if(!quoted&&(c=='M'||c=='L')){
                int end=i+1;while(end<pattern.length()&&pattern.charAt(end)==c)end++;
                if(end-i>=3){
                    if(i>from)result.appendPattern(pattern.substring(from,i));
                    String month=day.format(DateTimeFormatter.ofPattern(pattern.substring(i,end),locale));
                    result.appendLiteral(initialUpper(month,locale));from=end;
                }
                i=end;
            }else i++;
        }
        if(from<pattern.length())result.appendPattern(pattern.substring(from));
        return day.format(result.toFormatter(locale));
    }
}
