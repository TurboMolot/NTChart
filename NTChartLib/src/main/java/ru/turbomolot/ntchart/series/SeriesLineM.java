package ru.turbomolot.ntchart.series;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ru.turbomolot.ntchart.charts.NTChart;
import ru.turbomolot.ntchart.data.Point2D;
import ru.turbomolot.ntchart.utils.ConverterUtil;
import ru.turbomolot.ntchart.utils.FrameRender;
import ru.turbomolot.ntchart.utils.MathHelper;

/**
 * Created by TurboMolot on 27.12.17.
 */

public class SeriesLineM implements ISeries<Point2D> {
    private WeakReference<NTChart> wChart;
    private final AtomicBoolean dataChanged = new AtomicBoolean();
    private Paint linePaint;
    private final Paint paintDebugText = new Paint();
    private final Rect paintDebugSize = new Rect();

    private final RectF positionInChart = new RectF();
    private List<Point2D> data = new CopyOnWriteArrayList<>();
    private final AtomicReference<FrameRender<Point2D>> frameRender = new AtomicReference<>(new FrameRender<Point2D>());
    private final AtomicReference<FrameRender<Point2D>> frameRenderBack = new AtomicReference<>(new FrameRender<Point2D>());

    private final AtomicReference<Float> maxDistanceStore = new AtomicReference<>();

    private final AtomicReference<Float> rangeX = new AtomicReference<>(0f);
    private final AtomicReference<Float> offsetX = new AtomicReference<>(0f);
    private final Object frameBackLock = new Object();

    private final AtomicReference<Float> userOffsetX = new AtomicReference<>(0f);
    private final AtomicReference<Float> userOffsetY = new AtomicReference<>(0f);
    private final AtomicReference<Float> userScaleX = new AtomicReference<>(0f);
    private final AtomicReference<Float> userScaleY = new AtomicReference<>(0f);
    private final AtomicReference<Float> minX = new AtomicReference<>(null);
    private final AtomicReference<Float> maxX = new AtomicReference<>(null);
    private final AtomicReference<Float> minY = new AtomicReference<>(null);
    private final AtomicReference<Float> maxY = new AtomicReference<>(null);

    public SeriesLineM() {
        rangeX.lazySet(4f);
    }

    private void notifyUpdate() {
        dataChanged.lazySet(true);
        notifyChanged();
    }

    @Override
    public void addPoint(Point2D point) {
        if (point != null) {
            if (data == null)
                data = new CopyOnWriteArrayList<>();
            data.add(point);
            trimMaxDistanceStore();
            notifyUpdate();
        }
    }

    @Override
    public void addPoints(List<Point2D> points) {
        if (points != null) {
            if (data == null)
                data = new CopyOnWriteArrayList<>();
            data.addAll(points);
            trimMaxDistanceStore();
            notifyUpdate();
        }
    }

    @Override
    public void removePoint(Point2D point) {
        if (data != null && data.remove(point)) {
            trimMaxDistanceStore();
            notifyUpdate();
        }
    }

    @Override
    public void clearPoints() {
        if (data != null && !data.isEmpty()) {
            data.clear();
            trimMaxDistanceStore();
            notifyUpdate();
        }
    }

    @Override
    public int getPointCount() {
        return (data == null) ? 0 : data.size();
    }

    @Override
    public Point2D[] getPointArr() {
        return null;
    }

    @Override
    public List<Point2D> getPoints() {
        return (data != null) ? new ArrayList<>(data) : null;
    }

    @Override
    public void setRenderPosition(RectF positionInChart) {
        if (positionInChart == null) {
            this.positionInChart.setEmpty();
        } else {
            this.positionInChart.set(positionInChart);
        }
    }

