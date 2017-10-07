package ru.turbomolot.ntchart.series;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.Iterator;
import java.util.List;

import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.data.IPointLine;
import ru.turbomolot.ntchart.render.ISeriesHolder;
import ru.turbomolot.ntchart.utils.MathHelper;

/**
 * Created by TurboMolot on 04.10.2017.
 */

public class SeriesLineHolder implements ISeriesHolder<IPointLine> {
    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private List<? extends IPointLine> ptsSource;
    private List<? extends IPointLine> ptsRender;
    private RectF windowSize;

    private Float minX;
    private Float maxX;
    private Float minY;
    private Float maxY;

    private float maxDistanceX;
    private float maxDistanceY;

    private Float lastValueX = null;
    private boolean autoScale = true;
    private boolean autoMoveLastX = true;
    private boolean renderFromAxisRight = true;

    private float translateX = 0;
    private float translateY = 0;

    private boolean reducePointsEnabled;

    private final List<Path> paths = new DataList<>(2);

    public SeriesLineHolder() {
        paths.add(new Path()); // line
        paths.add(new Path()); // fill
    }

    @Override
    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void setMatrix(Matrix matrix) {
        this.matrix.set(matrix);
        this.matrix.getValues(matrixValues);
    }

    @Override
    public void calcRender(List<? extends IPointLine> pts) {
        this.ptsSource = pts;
        updateRenderParam();
        updateScale();
        if(isReducePointsEnabled())
            reducePoints();
        fillPath();
    }

    @SuppressWarnings("unchecked")
    private List<IPointLine> getPts() {
        return (List<IPointLine>) ptsSource;
    }

    private float getRenderWidth() {
        RectF wndSize = getWindowSize();
        return (wndSize != null) ?
                wndSize.width()
                : 0;
    }

    @SuppressWarnings("unchecked")
    protected void fillPath() {
        Path lPath = paths.get(0);
        Path fPath = paths.get(1);
        RectF wndSize = getWindowSize();
        if (lPath == null
                || fPath == null
                || ptsRender == null
                || ptsRender.isEmpty()
                || wndSize == null)
            return;
        lPath.rewind();
        fPath.rewind();
        Iterator<? extends IPointLine> itr = ptsRender.iterator();
        IPointLine curPoint = itr.next();
        float x = toRenderX(curPoint.getX());
        float y = toRenderY(curPoint.getY());
        lPath.moveTo(x, y);
        float minYPx = wndSize.bottom;
        fPath.moveTo(x, minYPx);
        lPath.moveTo(x, y);
        while (itr.hasNext()) {
            fPath.lineTo(x, y);
            curPoint = itr.next();
            x = toRenderX(curPoint.getX());
            y = toRenderY(curPoint.getY());
            lPath.lineTo(x, y);
        }
        fPath.lineTo(x, minYPx);
        fPath.close();
    }

    private float getRenderHeight() {
        RectF wndSize = getWindowSize();
        return (wndSize != null) ?
                wndSize.height()
                : 0;
    }

