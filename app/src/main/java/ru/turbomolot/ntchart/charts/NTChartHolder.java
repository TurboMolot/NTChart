package ru.turbomolot.ntchart.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.turbomolot.ntchart.axis.AxisLine;
import ru.turbomolot.ntchart.axis.AxisPosition;
import ru.turbomolot.ntchart.axis.IAxis;
import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.utils.ConverterUtil;
import ru.turbomolot.ntchart.utils.FPSHelper;
import ru.turbomolot.ntchart.utils.ThreadCalcParam;
import ru.turbomolot.ntchart.utils.ThreadRenderInvoker;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class NTChartHolder {
    private final WeakReference<NTChart> wChart;
    private WeakReference<Surface> wSurfaceRender;
    private final AtomicBoolean surfaceChanged = new AtomicBoolean(false);
    private final Rect surfaceSize = new Rect();
    private final Object surfaceSizeLock = new Object();
    private final List<ISeries> seriesList = new DataList<>();
    private final Object seriesListLock = new Object();
    private final Map<ISeries, ISeriesHolder> seriesHolderMap = new WeakHashMap<>();

    private final List<IAxis> axisList = new DataList<>();
    private final Object axisListLock = new Object();

//    private final Queue<Map<ISeries, ISeriesHolder>> readyRenderQueue = new LinkedBlockingDeque<>(100);
//    private final Queue<Map<ISeries, ISeriesHolder>> emptyRenderQueue = new LinkedBlockingDeque<>(100);

    private final ThreadCalcParam threadCalcParam = new ThreadCalcParam();
    private final ThreadRenderInvoker threadCalcRenderFrame = new ThreadRenderInvoker(new Runnable() {
        @Override
        public void run() {
            try {
                calcRenderFrame(seriesHolderMap);
                renderFrame(seriesHolderMap);
//                if(readyRenderQueue.size() > 10)
//                    return;
//                Map<ISeries, ISeriesHolder> holderMap = emptyRenderQueue.poll();
//                if(holderMap == null) {
//                    holderMap = new WeakHashMap<>();
//                }
//                calcRenderFrame(holderMap);
//                readyRenderQueue.add(holderMap);
////                renderFrame(seriesHolderMap);
//                threadRenderInvoker.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }, 1);

//    private final ThreadRenderInvoker threadRenderInvoker = new ThreadRenderInvoker(new Runnable() {
//        @Override
//        public void run() {
//            try {
//                Map<ISeries, ISeriesHolder> holderMap = readyRenderQueue.poll();
//                if(holderMap == null)
//                    return;
//                while (!readyRenderQueue.isEmpty() && emptyRenderQueue.size() < 20) {
//                    emptyRenderQueue.add(readyRenderQueue.poll());
//                    return;
//                }
//                readyRenderQueue.clear();
//                renderFrame(holderMap);
//                emptyRenderQueue.add(holderMap);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }, 1);

    private final FPSHelper renderFps = new FPSHelper();
    private final Paint fpsPaintText = new Paint();
    private final Rect fpsTxtSize = new Rect();
    private final AtomicBoolean showFps = new AtomicBoolean(true);


    public NTChartHolder(NTChart chart) {
        if (chart == null)
            throw new NullPointerException("chart can not be null");
        this.wChart = new WeakReference<>(chart);

        Context context = chart.getContext();
        fpsPaintText.setAntiAlias(true);
        fpsPaintText.setColor(Color.parseColor("#1A7000"));
        fpsPaintText.setTextSize(ConverterUtil.convertDpToPixels(12, context));

        axisList.add(new AxisLine(AxisPosition.LEFT, context, null));
        axisList.add(new AxisLine(AxisPosition.BOTTOM, context, null));
    }

    protected NTChart getChart() {
        return wChart.get();
    }

    public void updateSurface(Surface surfaceRender, int width, int height) {
        if (surfaceRender == null)
            throw new NullPointerException("surfaceRender can not be null");
        if (wSurfaceRender != null)
            wSurfaceRender.clear();
        wSurfaceRender = new WeakReference<>(surfaceRender);
        updateSurfaceSize(width, height);
        surfaceChanged.set(true);
        appForeground();
    }

    public void updateSurfaceSize(int width, int height) {
        synchronized (surfaceSizeLock) {
            surfaceSize.right = width;
            surfaceSize.bottom = height;
        }
        updateAxisSizeAll();
        updateSeriesSizeAll();
    }

    protected void appForeground() {
//        if (isRealTimeRender() && !isRealTimeRenderRun()) {
//            startThreadRender();
//        }
    }

//    protected boolean isRealTimeRenderRun() {
//        return !(renderFinish.get() | calcFinish.get());
//    }

    protected void appBackground() {
//        stopThreadRender();
    }

    protected void renderFps(Canvas canvas) {
        if (showFps.get()) {
            String fpsVal = "fps: " + renderFps.getFpsStr();
            fpsPaintText.getTextBounds(fpsVal, 0, fpsVal.length(), fpsTxtSize);
            int h = fpsTxtSize.height();
            canvas.drawText(
                    fpsVal,
                    0,
                    h,
                    fpsPaintText);
            renderFps.tick();
        }
    }

    protected void calcRenderFrame(Map<ISeries, ISeriesHolder> holderMap) throws InterruptedException {
        final List<ISeries> series = getSeriesList();
        if (series.isEmpty())
            return;
        try {
            if (series.size() == 1) {
                ISeries it = series.get(0);
                ISeriesHolder holder = holderMap.get(it);
                if (holder == null) {
                    holder = it.createHolder();
                    if (holder == null)
                        return;
                    holderMap.put(it, holder);
                }
                it.calcParamRender(holder);
            } else {
                for (ISeries it : series) {
                    ISeriesHolder holder = holderMap.get(it);
                    if (holder == null) {
                        holder = it.createHolder();
                        if (holder == null)
                            continue;
                        holderMap.put(it, holder);
                    }
                    threadCalcParam.addTask(new ThreadCalcParam.TaskItem(it, holder));
                }
                threadCalcParam.invokeAll();
            }
        } finally {
            synchronized (axisListLock) {
                for (IAxis it : axisList) {
                    it.calcRenderParam(holderMap);
                }
            }
        }
    }

    protected void renderFrame(Map<ISeries, ISeriesHolder> holderMap) throws InterruptedException {
        final List<ISeries> series = getSeriesList();
        if (series.isEmpty() || holderMap == null)
            return;
        final Surface surfaceRender = (wSurfaceRender != null) ? wSurfaceRender.get() : null;
        if (surfaceRender == null)
            return;
        Canvas canvas;
        synchronized (surfaceSizeLock) {
            canvas = surfaceRender.lockCanvas(surfaceSize);
        }
        if (canvas == null)
            return;
        try {
            long time = System.currentTimeMillis();

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.TRANSPARENT);
            for (IAxis it : axisList) {
                it.render(canvas, holderMap);
            }
            for (ISeries it : series) {
                ISeriesHolder holder = holderMap.get(it);
                if (holder == null)
                    continue;
                long timeStep = System.currentTimeMillis();
                it.render(canvas, holder, holderMap);
                Log.d("[NTChartHolder_FRAME]", "[time frame]: " + String.valueOf(System.currentTimeMillis() - timeStep));
            }
            Log.d("[NTChartHolder]", "[time frame]: " + String.valueOf(System.currentTimeMillis() - time));
            renderFps(canvas);
        } finally {
            surfaceRender.unlockCanvasAndPost(canvas);
        }
    }


    protected void notifyChanged() {
        threadCalcRenderFrame.execute();
//        threadRenderInvoker.execute();
    }

    protected List<ISeries> getSeriesList() {
        synchronized (seriesListLock) {
            return Collections.unmodifiableList(seriesList);
        }
    }

    protected int getSeriesCount() {
        return seriesList.size();
    }

    protected ISeries getSeries(int idx) {
        if (idx < 0)
            return null;
        synchronized (seriesListLock) {
            if (idx < seriesList.size())
                return seriesList.get(idx);
        }
        return null;
    }

    protected RectF getWindowSizeAxis(ISeries series) {
        synchronized (axisListLock) {
            RectF offsetRes = new RectF();
            for (IAxis it : axisList) {
                if(it.isDependedSeries(series)) {
                    RectF offset = it.getWindowSize();
                    offsetRes.top = Math.max(offset.top, offsetRes.top);
                    offsetRes.left = Math.max(offset.left, offsetRes.left);
                    offsetRes.right = Math.max(offset.right, offsetRes.right);
                    offsetRes.bottom = Math.max(offset.bottom, offsetRes.bottom);
                }
            }
            return offsetRes;
        }
    }

    protected void updateAxisSizeAll() {
        NTChart chart = getChart();
        if(chart == null)
            return;
        int val = ConverterUtil.convertDpToPixels(32, chart.getContext());
        synchronized (axisListLock) {
            for (IAxis it : axisList) {
                if(!it.isWindowSizeManual()) {
                    synchronized (surfaceSizeLock) {
                        RectF size = new RectF(surfaceSize);
                        size.left += val;
                        size.right -= val;
                        size.top += val;
                        size.bottom -= val;
                        it.setWindowSize(size.left, size.top, size.right, size.bottom, false);
                    }
                }
            }
        }
    }

    protected void updateSeriesSizeAll() {
        synchronized (seriesListLock) {
            for (ISeries it : seriesList) {
                updateSeriesSize(it);
            }
        }
    }

    protected void updateSeriesSize(ISeries series) {
        if (series != null && (series.getWindowSize().isEmpty() || !series.isWindowSizeManual())) {
            RectF size = getWindowSizeAxis(series);
            synchronized (surfaceSizeLock) {
                if(size.isEmpty())
                    size = new RectF(surfaceSize);
                series.setWindowSize(size.left, size.top, size.right, size.bottom, false);
            }
        }
    }

    protected void setDependedSeries(ISeries series, AxisPosition position) {
        synchronized (axisListLock) {
            for (IAxis it : axisList) {
                if(it.getPosition() == position) {
                    it.addDependsSeries(series);
                    break;
                }
            }
        }
    }

    protected void removeDependedSeries(ISeries series, AxisPosition position) {
        synchronized (axisListLock) {
            for (IAxis it : axisList) {
                if(it.getPosition() == position) {
                    it.removeDependsSeries(series);
                    break;
                }
            }
        }
    }

    protected void clearDependedSeries(ISeries series) {
        removeDependedSeries(series, AxisPosition.LEFT);
        removeDependedSeries(series, AxisPosition.TOP);
        removeDependedSeries(series, AxisPosition.RIGHT);
        removeDependedSeries(series, AxisPosition.BOTTOM);
//                removeDependedSeries(series, AxisPosition.CENTER_X);
//                removeDependedSeries(series, AxisPosition.CENTER_Y);
        series.parentChanged(null);
    }

    protected void addSeries(ISeries series) {
        if (series == null)
            return;
        synchronized (seriesListLock) {
            seriesList.add(series);
            series.parentChanged(getChart());
            setDependedSeries(series, AxisPosition.LEFT);
            setDependedSeries(series, AxisPosition.BOTTOM);
            updateSeriesSize(series);
        }
    }

    protected void removeSeries(ISeries series) {
        if (series == null)
            return;
        synchronized (seriesListLock) {
            if (seriesList.remove(series)) {
                clearDependedSeries(series);
            }
        }
    }

    protected void removeSeries(int idx) {
        if (idx < 0)
            return;
        synchronized (seriesListLock) {
            if (idx < seriesList.size()) {
                ISeries series;
                if ((series = seriesList.remove(idx)) != null) {
                    clearDependedSeries(series);
                }
            }
        }
    }

    protected void clearSeries() {
        synchronized (seriesListLock) {
            for (ISeries itm : seriesList) {
                clearDependedSeries(itm);
            }
            seriesList.clear();
        }
    }

    protected void applyMatrix(PointF startPoint, Matrix matrix) {
        synchronized (seriesListLock) {
            for (ISeries itm : seriesList)
                itm.setMatrix(matrix);
        }
    }
}
