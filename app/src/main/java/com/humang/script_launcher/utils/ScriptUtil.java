package com.humang.script_launcher.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.humang.script_launcher.MessageType;
import com.humang.script_launcher.expression_parsing.ExpressionParse;
import com.humang.script_launcher.expression_parsing.ExpressionTrans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : created by amon
 * 时间 : 2022/7/5 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：用于控制脚本的启动与暂停，单例模式保证同时只能执行一个脚本
 */
public class ScriptUtil {
    private boolean isPause;
    private boolean isStop;
    private boolean isComplete;
    private Thread scriptThread;

    private Thread performanceThread;
    private Context context;
    private Handler handler;

    private ScriptUtil(){}
    private static ScriptUtil scriptUtil;
    public static ScriptUtil getInstance(Context context,Handler handler){
        if (scriptUtil == null){
            scriptUtil = new ScriptUtil();
        }
        scriptUtil.context = context;
        scriptUtil.handler = handler;
        return scriptUtil;
    }

    public Map<String, String> vars = new HashMap<>();

    /*
    * 启动脚本，如果有暂停中的脚本会继续，有运行中的脚本会先停止
    * */
    public void excuteScript(List<String> batCmds){
        if (scriptThread != null) {
            if (isPause) {
                notifyScript();
                logThread.start();
                return;
            } else {
                stopScript();
            }
        }
        scriptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isStop = false;
                isComplete = false;
                saveLog();
                excuteCmds(purity(batCmds));
                if (!isStop) {
                    isComplete = true;
                    Message message = handler.obtainMessage();
                    message.what = MessageType.EXCUTE_COMPLETE;
                    handler.sendMessage(message);
                }
            }
        });

        scriptThread.start();

    }

    /*
     * 唤醒脚本，设置标记位为true，执行相应中断
     * */
    public void notifyScript() {
        isPause = false;
        if (scriptThread !=null && !scriptThread.isInterrupted()) {
            scriptThread.interrupt();
            Log.e("humang_script", "notify script");
        }
    }

    /*
     * 暂停脚本，设置标记位为true，执行相应中断
     * */
    public void pauseScript() {
        isPause = true;
        Log.e("humang_script", "pause script");
    }

    /*
    * 停止脚本，设置标记位为true，执行相应中断
    * */
    public void stopScript() {
        isPause = false;
        isStop = true;
        if (scriptThread !=null && !scriptThread.isInterrupted()) {
            scriptThread.interrupt();
            scriptThread = null;
            Log.e("humang_script", "stop script");
        }
    }

    public void saveLog() {
        logThread.start();
    }
    private Thread logThread = new Thread(new Runnable() {
        @Override
        public void run() {
            FileOutputStream os = null;
            try {
                //新建一个路径信息
//                String cmd = "logcat -v threadtime -b main -b system -b radio -b events -b crash -b kernel";
                String cmd = "logcat";
                String[] subCmds = cmd.trim().split(" ");
                Process exec = Runtime.getRuntime().exec(subCmds);
                final InputStream is = exec.getInputStream();
                os = new FileOutputStream("/sdcard/Download/Log.txt",true);
                int len = 0;
                byte[] buf = new byte[1024];
                while ((-1 != (len = is.read(buf))) && !(isStop || isPause || isComplete) ){
                    os.write(buf, 0, len);
                    os.flush();
                }
            } catch (Exception e) {
                Log.d("humang_script",
                        "read logcat process failed. message: "
                                + e.getMessage());
            } finally {
                if (null != os) {
                    try {
                        os.close();
                        os = null;
                    } catch (IOException e) {
                        // Do nothing
                    }
                }
            }
        }
    });

    public static String timeStamp2Date(long time) {
        String format = "yyyy-MM-dd_HH-mm-ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(time));
    }

    /*
    * 多命令执行，用于处理代码块
    * */
    public void excuteCmds(List<String> batCmds) {
        Stack<Integer> loopStack = new Stack<>();
        int loopCmdIndex = -1;
        String loopCmd = "";
        int loopTime = 1;
        ArrayList<String> loopCmds = new ArrayList<>();
        for (int i = 0; i < batCmds.size(); i++) {
            if (isStop) {
                break;
            } else if (isPause){
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String cmd = batCmds.get(i);

            // 判断是不是loop循环
            if (isLoopStart(cmd) || isForStart(cmd)) {
                if (loopStack.size() > 0) {
                    loopCmds.add(cmd);
                }
                loopStack.push(i);
            } else if (isLoopEnd(cmd) || isForEnd(cmd)){
                loopCmdIndex = loopStack.pop();
                if (loopStack.size() > 0) {
                    loopCmds.add(cmd);
                }
            } else  if (loopStack.size() > 0){
                loopCmds.add(cmd);
                continue;
            }
            // 循环栈为空，表示为最后一个循环或者普通命令
            if (loopStack.size() == 0){
                if (loopCmdIndex >= 0) {
                    loopCmd  = batCmds.get(loopCmdIndex);
                    if (isLoopStart(loopCmd)) {
                        loopTime = 100;
//                        loopTime = Integer.MAX_VALUE;
                    } else if(isForStart(loopCmd)) {
                        loopTime = getForTime(loopCmd);
                    }
                    Log.d("humang_script", "loopCmd: "+loopCmd+"  looptime: " +loopTime);
                    excuteLoop(loopCmds,loopTime);
                    loopCmds.clear();
                } else {
                    excuteCmd(cmd);
                }
            }
        }
    }

    /*
    * 单行命令处理
    * */
    public void excuteCmd(String cmd) {
        try {
            if (isVariableDefine(cmd)) {
                defineVariable(cmd);
            } else {
                cmd = replaceVariable(cmd);
            }
            if (isEcho(cmd)) {
                excuteEcho(cmd);
            } else if (isSleep(cmd)) {
                excuteSleep(cmd);
            } else if (
                    isVariableDefine(cmd)
                            || isLoopStart(cmd)
                            || isLoopEnd(cmd)
                            || isForStart(cmd)
                            || isForEnd(cmd)){
                return;
            } else if (isAdbCmd(cmd)){
                Log.d("humang_script", "excute adb cmd : " + cmd);
                excuteAdbCmd(cmd);
            } else if (isRebootCmd(cmd)) {
                excuteReboot();
            } else {
//                Log.e("humang_script","未知命令，无法处理："+cmd);
                    Log.d("humang_script", "excute nomal cmd : " + cmd);
                    excuteNomalCmd(cmd);

            }
        } catch (Exception e) {
            Log.e("humang_script", "excuteCmd: "+cmd, e);
        }
    }

    /*
    * 用于去掉不需要执行的命令以及移除特殊符号
    * */
    public List<String> purity(List<String> batCmds) {
        ArrayList<String> newBatCmds = new ArrayList<>();
        for (String batCmd : batCmds) {
            batCmd = batCmd.replace("\n","");
            batCmd = batCmd.replace("\t","");
            if (batCmd.contains("chcp")) {
                continue;
            } else if (batCmd.contains("setlocal enabledelayedexpansion")) {
                continue;
            } else if (batCmd.isEmpty()){
                continue;
            }else if (batCmd.contains("@echo off")){
                continue;
            }
            newBatCmds.add(batCmd);
        }
        return newBatCmds;
    }

    /*
    * 处理定义变量语句，会一直查找等式右边的变量并替换为最终数值，之后再计算最终表达式的值
    * */
    public void defineVariable(String batCmd) {
        String[] cmd = batCmd.split("=");
        String[] keyTemp = cmd[0].split(" ");
        String key = keyTemp[keyTemp.length-1];
        String value = cmd[1];
        String realValue = replaceVariable(value);
        while (!value.equals(realValue)) {
            value = realValue;
            realValue = replaceVariable(value);
//            Log.d("humang_script", "value = " + value);
//            Log.d("humang_script", "realValue = " + realValue);
        }
        realValue = calculateVariable(realValue);
//        Log.d("humang_script", "defineVariable:"+ key +" = "+realValue);
        vars.put(key,realValue);
    }

    /*
    * 用于进行表达式解析并计算最终值
    * */
    public String calculateVariable(String realValue) {
        ExpressionTrans expressionTrans = new ExpressionTrans();
        ArrayList<String> trans = expressionTrans.doTrans(realValue);
        System.out.println(trans);
        ExpressionParse expressionParse = new ExpressionParse();
        double output = expressionParse.doParse(trans);
        if (!realValue.contains(".")) {
            return String.valueOf((int)output);
        }
        return String.valueOf(output);
    }

    /*
    * 对于非定义变量的语句，会将引用的变量替换为最终值
    * */
    public String replaceVariable(String cmd) {
        if (isRandom(cmd)) {
            cmd = excuteRandom(cmd);
        } else if (isForVar(cmd)){
            cmd = cmd.replace("%%i",vars.get("%%i"));
        }
        String rule1 = "%(.*?)%";
        String rule2 = "!(.*?)!";
        Matcher matcher = matchRegex(cmd,rule1);
        while (matcher.find()) {
            try{
                if (matcher.group().length() > 2) {
                    String key = matcher.group().replace("%", "");
                    String value = vars.get(key);
                    cmd = cmd.replace(matcher.group(),value);
                }
            } catch (Exception e) {
            }
        }

        matcher = matchRegex(cmd,rule2);
        while (matcher.find()) {
            try {
                if (matcher.group().length() > 2) {
                    String key = matcher.group().replace("!", "");
                    String value = vars.get(key);
                    cmd = cmd.replace(matcher.group(),value);
                }
            } catch (Exception e) {
            }
        }
        return cmd;
    }

    /*
    * 通过 am start activity 会报错，因此对于启动activity的命令单独用 Intent 方式启动
    * */
    public void excuteStartActivity(String cmd){
        cmd = cmd.replace("am start ","");
        cmd = cmd.replace("\"","");
        String[] component = cmd.split("/");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(component[0], component[0] + component[1]));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            stopScript();
            Log.e("humang_script", "no such activity.  " + cmd);
            Message message = handler.obtainMessage(MessageType.SHOW_LOG);
            Bundle bundle = new Bundle();
            bundle.putString("log","脚本已停止运行：未找到目标activity  " + cmd) ;
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    /*
    * 将bat命令中的随机数转为java方式
    * */
    public String excuteRandom(String cmd) {
        Random random = new Random();
        int value = random.nextInt(65535);
        return cmd.replace("%random%",String.valueOf(value));
    }

    /*
     * 将bat命令中的休眠操作转为java方式
     * */
    public void excuteSleep(String cmd) {
        String[] subCmds = cmd.trim().split(" ");
        boolean findSleepTime = false;
        for (int i = 0; i < subCmds.length; i++) {
            if (findSleepTime) {
                int sleepTime = Integer.parseInt(subCmds[i]) * 1000;
                Log.d("humang_script", "sleep : " + sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            if ("/T".equals(subCmds[i])) {
                findSleepTime = true;
            }
        }
    }

    /*
     * 将bat命令中的echo转为java方式
     * */
    public void excuteEcho(String cmd) {
        String substring = cmd.trim().substring(5);
        Log.d("humang_script", "echo : " + substring);
        Message message = handler.obtainMessage(MessageType.SHOW_LOG);
        Bundle bundle = new Bundle();
        bundle.putString("log",substring);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    /*
     * 将bat命令中的loop循环转为java方式
     * */
    public void excuteLoop(ArrayList<String> cmds,int loopTime) {
        int loopTimes = 0;
        while (!isStop && loopTimes < loopTime){
            loopTimes += 1;
            Log.d("humang_script", "looptime " + loopTime + "   looptimes" + loopTimes);
            excuteCmds(cmds);
        }
    }

    /*
     * 将bat命令中的adb shell命令转为java方式
     * */
    public String excuteAdbCmd(String cmd) {
        cmd = cmd.replace("adb shell ","");
        if(isStartActivity(cmd)){
            excuteStartActivity(cmd);
            return "start activity";
        }
        return excuteNomalCmd(cmd);
    }

    /*
     * 直接执行命令
     * */
    public String excuteNomalCmd(String cmd) {
        String[] subCmds = cmd.trim().split(" ");
        String result = ShellUtil.getInstance().execute(subCmds);
        Log.e("humang_script", "excuteAdbCmd: "+cmd+"  result: "+result);
        return result;
    }


    /*
     * 执行重启命令
     * */
    public void excuteReboot() {
        handler.sendMessage(handler.obtainMessage(MessageType.EXCUTE_REBOOT));
    }


    /*
    * 正则匹配，根据规则返回匹配结果
    * */
    public Matcher matchRegex(String str, String rule) {
        Pattern pattern = Pattern.compile(rule);
        return pattern.matcher(str);
    }

    public boolean isLoopStart(String cmd) {
        return cmd.trim().startsWith(":loop");
    }
    public boolean isLoopEnd(String cmd) {
        return cmd.contains("goto loop");
    }

    public boolean isForStart(String cmd) {
        return cmd.trim().startsWith("for");
    }

    public boolean isForEnd(String cmd) {
        return cmd.trim().startsWith(")");
    }

    public int getForTime(String cmd) {
        int in = cmd.indexOf("in");
        int aDo = cmd.indexOf("do");
        cmd = cmd.substring(in,aDo);
        int begin = cmd.lastIndexOf(",");
        int end = cmd.lastIndexOf(")");
        String substring= "";
        try {
            substring = cmd.substring(begin+1, end);
            return Integer.parseInt(substring);
        } catch (Exception e) {
            Log.e("amon", "getForTime: "+substring);
            return -1;
        }
    }

    public boolean isRandom(String cmd) {
        return cmd.contains("%random%");
    }

    public boolean isForVar(String cmd) {
        return !isForStart(cmd) && cmd.contains("%%i");
    }

    public boolean isSleep(String cmd) {
        return cmd.contains("timeout /T");
    }

    public boolean isEcho(String cmd) {
        return "echo".equals(cmd.trim().split(" ")[0]);
    }

    public boolean isVariableDefine(String cmd) {
        return cmd.contains("set ") && cmd.contains("=");
    }

    public boolean isStartActivity(String cmd) {
        return cmd.contains("am start");
    }

    public boolean isAdbCmd(String cmd) {
        return cmd.contains("adb shell ");
    }

    public boolean isRebootCmd(String cmd) {
        return cmd.contains("adb reboot");
    }

    public void performanceMonitor() {
        new Thread() {
            @Override
            public void run() {
                Log.d("script", "isStop: "+isStop+"  isComplete: "+isComplete);
                while (!(isStop || isComplete)) {
                    String result = excuteAdbCmd("adb shell top -n 1");
                    int beginIndex = result.indexOf("Mem");
                    int endIndex = result.indexOf("host")+5;
                    result = result.substring(beginIndex,endIndex);
                    Log.d("script", "result: "+result);
                    float processCpuRate = getProcessCpuRate();
                    Message message = handler.obtainMessage(MessageType.SHOW_PERFORMANCE);
                    Bundle bundle = new Bundle();
                    bundle.putString("performance",processCpuRate+"");
                    message.setData(bundle);
                    handler.sendMessage(message);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    public float getProcessCpuRate() {
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try {
            Thread.sleep(360);
        }
        catch (Exception e)
        {
        }
        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();
        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);
        return cpuRate;
    }

    public long getTotalCpuTime() {
        // 获取系统总CPU使用时间
        String result = excuteAdbCmd("cat /proc/stat");
        String[] cpuInfos = result.split(" ");
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    public static long getAppCpuTime() { // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }
}

