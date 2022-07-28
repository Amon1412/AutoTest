package com.humang.script_launcher;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.humang.script_launcher.utils.ShellUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * @author : created by amon
 * 时间 : 2022/7/21 11
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class EditService extends Service implements View.OnClickListener {
    private static final String ADB_IME = "com.humang.script_launcher/.ime.AdbIME";
    private static final String SYS_IME = "com.android.inputmethod.pinyin/.PinyinIME";
    private WindowManager windowManager;
    private WindowManager.LayoutParams mMenuLayoutParams;
    private WindowManager.LayoutParams mMaskLayoutParams;
    private WindowManager.LayoutParams mDetailLayoutParams;
    private WindowManager.LayoutParams mDialogLayoutParams;
    private View mMenuView;
    private View mEditView;
    private View mMaskView;
    private View mDetailView;
    private View mDialogView;
    private RecyclerView mDetailRecycleView;
    private SubItemAdapter mSubItemAdapter;
    private View deleteBt;
    private View control;
    private Thread cmdThread;

    private int downX,downY;
    private int upX,upY;
    private long downTime,upTime;

    private String inputText = "";

    private boolean isEdit = false;
    private boolean isShow = false;
    private int loopTag = 1;

    int defaultSleepTime = 3;
    int randomSleepTime = 3;

    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 500;

    List<String> cmds = new ArrayList<>();
    private Queue<Integer> cmdQueue = new PriorityQueue<>();

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageType.MASK_TOUCHABLE:
                    setMaskTouchable(true);
                    break;
                case MessageType.MASK_NOT_TOUCHABLE:
                    setMaskTouchable(false);
                    break;
                case MessageType.UPDATE_DETAIL:
                    if (mSubItemAdapter != null) {
                        mSubItemAdapter.notifyDataSetChanged();
                        mDetailRecycleView.getLayoutManager().smoothScrollToPosition(mDetailRecycleView,null,cmds.size()-1);
                    }
                    break;
                case MessageType.EXCUTE_FAILURED:
                    Toast.makeText(EditService.this,"命令执行失败，请重试",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("amon", "EditService onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            defaultSleepTime = intent.getIntExtra("defaultSleepTime",3);
            randomSleepTime = intent.getIntExtra("randomSleepTime",3);
        }
        initMaskView();
        initMenuView();
        initDetailView();
        initCmdThread();
        return super.onStartCommand(intent, flags, startId);
    }


    private void showDialog(int scriptType){
        mDialogView = LayoutInflater.from(this).inflate(R.layout.fooview_dialog, null);
        switch (scriptType) {
            case ScriptType.INPUT_EVENT:
                mDialogView.findViewById(R.id.input_log).setVisibility(View.VISIBLE);

                Settings.Secure.putString(getContentResolver()
                        ,Settings.Secure.DEFAULT_INPUT_METHOD,SYS_IME);
                mDialogView.findViewById(R.id.input_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = ((EditText)mDialogView.findViewById(R.id.input_text)).getText().toString();
                        inputText = text;
                        Settings.Secure.putString(getContentResolver()
                                ,Settings.Secure.DEFAULT_INPUT_METHOD,ADB_IME);
                        removeDialog();
                    }
                });
                break;
            case ScriptType.START_ACTIVITY_ACTION:
                mDialogView.findViewById(R.id.start_activity_log).setVisibility(View.VISIBLE);
                Settings.Secure.putString(getContentResolver()
                        ,Settings.Secure.DEFAULT_INPUT_METHOD,SYS_IME);
                mDialogView.findViewById(R.id.start_activity_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String component = ((EditText)mDialogView.findViewById(R.id.start_activity_text)).getText().toString();
                        execute(String.format("am start %s", component));
                        removeDialog();
                    }
                });
                break;
            case ScriptType.SLEEP_ACTION:
                mDialogView.findViewById(R.id.sleep_log).setVisibility(View.VISIBLE);
                Settings.Secure.putString(getContentResolver()
                        ,Settings.Secure.DEFAULT_INPUT_METHOD,SYS_IME);
                mDialogView.findViewById(R.id.sleep_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int minTime = 0;
                        int randomTime = 0;
                        try {
                            minTime = Integer.parseInt(((EditText)mDialogView.findViewById(R.id.min_sleep_text)).getText().toString());
                            randomTime = Integer.parseInt(((EditText)mDialogView.findViewById(R.id.random_sleep_text)).getText().toString());
                        } catch (Exception e) {
                            minTime = 2;
                            randomTime = 3;
                        }
                        Random random = new Random();
                        int time = minTime + random.nextInt(randomTime+1);
                        addCmd(String.format("timeout /T %s /NOBREAK",time));
                        removeDialog();
                    }
                });
                break;
            case MessageType.SAVE:
                mDialogView.findViewById(R.id.save_log).setVisibility(View.VISIBLE);
                Settings.Secure.putString(getContentResolver()
                        ,Settings.Secure.DEFAULT_INPUT_METHOD,SYS_IME);
                mDialogView.findViewById(R.id.save_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String filename = ((EditText)mDialogView.findViewById(R.id.filename_text)).getText().toString();
                        removeDialog();
                        File file = new File(getFilesDir(), filename+".bat");
                        try {
                            FileOutputStream fos=new FileOutputStream(file,true);
                            Iterator<String> iterator = cmds.iterator();
                            while (iterator.hasNext()) {
                                String next = iterator.next();
                                fos.write(next.getBytes(StandardCharsets.UTF_8));
                                fos.write("\n".getBytes());
                            }
                            fos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(EditService.this, MainActivity.class);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        startActivity(intent);
                        onDestroy();
                        return;
                    }
                });
                break;
            default:
                return;
        }
        mDialogLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDialogLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mDialogLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mDialogLayoutParams.format = PixelFormat.TRANSLUCENT;
        mDialogLayoutParams.gravity = Gravity.CENTER;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mDialogView, mDialogLayoutParams);
    }

    private void initCmdThread() {
        cmdThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (cmdQueue) {
                        if (cmdQueue.size() == 0) {
                            try {
                                cmdQueue.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        int scriptType = cmdQueue.remove();
                        mHandler.sendMessage(mHandler.obtainMessage(MessageType.MASK_NOT_TOUCHABLE));
                        String cmd = "";
                        String result = "";
                        switch (scriptType) {
                            case ScriptType.CLICK_EVENT:
                                cmd = String.format("input tap %s %s", downX,downY);
                                break;
                            case ScriptType.LONGCLICK_EVENT:
                            case ScriptType.SWIPE_EVENT:_EVENT:
                            cmd = String.format("input swipe %s %s %s %s %s", downX,downY,upX,upY,upTime-downTime);
                                break;
                            default:
                                break;
                        }
                        result = execute(cmd);

                        if (!inputText.equals("")) {
                            cmd = String.format("am broadcast -a ADB_INPUT_TEXT --es msg %s",inputText);
                            inputText = "";
                            result = execute(cmd);
                        }
                        int retryTimes = 10;
                        while (retryTimes > 0 && result.contains("Exception")) {
                            retryTimes -= 1;
                            result = execute(cmd,false);
                            Log.d("amon", "retryTimes: "+retryTimes);
                        }
                        if (retryTimes == 0 && result.contains("Exception")) {
                            String remove = cmds.remove(cmds.size() - 1);
                            mHandler.sendMessage(mHandler.obtainMessage(MessageType.EXCUTE_FAILURED));
                            Log.d("amon", "cmd excute failured: "+remove);
                        }
                        Log.d("amon", "cmd: "+cmd);
                        Log.d("amon", "result: "+result);
                        mHandler.sendMessage(mHandler.obtainMessage(MessageType.MASK_TOUCHABLE));
                        try {
                            cmdQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        cmdThread.start();
    }

    private void stopCmdThread() {
        if (cmdThread !=null && !cmdThread.isInterrupted()) {
            cmdThread.interrupt();
            cmdThread = null;
            Log.e("humang_script", "stop script");
        }
    }

    private void sendEvent(int scriptType) {
        synchronized (cmdQueue) {
            cmdQueue.add(scriptType);
            cmdQueue.notifyAll();
        }
    }
    
    private String execute(String cmd) {
        return execute(cmd,true);
    }
    private String execute(String cmd,boolean remember) {
        if (remember) {
            addCmd("adb shell "+cmd);
        }
        return ShellUtil.getInstance().execute(cmd.split(" "));
    }

    private void initMenuView() {
        mMenuView = LayoutInflater.from(this).inflate(R.layout.fooview_edit,null);
        mEditView = mMenuView.findViewById(R.id.edit);
        control = mMenuView.findViewById(R.id.control);
        control.setOnClickListener(this);
        mMenuView.findViewById(R.id.cancel_edit).setOnClickListener(this);
        mMenuView.findViewById(R.id.save_cmds).setOnClickListener(this);
        mMenuView.findViewById(R.id.edit_cmds).setOnClickListener(this);
        mMenuView.findViewById(R.id.input_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.loop_start_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.loop_end_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.start_activity_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.home_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.back_bt).setOnClickListener(this);
        mMenuView.findViewById(R.id.sleep_bt).setOnClickListener(this);

        mMenuLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mMenuLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMenuLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mMenuLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mMenuLayoutParams.format = PixelFormat.TRANSLUCENT;
        mMenuLayoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        mMenuLayoutParams.x = 0;
        mMenuLayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mMenuView, mMenuLayoutParams);
    }

    private void initDetailView() {
        mDetailView = LayoutInflater.from(this).inflate(R.layout.fooview_detail,null);

        mDetailRecycleView = mDetailView.findViewById(R.id.detail_recycleview);
        mSubItemAdapter = new SubItemAdapter(this, cmds);
        mDetailRecycleView.setAdapter(mSubItemAdapter);
        mDetailRecycleView.setLayoutManager(new LinearLayoutManager(this));

        deleteBt = mDetailView.findViewById(R.id.delete_bt);
        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Queue<Integer> checkedQueue = mSubItemAdapter.getCheckedQueue();
                Iterator<Integer> iterator = checkedQueue.iterator();
                while (iterator.hasNext()) {
                    int next = iterator.next();
                    cmds.remove(next);
                }
                checkedQueue.clear();
                mSubItemAdapter.notifyDataSetChanged();
            }
        });

        mDetailLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mDetailLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDetailLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mDetailLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mDetailLayoutParams.format = PixelFormat.TRANSLUCENT;
        mDetailLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mDetailView, mDetailLayoutParams);
    }

    private void initMaskView() {
        mMaskView = new View(this);
        mMaskView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEdit) {
                    return false;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = (int)motionEvent.getX();
                    downY = (int)motionEvent.getY();
                    downTime = System.currentTimeMillis();
                    Log.d("amon", "onTouchDown: "+"clickX: "+ downX +"  clickY:"+ downY + "  downTime" + downTime);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    upX = (int)motionEvent.getX();
                    upY = (int)motionEvent.getY();
                    upTime = System.currentTimeMillis();
                    if (Math.abs(upX - downX) < 20 && Math.abs(upY - downY) < 20){
                        if (upTime - downTime < DEFAULT_LONG_PRESS_TIMEOUT) {
                            sendEvent(ScriptType.CLICK_EVENT);
                        } else {
                            sendEvent(ScriptType.LONGCLICK_EVENT);
                        }
                    } else {
                        sendEvent(ScriptType.SWIPE_EVENT);
                    }
                    Log.d("amon", "onTouchUp: "+"clickX: "+ upX +"  clickY:"+ upY + "  downTime" + upTime);
                }
                return false;
            }
        });
        mMaskView.setBackgroundColor(getColor(R.color.mask));
        mMaskLayoutParams = new WindowManager
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, PixelFormat.TRANSPARENT);
        mMaskLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMaskLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mMaskLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mMaskLayoutParams.format = PixelFormat.TRANSLUCENT;
        mMaskLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mMaskLayoutParams.x = 0;
        mMaskLayoutParams.y = 0;
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mMaskView, mMaskLayoutParams);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_edit:
                Intent intent = new Intent(EditService.this, MainActivity.class);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setAction(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);

                onDestroy();
                break;
            case R.id.control:
                isShow = !isShow;
                if (isShow) {
                    control.setBackgroundResource(R.drawable.hide);
                    mEditView.setVisibility(View.VISIBLE);
                } else {
                    control.setBackgroundResource(R.drawable.show);
                    mEditView.setVisibility(View.GONE);
                }
                break;
            case R.id.save_cmds:
                showDialog(MessageType.SAVE);
                break;
            case R.id.edit_cmds:
                isEdit = !isEdit;
                setDetailTouchable(isEdit);
                if (isEdit) {
                    mSubItemAdapter.showCheckBox();
                    deleteBt.setVisibility(View.VISIBLE);
                } else {
                    mSubItemAdapter.hideCheckBox();
                    deleteBt.setVisibility(View.GONE);
                }
                break;
            case R.id.input_bt:
                showDialog(ScriptType.INPUT_EVENT);
                break;
            case R.id.start_activity_bt:
                showDialog(ScriptType.START_ACTIVITY_ACTION);
                break;
            case R.id.home_bt:
                execute("input keyevent 3");
                break;
            case R.id.back_bt:
                execute("input keyevent 4");
                break;
            case R.id.loop_start_bt:
                addCmd(":loop"+loopTag++);
                break;
            case R.id.loop_end_bt:
                addCmd("goto loop"+(--loopTag));
                break;
            case R.id.sleep_bt:
                showDialog(ScriptType.SLEEP_ACTION);
                break;
            default:
                return;
        }
    }

    private void setMaskTouchable(boolean isTouchable) {
        if (isTouchable) {
            windowManager.addView(mMaskView,mMaskLayoutParams);
            windowManager.addView(mDetailView,mDetailLayoutParams);
            windowManager.addView(mMenuView,mMenuLayoutParams);
        } else {
            removeMaskView();
            removeDetailView();
            removeMenulView();
        }
//        mMaskLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//        mMaskView.setBackgroundColor(getColor(R.color.transparent));
//        if (isTouchable) {l
//            mMaskLayoutParams.flags ^= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//            mMaskView.setBackgroundColor(getColor(R.color.mask));
//        }
//        windowManager.updateViewLayout(mMaskView, mMaskLayoutParams);
    }
    private void setDetailTouchable(boolean isTouchable) {
        mDetailLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (isTouchable) {
            mDetailLayoutParams.flags ^= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        windowManager.updateViewLayout(mDetailView, mDetailLayoutParams);
    }

    private void removeDialog() {
        try {
            if (windowManager != null && mDialogView != null) {
                windowManager.removeView(mDialogView);
            }
        } catch (Exception e) {

        }
    }
    private void removeMenulView() {
        try {
            if (windowManager != null && mMenuView!= null) {
                windowManager.removeView(mMenuView);
            }
        } catch (Exception e) {

        }
    }
    private void removeDetailView() {
        try {
            if (windowManager != null && mDetailView != null) {
                windowManager.removeView(mDetailView);
            }
        } catch (Exception e) {

        }
    }
    private void removeMaskView() {
        try {
            if (windowManager != null && mMaskView != null) {
                windowManager.removeView(mMaskView);
            }
        } catch (Exception e) {

        }
    }

    private void clearFooview() {
        removeMaskView();
        removeMenulView();
        removeDetailView();
        removeDialog();
    }

    private void addCmd(String cmd) {
        cmds.add(cmd);
        mHandler.sendMessage(mHandler.obtainMessage(MessageType.UPDATE_DETAIL));
        if (!cmd.contains("timeout /T")) {
            Random random = new Random();
            int time = defaultSleepTime + random.nextInt(randomSleepTime+1);
            addCmd(String.format("timeout /T %s /NOBREAK",time));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        clearFooview();
        cmds.clear();
        stopCmdThread();
        mSubItemAdapter.getCheckedQueue().clear();
        super.onDestroy();
    }
}
