package ru.turbomolot.ntchart.utils;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.series.ISeries;

/**
 * Created by TurboMolot on 05.10.17.
 */

public class ThreadMultiRenderInvoker {
    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final List<TaskRender> taskList = new DataList<>();
    private final Object taskListLock = new Object();
    public ThreadMultiRenderInvoker() {
    }

    public void addTask(TaskRender task) {
        synchronized (taskListLock) {
            taskList.add(task);
        }
    }

    public void invokeAll() throws InterruptedException {
        synchronized (taskListLock) {
            if (taskList.isEmpty())
                return;
            es.invokeAll(taskList);
            taskList.clear();
        }
    }

    public static class TaskRender implements Callable<Void> {
        private final ISeries series;
        private final ISeriesHolder holder;
        private final Map<ISeries, ISeriesHolder> holders;
        private final Picture picture;

        public TaskRender(ISeries series, Picture picture, ISeriesHolder holder, Map<ISeries, ISeriesHolder> holders) {
            if(series == null)
                throw new NullPointerException("series can not be null");
            if(holder == null)
                throw new NullPointerException("holder can not be null");
            if(holders == null)
                throw new NullPointerException("holders can not be null");
            if(picture == null)
                throw new NullPointerException("picture can not be null");
            this.series = series;
            this.holder = holder;
            this.holders = holders;
            this.picture = picture;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Void call() throws Exception {
            RectF wndSize = holder.getWindowSize();
            Canvas canvas = picture.beginRecording((int) wndSize.right, (int)wndSize.bottom);
            series.render(canvas, holder, holders);
            picture.endRecording();
            return null;
        }
    }
}
