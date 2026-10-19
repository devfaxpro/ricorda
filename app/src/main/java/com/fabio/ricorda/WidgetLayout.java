package com.fabio.ricorda;

/** Size budget in dp. Text scaling reduces detail instead of shrinking touch targets. */
public final class WidgetLayout {
    public static final int MAX_ROWS=6;
    public final int padding,bodySp,titleSp,rowHeight,footerHeight,headerHeight,rows,bodyHeight;
    public final boolean brand,date,footer,saintTitle,saintLink;
    public final int saintSp,saintLines;
    private WidgetLayout(int width,int height,float scale){
        scale=Float.isFinite(scale)&&scale>0?Math.max(.85f,Math.min(3,scale)):1;
        width=Math.max(1,width);height=Math.max(1,height);
        padding=width>=280&&height>=240?18:12;
        bodySp=width>=280?16:14;titleSp=width>=280?18:16;
        brand=width>=220&&height>=260*scale;
        date=width>=210&&height>=220*scale;
        int titleHeight=(int)Math.ceil(titleSp*scale*1.45f)+4;
        headerHeight=titleHeight+(brand||date?(int)Math.ceil(18*scale*1.4f)+6:0);
        rowHeight=Math.max(48,(int)Math.ceil(bodySp*scale*2.6f)+8);
        footerHeight=Math.max(48,(int)Math.ceil(14*scale*2.6f)+12);
        footer=height>=2*padding+headerHeight+footerHeight+rowHeight/2;
        bodyHeight=Math.max(0,height-2*padding-headerHeight-(footer?footerHeight:0)-8);
        rows=Math.max(0,Math.min(MAX_ROWS,bodyHeight/rowHeight));
        saintSp=width>=280&&height>=230?24:width>=210&&height>=180?20:16;
        int saintLine=(int)Math.ceil(saintSp*scale*1.5f);
        int available=height-2*padding-(brand?(int)Math.ceil(18*scale*1.4f):0)-(date?(int)Math.ceil(12*scale*1.4f)+3:0)-8;
        saintTitle=available>=saintLine+(int)Math.ceil(13*scale*1.5f);
        if(saintTitle)available-=(int)Math.ceil(13*scale*1.5f);
        saintLink=available>=saintLine*2+(int)Math.ceil(12*scale*1.5f);
        if(saintLink)available-=(int)Math.ceil(12*scale*1.5f);
        saintLines=Math.max(1,Math.min(8,available/saintLine));
    }
    public static WidgetLayout of(int width,int height,float fontScale){return new WidgetLayout(width,height,fontScale);}
}
