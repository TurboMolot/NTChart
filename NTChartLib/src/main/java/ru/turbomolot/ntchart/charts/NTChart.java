package ru.turbomolot.ntchart.charts;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ru.turbomolot.ntchart.data.IPoint;
import ru.turbomolot.ntchart.listener.NTGesturesListener;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.utils.ForegroundDetector;

/**
 * Created by TurboMolot on 23.11.17.
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

//    protected static final int GL_TEXTURE_EXTERNAL_OES = 0x8D62;

    private void init(Context context, AttributeSet attrs) {
        setOpaque(false);
        if (isInEditMode())
            return;
//        isHardwareAccelerated()
        setSurfaceTextureListener(this);
        ForegroundDetector.init(Application.class.cast(context.getApplicationContext()));
        setClickable(true);
        setOnTouchListener(new NTGesturesListener(this));

//        setOnTouchListener(new NTTouchScaleMoveListener(this));

//        int tex[] = new int[1];
//        GLES20.glGenTextures(1, tex, 0);
////        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
//        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, tex[0]);
//        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
////        textureName = tex[0];
//        final SurfaceTexture t = new SurfaceTexture(tex[0]);
//        t.release();
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
//        TextureView textureView = new TextureView(getContext());
//        GLES20.glGenTextures();
//        surface.attachToGLContext();
        ntChartHolder.updateSurface(this.surface.get(), width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        ntChartHolder.updateSurfaceSize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        ntChartHolder.appBackground();
        surface.release();
        this.surface.set(null);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        notifyChanged();
    }

    public void notifyChanged() {
        if (ForegroundDetector.getInstance().isForeground())
            ntChartHolder.notifyChanged();
    }

    /**
     * Добавить элемент отрисовки
     *
     * @param series элемент отрисовки данных
     */
    public void addSeries(ISeries series) {
        ntChartHolder.addSeries(series);
    }

    /**
     * Удалить элемент отрисовки
     *
     * @param series элемент отрисовки данных
     */
    public void removeSeries(ISeries series) {
        ntChartHolder.removeSeries(series);
    }

    /**
     * Запрос списка элементов отрисовки данных
     * @return списка элементов
     */
    public List<ISeries<? extends IPoint>> getSeries() {
        return ntChartHolder.getSeries();
    }
}
