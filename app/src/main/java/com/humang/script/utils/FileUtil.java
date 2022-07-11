package com.humang.script.utils;

import android.content.Context;

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
    private static FileUtil fileUtil;
    public FileUtil(){}

    public String readFile(Context context,String filename){
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try{
            InputStream inputStream = context.getAssets().open(filename);
//            InputStream inputStream = new FileInputStream("sdcard/Download/douyin.bat");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String temp;
            while((temp = reader.readLine()) != null){
                sb.append(temp);
                sb.append("\n");
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public List<String> parseFile(String str) {
        return (List<String>) Arrays.asList(str.split("\n"));
    }

}
