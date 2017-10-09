package ru.turbomolot.ntchart.series;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.data.IPointLine;
import ru.turbomolot.ntchart.data.PointLine;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.utils.MathHelper;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class SeriesLine implements ISeries<IPointLine> {

    private final AtomicReference<Float> maxDistanceStore = new AtomicReference<>();
    private List<IPointLine> ptsSource;
    protected final Object ptsLock = new Object();
    private WeakReference<NTChart> wChart;
    private RectF windowSize = new RectF();
    private AtomicBoolean windowSizeManual = new AtomicBoolean();
    private final Object windowSizeLock = new Object();
    private final Matrix matrix = new Matrix();
    private final Object matrixLock = new Object();
    private final AtomicReference<Float> maxDistanceX = new AtomicReference<>();
    private final AtomicReference<Float> maxDistanceY = new AtomicReference<>();

    private final AtomicReference<Float> minX = new AtomicReference<>(null);
    private final AtomicReference<Float> maxX = new AtomicReference<>(null);
    private final AtomicReference<Float> minY = new AtomicReference<>(null);
    private final AtomicReference<Float> maxY = new AtomicReference<>(null);

    private Paint linePaint;
    private Paint fillPaint;
    private Drawable fillDrawable;
    private AtomicBoolean fill = new AtomicBoolean(true);
    private AtomicBoolean lineVisible = new AtomicBoolean(true);
    private AtomicBoolean reducePointsEnabled = new AtomicBoolean(false);
    private AtomicBoolean renderFromAxisRight = new AtomicBoolean(false);

    private final Object renderLock = new Object();
    private final AtomicReference<String> title = new AtomicReference<>();

    public SeriesLine(String title) {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.parseColor("#0090FF"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.parseColor("#3EBCBF80"));
        fillPaint.setStyle(Paint.Style.FILL);
        setTitle(title);
    }

    public SeriesLine() {
        this(null);
    }

    protected void trimMaxDistanceStore() {
        Float maxDistanceCurrent = getMaxDistanceStore();
        if (maxDistanceCurrent != null && maxDistanceCurrent > 0) {
            synchronized (ptsLock) {
                if (ptsSource != null && ptsSource.size() > 1) {
                    float lastVal = ptsSource.get(ptsSource.size() - 1).getX();
                    if (Math.abs(lastVal - ptsSource.get(0).getX()) > maxDistanceCurrent) {
                        int idxEnd = getIndexXBefore(lastVal - maxDistanceCurrent);
                        if (idxEnd >= 0 && idxEnd < ptsSource.size())
                            ptsSource.subList(0, idxEnd).clear();
                    }
                }
            }
        }
    }

    private int getIndexXBefore(float xVal) {
        return MathHelper.getIndexXBefore(ptsSource, xVal);
    }

    private void addPoint(IPointLine point, boolean notify, boolean trimDataSet) {
        if (point == null)
            return;
        synchronized (ptsLock) {
            if (ptsSource == null)
                ptsSource = new DataList<>();
            int idx = getIndexXBefore(point.getX()) + 1;
            ptsSource.add(idx, point);
        }
        if (trimDataSet)
            trimMaxDistanceStore();
        if (notify)
            notifyChanged();

    }

    @Override
    public void addPoint(IPointLine point) {
        addPoint(point, true, true);
    }

    @Override
    public void addPoints(List<? extends IPointLine> points) {
        addPoints(points, true);
    }

    @Override
    public void addPoint(IPointLine point, boolean notifyChanged) {
        addPoint(point, notifyChanged, true);
    }

    @Override
    public void addPoints(List<? extends IPointLine> points, boolean notifyChanged) {
        if (points == null || points.isEmpty())
            return;
        for (IPointLine itm : points)
            addPoint(itm, false, false);
        trimMaxDistanceStore();
        if (notifyChanged)
            notifyChanged();
    }

    @Override
    public void clearPoints() {
        synchronized (ptsLock) {
            ptsSource.clear();
        }
    }

    @Override
    public List<IPointLine> getPoints() {
        synchronized (ptsLock) {
            if (ptsSource != null && !ptsSource.isEmpty())
                return new DataList<>(ptsSource);
            return null;
        }
    }

    @Override
    public void notifyChanged() {
        NTChart chart = getChart();
        if (chart != null)
            chart.notifyChanged();
    }

    @Override
    public void setMaxDistanceStore(Float maxDistanceStore) {
        this.maxDistanceStore.lazySet(maxDistanceStore);
    }

    @Override
    public Float getMaxDistanceStore() {
        return maxDistanceStore.get();
    }

    @Override
    public void setFill(boolean fill) {
        this.fill.lazySet(fill);
    }

    @Override
    public boolean isFill() {
        return fill.get();
    }

    public void setLineVisible(boolean fill) {
        this.lineVisible.lazySet(fill);
    }
    public boolean isLineVisible() {
        return lineVisible.get();
    }

    @Override
    public void setFillDrawable(Drawable fillDrawable) {
        synchronized (renderLock) {
            this.fillDrawable = fillDrawable;
        }
    }

    @Override
    public Drawable getFillDrawable() {
        return fillDrawable;
    }

    @Override
    public void setFillColor(@ColorInt int fillColor) {
        synchronized (renderLock) {
            Paint paint = getFillPaint();
            if (paint != null)
                paint.setColor(fillColor);
        }
    }

    @Override
    public int getFillColor() {
        Paint paint = getFillPaint();
        return paint != null ? paint.getColor() : Color.TRANSPARENT;
    }

    @Override
    public void setFillPaint(Paint fillPaint) {
        synchronized (renderLock) {
            this.fillPaint = fillPaint;
        }
    }

    @Override
    public Paint getFillPaint() {
        return fillPaint;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public void setLinePaint(Paint linePaint) {
        synchronized (renderLock) {
            this.linePaint = linePaint;
        }
    }

    public void setLineWidth(float lineWidthPx) {
        Paint lPaint = getLinePaint();
        if (lPaint != null)
            lPaint.setStrokeWidth(lineWidthPx);
    }

    @Override
    public void setColor(@ColorInt int color) {
        synchronized (renderLock) {
            Paint paint = getLinePaint();
            if (paint != null)
                paint.setColor(color);
        }
    }

    @Override
    public int getColor() {
        Paint paint = getLinePaint();
        return paint != null ? paint.getColor() : Color.TRANSPARENT;
    }

    @Override
    public ISeriesHolder createHolder() {
        return new SeriesLineHolder();
    }

    @Override
    public void parentChanged(NTChart chart) {
        wChart = new WeakReference<>(chart);
    }

    protected final NTChart getChart() {
        return wChart != null ? wChart.get() : null;
    }

    @Override
    public void setWindowSize(float left, float top, float right, float bottom, boolean manual) {
        synchronized (windowSizeLock) {
            windowSize.set(left, top, right, bottom);
            windowSizeManual.lazySet(manual);
        }
    }

    @Override
    public boolean isWindowSizeManual() {
        return windowSizeManual.get();
    }

    @Override
    public RectF getWindowSize() {
        synchronized (windowSizeLock) {
            return new RectF(windowSize);
        }
    }

    @Override
    public Matrix getMatrix() {
        synchronized (matrixLock) {
            return new Matrix(matrix);
        }
    }

    @Override
    public void setMatrix(Matrix matrix) {
        synchronized (matrixLock) {
            this.matrix.set(matrix);
        }
    }

    @Override
    public Float getMaxDistanceX() {
        return maxDistanceX.get();
    }

    @Override
    public void setMaxDistanceX(Float maxDistanceX) {
        this.maxDistanceX.set(maxDistanceX);
    }

    @Override
    public Float getMaxDistanceY() {
        return maxDistanceY.get();
    }

    @Override
    public void setMaxDistanceY(Float maxDistanceY) {
        this.maxDistanceY.set(maxDistanceY);
    }

    @Override
    public void setTitle(String title) {
        this.title.lazySet(title);
    }

    @Override
    public String getTitle() {
        return title.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void calcParamRender(ISeriesHolder holder) {
        synchronized (windowSizeLock) {
            holder.setWindowSize(windowSize.left, windowSize.top,
                    windowSize.right, windowSize.bottom);
        }
        synchronized (matrixLock) {
            holder.setMatrix(matrix);
        }
        Float mdxy = maxDistanceX.get();
        holder.setMaxDistanceX(mdxy != null ? mdxy : 0);
        mdxy = maxDistanceY.get();
        holder.setMaxDistanceY(mdxy != null ? mdxy : 0);
        holder.setReducePointsEnabled(isReducePointsEnabled());
        holder.setMaxX(getMaxX());
        holder.setMaxY(getMaxY());
        holder.setMinX(getMinX());
        holder.setMinY(getMinY());
        holder.setRenderFromAxisRight(isRenderFromAxisRight());
        holder.calcRender(getPoints());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Canvas canvas, ISeriesHolder holder, Map<ISeries, ISeriesHolder> holders) {
        synchronized (renderLock) {
            final List<? extends IPointLine> ptsRender
                    = (List<? extends IPointLine>) holder.getRender();

            List<Path> paths = holder.getRenderPaths();
            Path lPath = paths.get(0);
            Path fPath = paths.get(1);

            if(isLineVisible())
                renderLine(canvas, ptsRender, holder, holders, lPath);
            if (isFill())
                renderFill(canvas, ptsRender, holder, holders, fPath);
        }
    }

    protected boolean renderLine(Canvas canvas, List<? extends IPointLine> ptsRender,
                                 ISeriesHolder holder, Map<ISeries, ISeriesHolder> holders,
                                 Path lPath) {
        RectF wndSize = holder.getWindowSize();
        Paint lPaint = getLinePaint();
        if (ptsRender == null
                || ptsRender.isEmpty()
                || lPath == null
                || lPaint == null
                || wndSize == null
                || wndSize.isEmpty())
            return false;
        int cnt = canvas.save();
        canvas.clipRect(wndSize.left, wndSize.top, wndSize.right, wndSize.bottom);
        canvas.drawPath(lPath, lPaint);
        canvas.restoreToCount(cnt);
        return true;
    }

    protected Paint getFillPaint(List<? extends IPointLine> ptsRender,
                                 ISeriesHolder holder,
                                 Map<ISeries, ISeriesHolder> holders) {
        return getFillPaint();
    }

    protected void renderFill(Canvas canvas,
                              List<? extends IPointLine> ptsRender,
                              ISeriesHolder holder,
                              Map<ISeries, ISeriesHolder> holders, Path fPath) {
        Paint fPaint = getFillPaint(ptsRender, holder, holders);
        RectF wndSize = holder.getWindowSize();
        if (fPath == null
                || fPaint == null
                || ptsRender == null
                || ptsRender.isEmpty()
                || wndSize == null)
            return;

        int cnt = canvas.save();
        canvas.clipRect(wndSize.left, wndSize.top, wndSize.right, wndSize.bottom);
        canvas.clipPath(fPath);
        Drawable drawable;
        if ((drawable = fillDrawable) == null) {
            canvas.drawPaint(fPaint);
        } else {
            drawable.draw(canvas);
        }
        canvas.restoreToCount(cnt);


    }

    @Override
    public void setReducePointsEnabled(boolean reducePointsEnabled) {
        this.reducePointsEnabled.lazySet(reducePointsEnabled);
    }

    @Override
    public boolean isReducePointsEnabled() {
        return reducePointsEnabled.get();
    }

    @Override
    public void setMinX(Float minX) {
        this.minX.lazySet(minX);
    }

    @Override
    public Float getMinX() {
        return this.minX.get();
    }

    @Override
    public void setMinY(Float minY) {
        this.minY.lazySet(minY);
    }

    @Override
    public Float getMinY() {
        return this.minY.get();
    }

    @Override
    public void setMaxX(Float maxX) {
        this.maxX.lazySet(maxX);
    }

    @Override
    public Float getMaxX() {
        return this.maxX.get();
    }

    @Override
    public void setMaxY(Float maxY) {
        this.maxY.lazySet(maxY);
    }

    @Override
    public Float getMaxY() {
        return this.maxY.get();
    }

    public boolean isRenderFromAxisRight() {
        return renderFromAxisRight.get();
    }

    public void setRenderFromAxisRight(boolean renderFromAxisRight) {
        this.renderFromAxisRight.lazySet(renderFromAxisRight);
    }
}
