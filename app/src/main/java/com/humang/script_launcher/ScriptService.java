package com.humang.script_launcher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.humang.script_launcher.excute_script.MainActivity;
import com.humang.script_launcher.utils.FileUtil;
import com.humang.script_launcher.utils.ScriptUtil;

import java.util.List;

public class ScriptService extends Service implements View.OnClickListener {

    private ScriptUtil mScriptUtil;
    private Context mContext;
    private String scriptName;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams mMasklayoutParams;
    
    public View mMenuView;
    private View mMaskView;
    public TextView mLogView;
    public TextView mPerformanceView;

    private boolean isShowLog;
    private boolean isShowPerformance;
    private boolean startImmediately;

    private int menuViewX = 0;
    private int menuViewY = 550;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MessageType.EXCUTE_MENU:
                    showMenu();
                    break;
                case MessageType.EXCUTE_START:
                    log("启动脚本：" + scriptName);
                    FileUtil fileUtil = new FileUtil();
                    String s = fileUtil.readFile(mContext, scriptName);
                    List<String> strings = fileUtil.parseFile(s);
                    mScriptUtil.excuteScript(strings);
                    hideMenu();
                    break;
                case MessageType.EXCUTE_PAUSE:
                    log("脚本暂停中\n",true);
                    mScriptUtil.pauseScript();
                    hideMenu();
                    break;
                case MessageType.EXCUTE_STOP:
                    log("脚本已停止");
                    mScriptUtil.stopScript();
                    hideMenu();
                    break;
                case MessageType.EXCUTE_SETTING:
                    mScriptUtil.stopScript();
                    log("打开设置界面");
                    hideMenu();
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.putExtra("isShowLog",isShowLog);
                    intent.putExtra("isShowPerformance",isShowPerformance);
                    mContext.startActivity(intent);
                    clearView();
                    onDestroy();
                    break;
                case MessageType.EXCUTE_CLOSE:
                    mScriptUtil.stopScript();
                    log("脚本关闭");
                    clearView();
                    onDestroy();
                    break;
                case MessageType.EXCUTE_COMPLETE:
                    log("脚本执行结束");
                    break;
                case MessageType.SHOW_LOG:
                    String log = message.getData().getString("log");
                    boolean append = message.getData().getBoolean("append");
                    showLog(log,append);
                    break;
                case MessageType.EXCUTE_REBOOT:
                    SharedPreferences sp = getApplication().getSharedPreferences("humang_script",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("scriptName",scriptName);
                    editor.apply();
                    reboot();
                    break;
                case MessageType.SHOW_PERFORMANCE:
                    String performance = message.getData().getString("performance");
                    showPerformance(performance);
                    break;
                default:
                    break;

            }
            return false;
        }
    });
    private void popupDialog(int scriptType) {
        TextView desTV = new TextView(this);
        desTV.setText("请输入操作描述：");
        EditText desET = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setView(desTV)
                .setView(desET)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ScriptService.this, "你输入的是: " + desET.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setCancelable(true);
        builder.create().show();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            String scriptName = extras.getString("scriptName");
            boolean isShowLog = extras.getBoolean("isShowLog");
            boolean isShowPerformance = extras.getBoolean("isShowPerformance");
            boolean startImmediately = extras.getBoolean("startImmediately");
            this.scriptName = scriptName;
            this.isShowLog = isShowLog;
            this.isShowPerformance = isShowPerformance;
            this.startImmediately = startImmediately;
        }

        this.mContext = this;
        mScriptUtil = ScriptUtil.getInstance(this,mHandler);

        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        initView();

        if (startImmediately) {
            startScript();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void reboot() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        pm.reboot("执行adb reboot命令");
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

    public void showPerformance(String performance) {
        if (isShowPerformance && mPerformanceView != null) {
            mPerformanceView.setText(performance);
        }
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
        addMaskView();
        addMenuView();
        if (isShowLog) {
            addLogView();
        }
        if (isShowPerformance) {
            addPerformanceView();
        }
    }
    private void clearView() {
        clearMaskView();
        clearMenuView();
        clearLogView();
        clearPerformanceView();
    }

    @SuppressLint("WrongConstant")
    private void addMenuView() {
        mMenuView = LayoutInflater.from(mContext).inflate(R.layout.fooview_menu,null);
        mMenuView.setOnTouchListener(new FloatingOnTouchListener());
        Button menu = mMenuView.findViewById(R.id.menu_bt);
        menu.setOnClickListener(this);
        Button start = mMenuView.findViewById(R.id.start_bt);
        start.setOnClickListener(this);
        Button pause = mMenuView.findViewById(R.id.pause_bt);
        pause.setOnClickListener(this);
        Button stop = mMenuView.findViewById(R.id.stop_bt);
        stop.setOnClickListener(this);
        Button setting = mMenuView.findViewById(R.id.setting_bt);
        setting.setOnClickListener(this);
        Button close = mMenuView.findViewById(R.id.close_bt);
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
        layoutParams.x = menuViewX;
        layoutParams.y = menuViewY;
        windowManager.addView(mMenuView, layoutParams);
    }

    @SuppressLint("WrongConstant")
    private void addMaskView() {
        mMaskView = new View(this);
        mMaskView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideMenu();
                return false;
            }
        });
        mMaskView.setBackgroundColor(getColor(R.color.transparent));
        mMasklayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, PixelFormat.TRANSPARENT);
        mMasklayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMasklayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mMasklayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mMasklayoutParams.format = PixelFormat.TRANSLUCENT;
        mMasklayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mMasklayoutParams.x = 0;
        mMasklayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mMaskView, mMasklayoutParams);
    }
    private void setMaskTouchable(boolean isTouchable) {
        mMasklayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (isTouchable) {
            mMasklayoutParams.flags ^= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        windowManager.updateViewLayout(mMaskView, mMasklayoutParams);
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
    @SuppressLint("ResourceAsColor")
    private void addPerformanceView() {
        mPerformanceView = new TextView(mContext);
        mPerformanceView.setTextColor(R.color.red);
        mPerformanceView.setTextSize(20);
        mPerformanceView.setGravity(Gravity.CENTER);
        mPerformanceView.setBackgroundColor(getColor(R.color.mask));
        WindowManager.LayoutParams layoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.x = 100;
        layoutParams.y = 100;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowManager.addView(mPerformanceView, layoutParams);
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
                    menuViewX = layoutParams.x;
                    menuViewY = layoutParams.y;

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private void clearMenuView() {
        try {
            if (mMenuView != null && windowManager != null) {
                windowManager.removeView(mMenuView);
            }
        } catch (Exception e) {

        }
    }
    private void clearMaskView() {
        try {
            if (mMaskView != null && windowManager != null) {
                windowManager.removeView(mMaskView);
            }
        } catch (Exception e) {

        }
    }

    private void clearLogView (){
        try {
            if (mLogView != null && windowManager != null) {
                windowManager.removeView(mLogView);
            }
        } catch (Exception e) {

        }
    }
    private void clearPerformanceView (){
        try {
            if (mPerformanceView != null && windowManager != null) {
                windowManager.removeView(mPerformanceView);
            }
        } catch (Exception e) {

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

    private void hideMenu() {
        mMenuView.findViewById(R.id.menu_bt).setVisibility(View.VISIBLE);
        mMenuView.findViewById(R.id.function).setVisibility(View.INVISIBLE);
        setMaskTouchable(false);
    }
    private void showMenu() {
        mMenuView.findViewById(R.id.menu_bt).setVisibility(View.INVISIBLE);
        mMenuView.findViewById(R.id.function).setVisibility(View.VISIBLE);
        setMaskTouchable(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}