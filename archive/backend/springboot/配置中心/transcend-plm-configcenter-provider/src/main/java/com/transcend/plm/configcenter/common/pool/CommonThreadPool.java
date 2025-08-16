package com.transcend.plm.configcenter.common.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 * 淘汰策略 为丢弃，会有风险，因此使用该线程池时，需要日志记录
 * @author jinpeng.bai
 * @date 2022/07/11 10:56
 **/
public enum CommonThreadPool {
    INSTANCES;
    private ThreadPoolExecutor threadPoolExecutor;

    CommonThreadPool(){
        BlockingQueue<Runnable> bq = new ArrayBlockingQueue<>(30);
        threadPoolExecutor = new ThreadPoolExecutor(3, Runtime.getRuntime().availableProcessors()*2, 50, TimeUnit.MILLISECONDS, bq);
    }
    public ThreadPoolExecutor getPool(){
         return threadPoolExecutor;
    }

}
