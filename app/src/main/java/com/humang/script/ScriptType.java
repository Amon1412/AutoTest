package com.humang.script;

/**
 * @author : created by amon
 * 时间 : 2022/7/13 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public interface ScriptType {
    int CLICK_EVENT = 0x0010;
    int LONGCLICK_EVENT = 0x0011;
    int SWIPE_EVENT = 0x0012;
    int INPUT_EVENT = 0x0013;

    int SLEEP_ACTION = 0x0021;
    int PAUSE_ACTION = 0x0022;
    int HOME_ACTION = 0x0023;
    int BACK_ACTION = 0x0024;
    int START_ACTIVITY_ACTION = 0x0025;
    int LOOP_START_ACTION = 0x0026;
    int LOOP_END_ACTION = 0x0027;
}
