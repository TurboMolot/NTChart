package ru.turbomolot.ntchart.render;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.List;

import ru.turbomolot.ntchart.data.IPoint;

/**
 * Created by TurboMolot on 04.10.17.
 */

public interface ISeriesHolder<P extends IPoint> {
    float getMaxX();
    float getMaxY();
    float getMinX();
    float getMinY();

    void setMinX(Float minX);
    void setMinY(Float minY);
    void setMaxX(Float maxX);
    void setMaxY(Float maxY);


    Matrix getMatrix();
    void setMatrix(Matrix matrix);

    void calcRender(List<? extends P> pts);
    List<? extends P> getRender();

    void setWindowSize(float left, float top, float right, float bottom);
    RectF getWindowSize();

    float toRenderX(float x);
    float toRenderY(float y);

    float toPointX(float x);
    float toPointY(float y);

    boolean isRenderFromAxisRight();
    void setRenderFromAxisRight(boolean renderFromAxisRight);
    boolean isAutoScale();
    void setAutoScale(boolean autoScale);
    boolean isAutoMoveLastX();
    void setAutoMoveLastX(boolean autoMoveLastX);

    float getMaxDistanceX();
    void setMaxDistanceX(float maxDistanceX);
    float getMaxDistanceY();
    void setMaxDistanceY(float maxDistanceY);

    List<Path> getRenderPaths();

    void setReducePointsEnabled(boolean reducePointsEnabled);
    boolean isReducePointsEnabled();
}