    protected void updateRenderParam() {
        List<IPointLine> pts = getPts();
        if (pts == null || pts.isEmpty()) {
            setMinX(0f);
            setMaxX(0f);
            setMinY(0f);
            setMaxY(0f);
            ptsRender = null;
            return;
        }
        final int sz = pts.size();
//
//        float[] values = new float[9];
//        matrix.getValues(values);
//        float scaleX = values[Matrix.MSCALE_X] <= 0 ? 1 : values[Matrix.MSCALE_X];
////        scaleX *= values[Matrix.MSCALE_X];
////        scaleY *= values[Matrix.MSCALE_Y];

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        float maxDistX = getMaxDistanceX();
//        IPointLine first = pts.get(0);
//        IPointLine last = pts.get(sz - 1);
//        float maxDistPtsX = Math.abs(first.getX() - last.getX());
//        if(maxDistX > maxDistPtsX) {
//            maxDistX = maxDistPtsX;
//        }
        Float lastX = getLastValueX();
        int lastIdx;
//        if(lastX == null && matrixValues[Matrix.MTRANS_X] != 0) {
//            lastX = pts.get(sz - 1).getX();
//            int idxFrom = 0;
//            if(maxDistX > 0) {
//                idxFrom = MathHelper.getIndexXBefore(pts, lastX - maxDistX) + 1;
//                if(idxFrom < 0 || idxFrom > sz - 1)
//                    idxFrom = 0;
//            }
//            float firstX = pts.get(idxFrom).getX();
//            float scaleX = getRenderWidth() / Math.abs(lastX - firstX);
//            matrixValues[Matrix.MTRANS_X] = matrixValues[Matrix.MTRANS_X]/scaleX;
//            lastX -= matrixValues[Matrix.MTRANS_X];
//            if(maxDistX > 0) {
//                idxFrom = MathHelper.getIndexXBefore(pts, lastX - maxDistX) + 1;
//                if(idxFrom < 0 || idxFrom > sz - 1)
//                    idxFrom = 0;
//                firstX = pts.get(idxFrom).getX();
//                if(Math.abs(lastX - firstX) < maxDistX) {
//                    lastX = firstX + maxDistX;
//                }
//            }
//        }


        if (lastX != null) {
            lastIdx = MathHelper.getIndexXBefore(pts, lastX) + 1;
            if (lastIdx < 0)
                lastIdx = 0;
            else if (lastIdx >= sz)
                lastIdx = sz - 1;
        } else {
            lastIdx = sz - 1;
        }
        if (!isAutoMoveLastX()) {
            if (minX == null)
                minX = pts.get(0).getX();
            if (maxX == null)
                maxX = pts.get(lastIdx).getX();
        } else {
            maxX = pts.get(lastIdx).getX();
            if (maxDistX > 0)
                minX = maxX - maxDistX;
            else if (minX == null)
                minX = pts.get(0).getX();
        }
        int idxFrom = (minX == null) ? 0 : MathHelper.getIndexXBefore(pts, minX) + 1;
        if (idxFrom < 0)
            idxFrom = 0;
        Float x = (idxFrom < sz) ?
                pts.get(idxFrom).getX() : null;

        final int idxTo = (maxDistX <= 0) ?
                lastIdx :
                Math.min((MathHelper.getIndexXBefore(pts, (x != null) ?
                        x + maxDistX : pts.get(lastIdx).getX()) + 1), lastIdx);
        if (x != null)
            min = Math.min(min, x);
        x = (idxTo >= 0 && idxTo < sz) ?
                pts.get(idxTo).getX() : null;
        if (x != null)
            max = Math.max(max, x);
        if(minX == null)
            minX = (min == Float.POSITIVE_INFINITY) ? 0 : min;
        if(maxX == null)
            maxX = (max == Float.NEGATIVE_INFINITY) ? 0 : max;
        if (minX < 0) {
            translateX = Math.abs(minX);
        } else {
            if(Math.abs(maxX - minX) < maxDistX && isRenderFromAxisRight())
                translateX = maxDistX - maxX;
            else
                translateX = -minX;
        }
        // Y param
        pts = pts.subList(idxFrom, idxTo + 1);
        if (pts.isEmpty())
            return;
        float[] minMaxY = new float[2];
        pts = MathHelper.reduceToPoint(pts, getRenderWidth(), minMaxY);
        if(minY == null)
            minY = minMaxY[0];
        if(maxY == null)
            maxY = minMaxY[1];

        translateY = -minY;
        ptsRender = pts;
    }

    protected void updateScale() {
        final float width = getRenderWidth();
        final float height = getRenderHeight();
        float scaleX = 1;
        float scaleY = 1;
        if (width <= 0 || height <= 0)
            return;
        if (autoScale) {
            float wndMaxX = getMaxDistanceX();
            float wndMaxY = getMaxDistanceY();
            if (maxX == minX && wndMaxX <= 0)
                scaleX = 1;
            else
                scaleX = (wndMaxX <= 0) ?
                        width / Math.abs(maxX - minX)
                        : width / wndMaxX;
            if (maxY == minY && wndMaxY <= 0)
                scaleY = 1;
            else
                scaleY = (wndMaxY <= 0) ?
                        height / Math.abs(maxY - minY)
                        : height / wndMaxY;
        }
        matrixValues[Matrix.MSCALE_X] = scaleX;// *= matrixValues[Matrix.MSCALE_X];
        matrixValues[Matrix.MSCALE_Y] = scaleY;// *= matrixValues[Matrix.MSCALE_Y];

//        float cx = (scaleX * Math.abs(minX - maxX) / 2);
//        float cy = (scaleY * Math.abs(minY - maxY) / 2);

        matrix.setScale(scaleX, scaleY);
//        matrix.setTranslate(translateX, translateY);
    }

