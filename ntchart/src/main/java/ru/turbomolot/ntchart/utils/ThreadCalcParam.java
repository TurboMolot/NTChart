package ru.turbomolot.ntchart.utils;

import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.series.ISeries;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class ThreadCalcParam {

    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final List<TaskItem> taskList = new DataList<>();
    private final Object taskListLock = new Object();
    public ThreadCalcParam() {
    }

    public void addTask(TaskItem task) {
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

    public static class TaskItem implements Callable<Void> {
        private final ISeries series;
        private final ISeriesHolder holder;

        public TaskItem(ISeries series, ISeriesHolder holder) {
            if(series == null)
                throw new NullPointerException("series can not be null");
            if(holder == null)
                throw new NullPointerException("holder can not be null");
            this.series = series;
            this.holder = holder;
        }

        @Override
        public Void call() throws Exception {
            series.calcParamRender(holder);
            return null;
        }
    }
}
