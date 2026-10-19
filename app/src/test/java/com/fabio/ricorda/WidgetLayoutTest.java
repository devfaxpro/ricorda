package com.fabio.ricorda;
import org.junit.Test;
import static org.junit.Assert.*;
public class WidgetLayoutTest {
 @Test public void rowsAlwaysFitBodyAcrossPhoneSizes(){for(int width:new int[]{160,180,240,320,480,720})for(int height=120;height<=640;height+=10)for(float scale:new float[]{.85f,1,1.3f,1.5f,2,3}){WidgetLayout l=WidgetLayout.of(width,height,scale);assertTrue(l.rows*l.rowHeight<=l.bodyHeight);assertTrue(l.rows<=6);assertTrue(l.rowHeight>=48);assertTrue(l.footerHeight>=48);}}
 @Test public void compactViewDoesNotSpendSpaceOnBrand(){WidgetLayout l=WidgetLayout.of(160,140,1);assertFalse(l.brand);assertFalse(l.date);assertTrue(l.footer);}
 @Test public void expandingShowsMoreReminders(){assertTrue(WidgetLayout.of(300,500,1).rows>WidgetLayout.of(300,200,1).rows);}
 @Test public void largeTextKeepsTouchTargetsAndReducesDetails(){WidgetLayout regular=WidgetLayout.of(300,350,1),large=WidgetLayout.of(300,350,2);assertTrue(large.rows<regular.rows);assertTrue(large.rowHeight>regular.rowHeight);assertEquals(regular.bodySp,large.bodySp);}
 @Test public void verySmallWidgetStillHasAHeading(){WidgetLayout l=WidgetLayout.of(160,90,1);assertFalse(l.footer);assertEquals(0,l.rows);assertTrue(l.titleSp>=16);}
 @Test public void landscapeUsesItsOwnBudget(){assertTrue(WidgetLayout.of(220,360,1).rows>WidgetLayout.of(420,150,1).rows);}
 @Test public void wideTallSaintHasLargerText(){assertTrue(WidgetLayout.of(320,260,1).saintSp>WidgetLayout.of(160,120,1).saintSp);}
 @Test public void saintCompactRetainsNameAndAtLeastOneLine(){WidgetLayout l=WidgetLayout.of(160,120,3);assertTrue(l.saintLines>=1);assertFalse(l.saintTitle);assertFalse(l.saintLink);}
 @Test public void invalidHostDimensionsAreBounded(){for(float scale:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY}){WidgetLayout l=WidgetLayout.of(-20,-40,scale);assertEquals(0,l.rows);assertFalse(l.footer);assertTrue(l.saintLines>=1);}}
}
