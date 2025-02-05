package com.humang.script_launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author : created by amon
 * 时间 : 2022/8/3 11
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sp = context.getSharedPreferences("humang_script", Context.MODE_PRIVATE);
            String excutingScriptName = sp.getString("scriptName", "");
            if (excutingScriptName != "") {
                Log.d("humang_script","自启动了 ！！！！！");
                Intent newIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
                //1.如果自启动APP，参数为需要自动启动的应用包名
                //Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                //这句话必须加上才能开机自动运行app的界面
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //2.如果自启动Activity
                context.startActivity(newIntent);
                //3.如果自启动服务
                //context.startService(newIntent);
            }
        }
    }
}

