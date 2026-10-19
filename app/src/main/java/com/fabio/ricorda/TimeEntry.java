package com.fabio.ricorda;

import java.time.LocalTime;

/** Accept short, whole numbers only, including localized decimal digits. */
public final class TimeEntry {
    public static int number(String text,int max){
        if(text==null||text.isEmpty()||text.length()>2)return -1;
        int value=0;
        for(int i=0;i<text.length();i++){int digit=Character.digit(text.charAt(i),10);if(digit<0)return -1;value=value*10+digit;}
        return value<=max?value:-1;
    }
    public static LocalTime parse(String hour,String minute){
        int h=number(hour,23),m=number(minute,59);
        return h<0||m<0?null:LocalTime.of(h,m);
    }
    private TimeEntry(){}
}
