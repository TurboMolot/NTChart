package ru.turbomolot.ntchart.axis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.formatter.IFormatterValue;
import ru.turbomolot.ntchart.formatter.NTFormatterValue;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.series.ISeries;
import ru.turbomolot.ntchart.utils.ConverterUtil;

/**
 * Created by TurboMolot on 05.10.17.
 */

public class AxisLine implements IAxis {
    private final AxisPosition axisPosition;
    private final List<ISeries> dependsSeries = new DataList<>();
    private final Object dependsSeriesLock = new Object();
    private final WeakReference<Context> wContext;
    private final RectF windowSize = new RectF();
    private final Object windowSizeLock = new Object();
    private AtomicBoolean windowSizeManual = new AtomicBoolean();
    private final Rect boundsTextMeasuring = new Rect();
    private final AtomicBoolean visible = new AtomicBoolean(true);
    private final AtomicBoolean axisLineVisible = new AtomicBoolean(true);
    private final AtomicBoolean gridVisible = new AtomicBoolean(true);
    private final AtomicBoolean valueVisible = new AtomicBoolean(true);
    private final AtomicReference<IFormatterValue> formatter =
            new AtomicReference<>((IFormatterValue) new NTFormatterValue());

    private Path axisPath;
    private Paint axisLinePaint;
    private Path gridLinePath;
    private Paint gridLinePaint;
    private Path valuePath;
    private Paint valuePaint;

    private float maxStep = 6;

    private float posXFrom;
    private float posXTo;
    private float posYFrom;
    private float posYTo;
    //
    private float posXAxisValue;
    private float posYAxisValue;

    private float prevMaxValueBounds = -1;
    private WeakReference<MaxValueBoundsChange> wMaxValueBoundsChangeListener;
    private float offsetValue;
//
//    private float xStart;
//    private float xEnd;
//    private float yStart;
//    private float yEnd;

    public AxisLine(AxisPosition axisPosition, Context context, RectF windowSize) {
        if (axisPosition == null)
            throw new NullPointerException("axisPosition can not be null");
        if (context == null)
            throw new NullPointerException("context can not be null");
        this.axisPosition = axisPosition;
        this.wContext = new WeakReference<>(context);
        this.offsetValue = ConverterUtil.convertDpToPixels(2, context);

        this.axisPath = new Path();
        this.axisLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.axisLinePaint.setStyle(Paint.Style.STROKE);
        this.axisLinePaint.setStrokeJoin(Paint.Join.ROUND);
        this.axisLinePaint.setStrokeCap(Paint.Cap.ROUND);
        this.axisLinePaint.setStrokeWidth(1);
        this.axisLinePaint.setColor(Color.parseColor("#1F1F1F"));

        this.gridLinePath = new Path();
        this.gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.gridLinePaint.setStyle(Paint.Style.STROKE);
        this.gridLinePaint.setStrokeJoin(Paint.Join.ROUND);
        this.gridLinePaint.setStrokeCap(Paint.Cap.ROUND);
        this.gridLinePaint.setStrokeWidth(1);
        this.gridLinePaint.setColor(Color.parseColor("#A0A0A0"));
        this.gridLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        this.valuePath = new Path();
        this.valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.valuePaint.setStyle(Paint.Style.FILL);
        this.valuePaint.setColor(Color.parseColor("#1F1F1F"));
        switch (axisPosition) {
            case LEFT:
                this.valuePaint.setTextAlign(Paint.Align.RIGHT);
                break;
            case RIGHT:
                this.valuePaint.setTextAlign(Paint.Align.LEFT);
                break;
            case BOTTOM:
            case TOP:
                this.valuePaint.setTextAlign(Paint.Align.CENTER);
                break;
        }


        if (windowSize != null)
            this.windowSize.set(windowSize);
        setValueSize(ConverterUtil.convertDpToPixels(10, context));
//        int val = ConverterUtil.convertDpToPixels(32, context);
    }

    @Override
    public AxisPosition getPosition() {
        return axisPosition;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible.lazySet(visible);
    }

    @Override
    public boolean isVisible() {
        return this.visible.get();
    }

