package com.humang.script;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.os.Build;
import android.content.Context;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.humang.script.utils.FileUtil;
import com.humang.script.utils.ScriptUtil;
import com.humang.script.utils.ShellUtil;

import java.util.List;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private Button button;
    /** 悬浮窗权限标识码 */
    public static final int CODE_WINDOW = 0;
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
        showFloatingWindow();
        finish();
    }

    //调用该方法，可创建一个悬浮窗显示于屏幕之上
    private void showFloatingWindow() {
        button = new Button(this);
        button.setBackgroundResource(R.drawable.bt_bg);
        button.setText("start");
        button.setOnTouchListener(new FloatingOnTouchListener());
        button.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                FileUtil fileUtil = new FileUtil();
                String s = fileUtil.readFile(MainActivity.this,"test.bat");
                List<String> strings = fileUtil.parseFile(s);
                if ("start".equals(button.getText())) {
                    ScriptUtil.getInstance(MainActivity.this).excuteScript(strings);
                    button.setText("stop");
                } else if ("stop".equals(button.getText())) {
                    ScriptUtil.getInstance(MainActivity.this).stopScript();
                    button.setText("start");
                }
//                Toast.makeText(getApplication(), "我被点击了", Toast.LENGTH_SHORT).show();
            }
        });

        layoutParams = new WindowManager
                .LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
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
        windowManager.addView(button, layoutParams);
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

}