package com.fabio.ricorda;

/** Durations use elapsed milliseconds, independent of recurring calendar dates. */
public final class AlarmTiming {
    public static final long TEN_MINUTES=10*60*1000L;
    public static long snoozeAt(long now){return Math.addExact(now,TEN_MINUTES);}
    public static boolean active(long expires,long now){return expires>now;}
    private AlarmTiming(){}
}
