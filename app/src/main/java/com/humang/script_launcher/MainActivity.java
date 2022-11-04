package com.humang.script_launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.humang.script_launcher.edit_script.EditService;
import com.humang.script_launcher.excute_script.ScriptService;
import com.humang.script_launcher.monky_script.MonkyService;
import com.humang.script_launcher.monky_script.MonkyUtil;
import com.humang.script_launcher.monky_script.MonkyBean;
import com.humang.script_launcher.utils.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    String[] assetsScripts;
    String[] localSctipts;
    int checkedRadioButtonId = -1;
    /** 悬浮窗权限标识码 */
    public static final int CODE_WINDOW = 0;

    private Switch showLogSwitch;
    private Switch showPerformanceSwitch;
    private String scriptName;

    int defaultSleepTime = 3;
    int randomSleepTime = 3;

    boolean startImmediately = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("humang_script", Context.MODE_PRIVATE);
        String excutingScriptName = sp.getString("scriptName", "");
        Boolean excutingStartImmediately = sp.getBoolean("startImmediately", false);
        if (excutingStartImmediately ) {

        }
        if (excutingScriptName != "") {
            scriptName = excutingScriptName;
            startImmediately = true;
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("scriptName","");
            editor.putBoolean("startImmediately",false);
            editor.apply();
            startScriptService();
        }
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
            }
        }
        initSctiptFile();
        initView();


    }

    private void initView() {
        RadioGroup radioGroup = findViewById(R.id.script_list);

        for (int i = 0; i < assetsScripts.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(i);
            radioButton.setText("通用脚本："+assetsScripts[i]);
            radioButton.setTextSize(30);
            radioGroup.addView(radioButton);
        }
        for (int i = 0; i < localSctipts.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(assetsScripts.length+i);
            radioButton.setText("本地脚本："+localSctipts[i]);
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
        btConfirm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        findViewById(R.id.edit_bt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditeService();
            }
        });

        findViewById(R.id.goback).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.set_default_params).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_default_params, null,false);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setView(view)
                        .setCancelable(false)
                        .create();
                view.findViewById(R.id.edit_ok).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            defaultSleepTime = Integer.parseInt(((EditText)view.findViewById(R.id.default_sleep_text)).getText().toString());
                            randomSleepTime = Integer.parseInt(((EditText)view.findViewById(R.id.random_sleep_text)).getText().toString());
                        } catch (Exception e){

                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });

        findViewById(R.id.clear_log_bt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShellUtil.getInstance().execute("rm /sdcard/Download/Log.txt");
            }
        });

        findViewById(R.id.monky_bt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startMonkyService();
            }
        });
    }

    private void initSctiptFile() {
        try {
            assetsScripts = getAssets().list("script");
            localSctipts = getAllDataFileName(Environment.getExternalStorageDirectory()+"/Download");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[]  getAllDataFileName(String folderPath){
        ArrayList<String> fileList = new ArrayList<>();
        File file = new File(folderPath);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                String fileName = tempList[i].getName();
                if (fileName.endsWith(".bat")){    //  根据自己的需要进行类型筛选
                    fileList.add(fileName);
                }
            }
        }
        return fileList.toArray(new String[fileList.size()]);
    }


    private void startScriptService() {

//        boolean isShowLog = showLogSwitch.isChecked();
//        boolean isShowPerformance = showPerformanceSwitch.isChecked();
//        Log.d("humang_script", "startScriptService: "+isShowLog);

        Intent intent = new Intent(this, ScriptService.class);
        intent.putExtra("scriptName",scriptName);
//        intent.putExtra("isShowLog",isShowLog);
//        intent.putExtra("isShowPerformance",isShowPerformance);
        intent.putExtra("startImmediately",startImmediately);
        startService(intent);
        finish();
    }

    private void startMonkyService() {
        Intent intent = new Intent(this, MonkyService.class);
        startService(intent);
        finish();
    }

    //调用该方法，可创建一个悬浮窗显示于屏幕之上
    private void startEditeService() {
        Intent intent = new Intent(this, EditService.class);
        intent.putExtra("defaultSleepTime",defaultSleepTime);
        intent.putExtra("randomSleepTime",randomSleepTime);
        startService(intent);
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}