    @Override
    public void setGridVisible(boolean visible) {
        gridVisible.lazySet(visible);
    }

    @Override
    public boolean isGridVisible() {
        return gridVisible.get();
    }

    @Override
    public void setValueVisible(boolean visible) {
        this.valueVisible.lazySet(visible);
    }

    @Override
    public boolean isValueVisible() {
        return valueVisible.get();
    }

    @Override
    public void setAxisLineVisible(boolean visible) {
        axisLineVisible.lazySet(visible);
    }

    @Override
    public boolean isAxisLineVisible() {
        return axisLineVisible.get();
    }

    @Override
    public void setAxisLineWidth(float width) {
        Paint paint = getAxisLinePaint();
        if (paint != null)
            paint.setStrokeWidth(width);
    }

    @Override
    public float getAxisLineWidth() {
        Paint paint = getAxisLinePaint();
        return ((paint != null)) ? paint.getStrokeWidth() : 0;
    }

    @Override
    public void setGridLineWidth(float width) {
        Paint paint = getGridLinePaint();
        if (paint != null)
            paint.setStrokeWidth(width);
    }

    @Override
    public float getGridLineWidth() {
        Paint paint = getGridLinePaint();
        return ((paint != null)) ? paint.getStrokeWidth() : 0;
    }

    @Override
    public void setValueSize(float sizePx) {
        Paint paint = getValuePaint();
        if (paint != null)
            paint.setTextSize(sizePx);
    }

    @Override
    public float getValueSize() {
        Paint paint = getValuePaint();
        return (paint != null) ? paint.getTextSize() : 0;
    }

    @Override
    public void setValueColor(@ColorInt int color) {
        Paint paint = getValuePaint();
        if (paint != null)
            paint.setColor(color);
    }

    @Override
    public int getValueColor() {
        Paint paint = getValuePaint();
        return (paint != null) ? paint.getColor() : Color.TRANSPARENT;
    }

    @Override
    public void setValuePaint(Paint paint) {
        this.valuePaint = paint;
    }

    @Override
    public Paint getValuePaint() {
        return valuePaint;
    }

    @Override
    public void setValueCount(int valueCount) {
        if(valueCount < 2)
            valueCount = 2;
        if(valueCount > 10)
            valueCount = 10;
        this.maxStep = valueCount;
    }

    @Override
    public int getValueCount() {
        return (int) this.maxStep;
    }

    @Override
    public void setAxisLinePaint(Paint paint) {
        this.axisLinePaint = paint;
    }

    @Override
    public Paint getAxisLinePaint() {
        return axisLinePaint;
    }

    @Override
    public void setGridLinePaint(Paint paint) {
        this.gridLinePaint = paint;
    }

    @Override
    public Paint getGridLinePaint() {
        return gridLinePaint;
    }

    @Override
    public IFormatterValue getFormatter() {
        return formatter.get();
    }

    @Override
    public void setFormatter(IFormatterValue formatter) {
        this.formatter.lazySet(formatter);
    }

    @Override
    public void addDependsSeries(ISeries series) {
        if (series == null)
            return;
        synchronized (dependsSeriesLock) {
            if (dependsSeries.contains(series))
                return;
            dependsSeries.add(series);
        }
    }

    @Override
    public void removeDependsSeries(ISeries series) {
        if (series == null)
            return;
        synchronized (dependsSeriesLock) {
            dependsSeries.remove(series);
        }
    }

    @Override
    public boolean isDependedSeries(ISeries series) {
        if (series == null)
            return false;
        synchronized (dependsSeriesLock) {
            return dependsSeries.contains(series);
        }
    }

    @Override
    public RectF getWindowSize() {
        synchronized (windowSizeLock) {
            return new RectF(windowSize);
        }
    }

    @Override
    public void setWindowSize(float left, float top, float right, float bottom, boolean manual) {
        synchronized (windowSizeLock) {
            windowSize.left = left;
            windowSize.top = top;
            windowSize.right = right;
            windowSize.bottom = bottom;
            windowSizeManual.lazySet(manual);
        }
    }

