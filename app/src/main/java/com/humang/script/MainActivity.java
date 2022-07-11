package com.humang.script;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.os.Build;
import android.content.Context;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.humang.script.utils.FileUtil;
import com.humang.script.utils.ScriptUtil;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    String[] scripts;
    Button fooView;
    int checkedRadioButtonId = -1;
    /** 悬浮窗权限标识码 */
    public static final int CODE_WINDOW = 0;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MessageType.EXCUTE_START:
                    Log.d("humang_script", "handleMessage: EXCUTE_START");
                    String filename = message.getData().getString("filename");
                    FileUtil fileUtil = new FileUtil();
                    String s = fileUtil.readFile(MainActivity.this, "script/"+filename);
                    List<String> strings = fileUtil.parseFile(s);
                    ScriptUtil.getInstance(MainActivity.this,mHandler).excuteScript(strings);
                    fooView.setText("stop");
                    break;
                case MessageType.EXCUTE_COMPLETE:
                    Log.d("humang_script", "handleMessage: EXCUTE_COMPLETE");
                    fooView.setText("start");
                    break;
                case MessageType.EXCUTE_STOP:
                    Log.d("humang_script", "handleMessage: EXCUTE_STOP");
                    ScriptUtil.getInstance(MainActivity.this,mHandler).stopScript();
                    fooView.setText("start");
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请打开此应用悬浮窗权限", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), CODE_WINDOW);
//                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.GET_INSTALLED_APPS},1);
            }
        }
        initSctiptFile();
        initView();

    }
    private void initView() {
        RadioGroup radioGroup = findViewById(R.id.script_list);
        for (int i = 0; i < scripts.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(scripts[i]);
            radioButton.setTextSize(30);
            radioGroup.addView(radioButton);
        }
        if (checkedRadioButtonId > 0) {
            RadioButton radioButton = findViewById(checkedRadioButtonId);
            radioButton.setChecked(true);
        }
        Button btConfirm = findViewById(R.id.confirm_button);
        btConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(checkedRadioButtonId);
                String filename = (String) radioButton.getText();
                showFloatingWindow(filename);
            }
        });

        findViewById(R.id.goback).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void initSctiptFile() {
        try {
            scripts = getAssets().list("script");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //调用该方法，可创建一个悬浮窗显示于屏幕之上
    private void showFloatingWindow(String filename) {
        clearFooView();
        fooView = new Button(this);
        fooView.setBackgroundResource(R.drawable.bt_bg);
        fooView.setText("start");
        fooView.setOnTouchListener(new FloatingOnTouchListener());
        fooView.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                if ("start".equals(fooView.getText())) {
                    message.what = MessageType.EXCUTE_START;
                    Bundle bundle = new Bundle();
                    bundle.putString("filename",filename);
                    message.setData(bundle);
                } else if ("stop".equals(fooView.getText())) {
                    message.what = MessageType.EXCUTE_STOP;
                }
                mHandler.sendMessage(message);
            }
        });

        layoutParams = new WindowManager
                .LayoutParams(50, 50, 0, 0, PixelFormat.TRANSPARENT);
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
        layoutParams.x = 0;
        layoutParams.y = 500;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(fooView, layoutParams);

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
        if (fooView != null && windowManager != null) {
            windowManager.removeView(fooView);
        }
    }

    @Override
    protected void onDestroy() {
        clearFooView();
        super.onDestroy();
    }
}