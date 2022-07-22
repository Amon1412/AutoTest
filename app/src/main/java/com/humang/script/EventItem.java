package com.humang.script;

import android.view.View;

/**
 * @author : created by amon
 * 时间 : 2022/7/14 14
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class EventItem {
    private float x;
    private float y;
    private View view;
    private String text;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
