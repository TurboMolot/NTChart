package ru.turbomolot.ntchart.axis;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import java.util.List;
import java.util.Map;

import ru.turbomolot.ntchart.formatter.IFormatterValue;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.series.ISeries;

/**
 * Created by TurboMolot on 04.10.17.
 */

public interface IAxis {
    AxisPosition getPosition();
    RectF getWindowSize();
    void setWindowSize(float left, float top, float right, float bottom, boolean manual);
    void setWindowSize(RectF size, boolean manual);
    boolean isWindowSizeManual();
    void setVisible(boolean visible);
    boolean isVisible();
    void setGridVisible(boolean visible);
    boolean isGridVisible();
    void setValueVisible(boolean visible);
    boolean isValueVisible();
    void setAxisLineVisible(boolean visible);
    boolean isAxisLineVisible();
    void setAxisLineWidth(float width);
    float getAxisLineWidth();
    void setGridLineWidth(float width);
    float getGridLineWidth();
    void setValueSize(float sizePx);
    float getValueSize();
    void setValueColor(@ColorInt int color);
    int getValueColor();
    void setValuePaint(Paint paint);
    Paint getValuePaint();
    void setValueCount(int valueCount);
    int getValueCount();
    void setAxisLinePaint(Paint paint);
    Paint getAxisLinePaint();
    void setGridLinePaint(Paint paint);
    Paint getGridLinePaint();
    IFormatterValue getFormatter();
    void setFormatter(IFormatterValue formatter);

    void addDependsSeries(ISeries series);
    void removeDependsSeries(ISeries series);
    boolean isDependedSeries(ISeries series);

    void calcRenderParam(Map<ISeries, ISeriesHolder> holders);
    void render(Canvas canvas, Map<ISeries, ISeriesHolder> holders);

    /**
     * For internal use
     * @param listener
     */
    void setMaxValueBoundsChange(MaxValueBoundsChange listener);
    interface MaxValueBoundsChange {
        void change(IAxis axis, float maxValueBounds);
    }
}
