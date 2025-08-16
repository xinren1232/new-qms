/*
package com.transcend.plm.datadriven.apm.powerjob.notify;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;

*/
/**
 * 时间轮基本数据结构
 *
 * @author quan.cheng
 * @title TimeWheel
 * @date 2024/1/24 17:52
 * @description TODO
 *//*



@Slf4j
public class TimeWheel {
    */
/**
     * 一个槽的时间间隔(时间轮最小刻度) 本系统为毫秒
     *//*

    private long tickMs;

    */
/**
     * 时间轮大小(槽的个数)
     *//*

    private int wheelSize;

    */
/**
     * 一轮的时间跨度
     *//*

    private long interval;

    */
/**
     * 当前时间 本系统为毫秒
     *//*

    private long currentTime;

    */
/**
     * 槽
     *//*

    private TimerTaskList[] buckets;

    */
/**
     * 上层时间轮
     *//*

    private volatile TimeWheel overflowWheel;

    */
/**
     * 一个timer只有一个delayqueue
     *//*

    private DelayQueue<TimerTaskList> delayQueue;

    public TimeWheel(long tickMs, int wheelSize, long currentTime, DelayQueue<TimerTaskList> delayQueue) {
        this.currentTime = currentTime;
        // 一个卡槽的时间间隔20毫秒
        this.tickMs = tickMs;
        // 一个时间轮20个槽
        this.wheelSize = wheelSize;
        //  20ms*20=400ms 20个卡槽每个卡槽20ms 一圈400ms
        this.interval = tickMs * wheelSize;
        // 创建一个长度为20的环形队列
        this.buckets = new TimerTaskList[wheelSize];
        // $$当前时间去余数  实例 1001%20=1 ,1001-1=1000 确保当前时间在时间轮的一个槽上
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        // 设置每个卡槽 需要存储任务的环形列表
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new TimerTaskList();
        }
    }


    */
/**
     * 核心算法
     * 添加一个timerTask 如果到期直接执行，否则插入到合适的槽中
     *
     *
     * @param entry 存储任务的容器entry
     * @return 是否成功
     *//*



    public boolean add(TimerTaskEntry entry) {
        // 获取任务的过期时间
        long expiration = entry.getExpireMs();
        // 如果过期时间小于当前时间加卡槽，直接执行
        log.info("expiration:{},currentTime:{},interval:{},systime:{}", expiration, currentTime, interval, System.currentTimeMillis());
        if (expiration < tickMs + currentTime) {
            //到期了
            return false;
        } else if (expiration < currentTime + interval) {
            //如果小于当前时间加一伦的时间跨度则进行降级处理
            //扔进=====当前时间轮====的某个槽里,只有时间大于某个槽,才会放进去
            long virtualId = (expiration / tickMs);
            // % wheelSize 保证在时间轮的一个槽上
            long l = virtualId % wheelSize;
            // l需要向上取整
            int index = (int) Math.ceil(l);
            // 获取到期时间的槽
            TimerTaskList bucket = buckets[index];
            //$$ 任务添加到环形链表中(会导致链表无限循环)
            bucket.addTask(entry);
            //设置bucket 过期时间 如果过期时间不一致则设置为最小的过期时间
            if (bucket.setExpiration(virtualId * tickMs)) {
                //设好过期时间的bucket需要重新入队
                delayQueue.offer(bucket);
            }
            // 返回会导致循环
            return true;
        } else {
            //当前轮不能满足,需要扔到上一轮
            TimeWheel timeWheel = getOverflowWheel();
            return timeWheel.add(entry);
        }
    }


    */
/**
     * 创建上层时间轮
     *//*

    private TimeWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimeWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    */
/**
     * 推进指针
     *
     * @param timestamp
     *//*

    public void advanceLock(long timestamp) {
        if (timestamp > currentTime + tickMs) {
            // 将当前时间轮的指针推进到timestamp所在的槽
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                // 推进上层时间轮的指针
                this.getOverflowWheel().advanceLock(timestamp);
            }
        }
    }
}
*/
