package com.humang.script;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {
    String[] scripts;
    int checkedRadioButtonId = -1;
    /** 悬浮窗权限标识码 */
    public static final int CODE_WINDOW = 0;

    private Switch showLogSwitch;
    private Switch showPerformanceSwitch;
    private String scriptName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isShowLog = getIntent().getBooleanExtra("isShowLog",true);
        boolean isShowPerformance = getIntent().getBooleanExtra("isShowPerformance",true);

        showLogSwitch = findViewById(R.id.show_log);
        showLogSwitch.setChecked(isShowLog);
        showPerformanceSwitch = findViewById(R.id.show_performance);
        showPerformanceSwitch.setChecked(isShowPerformance);

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
            radioButton.setId(i);
            radioButton.setText(scripts[i]);
            radioButton.setTextSize(30);
            radioGroup.addView(radioButton);
        }
        if (checkedRadioButtonId > 0) {
            RadioButton radioButton = findViewById(checkedRadioButtonId);
            radioButton.setChecked(true);
        } else {
            radioGroup.check(0);
        }
        Button btConfirm = findViewById(R.id.confirm_button);
        btConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(checkedRadioButtonId);
                scriptName = (String) radioButton.getText();
                startScriptService();
            }
        });

        findViewById(R.id.goback).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
    private void startScriptService() {

        boolean isShowLog = showLogSwitch.isChecked();
        boolean isShowPerformance = showPerformanceSwitch.isChecked();
        Log.d("humang_script", "startScriptService: "+isShowLog);

        Intent intent = new Intent(this,ScriptService.class);
        intent.putExtra("scriptName",scriptName);
        intent.putExtra("isShowLog",isShowLog);
        intent.putExtra("isShowPerformance",isShowPerformance);
        startService(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}