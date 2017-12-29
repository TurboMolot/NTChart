package ru.turbomolot.ntchart.axis;

import java.util.Collection;

import ru.turbomolot.ntchart.data.IPoint;

/**
 * Created by TurboMolot on 01.12.17.
 */

public interface IAxis<P extends IPoint> {
    void addPoint(P point);
    void addPoints(Collection<P> points);
    void addPoints(P[] points);
    void removePoint(P point);
    void clearPoints();
    P[] getPoints();
}

