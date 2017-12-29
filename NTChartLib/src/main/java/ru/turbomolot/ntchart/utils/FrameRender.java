package ru.turbomolot.ntchart.utils;

import java.util.List;

import ru.turbomolot.ntchart.data.IPoint2D;

/**
 * Created by XE on 05.12.2017.
 */
public class FrameRender<Point extends IPoint2D> {
    private List<Point> points;
    private float[] pointsPlain;
    private Point pointXMin;
    private Point pointXMax;
    private Point pointYMin;
    private Point pointYMax;
    private float scaleX;
    private float scaleY;
    private MathHelper.MinMaxPoint2D<Point> minMaxPoint;
    private int idxPointFrom;
    private int idxPointTo;

    private Float maxX;
    private Float minX;

    private Float maxY;
    private Float minY;


    public void reset() {
        points = null;
        pointXMin = null;
        pointXMax = null;
        pointYMin = null;
        pointYMax = null;
        scaleX = 0;
        scaleY = 0;
        idxPointFrom = 0;
        idxPointTo = 0;
        minMaxPoint = null;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public Point getPointXMin() {
        return pointXMin;
    }

    public void setPointXMin(Point pointXMin) {
        this.pointXMin = pointXMin;
    }

    public Point getPointXMax() {
        return pointXMax;
    }

    public void setPointXMax(Point pointXMax) {
        this.pointXMax = pointXMax;
    }

    public Point getPointYMin() {
        return pointYMin;
    }

    public void setPointYMin(Point pointYMin) {
        this.pointYMin = pointYMin;
    }

    public Point getPointYMax() {
        return pointYMax;
    }

    public void setPointYMax(Point pointYMax) {
        this.pointYMax = pointYMax;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float[] getPointsPlain() {
        return pointsPlain;
    }

    public void setPointsPlain(float[] pointsPlain) {
        this.pointsPlain = pointsPlain;
    }

    public MathHelper.MinMaxPoint2D<Point> getMinMaxPoint() {
        return minMaxPoint;
    }

    public void setMinMaxPoint(MathHelper.MinMaxPoint2D<Point> minMaxPoint) {
        this.minMaxPoint = minMaxPoint;
    }

    public int getIdxPointFrom() {
        return idxPointFrom;
    }

    public void setIdxPointFrom(int idxPointFrom) {
        this.idxPointFrom = idxPointFrom;
    }

    public int getIdxPointTo() {
        return idxPointTo;
    }

    public void setIdxPointTo(int idxPointTo) {
        this.idxPointTo = idxPointTo < 0 ? 0 : idxPointTo;
    }

    public Float getMaxY() {
        return maxY;
    }

    public void setMaxY(Float maxY) {
        this.maxY = maxY;
    }

    public Float getMinY() {
        return minY;
    }

    public void setMinY(Float minY) {
        this.minY = minY;
    }

    public Float getMaxX() {
        return maxX;
    }

    public void setMaxX(Float maxX) {
        this.maxX = maxX;
    }

    public Float getMinX() {
        return minX;
    }

    public void setMinX(Float minX) {
        this.minX = minX;
    }

    public float getMinXRender() {
        Point xMin = getPointXMin();
        Float xMinF = getMinX();
        return (xMinF != null) ? xMinF : (xMin != null) ? xMin.getX() : 0;
    }

    public float getMaxXRender() {
        Point xMax = getPointXMax();
        Float xMaxF = getMaxX();
        return (xMaxF != null) ? xMaxF : (xMax != null) ? xMax.getX() : 0;
    }

    public float getMinYRender() {
        Point yMin = getPointYMin();
        Float yMinF = getMinY();
        return (yMinF != null) ? yMinF : (yMin != null) ? yMin.getY() : 0;
    }

    public float getMaxYRender() {
        Point yMax = getPointYMax();
        Float yMaxF = getMaxY();
        return (yMaxF != null) ? yMaxF : (yMax != null) ? yMax.getY() : 0;
    }

    public float getOffsetX() {
        float val = getMinXRender();
        return val < 0 ? Math.abs(val) : -val;
    }

    public float getOffsetY() {
        float val = getMinYRender();
        return val < 0 ? Math.abs(val) : 0; //-frameRender.renderYMin.getY()
    }
}
