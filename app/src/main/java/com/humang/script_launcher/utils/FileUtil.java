package com.humang.script_launcher.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : created by amon
 * 时间 : 2022/7/5 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class FileUtil {

    public String readFile(Context context,String filename){
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            InputStream inputStream = null;
            if (filename.contains("通用脚本：")) {
                inputStream = context.getAssets().open(filename.replace("通用脚本：","script/"));
            } else if (filename.contains("本地脚本：")) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory()+"/Download/"+filename.replace("本地脚本：","/"));
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String temp;
            while((temp = reader.readLine()) != null){
                sb.append(temp);
                sb.append("\n");
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("amon", "sb: " + sb.toString());
        return sb.toString();
    }

    public List<String> parseFile(String str) {
        return (List<String>) Arrays.asList(str.split("\n"));
    }

}
