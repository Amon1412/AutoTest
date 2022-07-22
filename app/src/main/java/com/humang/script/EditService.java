package com.humang.script;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.humang.script.utils.FileUtil;
import com.humang.script.utils.ScriptUtil;
import com.humang.script.utils.ShellUtil;

import java.util.List;

/**
 * @author : created by amon
 * 时间 : 2022/7/21 11
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class EditService extends Service implements View.OnClickListener {
    private WindowManager windowManager;
    private WindowManager.LayoutParams mMenuLayoutParams;
    private WindowManager.LayoutParams mMaskLayoutParams;
    private WindowManager.LayoutParams mDetailLayoutParams;
    private WindowManager.LayoutParams mDialogLayoutParams;
    private View mMenuView;
    private View mMaskView;
    private View mDetailView;
    private View mDialogView;

    private float downX,downY;
    private float upX,upY;
    private long downTime,upTime;

    private String inputText = "";

    private int loopTag = 1;
    
    StringBuffer sb = new StringBuffer();

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageType.MASK_TOUCHABLE:
                    setMaskTouchable(true);
                    break;
                case MessageType.MASK_NOT_TOUCHABLE:
                    setMaskTouchable(false);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("amon", "EditService onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMaskView();
        initMenuView();
        return super.onStartCommand(intent, flags, startId);
    }

    

    private void showDialog(int scriptType){
        mDialogView = LayoutInflater.from(this).inflate(R.layout.fooview_dialog, null);
        if (scriptType == ScriptType.INPUT_EVENT) {
            mDialogView.findViewById(R.id.input_log).setVisibility(View.VISIBLE);

            Settings.Secure.putString(getContentResolver()
                    ,Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.inputmethod.pinyin/.PinyinIME");
            mDialogView.findViewById(R.id.input_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = ((EditText)mDialogView.findViewById(R.id.input_text)).getText().toString();
                    inputText = text;
                    Settings.Secure.putString(getContentResolver()
                            ,Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.adbkeyboard/.AdbIME");
                    removeDialog();
                }
            });
        } else if (scriptType == ScriptType.START_ACTIVITY_ACTION) {
            mDialogView.findViewById(R.id.start_activity_log).setVisibility(View.VISIBLE);
            Settings.Secure.putString(getContentResolver()
                    ,Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.inputmethod.pinyin/.PinyinIME");
            mDialogView.findViewById(R.id.start_activity_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String component = ((EditText)mDialogView.findViewById(R.id.start_activity_text)).getText().toString();
                    execute(String.format("am start %s", component));
                    removeDialog();
                }
            });
        } else if (scriptType == ScriptType.SLEEP_ACTION) {
            mDialogView.findViewById(R.id.sleep_log).setVisibility(View.VISIBLE);
            Settings.Secure.putString(getContentResolver()
                    ,Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.inputmethod.pinyin/.PinyinIME");
            mDialogView.findViewById(R.id.sleep_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String time = ((EditText)mDialogView.findViewById(R.id.sleep_text)).getText().toString();
                    sb.append(String.format("timeout /T %s /NOBREAK", time)).append("\n");
                    removeDialog();
                }
            });
        }

        mDialogLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDialogLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mDialogLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mDialogLayoutParams.format = PixelFormat.TRANSLUCENT;
        mDialogLayoutParams.gravity = Gravity.CENTER;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mDialogView, mDialogLayoutParams);
    }

    private void removeDialog() {
        if (windowManager != null && mDialogView != null) {
            windowManager.removeView(mDialogView);
        }
    }



    private void sendEvent(int scriptType) {
        mHandler.sendMessage(mHandler.obtainMessage(MessageType.MASK_NOT_TOUCHABLE));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    String cmd = "";
                    String result = "";
                    switch (scriptType) {
                        case ScriptType.CLICK_EVENT:
                            cmd = String.format("input tap %s %s", downX,downY);
                            break;
                        case ScriptType.LONGCLICK_EVENT:
                        case ScriptType.SWIPE_EVENT:_EVENT:
                            cmd = String.format("input swipe %s %s %s %s %s", downX,downY,upX,upY,upTime-downTime);
                            break;
                        default:
                            break;
                    }
                    result = execute(cmd);

                    if (!inputText.equals("")) {
                        cmd = String.format("am broadcast -a ADB_INPUT_TEXT --es msg %s",inputText);
                        inputText = "";
                        result = execute(cmd);
                    }
                    int retryTimes = 10;
                    while (retryTimes > 0 &&result.contains("Exception")) {
                        retryTimes -= 1;
                        result = execute(cmd);
                        Log.d("amon", "retryTimes: "+retryTimes);
                    }
                    Log.d("amon", "cmd: "+cmd);
                    Log.d("amon", "result: "+result);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(MessageType.MASK_TOUCHABLE));
            }
        }).start();
    }
    
    private String execute(String cmd) {
        return execute(cmd,true);
    }
    private String execute(String cmd,boolean remenber) {
        if (remenber) {
            sb.append(cmd).append("\n");
        }
        return ShellUtil.getInstance().execute(cmd.split(" "));
    }

    private void initMenuView() {
        mMenuView = LayoutInflater.from(this).inflate(R.layout.fooview_edit,null);
        mMenuView.findViewById(R.id.show_cmds).setOnClickListener(this);
        mMenuView.findViewById(R.id.input_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.loop_start_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.loop_end_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.start_activity_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.home_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.back_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.sleep_bt).setOnClickListener(this);

        mMenuLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mMenuLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMenuLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mMenuLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mMenuLayoutParams.format = PixelFormat.TRANSLUCENT;
        mMenuLayoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        mMenuLayoutParams.x = 0;
        mMenuLayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mMenuView, mMenuLayoutParams);
    }

    private void initDetailView() {
        mDetailView = LayoutInflater.from(this).inflate(R.layout.fooview_edit,null);
        mDetailLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mDetailLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDetailLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mDetailLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mDetailLayoutParams.format = PixelFormat.TRANSLUCENT;
        mDetailLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        mDetailLayoutParams.x = 1080;
        mDetailLayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mDetailView, mDetailLayoutParams);
    }

    private void initMaskView() {
        mMaskView = new View(this);
        mMaskView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = motionEvent.getX();
                    downY = motionEvent.getY();
                    downTime = System.currentTimeMillis();
                    Log.d("amon", "onTouchDown: "+"clickX: "+ downX +"  clickY:"+ downY + "  downTime" + downTime);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    upX = motionEvent.getX();
                    upY = motionEvent.getY();
                    upTime = System.currentTimeMillis();
                    if (upX - downX < 10 && upY - downY < 10){
                        if (upTime - downTime < 100) {
                            sendEvent(ScriptType.CLICK_EVENT);
                        } else {
                            sendEvent(ScriptType.LONGCLICK_EVENT);
                        }
                    } else {
                        sendEvent(ScriptType.SWIPE_EVENT);
                    }
                    Log.d("amon", "onTouchUp: "+"clickX: "+ upX +"  clickY:"+ upY + "  downTime" + upTime);
                }
                return false;
            }
        });
        mMaskView.setBackgroundColor(getColor(R.color.mask));
        mMaskLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, PixelFormat.TRANSPARENT);
        mMaskLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMaskLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mMaskLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mMaskLayoutParams.format = PixelFormat.TRANSLUCENT;
        mMaskLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mMaskLayoutParams.x = 0;
        mMaskLayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mMaskView, mMaskLayoutParams);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_cmds:
                Log.e("amon", "cmds: \n"+sb);
                break;
            case R.id.input_bt:
                showDialog(ScriptType.INPUT_EVENT);
                break;
            case R.id.start_activity_bt:
                showDialog(ScriptType.START_ACTIVITY_ACTION);
                break;
            case R.id.home_bt:
                execute("input keyevent 3");
                break;
            case R.id.back_bt:
                execute("input keyevent 4");
                break;
            case R.id.loop_start_bt:
                sb.append(":loop"+loopTag++).append("\n");
                break;
            case R.id.loop_end_bt:
                sb.append("goto loop"+--loopTag).append("\n");
                break;
            case R.id.sleep_bt:
                showDialog(ScriptType.SLEEP_ACTION);
                break;
            default:
                return;
        }
    }

    private void setMaskTouchable(boolean isTouchable) {
        mMaskLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (isTouchable) {
            mMaskLayoutParams.flags ^= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        mMaskLayoutParams.alpha = 1;
        windowManager.updateViewLayout(mMaskView, mMaskLayoutParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
