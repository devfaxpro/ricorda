package com.fabio.ricorda;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

final class AccessibilityUi {
    private AccessibilityUi(){}
    static boolean touchExploration(Context context){
        AccessibilityManager manager=context.getSystemService(AccessibilityManager.class);
        return manager!=null&&manager.isEnabled()&&manager.isTouchExplorationEnabled();
    }
    static void label(TextView label,View control){
        if(control.getId()==View.NO_ID)control.setId(View.generateViewId());
        label.setLabelFor(control.getId());
    }
    static void button(View view,CharSequence description,boolean selected){
        view.setContentDescription(description);view.setFocusable(true);view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);view.setSelected(selected);
        if(Build.VERSION.SDK_INT>=28)view.setScreenReaderFocusable(true);
        view.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName("android.widget.Button");}
        });
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)group.getChildAt(i).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);}
    }
    static void restoreFocus(View view){
        if(touchExploration(view.getContext()))view.post(()->view.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,null));
    }
    static void heading(TextView view){if(Build.VERSION.SDK_INT>=28)view.setAccessibilityHeading(true);}
}
