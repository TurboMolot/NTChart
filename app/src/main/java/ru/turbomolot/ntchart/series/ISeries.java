package ru.turbomolot.ntchart.series;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import java.util.List;
import java.util.Map;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.IPoint;
import ru.turbomolot.ntchart.render.ISeriesHolder;

/**
 * Created by TurboMolot on 04.10.17.
 */

public interface ISeries<P extends IPoint> {
    void calcParamRender(ISeriesHolder holder);
    void render(Canvas canvas, ISeriesHolder holder, Map<ISeries, ISeriesHolder> holders);

    void addPoint(P point);
    void addPoints(List<? extends P> points);
    void clearPoints();
    List<P> getPoints();
    void notifyChanged();

    void setWindowSize(float left, float top, float right, float bottom, boolean manual);
    RectF getWindowSize();
    boolean isWindowSizeManual();

    /**
     * The size of the window data, which will store the source display data
     * last index x (lasIdxX) (lasIdxX - maxDistanceStore) will be store
     * @param maxDistanceStore window store. If null all data will be saved
     */
    void setMaxDistanceStore(Float maxDistanceStore);

    /**
     * @see {@link ISeries#setMaxDistanceStore(Float)}
     */
    Float getMaxDistanceStore();

    /**
     * Fill the space under the graph
     * @param fill if true area will be fill
     */
    void setFill(boolean fill);
    /**
     * @see {@link ISeries#setFill(boolean)}
     */
    boolean isFill();

    void setFillDrawable(Drawable fillDrawable);
    Drawable getFillDrawable();
    void setFillColor(@ColorInt int fillColor);
    int getFillColor();
    void setFillPaint(Paint fillPaint);
    Paint getFillPaint();
    void setColor(@ColorInt int color);
    int getColor();

    ISeriesHolder createHolder();
    void parentChanged(NTChart chart);

    Matrix getMatrix();
    void setMatrix(Matrix matrix);

    Float getMaxDistanceX();
    void setMaxDistanceX(Float maxDistanceX);
    Float getMaxDistanceY();
    void setMaxDistanceY(Float maxDistanceY);

    void setTitle(String title);
    String getTitle();
}
