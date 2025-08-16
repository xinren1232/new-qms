/*
package com.transcend.plm.datadriven.apm.powerjob.notify;

import lombok.Data;

*/
/**
 * 存储任务的容器entry
 *
 * @author quan.cheng
 * @title TimerTaskEntry
 * @date 2024/1/24 17:55
 * @description TODO
 *//*

@Data
public class TimerTaskEntry implements Comparable<TimerTaskEntry> {
    //任务 包装类 需要执行的任务
    private TimerTask timerTask;
    private long expireMs;
    //任务所在的链表
    volatile TimerTaskList timedTaskList;
    //双向链表
    //下一个
    TimerTaskEntry next;
    //上一个
    TimerTaskEntry prev;

    public TimerTaskEntry(TimerTask timedTask, long expireMs) {
        this.timerTask = timedTask;
        this.expireMs = expireMs;
        this.next = null;
        this.prev = null;
    }

    void remove() {
        TimerTaskList currentList = timedTaskList;
        while (currentList != null) {
            currentList.remove(this);
            currentList = timedTaskList;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        // 两个任务的执行时间差
        return ((int) (this.expireMs - o.expireMs));
    }
}*/