    protected final void reducePoints() {
        if (ptsRender == null || ptsRender.isEmpty())
            return;
        float[] values = new float[9];
        matrix.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        if (scaleX <= 0 && scaleY <= 0)
            return;
        float tolerance = 1 / Math.max(scaleX, scaleY);
        ptsRender = MathHelper.reduce(ptsRender, tolerance);
    }

    protected final Float getLastValueX() {
        return lastValueX;
    }

    protected final void setLastValueX(Float lastValueX) {
        this.lastValueX = lastValueX;
    }

    @Override
    public List<? extends IPointLine> getRender() {
        return ptsRender;
    }

    @Override
    public void setWindowSize(float left, float top, float right, float bottom) {
        if(windowSize == null)
            windowSize = new RectF();
        this.windowSize.set(left, top, right, bottom);
    }

    @Override
    public RectF getWindowSize() {
        return this.windowSize;
    }

    @Override
    public float toRenderX(float x) {
        float scX = matrixValues[Matrix.MSCALE_X];
        float tX = translateX;// + matrixValues[Matrix.MTRANS_X];
        return ((x + tX) * scX) + windowSize.left;
    }

    @Override
    public float toRenderY(float y) {
        float scY = matrixValues[Matrix.MSCALE_Y];
        float tY = translateY;
        return windowSize.bottom - ((y + tY) * scY);
    }

    @Override
    public float toPointX(float x) {
        float scX = matrixValues[Matrix.MSCALE_X];
        float tX = translateX;// + matrixValues[Matrix.MTRANS_X];
        return ((x - windowSize.left) / scX) - tX;
    }

    @Override
    public float toPointY(float y) {
        float scY = matrixValues[Matrix.MSCALE_Y];
        float tY = translateY;
        return ((windowSize.bottom - y) / scY) - tY;
    }

    @Override
    public boolean isRenderFromAxisRight() {
        return renderFromAxisRight;
    }

    @Override
    public void setRenderFromAxisRight(boolean renderFromAxisRight) {
        this.renderFromAxisRight = renderFromAxisRight;
    }

    @Override
    public boolean isAutoScale() {
        return autoScale;
    }

    @Override
    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    @Override
    public boolean isAutoMoveLastX() {
        return autoMoveLastX;
    }

    @Override
    public void setAutoMoveLastX(boolean autoMoveLastX) {
        this.autoMoveLastX = autoMoveLastX;
    }

    @Override
    public float getMaxDistanceX() {
        return maxDistanceX;
    }

    @Override
    public void setMaxDistanceX(float maxDistanceX) {
        this.maxDistanceX = maxDistanceX;
    }

    @Override
    public float getMaxDistanceY() {
        return maxDistanceY;
    }

    @Override
    public void setMaxDistanceY(float maxDistanceY) {
        this.maxDistanceY = maxDistanceY;
    }

    @Override
    public float getMinX() {
        return minX;
    }

    @Override
    public float getMaxX() {
        return maxX;
    }

    @Override
    public float getMinY() {
        return minY;
    }

    @Override
    public float getMaxY() {
        return maxY;
    }

    @Override
    public List<Path> getRenderPaths() {
        return paths;
    }

    @Override
    public void setReducePointsEnabled(boolean reducePointsEnabled) {
        this.reducePointsEnabled = reducePointsEnabled;
    }

    @Override
    public boolean isReducePointsEnabled() {
        return reducePointsEnabled;
    }

    @Override
    public void setMinX(Float minX) {
        this.minX = minX;
    }

    @Override
    public void setMinY(Float minY) {
        this.minY = minY;
    }

    @Override
    public void setMaxX(Float maxX) {
        this.maxX = maxX;
    }

    @Override
    public void setMaxY(Float maxY) {
        this.maxY = maxY;
    }
}
