package com.humang.script_launcher.expression_parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * @author : created by amon
 * 时间 : 2022/7/5 15
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：用于将字符串表达式由默认的中缀转为后缀，并将每个单元存储在list中
 */
public class ExpressionTrans {

    private Stack<String> stack;
    ArrayList<String> outputList;

    public ExpressionTrans(){
        stack = new Stack<>();
    }

    public ArrayList<String> doTrans(String input) {
        outputList = new ArrayList<>();
        for (int i = 0; i < input.toCharArray().length; i++) {
            String ch = String.valueOf(input.charAt(i));
            System.out.println("For "+ch+" "+stack);
            switch (ch) {
                case "+":
                case "-":
                    getOper(ch , 1);
                    break;
                case "*":
                case "/":
                case "%":
                    getOper(ch ,2);
                    break;
                case "(":
                    stack.push(ch);
                    break;
                case ")":
                    //遇到右括号，把括号中的操作符，添加到后缀表达式字符串中。
                    getParen();
                    break;
                case ".":
                default:
                    try {
                        if (i>0){
                            char lastCh = input.charAt(i - 1);
                            if (lastCh != '.'){
                                Integer.parseInt(String.valueOf(lastCh));
                            }
                            String lastNum = outputList.remove(outputList.size() - 1);
                            System.out.println("lastNum = " + lastNum);
                            String num = lastNum+ch;
                            outputList.add(num);
                            break;
                        }
                    } catch (Exception e) {
                    }
                    outputList.add(ch);
                    break;
            }
        }
        while (!stack.isEmpty()){
            System.out.println("While "+Arrays.asList(stack));
            outputList.add(stack.pop());
        }
        System.out.println("End "+Arrays.asList(stack));

        return outputList;
    }

    /**
     * 从input获得操作符
     *
     * @param opThis
     * @param currentPriority 操作符的优先级
     */
    private void getOper(String  opThis, int currentPriority) {
        while (!stack.isEmpty()) {
            String opTop =  stack.pop();
//            System.out.println("opTop = " + opTop);
            //括号有较高优先级重新压入栈中
            if ("(".equals(opTop)) {
                stack.push(opTop);
                break;
            } else if ("%".equals(opTop)) {
                opThis += opTop;
            } else {
                int stackTopPriority;
                //+ ，-优先级都是1
                if ("+".equals(opTop) || "-".equals(opTop)) {
                    stackTopPriority = 1;
                } else {
                    stackTopPriority = 2;
                }
                //如果当前优先级大于栈顶部的优先级，重新压入栈中，否则出栈加入到后缀表达式字符串中
                if (stackTopPriority<currentPriority){
                    stack.push(opTop);
                    break;
                }else {
                    outputList.add(opTop);
                }
            }
        }
        stack.push(opThis);
    }

    public void getParen(){
        while (!stack.isEmpty()){
            String chx =  stack.pop();
            //如果是'('直接返回，其他操作符直接拼接到后缀表达式中。
            if ("(".equals(chx)){
                break;
            }else {
                outputList.add(chx);
            }
        }
    }
}


