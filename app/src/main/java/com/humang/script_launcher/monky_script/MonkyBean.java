package com.humang.script_launcher.monky_script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : created by amon
 * 时间 : 2022/9/24 16
 * 邮箱 ： yimeng.tang@humang.com
 * 主要功能 ：
 */
public class MonkyBean {
    public MonkyBean(String beanStr) {
        String[] bean = beanStr.split(",");
        this.id = Integer.parseInt(bean[0].trim());
        this.name = bean[1].trim();
        this.x1 = Integer.parseInt(bean[2].trim());
        this.y1 = Integer.parseInt(bean[3].trim());
        this.x2 = Integer.parseInt(bean[4].trim());
        this.y2 = Integer.parseInt(bean[5].trim());
        this.parentId = Integer.parseInt(bean[6].trim());
        this.weight = Integer.parseInt(bean[7].trim());
        this.times = Integer.parseInt(bean[8].trim());
        this.end = Integer.parseInt(bean[9].trim());
        initTempWeight();
    }

    public MonkyBean(int id) {
        this.id = id;
        this.weight = 10;
        initTempWeight();

    }

    private int id;
    private String name;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int parentId;
    private int weight;
    private int tempWeight;
    private int times;
    private int end;
    private int lastState;
    private Map<Integer,MonkyBean> childs = new HashMap<>();
    private Map<Integer,MonkyBean> finalChilds = new HashMap<>();

    @Override
    public String toString() {
        return "MonkyBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", parentId=" + parentId +
                ", weight=" + weight +
                ", tempWeight=" + tempWeight +
                ", times=" + times +
                ", end=" + end +
                ", lastState=" + lastState +
                ", childs=" + childs +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getTempWeight() {
        return tempWeight;
    }

    public void initTempWeight() {
        tempWeight = weight;
    }

    public void reduceWeight() {
        tempWeight -= 1;
        if (tempWeight <= weight/5) {
            initTempWeight();
        }
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getLastState() {
        return lastState;
    }

    public void setLastState(int lastState) {
        this.lastState = lastState;
    }

    public Map<Integer, MonkyBean> getChilds() {
        return childs;
    }

    public int getChildSize() {
        return childs.size();
    }

    public void putChild(int id,MonkyBean monkyBean) {
        childs.put(id,monkyBean);
    }

    public MonkyBean getChildById(int id) {
        return childs.get(id);
    }

    public Map<Integer, MonkyBean> getFinalChilds() {
        return finalChilds;
    }

    public int getFinalChildSize() {
        return finalChilds.size();
    }

    public void putFinalChild(int id,MonkyBean monkyBean) {
        finalChilds.put(id,monkyBean);
    }

    public MonkyBean getFinalChildById(int id) {
        return finalChilds.get(id);
    }
}
