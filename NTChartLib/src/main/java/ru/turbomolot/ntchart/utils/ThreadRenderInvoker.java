package ru.turbomolot.ntchart.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class ThreadRenderInvoker {
    private final Runnable renderProcessor;
    private final Object lock = new Object();
    private final ExecutorService es;
    private final Callable<Void> callable;
    private final AtomicBoolean needUpdate = new AtomicBoolean();
    private final FPSHelper fpsHelper = new FPSHelper();
    private final static long ACTIVE_TIME_MS = TimeUnit.SECONDS.toMillis(1);
    private Future<Void> future;

    public ThreadRenderInvoker(Runnable renderProcessor) {
        if (renderProcessor == null)
            throw new NullPointerException("renderProcessor can not be null");
        this.renderProcessor = renderProcessor;
        this.es = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("ThRenderInvoker");
                return th;
            }
        });
        this.callable = createCallable();
        this.fpsHelper.setNormalFPS(30);
    }

    private Callable<Void> createCallable() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long currTime = System.currentTimeMillis();
                long time = currTime + ACTIVE_TIME_MS;
                try {
                    while (time > (currTime = System.currentTimeMillis())) {
                        renderProcessor.run();
                        fpsHelper.sleepNormal();
                        if (needUpdate.get()) {
                            needUpdate.set(false);
                            time = currTime + ACTIVE_TIME_MS;
                        }
                    }
                } catch (InterruptedException e) {
                    // skip
                }
                return null;
            }
        };
    }

    public void execute() {
        synchronized (lock) {
            needUpdate.lazySet(true);
            if (future == null || future.isCancelled() || future.isDone())
                future = es.submit(callable);
        }
    }
//    private final ExecutorService es;
//    private final Runnable renderProcessor;
//    private final List<Callable<Void>> cbInvoke = new DataList<>(1);
//    public ThreadRenderInvoker(Runnable renderProcessor, int maxThreadCount) {
//        if(renderProcessor == null)
//            throw new NullPointerException("renderProcessor can not be null");
//        if(maxThreadCount > 0)
//            es = Executors.newFixedThreadPool(maxThreadCount);
//        else
//            es = Executors.newFixedThreadPool(1);
//        this.renderProcessor = renderProcessor;
//        initCb();
//    }
//    private void initCb() {
//        cbInvoke.add(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                renderProcessor.run();
//                return null;
//            }
//        });
//    }
//
//    public void execute() {
//        es.execute(renderProcessor);
//    }
//
//    public void invokeAndWait() throws InterruptedException {
//        es.invokeAll(cbInvoke);
//    }
//
//    public void stop() {
//        if(!es.isShutdown() && !es.isTerminated()) {
//            es.shutdownNow();
//        }
//    }
}

