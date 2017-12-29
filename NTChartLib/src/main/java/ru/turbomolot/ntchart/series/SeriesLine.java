package ru.turbomolot.ntchart.series;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.Point2D;
import ru.turbomolot.ntchart.utils.ConverterUtil;

/**
 * Created by TurboMolot on 01.12.17.
 */

public class SeriesLine implements ISeries<Point2D> {
    private WeakReference<NTChart> wChart;
    private long npSeries = 0;
    private final Path linePath = new Path();
    private Paint linePaint;
    private RectF positionInChart;

    private final Paint paintDebugText = new Paint();
    private final Rect paintDebugSize = new Rect();


    public SeriesLine() {
        nInitSeries();
    }

    private void checkNative() {
        if (npSeries == 0)
            throw new NullPointerException("Native object released");
    }

    private void checkAndInitLinePaint() {
        if (linePaint != null)
            return;
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#00DDBB"));
        linePaint.setStyle(Paint.Style.STROKE);
        Context context = getContext();
        if (context != null) {
            linePaint.setStrokeWidth(ConverterUtil.convertDpToPixels(1, context));

            paintDebugText.setAntiAlias(true);
            paintDebugText.setColor(Color.parseColor("#FF0000"));
            paintDebugText.setTextSize(ConverterUtil.convertDpToPixels(12, context));
        }
    }

    private void drawDebugText(Canvas canvas, String txt) {
        paintDebugText.getTextBounds(txt, 0, txt.length(), paintDebugSize);
        int h = paintDebugSize.height() * 4;
        canvas.drawText(
                txt,
                0,
                h,
                paintDebugText);
    }

    private Context getContext() {
        NTChart chart = getChart();
        return (chart == null) ? null : chart.getContext();
    }

    @Override
    public void addPoint(Point2D point) {
        if (point != null) {
            checkNative();
            nAddPoint(npSeries, point.getX(), point.getY());
        }
        notifyChanged();
    }

    @Override
    public void addPoints(List<Point2D> points) {
        if (points != null) {
            checkNative();
            nAddPointList(npSeries, points);
        }
        notifyChanged();
    }

    @Override
    public void removePoint(Point2D point) {
        notifyChanged();
    }


    @Override
    public void clearPoints() {
        checkNative();
        nClearPoints(npSeries);
        notifyChanged();
    }

    @Override
    public int getPointCount() {
        return 0;
    }

    @Override
    public Point2D[] getPointArr() {
        return null;
    }

    @Override
    public List<Point2D> getPoints() {
        checkNative();
        return nGetPointList(npSeries);
    }

    public List<Point2D> getPointsRender() {
        checkNative();
        return nGetPointListRender(npSeries);
    }

    public float[] getPointsArrayRender() {
        checkNative();
        return nGetPointsRender(npSeries);
    }

    @Override
    public void render(Canvas canvas) {
        Paint lPaint = getLinePaint();
        if (lPaint == null)
            return;
        float[] pts = getPointsArrayRender();
        if (pts == null || pts.length == 0)
            return;
        canvas.drawLines(pts, lPaint);
        drawDebugText(canvas, "pts: " + String.valueOf((pts.length + 4) >>> 2));

//        Paint lPaint = getLinePaint();
//        if (lPaint == null)
//            return;
//        List<Point2D> ptsRender = getPointsRender();
//        if (ptsRender == null || ptsRender.isEmpty())
//            return;
//        linePath.rewind();
//        Iterator<Point2D> itr = ptsRender.iterator();
//        Point2D itm = itr.next();
//        linePath.moveTo(itm.getX(), itm.getY());
//        while (itr.hasNext()) {
//            itm = itr.next();
//            linePath.lineTo(itm.getX(), itm.getY());
//        }
//        canvas.drawPath(linePath, lPaint);
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    @Override
    public void calculateRender() {
        checkNative();
        nCalculateRender(npSeries);
    }

    @Override
    public void setRenderPosition(RectF positionInChart) {
        this.positionInChart = positionInChart;
        checkNative();
        if (positionInChart != null)
            nSetRenderSize(npSeries, positionInChart.width(), positionInChart.height());
        else
            nSetRenderSize(npSeries, 0f, 0f);
    }


    protected final NTChart getChart() {
        return wChart != null ? wChart.get() : null;
    }

    @Override
    public void parentChanged(NTChart chart) {
        wChart = chart == null ? null : new WeakReference<>(chart);
        if (chart != null)
            checkAndInitLinePaint();
    }

    @Override
    public void notifyChanged() {
        NTChart chart = getChart();
        if (chart != null)
            chart.notifyChanged();
    }

    @Override
    public void release() {
        if (npSeries == 0)
            return;
        nDeinitSeries(npSeries);
        npSeries = 0;
    }

    @Override
    public void setOffsetX(float offsetX) {

    }

    @Override
    public float getOffsetX() {
        return 0;
    }

    @Override
    public void setOffsetY(float offsetY) {

    }

    @Override
    public float getOffsetY() {
        return 0;
    }

    @Override
    public void setScaleX(float scaleX) {

    }

    @Override
    public float getScaleX() {
        return 0;
    }

    @Override
    public void setScaleY(float scaleY) {

    }

    @Override
    public float getScaleY() {
        return 0;
    }

    @Override
    public void setMinX(Float minX) {

    }

    @Override
    public Float getMinX() {
        return null;
    }

    @Override
    public void setMinY(Float minY) {

    }

    @Override
    public Float getMinY() {
        return null;
    }

    @Override
    public void setMaxX(Float maxX) {

    }

    @Override
    public Float getMaxX() {
        return null;
    }

    @Override
    public void setMaxY(Float maxY) {

    }

    @Override
    public Float getMaxY() {
        return null;
    }

    @Override
    public RectF getRenderPosition() {
        RectF pos = positionInChart;
        return pos != null ? new RectF(pos) : null;
    }

    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private native void nInitSeries();

    private native void nDeinitSeries(long npSeries);

    private native void nAddPoint(long npSeries, float x, float y);

    private native void nAddPointList(long npSeries, List<Point2D> pts);

    private native List<Point2D> nGetPointList(long npSeries);

    private native void nClearPoints(long npSeries);

    private native void nCalculateRender(long npSeries);

    private native List<Point2D> nGetPointListRender(long npSeries);

    private native void nSetRenderSize(long npSeries, float width, float height);

    private native float[] nGetPointsRender(long npSeries);

    static {
        System.loadLibrary("ntchart-lib");
    }
}
