package ru.turbomolot.ntchart.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.turbomolot.ntchart.axis.IAxis;
import ru.turbomolot.ntchart.data.IPoint;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.utils.ConverterUtil;
import ru.turbomolot.ntchart.utils.FPSHelper;
import ru.turbomolot.ntchart.utils.ForegroundDetector;
import ru.turbomolot.ntchart.utils.ThreadRenderInvoker;

/**
 * Created by TurboMolot on 23.11.17.
 */

public class NTChartHolder {
    private final static String TAG = "[NTChartHolder]";
    private final WeakReference<NTChart> wChart;
    private WeakReference<Surface> wSurfaceRender;
    private final Rect surfaceSize = new Rect();
    private final Object surfaceSizeLock = new Object();
    private final List<ISeries<? extends IPoint>> seriesList = new CopyOnWriteArrayList<>();
    private final ThreadRenderInvoker thRenderFrame;

    private final FPSHelper renderFps = new FPSHelper();
    private final Paint fpsPaintText = new Paint();
    private final Rect fpsTxtSize = new Rect();
    private final AtomicBoolean showFps = new AtomicBoolean(true);

    NTChartHolder(NTChart chart) {
        if (chart == null)
            throw new NullPointerException("chart can not be null");
        this.wChart = new WeakReference<>(chart);
        this.thRenderFrame = createThreadRender();
        initFps();
    }

    private ThreadRenderInvoker createThreadRender() {
        return new ThreadRenderInvoker(new Runnable() {
            @Override
            public void run() {
                try {
                    renderFrame();
                } catch (Exception ex) {
                    Log.e(TAG, "Render failed", ex);

                }
            }
        });
    }

    private void initFps() {
        NTChart chart = getChart();
        if (chart == null)
            return;
        Context context = chart.getContext();
        fpsPaintText.setAntiAlias(true);
        fpsPaintText.setColor(Color.parseColor("#1A7000"));
        fpsPaintText.setTextSize(ConverterUtil.convertDpToPixels(12, context));
    }

    void appForeground() {
    }

    void appBackground() {
    }

    private void updateSeriesSize() {
        for (ISeries itm : seriesList) {
            RectF size = new RectF(surfaceSize);
            itm.setRenderPosition(size);
        }
    }

    void updateSurfaceSize(int width, int height) {
        synchronized (surfaceSizeLock) {
            surfaceSize.right = width;
            surfaceSize.bottom = height;
            updateSeriesSize();
        }
    }

    void updateSurface(Surface surfaceRender, int width, int height) {
        if (surfaceRender == null)
            throw new NullPointerException("surfaceRender can not be null");
        if (wSurfaceRender != null)
            wSurfaceRender.clear();
        wSurfaceRender = new WeakReference<>(surfaceRender);
        updateSurfaceSize(width, height);
    }

    private NTChart getChart() {
        return wChart.get();
    }

    /**
     * Добавить элемент отрисовки
     *
     * @param series элемент отрисовки данных
     */
    void addSeries(ISeries series) {
        if (series == null || seriesList.contains(series))
            return;
        seriesList.add(series);
        series.parentChanged(getChart());

        RectF size = new RectF(surfaceSize);
        series.setRenderPosition(size);
    }

    /**
     * Удалить элемент отрисовки
     *
     * @param series элемент отрисовки данных
     */
    void removeSeries(ISeries series) {
        if (series == null || !seriesList.contains(series))
            return;
        seriesList.remove(series);
        series.parentChanged(null);
    }

    List<ISeries<? extends IPoint>> getSeries() {
        return Collections.unmodifiableList(seriesList);
    }


    void notifyChanged() {
        if (ForegroundDetector.getInstance().isForeground()) {
            thRenderFrame.execute();
        }
    }

    private void renderFps(Canvas canvas) {
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

    private void renderSeries(Canvas canvas) {
        for (ISeries itm : seriesList) {
            long time = System.currentTimeMillis();
            itm.calculateRender();
            Log.d(TAG, "Time calc: [" + String.valueOf(System.currentTimeMillis() - time) + "]");
            time = System.currentTimeMillis();
            itm.render(canvas);
            Log.d(TAG, "Time render: [" + String.valueOf(System.currentTimeMillis() - time) + "]");
        }
    }

    private void renderFrame() {
        final Surface surfaceRender = (wSurfaceRender != null) ? wSurfaceRender.get() : null;
        if (surfaceRender == null || !surfaceRender.isValid())
            return;
        Canvas canvas;
        synchronized (surfaceSizeLock) {
            if (surfaceSize.isEmpty())
                return;
            canvas = surfaceRender.lockCanvas(surfaceSize);
        }
        if (canvas == null)
            return;
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.TRANSPARENT);
            renderSeries(canvas);
            renderFps(canvas);
        } catch (Exception e) {
            Log.e(TAG, "Error render frame", e);
        } finally {
            surfaceRender.unlockCanvasAndPost(canvas);
        }
    }
}
