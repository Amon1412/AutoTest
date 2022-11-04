package com.humang.script_launcher.monky_script;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.humang.script_launcher.utils.ShellUtil;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author : created by amon
 * 时间 : 2022/9/26 11
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class MonkyUtil {
    public static final int RUNING = 1;
    public static final int PAUSE = 2;
    public static final int STOP = 3;

    private static final String TAG = "amon";
    Map<Integer,MonkyBean> monkyBeanHead = new HashMap<>();
    HashMap<Integer, MonkyBean> monkyBeanMap = new LinkedHashMap<>();

    private Context context;
    private Handler handler;
    private int state;
    private Thread monkyThread;


    private MonkyUtil(){}
    private static MonkyUtil monkyUtil;
    public static MonkyUtil getInstance(Context context, Handler handler){
        if (monkyUtil == null){
            monkyUtil = new MonkyUtil();
        }
        monkyUtil.context = context;
        monkyUtil.handler = handler;
        return monkyUtil;
    }

    public void run(){
        if (state == RUNING) {
            return;
        }

        if (monkyThread != null) {
            if (state == PAUSE) {
                notifyScript();
                saveLog();
                return;
            } else {
                stop();
                monkyThread = null;
            }
        }
        monkyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                state = RUNING;
                saveLog();
                startMonky();
            }
        });

        monkyThread.start();

    }

    /*
     * 唤醒脚本，设置标记位为true，执行相应中断
     * */
    public void notifyScript() {
        state = RUNING;
        if (monkyThread !=null && !monkyThread.isInterrupted()) {
            monkyThread.interrupt();
            Log.e(TAG, "notify script");
        }
    }

    /*
     * 暂停脚本，设置标记位为true，执行相应中断
     * */
    public void pause() {
        if (state == PAUSE) {
            return;
        }
        state = PAUSE;
        Log.e("humang_script", "pause script");
    }

    /*
     * 停止脚本，设置标记位为true，执行相应中断
     * */
    public void stop() {
        if (state == STOP) {
            return;
        }
        state = STOP;
        if (monkyThread !=null && !monkyThread.isInterrupted()) {
            monkyThread.interrupt();
            monkyThread = null;
            Log.e("humang_script", "stop script");
        }
    }


    public void parseMonkyBean() {
        Log.d(TAG, "parseMonkyBean: ");
        InputStream inputStream = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try{
            inputStream = context.getAssets().open("script/monkybean.txt");
            isr = new InputStreamReader(inputStream);
            br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null){
                MonkyBean monkyBean = new MonkyBean(line);
                monkyBeanMap.put(monkyBean.getId(),monkyBean);
            }

            br.close();
            isr.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void relativeMonkyBean() {
        monkyBeanMap.forEach((id,monkyBean) -> {
            if (monkyBean.getParentId() != 0) {
                // id为-1表示切换到父级，只有非首级才可切换至上级
                monkyBean.putChild(-1,new MonkyBean(-1));

                if (monkyBean.getEnd() == 1) {
                    monkyBeanMap.get(monkyBean.getParentId()).putFinalChild(monkyBean.getId(),monkyBean);
                } else {
                    // 如果父级id不为0，则将自己添加到父级的子级
                    monkyBeanMap.get(monkyBean.getParentId()).putChild(monkyBean.getId(),monkyBean);
                }
            } else {
                // id为0表示切换到同级
                monkyBean.putChild(0,new MonkyBean(0));
            }
        });
    }

    public void startMonky() {
        Log.d(TAG, "startMonky: ");

        parseMonkyBean();
        relativeMonkyBean();

        monkyBeanHead = new HashMap<>();
        monkyBeanMap.forEach((id,monkyBean) -> {
            if (monkyBean.getParentId() == 0 && monkyBean.getWeight() > 0) {
                monkyBeanHead.put(id,monkyBean);
            }
        });
        MonkyBean monkyBean = getRandomItem(monkyBeanHead);
        excuteMonky(monkyBean);
    }

    public void excuteMonky(MonkyBean monkyBean) {
        Log.d(TAG, "excuteMonky: ");

        while (true) {
            if (state == STOP) {
                break;
            } else if (state == PAUSE){
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            monkyBean = clickItem(monkyBean);
        }

    }

    public MonkyBean clickItem(MonkyBean monkyBean) {

        Log.d(TAG, "clickItem:  name: "+monkyBean.getName() + "weight:"+monkyBean.getTempWeight());
        Random random = new Random();
        try {
            // 1.点击按钮
            if (monkyBean.getX2() == -1 && monkyBean.getY2() == -1) {
                // 按钮事件
                for (int i = 0; i < random.nextInt(monkyBean.getTimes())+1; i++) {
                    Log.d(TAG, String.format("点击: %s  坐标： x=%s y=%s 第 %s 次",
                            monkyBean.getName(),monkyBean.getX1(),monkyBean.getY1(),i+1));
//                    ShellUtil.getInstance().execute(String.format("input tap %s %s", monkyBean.getX1(),monkyBean.getY1()));
                    ShellUtil.getInstance().sendClickEvent(monkyBean.getX1(),monkyBean.getY1());
                }
            } else {
                // 拖动条或滑动事件，滑动事件暂未处理
                for (int i = 0; i < random.nextInt(monkyBean.getTimes())+1; i++) {
                    int x = monkyBean.getX1()+random.nextInt(monkyBean.getX2()-monkyBean.getX1()+1);
                    int y = monkyBean.getY1()+random.nextInt(monkyBean.getY2()-monkyBean.getY1()+1);
                    Log.d(TAG, String.format("点击: %s  坐标： x=%s y=%s 第 %s 次",
                            monkyBean.getName(),x,y,i+1));
//                    ShellUtil.getInstance().execute(String.format("input tap %s %s", x,y));
                    ShellUtil.getInstance().sendClickEvent(x,y);

                }
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "monkyBean: "+monkyBean);
        }

        // 2.随机下一步操作
        MonkyBean next = getRandomItem(monkyBean.getChilds());
        if (next.getId() == -1) {
            try {
                // 如果id为-1，执行返回父级
                // 如果有最后子点击任务，则点击
                if (monkyBean.getFinalChildSize() > 0) {
                    MonkyBean finalChild = getRandomItem(monkyBean.getFinalChilds(),true);
                    Log.d(TAG, "clickItem: 保存");
//                    ShellUtil.getInstance().execute(String.format("input tap %s %s", finalChild.getX1(),finalChild.getY1()));
                    ShellUtil.getInstance().sendClickEvent(finalChild.getX1(),finalChild.getY1());
                    Thread.sleep(1000);
                }
                next = monkyBeanMap.get(monkyBean.getParentId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (next.getId() == 0) {
            // 如果id为0，即返回首级
            next =  getRandomItem(monkyBeanHead);
        }
        return next;
    }

    public MonkyBean getRandomItem(Map<Integer,MonkyBean> monkyBeanMap) {
        ArrayList<Integer> list = new ArrayList<>();
        monkyBeanMap.forEach((id,monkyBean) -> {
            for (int i = 0; i < monkyBean.getTempWeight(); i++) {
                list.add(monkyBean.getId());
            }
        });
        Random random = new Random();
        Integer id = list.get(random.nextInt(list.size()));
        MonkyBean monkyBean = monkyBeanMap.get(id);
        monkyBean.reduceWeight();
        return monkyBean;
    }

    public MonkyBean getRandomItem(Map<Integer,MonkyBean> monkyBeanMap, boolean noWeight) {
        if (!noWeight) {
            return getRandomItem(monkyBeanMap);
        }
        ArrayList<Integer> list = new ArrayList<>();
        monkyBeanMap.forEach((id,monkyBean) -> {
            list.add(monkyBean.getId());
        });
        Random random = new Random();
        Integer id = list.get(random.nextInt(list.size()));
        MonkyBean monkyBean = monkyBeanMap.get(id);
        monkyBean.reduceWeight();
        return monkyBean;
    }

    public void saveLog() {
        Thread logThread = new Thread(new Runnable() {
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
                    while ((-1 != (len = is.read(buf))) && (state == RUNING) ){
                        os.write(buf, 0, len);
                        os.flush();
                    }
                    Log.d("humang_script", "logThread close ");
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
        logThread.start();
    }
}
