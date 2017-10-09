package ru.turbomolot.ntchart.charts;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ru.turbomolot.ntchart.axis.AxisPosition;
import ru.turbomolot.ntchart.axis.IAxis;
import ru.turbomolot.ntchart.listener.NTTouchScaleMoveListener;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.utils.ForegroundDetector;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class NTChart extends TextureView implements TextureView.SurfaceTextureListener {
    private final NTChartHolder ntChartHolder = new NTChartHolder(this);
    private final AtomicReference<Surface> surface = new AtomicReference<>();

    /**
     * Контроль перехода приложения в свёрнутое/приостановленное состояние
     */
    private final ForegroundDetector.ForegroundListener foregroundListener
            = createForegroundListener();

    public NTChart(Context context) {
        super(context);
        init(context, null);
    }

    public NTChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NTChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public NTChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOpaque(false);
        if (isInEditMode())
            return;
        setSurfaceTextureListener(this);
        ForegroundDetector.init(Application.class.cast(context.getApplicationContext()));
        setOnTouchListener(new NTTouchScaleMoveListener(this));
    }

    private ForegroundDetector.ForegroundListener createForegroundListener() {
        return new ForegroundDetector.ForegroundListener() {
            @Override
            public void onBecameForeground() {
                ntChartHolder.appForeground();
            }

            @Override
            public void onBecameBackground() {
                ntChartHolder.appBackground();
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        if (!isInEditMode()) {
            ForegroundDetector.getInstance().addListener(foregroundListener);
            ntChartHolder.appForeground();
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            ForegroundDetector.getInstance().removeListener(foregroundListener);
            ntChartHolder.appBackground();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface.set(new Surface(surface));
        ntChartHolder.updateSurface(this.surface.get(), width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        ntChartHolder.updateSurfaceSize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        ntChartHolder.appBackground();
        this.surface.set(null);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        notifyChanged();
    }

    public void notifyChanged() {
        if (ForegroundDetector.getInstance().isForeground())
            ntChartHolder.notifyChanged();
    }

    public List<ISeries> getSeriesList() {
        return ntChartHolder.getSeriesList();
    }

    public int getSeriesCount() {
        return ntChartHolder.getSeriesCount();
    }

    public ISeries getSeries(int idx) {
        return ntChartHolder.getSeries(idx);
    }

    public void addSeries(ISeries series) {
        ntChartHolder.addSeries(series);
    }

    public void removeSeries(ISeries series) {
        ntChartHolder.removeSeries(series);
    }

    public void removeSeries(int idx) {
        ntChartHolder.removeSeries(idx);
    }

    public void clearSeries() {
        ntChartHolder.clearSeries();
    }

    public void applyMatrix(PointF startPoint, Matrix matrix) {
        ntChartHolder.applyMatrix(startPoint, matrix);
    }

    public boolean isShowFps() {
        return ntChartHolder.isShowFps();
    }

    public void setShowFps(boolean showFps) {
        ntChartHolder.setShowFps(showFps);
    }

    public IAxis getAxis(AxisPosition position) {
        return ntChartHolder.getAxis(position);
    }
}
