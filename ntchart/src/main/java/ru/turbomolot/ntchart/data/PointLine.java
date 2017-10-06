package ru.turbomolot.ntchart.data;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class PointLine implements IPointLine {
    private float x;
    private float y;

    public PointLine() {
    }

    public PointLine(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }
}
