/*
package com.transcend.plm.datadriven.apm.powerjob.notify;


import com.alibaba.fastjson.JSON;
//import com.transcend.plm.datadriven.apm.powerjob.notify.sevice.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

*/
/**
 * 定时器实现
 *
 * @author quan.cheng
 * @title SystemTimer
 * @date 2024/1/24 17:54
 * @description TODO
 *//*


@Slf4j
@Service
public class SystemTimer implements Timer {
    */
/**
     * 底层时间轮
     *//*

    private TimeWheel timeWheel;
    */
/**
     * 一个Timer（定时器）只有一个延时队列
     * 存储过期的任务
     *//*

    private DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();
    */
/**
     * 过期任务执行线程
     *//*

    private ThreadPoolExecutor workerThreadPool;
    */
/**
     * 轮询delayQueue获取过期任务线程
     *//*

    private ThreadPoolExecutor bossThreadPool;


    public SystemTimer() {
        this.timeWheel = new TimeWheel(1, 20, System.currentTimeMillis(), delayQueue);

        this.workerThreadPool = new ThreadPoolExecutor(1,
                100,
                60,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        this.bossThreadPool = new ThreadPoolExecutor(1,
                1,
                60,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        //20ms推动一次时间轮运转
        this.bossThreadPool.execute(() -> {
            int i = 0;
            for (; ; ) {
                advanceClock(1000);
            }
        });
    }

//    public static void main(String[] args) {
//        SystemTimer systemTimer1 = new SystemTimer();
//        log.info("start");
//
//        int a = 0;
//
//        for (int i = 0; i < 10; i++) {
//            //休眠20ms
//            try {
//                TimeUnit.MILLISECONDS.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (i == 9) {
//                log.info("add task {}", i);
//            }
//            a += i * 10000;
//            ApmNotifyExecuteRecord apmNotifyExecuteRecord = new ApmNotifyExecuteRecord();
//            systemTimer1.add(new TimerTask(Collections.singletonList(apmNotifyExecuteRecord), a));
//        }
//        System.out.println("end");
//
//
//    }


    public void addTimerTaskEntry(TimerTaskEntry entry) {
        if (!timeWheel.add(entry)) {
            //已经过期了
            TimerTask timerTask = entry.getTimerTask();
            if (timerTask != null) {
                log.info("=====任务:{} 已到期,准备执行============", JSON.toJSONString(timerTask.getExtractedRecord()));
                workerThreadPool.execute(timerTask);
                //entry.remove();
            } else {
                // 获取任务为空
                log.info("=====任务:{} 已到期,但是任务为空============", JSON.toJSONString(timerTask.getExtractedRecord()));
            }
        }
    }

    @Override
    public void add(TimerTask timerTask) {
        log.info("=======添加任务开始====task:{}", JSON.toJSONString(timerTask.getExtractedRecord()));
        TimerTaskEntry entry = new TimerTaskEntry(timerTask, timerTask.getDelayMs() + System.currentTimeMillis());
        timerTask.setTimerTaskEntry(entry);
        addTimerTaskEntry(entry);
    }

    */
/**
     * 推动指针运转获取过期任务
     *
     * @param timeout 时间间隔
     * @return
     *//*

    @Override
    public synchronized void advanceClock(long timeout) {
        try {
            */
/**
             * 检索并删除此队列的头，如有必要，请等待，直到具有过期延迟的元素在此队列中可用，或者指定的等待时间过期。
             * 退货：
             * 该队列的头，如果在具有过期延迟的元素可用之前经过了指定的等待时间，则为null
             * 投掷：
             * InterruptedException–如果在等待时被中断
             *//*

            // 获取队列中过期的环形列表数据
            TimerTaskList bucket = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                //推进时间
                timeWheel.advanceLock(bucket.getExpiration());
                //执 行过期任务(包含降级)
                bucket.clear(this::addTimerTaskEntry);
            }
        } catch (InterruptedException e) {
            log.error("advanceClock error");
        }
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void shutdown() {
        this.bossThreadPool.shutdown();
        this.workerThreadPool.shutdown();
        this.timeWheel = null;
    }
}
*/
