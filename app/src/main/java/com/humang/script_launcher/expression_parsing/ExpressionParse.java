package com.humang.script_launcher.expression_parsing;

import android.content.Context;

import com.humang.script_launcher.utils.ScriptUtil;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author : created by amon
 * 时间 : 2022/7/5 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：用于将 {@link ExpressionTrans} 转换后的结果进行解析
 */
public class ExpressionParse {

    private Stack<Double> stack;

    public ExpressionParse(){}

    public double doParse(ArrayList<String> inputList){
        stack = new Stack<>();
        double num1 , num2 ,interAns;
        for (int i = 0; i < inputList.size(); i++) {
            String s = inputList.get(i);
            try {
                Double value = Double.valueOf(s);
                stack.push(value);
            } catch (Exception e) {
                num2 = stack.pop();
                num1 = stack.pop();
                switch (s){
                    case "+":
                        interAns = num1 + num2;
                        break;
                    case "-":
                        interAns = num1 - num2;
                        break;
                    case "*":
                        interAns = num1 * num2;
                        break;
                    case "/":
                        interAns = num1 / num2;
                        break;
                    case "%":
                    case "%%":
                        interAns = num1 % num2;
                        break;
                    default:
                        interAns = 0;
                        break;
                }
                stack.push(interAns);
            }
        }
        interAns = stack.pop();
        return interAns;
    }
}
