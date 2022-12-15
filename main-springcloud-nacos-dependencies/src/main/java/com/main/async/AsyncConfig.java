package com.main.async;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

@Configuration
@EnableConfigurationProperties(AsyncConfig.AsyncThreadPoolProperties.class)
@Slf4j
public class AsyncConfig implements AsyncConfigurer {


    @Autowired
    private AsyncThreadPoolProperties asyncThreadPoolProperties;

    /**
     * 当线程数小于核心线程数时，创建线程。
     * 当线程数大于等于核心线程数，且任务队列未满时，将任务放入任务队列。
     * 当线程数大于等于核心线程数，且任务队列已满
     * 若线程数小于最大线程数，创建线程
     * 若线程数等于最大线程数，抛出异常，拒绝任务
     * <p>
     * <p>
     * 如何来设置
     * 需要根据几个值来决定
     * tasks ：每秒的任务数，假设为500~1000
     * taskcost：每个任务花费时间，假设为0.1s
     * responsetime：系统允许容忍的最大响应时间，假设为1s
     * 做几个计算
     * corePoolSize = 每秒需要多少个线程处理？
     * threadcount = tasks/(1/taskcost) =tasks*taskcout =  (500~1000)*0.1 = 50~100 个线程。corePoolSize设置应该大于50
     * 根据8020原则，如果80%的每秒任务数小于800，那么corePoolSize设置为80即可
     * queueCapacity = (coreSizePool/taskcost)*responsetime
     * 计算可得 queueCapacity = 80/0.1*1 = 80。意思是队列里的线程可以等待1s，超过了的需要新开线程来执行
     * 切记不能设置为Integer.MAX_VALUE，这样队列会很大，线程数只会保持在corePoolSize大小，当任务陡增时，不能新开线程来执行，响应时间会随之陡增。
     * maxPoolSize = (max(tasks)- queueCapacity)/(1/taskcost)
     * 计算可得 maxPoolSize = (1000-80)/10 = 92
     * （最大任务数-队列容量）/每个线程每秒处理能力 = 最大线程数
     * rejectedExecutionHandler：根据具体情况来决定，任务不重要可丢弃，任务重要则要利用一些缓冲机制来处理
     * keepAliveTime和allowCoreThreadTimeout采用默认通常能满足
     *
     * @return
     */
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数，核心线程不销毁
        executor.setCorePoolSize(asyncThreadPoolProperties.corePoolSize);
        // 设置最大线程数，任务超过核心线程后会创建新线程，直到线程数=maxPoolSize
        executor.setMaxPoolSize(asyncThreadPoolProperties.maxPoolSize);
        // 设置任务队列容量，任务数量超过容量后，按照setRejectedExecutionHandler设置的拒绝策略执行
        executor.setQueueCapacity(asyncThreadPoolProperties.queueCapacity);
        // 设置线程空闲时间（秒），超时空闲时间，线程被回收，直到线程数量=corePoolSize
        executor.setKeepAliveSeconds(asyncThreadPoolProperties.keepAliveSeconds);
        // 设置默认线程名称
        executor.setThreadNamePrefix("main-task-");

        try {
            // 设置拒绝策略，AbortPolicy为直接抛错
            Class<?> aClass = Class.forName("java.util.concurrent.ThreadPoolExecutor$" + asyncThreadPoolProperties.rejectedExecutionHandler);
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object o = constructor.newInstance();
            executor.setRejectedExecutionHandler((RejectedExecutionHandler) o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.setWaitForTasksToCompleteOnShutdown(asyncThreadPoolProperties.waitForTaskToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(asyncThreadPoolProperties.awaitTerminationSeconds);
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new MyAsyncExceptionHandler();
    }

    /**
     * 自定义异常处理类
     */

    class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            if (log.isErrorEnabled()) {
                log.error("Async Exception - " + throwable.getMessage());
            }
        }
    }

    @ConfigurationProperties(prefix = "async.thead.pool")
    @Data
    class AsyncThreadPoolProperties {
        private int corePoolSize = 10;
        private int maxPoolSize = 30;
        private int queueCapacity = 200;
        private int keepAliveSeconds = 30;
        private String threadNamePrefix = "main-task-";
        private String rejectedExecutionHandler = "AbortPolicy";
        private boolean waitForTaskToCompleteOnShutdown = true;
        private int awaitTerminationSeconds = 60;
    }
}
