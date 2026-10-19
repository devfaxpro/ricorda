package com.fabio.ricorda;

/** Opaque, named sRGB swatches shared by every app surface. */
public final class ThemePalette {
    public static final String[] IDS={"sage","blue","amber","pink","lavender","ruby","chocolate","blueberry","white","emerald","mint","turquoise","sky","sapphire","amethyst","fuchsia","coral","tangerine","lemon","lime","apricot"};
    private static final int[] SWATCHES={0xff87a878,0xff6495ed,0xffffbf00,0xfff34f8b,0xffb57edc,0xffd20a3c,0xff7b3f00,0xff4d47a8,0xffffffff,0xff00a86b,0xff3ddca0,0xff00b8a9,0xff38bdf8,0xff0f52ba,0xff7d3c98,0xffff00ff,0xffff6b6b,0xffff8c00,0xfffde047,0xffa3e635,0xffffb57b};
    public final int accent,onAccent,action,ink,muted,background,surface,soft,border;
    public final boolean white;
    public final String id;

    private ThemePalette(String id,int accent,boolean dark){
        this.id=id;this.accent=accent;white=id.equals("white");
        background=dark?0xff182331:0xffffffff;
        surface=dark?0xff223144:0xffffffff;
        soft=dark?0xff2b3c50:0xfff3f6fa;
        ink=dark?0xfff4f7fc:0xff24364d;
        muted=dark?0xffc3cedc:0xff56677c;
        border=dark?0xff8798ad:0xff788ba2;
        int deepBlue=0xff14243b;
        onAccent=contrast(accent,0xffffffff)>=contrast(accent,deepBlue)?0xffffffff:deepBlue;
        // Bright swatches remain untouched. Text uses neutral ink when necessary.
        action=contrast(accent,surface)>=4.5&&contrast(accent,soft)>=4.5?accent:ink;
    }
    public static ThemePalette of(String id,boolean dark){
        for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return new ThemePalette(id,SWATCHES[i],dark);
        return new ThemePalette(IDS[0],SWATCHES[0],dark);
    }
    public static double contrast(int a,int b){
        double x=luminance(a),y=luminance(b);return (Math.max(x,y)+.05)/(Math.min(x,y)+.05);
    }
    private static double luminance(int color){return .2126*linear((color>>16)&255)+.7152*linear((color>>8)&255)+.0722*linear(color&255);}
    private static double linear(int channel){double v=channel/255.0;return v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4);}
}
