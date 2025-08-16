package com.transcend.plm.datadriven.common.pool;

import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/18 14:48
 */
@Slf4j
public class IOThreadPool {

    private IOThreadPool() {

    }

    public static ExecutorService getInstance() {
        return ExecutorServiceHolder.executorService;
    }

    private static final class ExecutorServiceHolder {
        static final ExecutorService executorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(
                16,
                16,
                120,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2048),
                new SimpleThreadFactory("simple-io-"),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t == null && r instanceof Future<?>) {
                    try {
                        Future<?> future = (Future<?>) r;
                        if (future.isDone()) {
                            future.get();
                        }
                    } catch (CancellationException ce) {
                        t = ce;
                    } catch (ExecutionException ee) {
                        t = ee.getCause();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (t != null){
                    log.error("simple thread pool 执行任务异常", t);
                }
            }
        });
    }

    static class SimpleThreadFactory implements ThreadFactory {

        private final String threadName;

        public SimpleThreadFactory(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            //线程名称
            t.setName(threadName + t.getName());
            return t;
        }
    }
}