    @Override
    public void setWindowSize(RectF size, boolean manual) {
        if(size == null)
            return;
        synchronized (windowSizeLock) {
            windowSize.set(size);
            windowSizeManual.lazySet(manual);
        }
    }

    @Override
    public boolean isWindowSizeManual() {
        return windowSizeManual.get();
    }

    private ISeriesHolder getHolder(Map<ISeries, ISeriesHolder> holders) {
        Iterator<Map.Entry<ISeries, ISeriesHolder>> itr = holders.entrySet().iterator();
        ISeriesHolder holder = null;
        while (itr.hasNext()) { // TODO продумать механизм выбора требуемого отрисовщика
            Map.Entry<ISeries, ISeriesHolder> it = itr.next();
            if (isDependedSeries(it.getKey())) {
                holder = it.getValue();
                break;
            }
        }
        return holder;
    }

    @Override
    public void calcRenderParam(Map<ISeries, ISeriesHolder> holders) {
        RectF wndSize = getWindowSize();
//        xStart = renderPos.left + offset.left;
//        xEnd = renderPos.right - offset.right;
//        yStart = renderPos.top + offset.top;
//        yEnd = renderPos.bottom - offset.bottom;
//        ISeriesHolder holder = getHolder(holders);
//        if(holder == null)
//            return;

        switch (axisPosition) {
            case LEFT:
                posXFrom = wndSize.left;
                posXTo = posXFrom;
                posYFrom = wndSize.top;
                posYTo = wndSize.bottom;
                posXAxisValue = wndSize.left;
                break;
            case TOP:
                posXFrom = wndSize.left;
                posXTo = wndSize.right;
                posYFrom = wndSize.top;
                posYTo = posYFrom;
                posYAxisValue = wndSize.top;
                break;
            case RIGHT:
                posXFrom = wndSize.right;
                posXTo = posXFrom;
                posYFrom = wndSize.top;
                posYTo = wndSize.bottom;
                posXAxisValue = wndSize.right;
                break;
            case BOTTOM:
                posXFrom = wndSize.left;
                posXTo = wndSize.right;
                posYFrom = wndSize.bottom;
                posYTo = posYFrom;
                posYAxisValue = wndSize.bottom;
                break;

        }
    }

    @Override
    public void render(Canvas canvas, Map<ISeries, ISeriesHolder> holders) {
        if (!isVisible())
            return;
        if (isAxisLineVisible())
            renderAxis(canvas);
        if (isGridVisible())
            renderGrid(canvas);
        if (isValueVisible())
            renderAxisValue(canvas, getHolder(holders));
    }

    protected void renderAxis(Canvas canvas) {
        Path pathA = getAxisPath();
        Paint paintA = getAxisLinePaint();
        if (pathA != null && paintA != null) {
            pathA.rewind();

            pathA.moveTo(posXFrom, posYFrom);
            pathA.lineTo(posXTo, posYTo);
            canvas.drawPath(pathA, paintA);
        }
    }

    protected void renderGrid(Canvas canvas) {
        Path pathG = getGridLinePath();
        Paint paintG = getGridLinePaint();
        RectF wndSize = getWindowSize();
        if (pathG != null && paintG != null) {
            pathG.rewind();
            float from, to, step;
            boolean horizontal;
            if ((axisPosition == AxisPosition.LEFT || axisPosition == AxisPosition.RIGHT)) {
                from = posYFrom;
                to = posYTo;
                horizontal = false;
            } else {
                from = posXFrom;
                to = posXTo;
                horizontal = true;
            }
            step = (to - from) / (maxStep - 1f);

            if (step > 0) {
                int cnt = (int)maxStep - 1;
                if (horizontal) {
                    while (--cnt > 0) {
                        to -= step;
                        pathG.moveTo(to, wndSize.top);
                        pathG.lineTo(to, wndSize.bottom);
                    }
                } else {
                    while (--cnt > 0) {
                        to -= step;
                        pathG.moveTo(wndSize.left, to);
                        pathG.lineTo(wndSize.right, to);
                    }
                }
            }
            canvas.drawPath(pathG, paintG);
        }
    }

