package com.humang.script;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.humang.script.utils.FileUtil;
import com.humang.script.utils.ScriptUtil;

import java.util.List;

public class ScriptService extends Service implements View.OnClickListener {
    private ScriptUtil mScriptUtil;
    private Context mContext;
    public View mFootView;
    public TextView mLogView;
    private String scriptName;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private boolean isShowLog;

    private int fooviewX = 0;
    private int fooviewY = 550;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MessageType.EXCUTE_MENU:
                    hideMenu();
                    break;
                case MessageType.EXCUTE_START:
                    log("启动脚本：" + scriptName);
                    FileUtil fileUtil = new FileUtil();
                    String s = fileUtil.readFile(mContext, "script/"+scriptName);
                    List<String> strings = fileUtil.parseFile(s);
                    mScriptUtil.excuteScript(strings);
                    showMenu();
                    break;
                case MessageType.EXCUTE_PAUSE:
                    log("脚本暂停中\n",true);
                    mScriptUtil.pauseScript();
                    showMenu();
                    break;
                case MessageType.EXCUTE_STOP:
                    log("脚本已停止");
                    mScriptUtil.stopScript();
                    showMenu();
                    break;
                case MessageType.EXCUTE_SETTING:
                    log("打开设置界面");
                    showMenu();
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.putExtra("isShowLog",isShowLog);
                    mContext.startActivity(intent);
                    clearView();
                    break;
                case MessageType.EXCUTE_CLOSE:
                    log("脚本关闭");
                    clearView();
                    onDestroy();
                    break;
                case MessageType.EXCUTE_COMPLETE:
//                    log("脚本执行结束");
                    break;
                case MessageType.SHOW_LOG:
                    String log = message.getData().getString("log");
                    boolean append = message.getData().getBoolean("append");
                    showLog(log,append);
                    break;
                default:
                    break;

            }
            return false;
        }
    });

    public ScriptService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        String scriptName = extras.getString("scriptName");
        boolean isShowLog = extras.getBoolean("isShowLog");

        this.mContext = this;
        mScriptUtil = ScriptUtil.getInstance(this,mHandler);
        this.scriptName = scriptName;
        this.isShowLog = isShowLog;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        initView();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void log(String log) {
        log(log,false);
    }

    private void log(String log,boolean append) {
        Log.e("humang_script",log);
        Message message = mHandler.obtainMessage(MessageType.SHOW_LOG);
        Bundle bundle = new Bundle();
        bundle.putString("log",log);
        bundle.putBoolean("append",append);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void showLog(String log,boolean append) {
        if (isShowLog && mLogView != null) {
            if (append) {
                log += mLogView.getText();
            }
            mLogView.setText(log);
        }
    }

    public void initView() {
        addFooView();
        if (isShowLog) {
            addLogView();
        }
    }
    private void clearView() {
        clearFooView();
        clearLogView();
    }

    private void addFooView() {
        mFootView = LayoutInflater.from(mContext).inflate(R.layout.fooview,null);
        mFootView.setOnTouchListener(new FloatingOnTouchListener());
        Button menu = mFootView.findViewById(R.id.menu_bt);
        menu.setOnClickListener(this);
        Button start = mFootView.findViewById(R.id.start_bt);
        start.setOnClickListener(this);
        Button pause = mFootView.findViewById(R.id.pause_bt);
        pause.setOnClickListener(this);
        Button stop = mFootView.findViewById(R.id.stop_bt);
        stop.setOnClickListener(this);
        Button setting = mFootView.findViewById(R.id.setting_bt);
        setting.setOnClickListener(this);
        Button close = mFootView.findViewById(R.id.close_bt);
        close.setOnClickListener(this);
        layoutParams = new WindowManager
                .LayoutParams(50, 290, 0, 0, PixelFormat.TRANSPARENT);
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.x = fooviewX;
        layoutParams.y = fooviewY;
        windowManager.addView(mFootView, layoutParams);
    }

    @SuppressLint("ResourceAsColor")
    private void addLogView() {
        mLogView = new TextView(mContext);
        mLogView.setTextColor(R.color.red);
        mLogView.setTextSize(20);
        mLogView.setGravity(Gravity.CENTER);
        mLogView.setText("当前执行脚本为： " + scriptName);
        WindowManager.LayoutParams layoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.CENTER;
        windowManager.addView(mLogView, layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_bt:
                menu();
                break;
            case R.id.start_bt:
                startScript();
                break;
            case R.id.pause_bt:
                pauseScript();
                break;
            case R.id.stop_bt:
                stopScript();
                break;
            case R.id.setting_bt:
                settingScript();
                break;
            case R.id.close_bt:
                closeScript();
                break;
            default:
                break;
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int startX;
        private int startY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - startX;
                    int movedY = nowY - startY;
                    startX = nowX;
                    startY = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    fooviewX = layoutParams.x;
                    fooviewY = layoutParams.y;

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private void clearFooView() {
        if (mFootView != null && windowManager != null) {
            windowManager.removeView(mFootView);
        }
    }


    private void clearLogView (){
        if (mLogView != null && windowManager != null) {
            windowManager.removeView(mLogView);
        }
    }

    public void menu() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_MENU);
        mHandler.sendMessage(message);
    }

    public void startScript() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_START);
        mHandler.sendMessage(message);
    }

    public void pauseScript() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_PAUSE);
        mHandler.sendMessage(message);
    }

    public void stopScript() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_STOP);
        mHandler.sendMessage(message);
    }

    public void settingScript() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_SETTING);
        mHandler.sendMessage(message);
    }

    public void closeScript() {
        Message message = mHandler.obtainMessage(MessageType.EXCUTE_CLOSE);
        mHandler.sendMessage(message);
    }

    private void showMenu() {
        mFootView.findViewById(R.id.menu_bt).setVisibility(View.VISIBLE);
        mFootView.findViewById(R.id.function).setVisibility(View.INVISIBLE);
    }
    private void hideMenu() {
        mFootView.findViewById(R.id.menu_bt).setVisibility(View.INVISIBLE);
        mFootView.findViewById(R.id.function).setVisibility(View.VISIBLE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}