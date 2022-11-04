package com.humang.script_launcher.monky_script;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.humang.script_launcher.MainActivity;
import com.humang.script_launcher.MessageType;
import com.humang.script_launcher.R;

public class MonkyService extends Service implements View.OnClickListener {

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    
    public View mMenuView;
    MonkyUtil monkyUtil;


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
                    monkyUtil.run();
                    hideMenu();
                    break;
                case MessageType.EXCUTE_PAUSE:
                    monkyUtil.pause();
                    hideMenu();
                    break;
                case MessageType.EXCUTE_STOP:
                    monkyUtil.stop();
                    hideMenu();
                    break;
                case MessageType.EXCUTE_SETTING:
                    hideMenu();
                    Intent intent = new Intent(MonkyService.this, MainActivity.class);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    MonkyService.this.startActivity(intent);
                    clearView();
                    onDestroy();
                    break;
                default:
                    break;

            }
            return false;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();
        monkyUtil = MonkyUtil.getInstance(this, mHandler);
        return super.onStartCommand(intent, flags, startId);
    }

    public void initView() {
        addMenuView();
    }
    private void clearView() {
        clearMenuView();
    }

    @SuppressLint("WrongConstant")
    private void addMenuView() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mMenuView = LayoutInflater.from(MonkyService.this).inflate(R.layout.fooview_menu,null);
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
            default:
                break;
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

    private void hideMenu() {
        mMenuView.findViewById(R.id.menu_bt).setVisibility(View.VISIBLE);
        mMenuView.findViewById(R.id.function).setVisibility(View.INVISIBLE);
    }
    private void showMenu() {
        mMenuView.findViewById(R.id.menu_bt).setVisibility(View.INVISIBLE);
        mMenuView.findViewById(R.id.function).setVisibility(View.VISIBLE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}