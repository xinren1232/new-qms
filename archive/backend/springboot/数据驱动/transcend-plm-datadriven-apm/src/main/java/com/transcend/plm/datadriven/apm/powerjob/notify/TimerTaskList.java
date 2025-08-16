/*
package com.transcend.plm.datadriven.apm.powerjob.notify;

*/
/**
 * @author quan.cheng
 * @title TimerTaskList
 * @date 2024/1/24 17:55
 * @description TODO
 *//*


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

*/
/**
 * 将存储任务的容器entry 用环形链表连接起来
 *
 * @author apdoer
 * @version 1.0
 * @date 2021/3/22 19:26
 *//*

@Data
@Slf4j
public class TimerTaskList implements Delayed {
    */
/**
     * TimerTaskList 环形链表使用一个虚拟根节点root
     * 用于指向第一个节点
     *//*

    private TimerTaskEntry root = new TimerTaskEntry(null, -1);

    {
        root.next = root;
        root.prev = root;
    }

    */
/**
     * bucket的过期时间
     *//*

    private AtomicLong expiration = new AtomicLong(-1L);

    public long getExpiration() {
        return expiration.get();
    }

    */
/**
     * 设置bucket的过期时间,设置成功返回true
     *
     * @param expirationMs
     * @return
     *//*

    boolean setExpiration(long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    public boolean addTask(TimerTaskEntry entry) {
        boolean done = false;
        while (!done) {
            //如果TimerTaskEntry已经在别的list中就先移除,同步代码块外面移除,避免死锁,一直到成功为止
            entry.remove();
            synchronized (this) {
                if (entry.timedTaskList == null) {
                    //加到链表的末尾
                    entry.timedTaskList = this;
                    TimerTaskEntry tail = root.prev;
                    entry.prev = tail;
                    entry.next = root;
                    tail.next = entry;
                    root.prev = entry;
                    done = true;
                }
            }
        }
        return true;
    }

    */
/**
     * 从 TimedTaskList 移除指定的 timerTaskEntry
     *
     * @param entry
     *//*

    public void remove(TimerTaskEntry entry) {
        synchronized (this) {
            //如果TimerTaskEntry 已存在于当前链表中则移除
            if (entry.getTimedTaskList().equals(this)) {

                // 将entry的上个节点的next指向entry的下个节点
                entry.prev.next = entry.next;
                // 将entry的下个节点的prev指向entry的上个节点
                entry.next.prev = entry.prev;
                entry.next = null;
                entry.prev = null;
                entry.timedTaskList = null;
            }
        }
    }

    */
/**
     * 移除所有
     *//*

    public synchronized void clear(Consumer<TimerTaskEntry> entry) {
        TimerTaskEntry head = root.next;
        // 从头到尾遍历链表双向链表,并移除
        while (!head.equals(root)) {
            remove(head);
            entry.accept(head);
            head = root.next;
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expiration.get(), ((TimerTaskList) o).expiration.get());
        }
        return 0;
    }
}*/
