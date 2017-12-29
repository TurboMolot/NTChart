package ru.turbomolot.ntchart.data;

/**
 * Created by TurboMolot on 01.12.17.
 */

public class Point2D implements IPoint2D {
    private float x;
    private float y;

    public Point2D() {
    }


    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends IPoint2D> P copy() {
        return (P)new Point2D(x, y);
    }

    //    @Override
//    public Point2D copy() {
//        return new Point2D(x, y);
//    }
}
