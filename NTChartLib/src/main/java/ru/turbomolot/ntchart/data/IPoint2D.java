package ru.turbomolot.ntchart.data;

/**
 * Created by TurboMolot on 05.12.17.
 */

public interface IPoint2D extends IPoint {
    float getY();
    void setY(float y);
    <P extends IPoint2D> P copy();
}
