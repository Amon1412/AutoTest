package com.humang.script_launcher.accessibility_script;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;

/**
 * @author : created by amon
 * 时间 : 2022/8/11 10
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class RobService extends AccessibilityService{
    AccessibilityNodeInfo nodeInfo;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //界面点击
                Log.d("amon_rob","界面点击: ");
                nodeInfo = event.getSource();

                if (nodeInfo != null) {
                    event.getWindowId();
                    AccessibilityRecord record = event.getRecord(event.getCurrentItemIndex());
                    int windowId = nodeInfo.getWindowId();
                    CharSequence text = nodeInfo.getText();
                    CharSequence className = nodeInfo.getClassName();
                    Log.d("amon_rob","record: "+record.toString());
                    Log.d("amon_rob","windowId: "+windowId);
                    Log.d("amon_rob","getText: "+text);
                    Log.d("amon_rob","getClassName: "+className);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                //界面文字改动
                Log.d("amon_rob","界面文字改动: ");
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {
        if (node.getChildCount() == 0) {
            if (node.getText() != null) {
                    return node;
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    recycle(node.getChild(i));
                }
            }
        }
        return node;
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.packageNames = new String[]{"com.ss.android.ugc.aweme"};
        serviceInfo.notificationTimeout=100;
        setServiceInfo(serviceInfo);
    }

}
