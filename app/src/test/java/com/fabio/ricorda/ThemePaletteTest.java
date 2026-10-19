package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThemePaletteTest {
 @Test public void twelveNewColorsExtendTheOriginalNineWithoutChangingTheirSwatches(){
  assertEquals(21,ThemePalette.IDS.length);assertEquals(21,new java.util.HashSet<>(java.util.Arrays.asList(ThemePalette.IDS)).size());
  String[] original={"sage","blue","amber","pink","lavender","ruby","chocolate","blueberry","white"};int[] expected={0xff87a878,0xff6495ed,0xffffbf00,0xfff34f8b,0xffb57edc,0xffd20a3c,0xff7b3f00,0xff4d47a8,0xffffffff};
  for(int i=0;i<original.length;i++)assertEquals(expected[i],ThemePalette.of(original[i],false).accent);
  for(String id:new String[]{"emerald","mint","turquoise","sky","sapphire","amethyst","fuchsia","coral","tangerine","lemon","lime","apricot"})assertEquals(id,ThemePalette.of(id,false).id);
 }
 @Test public void swatchesNeverChangeWithDisplayMode(){
  for(String id:ThemePalette.IDS){ThemePalette light=ThemePalette.of(id,false),dark=ThemePalette.of(id,true);assertEquals(id,light.accent,dark.accent);assertEquals(255,light.accent>>>24);assertNotEquals(0xff000000,light.accent);}
 }
 @Test public void pureWhiteHasVisibleTextAndOutline(){
  ThemePalette p=ThemePalette.of("white",false);assertEquals(0xffffffff,p.accent);assertEquals(0xffffffff,p.background);assertEquals(0xffffffff,p.surface);assertTrue(ThemePalette.contrast(p.onAccent,p.accent)>=4.5);assertTrue(ThemePalette.contrast(p.border,p.surface)>=3);
 }
 @Test public void textRemainsLegibleInEveryPaletteAndMode(){
  for(String id:ThemePalette.IDS)for(boolean dark:new boolean[]{false,true}){ThemePalette p=ThemePalette.of(id,dark);assertTrue(id+" button",ThemePalette.contrast(p.onAccent,p.accent)>=4.5);assertTrue(id+" action",ThemePalette.contrast(p.action,p.surface)>=4.5);assertTrue(id+" secondary action",ThemePalette.contrast(p.action,p.soft)>=4.5);assertTrue(id+" body",ThemePalette.contrast(p.ink,p.background)>=4.5);assertTrue(id+" secondary",ThemePalette.contrast(p.muted,p.surface)>=4.5);}
 }
 @Test public void savedIdentifiersArePreservedAndUnknownOnesUseSage(){
  for(String id:new String[]{"sage","blue","amber","pink","lavender","ruby","chocolate","blueberry"})assertEquals(id,ThemePalette.of(id,false).id);
  assertEquals("sage",ThemePalette.of("unknown",false).id);assertEquals("sage",ThemePalette.of(null,false).id);
 }
}
