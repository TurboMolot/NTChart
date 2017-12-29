package ru.turbomolot.ntchart.series;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Collection;
import java.util.List;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.IPoint;

/**
 * Created by TurboMolot on 23.11.17.
 */

public interface ISeries<P extends IPoint> {
    void addPoint(P point);
    void addPoints(List<P> points);
    int getPointCount();
//    void addPoints(P[] points);
    void removePoint(P point);
    void clearPoints();
    P[] getPointArr();
    List<P> getPoints();

    void setRenderPosition(RectF positionInChart);
    RectF getRenderPosition();
    void calculateRender();
//    void calcRender();
    void render(Canvas canvas);

    void parentChanged(NTChart chart);
    void notifyChanged();

    /**
     * Free all native object.
     * Don't use object after invoke
     */
    void release();

    void setOffsetX(float offsetX);
    float getOffsetX();

    void setOffsetY(float offsetY);
    float getOffsetY();

    void setScaleX(float scaleX);
    float getScaleX();

    void setScaleY(float scaleY);
    float getScaleY();

    void setMinX(Float minX);
    Float getMinX();
    void setMinY(Float minY);
    Float getMinY();
    void setMaxX(Float maxX);
    Float getMaxX();
    void setMaxY(Float maxY);
    Float getMaxY();
}