    @Override
    public void calculateRender() {
        RectF pos = positionInChart;
        if (!dataChanged.get() || pos == null || pos.isEmpty()) {
            return;
        }
        dataChanged.lazySet(false);
        synchronized (frameBackLock) {
            FrameRender<Point2D> frame = frameRenderBack.get();
            if (frame == null) {
                return;
            }
            List<Point2D> pts = getPoints();
            if (pts == null || pts.isEmpty()) {
                frame.reset();
                exchangeFrame();
                return;
            }

            Float maxY = getMaxY();
            Float minY = getMinY();

            float rX = rangeX.get();
            float offX = offsetX.get();

            int idxFrom = 0;
            int idxTo = pts.size() - 1;
            float rWidth = pos.width();
            float rHeight = pos.height();

            if (rX > 0) {
                idxFrom = MathHelper.getIndexXBefore(pts, idxFrom, idxTo, offX);
                if (idxFrom < 0)
                    idxFrom = 0;
                else if (idxFrom >= pts.size())
                    idxFrom = pts.size() - 1;
                idxTo = MathHelper.getIndexXBefore(pts, idxFrom, idxTo, pts.get(idxFrom).getX() + rX);
            }
            if (idxFrom > 0 && idxFrom >= pts.size()) {
                idxFrom = pts.size() - 1;
            } else if (idxFrom < 0) {
                idxFrom = 0;
            }
            if (idxTo > 0 && idxTo >= pts.size()) {
                idxTo = pts.size() - 1;
            } else if (idxTo < 0)
                idxTo = 0;
            if (rX > 0) {
                offX = (Math.abs(pts.get(pts.size() - 1).getX() - pts.get(idxFrom).getX()) - rX) > 0.0f ?
                        pts.get(pts.size() - 1).getX() - rX : 0.0f;
            }

            MathHelper.MinMaxPoint2D<Point2D> minMaxPoint2D = MathHelper.getMinMaxCache(pts,
                    idxFrom, frame.getIdxPointTo(), idxTo, frame.getMinMaxPoint());
            frame.setIdxPointFrom(idxFrom);
            frame.setIdxPointTo(idxTo);
            if (minMaxPoint2D == null)
                return;



            frame.setPointXMin(minMaxPoint2D.getMinX());
            frame.setPointXMax(minMaxPoint2D.getMaxX());
            frame.setPointYMin(minMaxPoint2D.getMinY());
            frame.setPointYMax(minMaxPoint2D.getMaxY());
            frame.setMinMaxPoint(minMaxPoint2D);

            frame.setMinY(getMinY());
            frame.setMaxY(getMaxY());

            float valRange = Math.abs(pts.get(idxTo).getX() - pts.get(idxFrom).getX());
            frame.setScaleX(rWidth / ((rX > 0) ? rX : valRange));


            float maxForScY = (frame.getMaxY() != null) ? frame.getMaxY() : minMaxPoint2D.getMaxY().getY();
            float minForScY = (frame.getMinY() != null) ? frame.getMinY() : minMaxPoint2D.getMinY().getY();
            valRange = Math.abs(maxForScY - minForScY);
            frame.setScaleY(rHeight / ((valRange != 0f) ? valRange : 1f));

            frame.setPoints(pts.subList(idxFrom, idxTo));
            MathHelper.reducePointAbsScale(frame, rWidth, rHeight);

//            float t = (float) Math.sqrt(Math.max(Math.abs(minMaxPoint2D.getMaxX().getX() - minMaxPoint2D.getMinX().getX()),
//                    Math.abs(minMaxPoint2D.getMaxY().getY() - minMaxPoint2D.getMinY().getY())));
//            t = (1f/(t == 0 ? 1f : t));
//            frame.setPoints(MathHelper.reducePtsNative(pts.subList(idxFrom, idxTo), t));
//            MathHelper.pointAbsScale(frame, rWidth);

//            MathHelper.pointAbsScale(frame, rWidth);
//            pathLine.rewind();
//            pts = frame.getPoints();
//            if (pts != null && !pts.isEmpty()) {
//                float xOffset = frame.getPointXMin().getX() < 0 ? Math.abs(frame.getPointXMin().getX())
//                        : -frame.getPointXMin().getX();
//                float yOffset = frame.getPointYMin().getY() < 0 ? Math.abs(frame.getPointYMin().getY())
//                        : 0;
//
//                idxFrom = 0;
//                idxTo = pts.size() - 1;
//
//                Point2D p = MathHelper.absScalePoint(frame, pts.get(idxFrom++), xOffset, yOffset);
//                pathLine.moveTo(p.getX(), p.getY());
//                while (idxFrom <= idxTo) {
//                    p = MathHelper.absScalePoint(frame, pts.get(idxFrom++), xOffset, yOffset);
//                    pathLine.lineTo(p.getX(), p.getY());
//                }
//            }

//            MathHelper.
//            prepareRender(frame, rWidth);

//            float xOffset = frame.getPointXMin().getX() < 0 ? Math.abs(frame.getPointXMin().getX())
//                    : -frame.getPointXMin().getX();
//            float yOffset = frame.getPointYMin().getY() < 0 ? Math.abs(frame.getPointYMin().getY())
//                    : 0; //-frameRender.renderYMin.getY()
//            pathLine.rewind();
//            Point2D p = MathHelper.absScalePoint(frame, pts.get(idxFrom++), xOffset, yOffset);
//            pathLine.moveTo(p.getX(), p.getY());
//            while (idxFrom <= idxTo) {
//                p = MathHelper.absScalePoint(frame, pts.get(idxFrom++), xOffset, yOffset);
//                pathLine.lineTo(p.getX(), p.getY());
//            }
//            pathLine.rewind();
//            pts = frame.getPoints();
//            if (pts != null && !pts.isEmpty()) {
//                idxFrom = 0;
//                idxTo = pts.size() - 1;
//
//                Point2D p = pts.get(idxFrom);
//                pathLine.moveTo(p.getX(), p.getY());
//                while (idxFrom <= idxTo) {
//                    p = pts.get(idxFrom++);
//                    pathLine.lineTo(p.getX(), p.getY());
//                }
//            }
            offsetX.lazySet(offX);
            exchangeFrame();
        }
    }

