package com.fabio.ricorda;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Artwork only: Android Switch retains gestures, keyboard and accessibility semantics. */
final class SwitchArtwork extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ThemePalette palette;
    private final boolean thumb;
    private final float density;
    private int alpha=255;
    SwitchArtwork(Context context,ThemePalette palette,boolean thumb){
        this.palette=palette;this.thumb=thumb;density=context.getResources().getDisplayMetrics().density;
    }
    private int dp(float value){return Math.round(value*density);}
    @Override public int getIntrinsicWidth(){return dp(thumb?24:56);}
    @Override public int getIntrinsicHeight(){return dp(32);}
    @Override public boolean getPadding(Rect padding){padding.set(thumb?0:dp(4),0,thumb?0:dp(4),0);return !thumb;}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] state){invalidateSelf();return true;}
    @Override public void draw(Canvas canvas){
        boolean checked=false,enabled=false;
        for(int state:getState()){if(state==android.R.attr.state_checked)checked=true;if(state==android.R.attr.state_enabled)enabled=true;}
        Rect bounds=getBounds();paint.setStyle(Paint.Style.FILL);
        paint.setColor(thumb?(checked?palette.onAccent:palette.muted):(checked?palette.accent:palette.soft));
        paint.setAlpha(enabled?alpha:Math.round(alpha*.45f));
        if(thumb){
            // Switch assigns the entire track height to its thumb. Draw a true circle
            // inside those bounds, with equal insets above and below.
            float radius=Math.min(dp(12),Math.min(bounds.width(),bounds.height())/2f);
            canvas.drawCircle(bounds.exactCenterX(),bounds.exactCenterY(),radius,paint);
        }else{
            float stroke=dp(1),inset=stroke/2f;
            RectF pill=new RectF(bounds.left+inset,bounds.top+inset,bounds.right-inset,bounds.bottom-inset);
            float radius=pill.height()/2f;canvas.drawRoundRect(pill,radius,radius,paint);
            if(!checked||palette.white){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);paint.setColor(palette.border);paint.setAlpha(enabled?alpha:Math.round(alpha*.45f));canvas.drawRoundRect(pill,radius,radius,paint);}
        }
    }
    @Override public void setAlpha(int alpha){this.alpha=alpha;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
