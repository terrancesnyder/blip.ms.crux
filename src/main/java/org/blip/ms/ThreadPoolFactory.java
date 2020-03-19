package org.blip.ms;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic builder for common thread pools and common patterns for building
 * thread pools with proper rejection and thread naming conventions and reasonable
 * defaults.
 */
public class ThreadPoolFactory {
    private int coreSize = Runtime.getRuntime().availableProcessors() * 4;
    private int poolSize = 1000;
    private int timeout = 5;
    private TimeUnit timeunit = TimeUnit.SECONDS;
    private ThreadPoolType type = ThreadPoolType.CacheThreadPool;

    private BlockingQueue<Runnable> taskQ;

    private ThreadPoolFactory() {
    }

    private ThreadPoolFactory(Builder builder) {
        this(builder.coreSize, builder.poolSize, builder.timeout, builder.timeunit, builder.type, builder.taskQ);
    }

    private ThreadPoolFactory(int coreSize, int poolSize, int timeout, TimeUnit timeunit) {
        this(coreSize, poolSize, timeout, timeunit, ThreadPoolType.CacheThreadPool);
    }

    private ThreadPoolFactory(int coreSize, int poolSize, int timeout, TimeUnit timeunit, ThreadPoolType type) {
        this(coreSize, poolSize, timeout, timeunit, type, null);
    }

    private ThreadPoolFactory(int coreSize, int poolSize, int timeout, TimeUnit timeunit, ThreadPoolType type, BlockingQueue<Runnable> taskQ) {
        this.coreSize = coreSize;
        this.poolSize = poolSize;
        this.timeout = timeout;
        this.timeunit = timeunit;
        this.type = type;
        this.taskQ = taskQ;
    }

    public ExecutorService create(final String name) {
        return this.create(name, new ThreadPoolExecutor.DiscardPolicy(),this::handle);
    }

    public ExecutorService create() {
        return this.create(UUID.randomUUID().toString(), new ThreadPoolExecutor.DiscardPolicy(), this::handle);
    }

    public ExecutorService create(final String name, final RejectedExecutionHandler handler) {
        return this.create(name, handler, this::handle);
    }

    private void handle(Thread t, Throwable e) {
        // ignore
    }

    private ExecutorService create(final String name, final RejectedExecutionHandler handler, final Thread.UncaughtExceptionHandler exHandler) {

        ThreadFactory tf = new ThreadFactory() {
            private AtomicInteger count = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                String id = name + "-" + count.incrementAndGet();
                Thread t = new Thread(r, id);
                t.setUncaughtExceptionHandler(exHandler);
                t.setDaemon(true);
                return t;
            }
        };


        BlockingQueue<Runnable> taskQ = this.taskQ;
        if (type == ThreadPoolType.ArrayBlockingQueue && taskQ == null) {
            taskQ = new ArrayBlockingQueue<>(this.coreSize);    // TODO tune me
        } else if (type == ThreadPoolType.LinkedBlockingQueue && taskQ == null) {
            taskQ = new LinkedBlockingQueue<>();
        } else if (type == ThreadPoolType.SynchronousQueue && taskQ == null) {
            taskQ = new SynchronousQueue<>();
        } else if (type == ThreadPoolType.CacheThreadPool) {
            return Executors.newCachedThreadPool(tf);
        } else if (type == ThreadPoolType.FixedThreadPool) {
            return Executors.newFixedThreadPool(this.coreSize, tf);
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(this.coreSize,
                this.poolSize,
                this.timeout,
                this.timeunit,
                taskQ,
                tf);

        pool.setRejectedExecutionHandler(handler);
        pool.setThreadFactory(tf);

        return pool;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int coreSize = 1;
        private int poolSize = Runtime.getRuntime().availableProcessors();
        private int timeout = 5;
        private TimeUnit timeunit = TimeUnit.SECONDS;
        private ThreadPoolType type = ThreadPoolType.CacheThreadPool;
        private BlockingQueue<Runnable> taskQ;

        private Builder() {

        }

        public Builder withTimeout(int value) {
            this.timeout = value;
            return this;
        }

        public Builder withTimeUnit(TimeUnit unit) {
            this.timeunit = unit;
            return this;
        }

        public Builder withCoreSize(int size) {
            this.coreSize = size;
            return this;
        }

        public Builder withPoolSize(int size) {
            this.poolSize = size;
            return this;
        }

        public Builder withBlockingQueue(BlockingQueue<Runnable> taskQ) {
            this.taskQ = taskQ;
            return this;
        }

        public ThreadPoolFactory build() {
            return new ThreadPoolFactory(this);
        }

        public Builder setType(ThreadPoolType type) {
            this.type = type;
            return this;
        }
    }
}
