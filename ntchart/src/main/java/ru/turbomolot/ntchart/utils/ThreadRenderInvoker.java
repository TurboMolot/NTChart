package ru.turbomolot.ntchart.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.turbomolot.ntchart.data.DataList;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class ThreadRenderInvoker {
    private final ExecutorService es;
    private final Runnable renderProcessor;
    private final List<Callable<Void>> cbInvoke = new DataList<>(1);
    public ThreadRenderInvoker(Runnable renderProcessor, int maxThreadCount) {
        if(renderProcessor == null)
            throw new NullPointerException("renderProcessor can not be null");
        if(maxThreadCount > 0)
            es = Executors.newFixedThreadPool(maxThreadCount);
        else
            es = Executors.newFixedThreadPool(1);
        this.renderProcessor = renderProcessor;
        initCb();
    }
    private void initCb() {
        cbInvoke.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                renderProcessor.run();
                return null;
            }
        });
    }

    public void execute() {
        es.execute(renderProcessor);
    }

    public void invokeAndWait() throws InterruptedException {
        es.invokeAll(cbInvoke);
    }

    public void stop() {
        if(!es.isShutdown() && !es.isTerminated()) {
            es.shutdownNow();
        }
    }
}
