package com.humang.script_launcher.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author : created by amon
 * 时间 : 2022/7/5 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class ShellUtil {
    private static ShellUtil shellUtil;
    private ShellUtil(){}

    public static ShellUtil getInstance(){
        if (shellUtil == null){
            shellUtil = new ShellUtil();
        }
        return shellUtil;
    }
    public  String execute(String[] args) {
        //初始化指令
        String result = "";
        //进程生成器，将指令封装成独立的进程，进行调用
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        //进程
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            //字节流，临时存储控制台的输出内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            //执行指令
            process = processBuilder.start();
            //接收控制台输出的异常情况，输入流
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write("\r\n".getBytes());
            //正常输出
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            //将控制台输出转化为字符串返回
            result = new String(data);
        } catch (IOException e) {
            result = e.toString();
        } catch (Exception e) {
            result = e.toString();
        } finally {//最后必须将所有输入输出流关闭
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