    @Override
    public void render(Canvas canvas) {
        Paint lPaint = getLinePaint();
        FrameRender<Point2D> frame = frameRender.get();
        RectF pos = getRenderPosition();
        if (lPaint == null || frame == null || pos == null || pos.isEmpty())
            return;
        float[] pts = frame.getPointsPlain();
        if (pts == null || pts.length == 0)
            return;
        int svCnt = canvas.save();
        float offY = getOffsetY();
        if (offY != 0) {
            if (Math.abs(offY) > pos.height())
                offY = (offY < 0) ? -pos.height() : pos.height();
            canvas.translate(0f, offY);
        }
        canvas.drawLines(pts, lPaint);
        canvas.restoreToCount(svCnt);
        drawDebugText(canvas, "pts: " + String.valueOf((pts.length + 4) >>> 2));
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

    }

    @Override
    public void setOffsetX(float offsetX) {
        if (offsetX != getOffsetX()) {
            List<Point2D> pts = getPoints();
            if (pts == null || pts.isEmpty()) {
                offsetX = 0;
            } else {
                FrameRender<Point2D> fr = frameRender.get();
                if (fr != null) {
                    offsetX = offsetX / ((fr.getScaleX() != 0f) ? fr.getScaleX() : 1f);
                    if (offsetX < pts.get(0).getX())
                        offsetX = pts.get(0).getX();
                    else if (offsetX > pts.get(pts.size() - 1).getX())
                        offsetX = pts.get(pts.size() - 1).getX();
                } else {
                    offsetX = 0;
                }
            }
            userOffsetX.lazySet(offsetX);
            notifyUpdate();
        }
    }

    @Override
    public float getOffsetX() {
        return userOffsetX.get();
    }

    @Override
    public void setOffsetY(float offsetY) {
        if (offsetY != getOffsetY()) {
            userOffsetY.lazySet(offsetY);
            notifyUpdate();
        }
    }

    @Override
    public float getOffsetY() {
        return userOffsetY.get();
    }

    @Override
    public void setScaleX(float scaleX) {
        if(scaleX != getScaleX()) {
            userScaleX.lazySet(scaleX);
            notifyUpdate();
        }
    }

    @Override
    public float getScaleX() {
        return userScaleX.get();
    }

    @Override
    public void setScaleY(float scaleY) {
        if(scaleY != getScaleY()) {
            userScaleY.lazySet(scaleY);
            notifyUpdate();
        }
    }

    @Override
    public float getScaleY() {
        return userScaleY.get();
    }

    @Override
    public void setMinX(Float minX) {
        Float old = getMinX();
        if((old == null && minX != null)
                || (old != null && minX == null)
                || (old != null && !minX.equals(old))) {
            this.minX.lazySet(minX);
            notifyUpdate();
        }
    }

    @Override
    public Float getMinX() {
        return minX.get();
    }

    @Override
    public void setMinY(Float minY) {
        Float old = getMinY();
        if((old == null && minY != null)
                || (old != null && minY == null)
                || (old != null && !minY.equals(old))) {
            this.minY.lazySet(minY);
            notifyUpdate();
        }
    }

    @Override
    public Float getMinY() {
        return minY.get();
    }