    protected void renderAxisValue(Canvas canvas, ISeriesHolder holder) {
        if (holder == null)
            return;
        Path pathV = getValuePath();
        Paint paintV = getValuePaint();

        IFormatterValue formatterValue = getFormatter();
        if (paintV != null
                && pathV != null
                && formatterValue != null) {
            pathV.rewind();
            float from, to, step;
            boolean horizontal;
            if ((axisPosition == AxisPosition.LEFT || axisPosition == AxisPosition.RIGHT)) {
                from = posYFrom;
                to = posYTo;
                horizontal = false;
            } else {
                from = posXFrom;
                to = posXTo;
                horizontal = true;
            }
            step = (to - from) / (maxStep - 1f);
            if (step <= 0)
                return;
            // TODO Оптимизировать отрисовка текста, например,
            // TODO в процессе рассчёта получать paintV.getTextPath(); для каждого необходимого значения
            String txtVal;
            float maxValueBounds;
            int cnt = (int) maxStep;
            if (horizontal) {
                float val = Math.max(Math.abs(holder.getMaxY()), Math.abs(holder.getMinY()));
                txtVal = formatterValue.formatText(val, this);
                if (txtVal == null)
                    return;
                paintV.getTextBounds(txtVal, 0, txtVal.length(), boundsTextMeasuring);
//                float offsetXc = boundsTextMeasuring.width() >> 1;
                float offsetY = posYAxisValue + ((axisPosition == AxisPosition.BOTTOM) ?
                        boundsTextMeasuring.height() + offsetValue
                        : -boundsTextMeasuring.height() + offsetValue);

                maxValueBounds = boundsTextMeasuring.height();
                while (--cnt >= 0) {
                    float valX = holder.toPointX(to);
                    txtVal = formatterValue.formatText(valX, this);
                    if (txtVal == null || txtVal.length() <= 0)
                        continue;
//                    canvas.drawTextOnPath(txtVal, pathV, from - offsetXc, posYAxisValue, paintV);
                    canvas.drawText(txtVal,
                            to,
                            offsetY,
                            paintV);
                    to -= step;
                }
            } else {
                float val = Math.max(Math.abs(holder.getMaxY()), Math.abs(holder.getMinY()));
                txtVal = formatterValue.formatText(val, this);
                if (txtVal == null)
                    return;
                paintV.getTextBounds(txtVal, 0, txtVal.length(), boundsTextMeasuring);
                float offsetYc = boundsTextMeasuring.centerY();
                float offsetX = posXAxisValue + ((axisPosition == AxisPosition.LEFT) ? -offsetValue : offsetValue);

                maxValueBounds = boundsTextMeasuring.width();
                while (--cnt >= 0) {
                    txtVal = formatterValue.formatText(holder.toPointY(to), this);
                    if (txtVal == null || txtVal.length() <= 0)
                        continue;
//                    canvas.drawTextOnPath(txtVal, pathV, posXAxisValue, from - offsetYc, paintV);
                    canvas.drawText(txtVal,
                            offsetX,
                            to - offsetYc,
                            paintV);
                    to -= step;
                }
            }
            updateMaxValueBounds(maxValueBounds);
            canvas.drawPath(pathV, paintV);
        }
    }

    protected void updateMaxValueBounds(float maxValueBounds) {
        if (maxValueBounds != prevMaxValueBounds) {
            prevMaxValueBounds = maxValueBounds;
            MaxValueBoundsChange listener = wMaxValueBoundsChangeListener != null ?
                    wMaxValueBoundsChangeListener.get() : null;
            if (!isWindowSizeManual() && listener != null) {
                listener.change(this, maxValueBounds);
            }
        }
    }

    @Override
    public void setMaxValueBoundsChange(MaxValueBoundsChange listener) {
        wMaxValueBoundsChangeListener = new WeakReference<>(listener);
    }

    protected Path getAxisPath() {
        return axisPath;
    }

    protected Path getGridLinePath() {
        return gridLinePath;
    }

    protected Path getValuePath() {
        return valuePath;
    }
}
