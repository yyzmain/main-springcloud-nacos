package com.main.feign;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 自定义hystrix的策略，覆盖thread策略
 * 原因：因为加入了hystrix后，默认的隔离策略是thread，这会导致发起feign请求时是使用线程池发起请求
 * 会导致在feign的RequestInterceptor获取不到当前请求的header信息而无法把header参数完后面的服务传递，因此使用自定义的策略，在线程中共享request参数
 * 为什么不用hystrix.command.default.execution.isolation.strategy: SEMAPHORE
 * 因为这样会导致发起feign请求跟当前请求在同一个线程中，性能不好
 */
@Slf4j
public class FeignHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private HystrixConcurrencyStrategy delegate;

    public FeignHystrixConcurrencyStrategy() {
        try {
            this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (this.delegate instanceof FeignHystrixConcurrencyStrategy) {
                // Welcome to singleton hell...
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();

            log.debug("Current Hystrix plugins configuration is [" + "concurrencyStrategy ["
                    + this.delegate + "]," + "eventNotifier [" + eventNotifier + "]," + "metricPublisher ["
                    + metricsPublisher + "]," + "propertiesStrategy [" + propertiesStrategy + "]," + "]");

            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);

        } catch (Exception e) {
            log.error("Failed to register Feign Hystrix Concurrency Strategy", e);
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return new WrappedCallable<>(callable, requestAttributes);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                            HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize,
                                            HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime,
                unit, workQueue);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                            HystrixThreadPoolProperties threadPoolProperties) {
        return this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return this.delegate.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
        return this.delegate.getRequestVariable(rv);
    }

    static class WrappedCallable<T> implements Callable<T> {
        private final Callable<T> target;
        private final RequestAttributes requestAttributes;

        public WrappedCallable(Callable<T> target, RequestAttributes requestAttributes) {
            this.target = target;
            this.requestAttributes = requestAttributes;
        }

        @Override
        public T call() throws Exception {
            try {
                RequestContextHolder.setRequestAttributes(requestAttributes);

                return target.call();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }
}    