    @Override
    public void setMaxX(Float maxX) {
        Float old = getMaxX();
        if((old == null && maxX != null)
                || (old != null && maxX == null)
                || (old != null && !maxX.equals(old))) {
            this.maxX.lazySet(maxX);
            notifyUpdate();
        }
    }

    @Override
    public Float getMaxX() {
        return this.maxX.get();
    }

    @Override
    public void setMaxY(Float maxY) {
        Float old = getMaxY();
        if((old == null && maxY != null)
                || (old != null && maxY == null)
                || (old != null && !maxY.equals(old))) {
            this.maxY.lazySet(maxY);
            notifyUpdate();
        }
    }

    @Override
    public Float getMaxY() {
        return this.maxY.get();
    }

    @Override
    public RectF getRenderPosition() {
        RectF pos = positionInChart;
        return pos != null ? new RectF(pos) : null;
    }

    public Float getMaxDistanceX() {
        return rangeX.get();
    }

    public void setMaxDistanceX(Float maxDistanceX) {
        Float old = getMaxDistanceX();
        if((old == null && maxDistanceX != null)
                || (old != null && maxDistanceX == null)
                || (old != null && !maxDistanceX.equals(old))) {
            this.rangeX.lazySet(maxDistanceX);
            notifyUpdate();
        }
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public void setMaxDistanceStore(Float maxDistanceStore) {
        this.maxDistanceStore.lazySet(maxDistanceStore);
    }

    public Float getMaxDistanceStore() {
        return maxDistanceStore.get();
    }

//    private void prepareRender(final FrameRender frame, float width) {
//        if (frame.getPoints().isEmpty())
//            return;
//        MathHelper.reducePointAbsScale(frame, width);
////        float xOffset = frame.getPointXMin().getX() < 0 ? Math.abs(frame.getPointXMin().getX())
////                : -frame.getPointXMin().getX();
////        float yOffset = frame.renderYMin.getY() < 0 ? Math.abs(frame.renderYMin.getY())
////                : 0; //-frameRender.renderYMin.getY()
//    }

    private void trimMaxDistanceStore() {
        Float maxDistanceCurrent = getMaxDistanceStore();
        if (maxDistanceCurrent != null && maxDistanceCurrent > 0) {
            List<Point2D> pts = data;
            int sz = pts != null ? pts.size() : 0;
            if (pts != null && sz > 1) {
                float lastVal = pts.get(sz - 1).getX();
                if (Math.abs(lastVal - pts.get(0).getX()) > maxDistanceCurrent) {
                    int idxEnd = MathHelper.getIndexXBefore(pts, 0, sz, lastVal - maxDistanceCurrent);
                    if (idxEnd >= 0 && idxEnd < pts.size()) {
                        pts.subList(0, idxEnd).clear();
                        synchronized (frameBackLock) {
                            FrameRender<Point2D> back = frameRenderBack.get();
                            if (back != null) {
                                back.setIdxPointTo(back.getIdxPointTo() - idxEnd);
                                MathHelper.MinMaxPoint2D<Point2D> minMax = back.getMinMaxPoint();
                                if (minMax != null) {
                                    minMax.setIdxMaxX((minMax.getIdxMaxX() - idxEnd));
                                    minMax.setIdxMinX((minMax.getIdxMinX() - idxEnd));
                                    minMax.setIdxMaxY((minMax.getIdxMaxY() - idxEnd));
                                    minMax.setIdxMinY((minMax.getIdxMinY() - idxEnd));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected List<Point2D> getPointsRender() {
        FrameRender<Point2D> fr = frameRender.get();
        return fr != null ? fr.getPoints() : null;
    }

    protected float[] getPointsArrayRender() {
        FrameRender<Point2D> fr = frameRender.get();
        return fr != null ? fr.getPointsPlain() : null;
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

    private void exchangeFrame() {
        FrameRender<Point2D> back = frameRenderBack.get();
        FrameRender<Point2D> front = frameRender.get();
        FrameRender<Point2D> backNew = new FrameRender<>();
        if (front != null && back != null) {
            backNew.setMinMaxPoint(back.getMinMaxPoint());
            backNew.setIdxPointFrom(back.getIdxPointFrom());
            backNew.setIdxPointTo(back.getIdxPointTo());
        }
        frameRenderBack.set(backNew);
        frameRender.lazySet(back);
    }

    protected final NTChart getChart() {
        return wChart != null ? wChart.get() : null;
    }

    protected final Context getContext() {
        NTChart chart = getChart();
        return (chart == null) ? null : chart.getContext();
    }
}
