package com.humang.script_launcher;

/**
 * @author : created by amon
 * 时间 : 2022/7/6 13
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public interface MessageType {
    int EXCUTE_MENU = 0;
    int EXCUTE_START = 1;
    int EXCUTE_PAUSE = 2;
    int EXCUTE_STOP = 3;
    int EXCUTE_SETTING = 4;
    int EXCUTE_CLOSE = 5;
    int EXCUTE_COMPLETE = 6;
    int EXCUTE_FAILURED = 7;
    int EXCUTE_CMD = 8;
    int SHOW_LOG = 10;
    int SHOW_PERFORMANCE = 11;
    int SAVE = 12;

    int MASK_TOUCHABLE = 41;
    int MASK_NOT_TOUCHABLE = 42;
    int UPDATE_DETAIL = 43;
}
