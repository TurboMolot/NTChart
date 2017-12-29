package ru.turbomolot.ntchart.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.turbomolot.ntchart.data.IPoint;
import ru.turbomolot.ntchart.data.IPoint2D;
import ru.turbomolot.ntchart.data.Point2D;

/**
 * Created by TurboMolot on 05.12.17.
 */

public class MathHelper {
    /**
     * Бинарный поиск ближайшего элемента в списке
     *
     * @param points список точек
     * @param xVal   искомое значение
     * @return индекс точки предшествующий указанному значению или -1
     */
    public static int getIndexXBefore(final List<? extends IPoint> points, int from, int to, float xVal) {
        if (points != null && !points.isEmpty()) {
            if (from >= points.size())
                from = points.size() - 1;
            else if (from < 0) {
                from = 0;
            }
            if (to < from)
                to = from;
            else if (to >= points.size())
                to = points.size() - 1;
        }
        if (points == null
                || points.isEmpty()
                || xVal < points.get(from).getX())
            return from - 1;


        final int sz = to + 1;
        final int lastIdx = sz - 1;
        if (xVal > points.get(lastIdx).getX())
            return lastIdx;
        int low = from, high = sz, mid = -1;
        while (low < high) {
            mid = (low + high) >>> 1;
            if (xVal == points.get(mid).getX()) {
                // idx = mid; // Точное совпадение
                break;
            } else {
                if (xVal < points.get(mid).getX()) {
                    high = mid;
                } else {
                    low = mid + 1;
                }
            }
        }
        return ((xVal <= points.get(mid).getX()) ? mid : Math.min(mid + 1, lastIdx)) - 1;
    }

    public static <P extends IPoint2D> P absScalePoint(final FrameRender<P> frame, final P point,
                                                        float xOffset,
                                                        float yOffset, float h) {
        P itm = point.copy();
        itm.setX((itm.getX() + xOffset) * frame.getScaleX());
        itm.setY(h - ((itm.getY() + yOffset) * frame.getScaleY()));
//        itm.setY(((itm.getY() + yOffset) * frame.getScaleY()));
        return itm;
    }

    private static <P extends IPoint2D> int addPtsPlain(float pts[], P itm, int idx) {
        pts[idx++] = itm.getX();
        pts[idx++] = itm.getY();
        if (idx > 2 && idx + 2 < pts.length) {
            pts[idx++] = itm.getX();
            pts[idx++] = itm.getY();
        }
        return idx;
    }

    public static <P extends IPoint2D> void pointAbsScale(final FrameRender<P> frame, float width, float height) {
        List<P> points = frame.getPoints();
        if (width <= 0 || points == null || points.isEmpty())
            return;
        float xOffset = frame.getOffsetX();
        float yOffset = frame.getOffsetY();
        final int sz = points.size();
        float[] ptsRetPlain = new float[(sz << 2) - 4];
        int idxPlain = 0;
        for (P itm : points) {
            P itmAbs = absScalePoint(frame, itm, xOffset, yOffset, height);
            idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
        }
        frame.setPointsPlain(Arrays.copyOf(ptsRetPlain, idxPlain));
    }

    public static <P extends IPoint2D> void reducePointAbsScale(final FrameRender<P> frame, float width, float height) {
        List<P> points = frame.getPoints();
        final int sz = points != null ? points.size() : 0;
        if (width <= 0 || sz <= 1 || points.isEmpty())
            return;
        float xOffset = frame.getOffsetX();
        float yOffset = frame.getOffsetY();

//        final float widthPts = width / frame.getScaleX();
        int ptWnd = Math.round((float) sz / width);
        List<P> ptsRet = new ArrayList<>();
        float[] ptsRetPlain = new float[(sz << 2) - 4];
        int idxPlain = 0;
        frame.setPoints(ptsRet);
        frame.setPointsPlain(ptsRetPlain);
        if (ptWnd <= 1) {
            for (P itm : points) {
                P itmAbs = absScalePoint(frame, itm, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);
            }
            return;
        }
        int idx = 0;
        int nextWnd = ptWnd;
        while (idx < sz) {
            P ptMin = points.get(idx);
            P ptMax = ptMin;
            while (idx < nextWnd && idx < sz) {
                P ptCur = points.get(idx);
                if (ptMin.getY() > ptCur.getY()) {
                    ptMin = ptCur;
                } else if (ptMax.getY() < ptCur.getY()) {
                    ptMax = ptCur;
                }
                idx++;
            }
            nextWnd += ptWnd;
            if (ptMin == ptMax) {
                P itmAbs = absScalePoint(frame, ptMax, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);
            } else if (ptMin.getX() < ptMax.getX()) {
                P itmAbs = absScalePoint(frame, ptMin, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);

                itmAbs = absScalePoint(frame, ptMax, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);
            } else {
                P itmAbs = absScalePoint(frame, ptMax, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);

                itmAbs = absScalePoint(frame, ptMin, xOffset, yOffset, height);
                idxPlain = addPtsPlain(ptsRetPlain, itmAbs, idxPlain);
                ptsRet.add(itmAbs);

//                ptsRet.add(absScalePoint(frame, ptMax, xOffset, yOffset));
//                ptsRet.add(absScalePoint(frame, ptMin, xOffset, yOffset));
            }
        }
        frame.setPointsPlain(Arrays.copyOf(ptsRetPlain, idxPlain));
        frame.setPoints(ptsRet);
    }

    public static class MinMaxPoint2D<Point extends IPoint2D> {
        private Point minX;
        private Point maxX;
        private Point minY;
        private Point maxY;

        private int idxMinX;
        private int idxMinY;
        private int idxMaxX;
        private int idxMaxY;

        public Point getMinX() {
            return minX;
        }

        public Point getMaxX() {
            return maxX;
        }

        public Point getMinY() {
            return minY;
        }

        public Point getMaxY() {
            return maxY;
        }

        public int getIdxMinX() {
            return idxMinX;
        }

        public void setIdxMinX(int idxMinX) {
            if (idxMinX < 0) {
                this.idxMinX = 0;
                this.minX = null;
            } else {
                this.idxMinX = idxMinX;
            }
        }

        public int getIdxMinY() {
            return idxMinY;
        }

        public void setIdxMinY(int idxMinY) {
            if (idxMinY < 0) {
                this.idxMinY = 0;
                this.minY = null;
            } else {
                this.idxMinY = idxMinY;
            }
        }

        public int getIdxMaxX() {
            return idxMaxX;
        }

        public void setIdxMaxX(int idxMaxX) {
            if (idxMaxX < 0) {
                this.idxMaxX = 0;
                this.maxX = null;
            } else {
                this.idxMaxX = idxMaxX;
            }
        }

        public int getIdxMaxY() {
            return idxMaxY;
        }

        public void setIdxMaxY(int idxMaxY) {
            if (idxMaxY < 0) {
                this.idxMaxY = 0;
                this.maxY = null;
            } else {
                this.idxMaxY = idxMaxY;
            }
        }
    }

    public static <Point extends IPoint2D> MinMaxPoint2D<Point> getMinMax(Point[] ptsInput) {
        if (ptsInput == null || ptsInput.length == 0)
            return null;
        int sz = ptsInput.length;
        MinMaxPoint2D<Point> ret = new MinMaxPoint2D<>();
        if (ptsInput[0] == null) {
            return ret;
        }
        ret.minX = ptsInput[0];
        ret.maxX = ptsInput[sz - 1];
        ret.minY = ptsInput[0];
        ret.maxY = ptsInput[0];
        int idx = (sz % 2 == 0) ? sz : sz - 1;
        while (idx > 0) {
            Point itm = ptsInput[--idx];
            if (ret.minY.getY() > itm.getY())
                ret.minY = itm;
            if (ret.maxY.getY() < itm.getY())
                ret.maxY = itm;
            itm = ptsInput[--idx];
            if (ret.minY.getY() > itm.getY())
                ret.minY = itm;
            if (ret.maxY.getY() < itm.getY())
                ret.maxY = itm;
        }
        if (sz % 2 != 0) {
            Point itm = ptsInput[sz - 1];
            if (ret.minY.getY() > itm.getY())
                ret.minY = itm;
            if (ret.maxY.getY() < itm.getY())
                ret.maxY = itm;
        }
        return ret;

//        float minY = Float.POSITIVE_INFINITY;
//        float maxY = Float.NEGATIVE_INFINITY;
//        int idx = (ptsInput.size() % 2 == 0) ? ptsInput.size() : ptsInput.size() - 1;
//        Object[] ptsArr = ptsInput.toArray(); // Плохая работа с памятью...
//        float y1, y2;
//        while (idx > 0) {
//            y1 = ((IPoint2D) ptsArr[--idx]).getY();
//            y2 = ((IPoint2D) ptsArr[--idx]).getY();
//            minY = Math.min(Math.min(minY, y1), y2);
//            maxY = Math.max(Math.max(maxY, y1), y2);
//        }
//        if (ptsInput.size() % 2 != 0) {
//            float y = ((IPoint2D) ptsArr[ptsInput.size() - 1]).getY();
//            minY = Math.min(minY, y);
//            maxY = Math.max(maxY, y);
//        }
//        return new float[] {minY, maxY};
    }

    public static <Point extends IPoint2D> MinMaxPoint2D<Point> getMinMaxCache(List<Point> pts,
                                                                               int from,
                                                                               int fromCache, int toCache,
                                                                               MinMaxPoint2D<Point> previous) {
        if (pts == null || pts.isEmpty())
            return null;
        if (fromCache >= pts.size())
            return previous;
        else if (fromCache < 0)
            fromCache = 0;
        if (toCache >= pts.size())
            toCache = pts.size() - 1;
        else if (toCache < fromCache)
            toCache = fromCache;
        if (previous == null) {
            previous = new MinMaxPoint2D<>();
        }
        if (from < 0)
            from = 0;
        else if (from >= pts.size())
            from = pts.size() - 1;

        previous.minX = pts.get(from);
        previous.idxMinX = from;
        if (previous.minY == null
                || previous.idxMinY < from
                || previous.idxMinY > toCache) {
            previous.minY = pts.get(from);
            previous.idxMinY = from;
        }
        previous.maxX = pts.get(toCache);
        previous.idxMaxX = toCache;

        if (previous.maxY == null
                || previous.idxMaxY < from
                || previous.idxMaxY > toCache) {
            previous.maxY = pts.get(toCache);
            previous.idxMaxY = toCache;
        }
        while (fromCache <= toCache) {
            Point itm = pts.get(fromCache);
            if (previous.minY.getY() > itm.getY()) {
                previous.minY = itm;
                previous.idxMinY = fromCache;
            }
            if (previous.maxY.getY() < itm.getY()) {
                previous.maxY = itm;
                previous.idxMaxY = fromCache;
            }
            fromCache++;
        }
        return previous;
    }

    public static <Point extends IPoint2D> MinMaxPoint2D<Point> getMinMax(List<Point> ptsInput) {
        if (ptsInput == null || ptsInput.isEmpty())
            return null;
        MinMaxPoint2D<Point> ret = new MinMaxPoint2D<>();
        for (Point itm : ptsInput) {
            if (ret.minY.getY() > itm.getY())
                ret.minY = itm;
            if (ret.maxY.getY() < itm.getY())
                ret.maxY = itm;
        }
        return ret;
    }

    static class Line<P extends IPoint2D> {
        private P start;
        private P end;

        private double dx;
        private double dy;
        private double sxey;
        private double exsy;
        private  double length;

        public Line(P start, P end) {
            this.start = start;
            this.end = end;
            dx = start.getX() - end.getX();
            dy = start.getY() - end.getY();
            sxey = start.getX() * end.getY();
            exsy = end.getX() * start.getY();
            length = Math.sqrt(dx*dx + dy*dy);
        }

        @SuppressWarnings("unchecked")
        public List<P> asList() {
            return Arrays.asList(start, end);
        }

        double distance(P p) {
            return Math.abs(dy * p.getX() -
                    dx * p.getY() + sxey - exsy) / length;
        }
    }
    /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
     *
     * @param points
     *          initial, ordered list of points (objects implementing the {@link IPoint2D} interface)
     * @param epsilon
     *          allowed margin of the resulting curve, has to be > 0
     */
    public static <P extends IPoint2D> List<P> reducePts(List<P> points, float epsilon) {
        if(points == null || points.isEmpty())
            return points;
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        double furthestPointDistance = 0.0;
        int furthestPointIndex = 0;
        Line<P> line = new Line<P>(points.get(0), points.get(points.size() - 1));
        for (int i = 1; i < points.size() - 1; i++) {
            double distance = line.distance(points.get(i));
            if (distance > furthestPointDistance ) {
                furthestPointDistance = distance;
                furthestPointIndex = i;
            }
        }
        if (furthestPointDistance > epsilon) {
            List<P> reduced1 = reducePts(points.subList(0, furthestPointIndex+1), epsilon);
            List<P> reduced2 = reducePts(points.subList(furthestPointIndex, points.size()), epsilon);
            List<P> result = new CopyOnWriteArrayList<>(reduced1);
            result.addAll(reduced2.subList(1, reduced2.size()));
            return result;
        } else {
            return line.asList();
        }
    }

    public static <P extends IPoint2D> List<P> reducePtsNative(List<P> points, float epsilon) {
        if(points == null || points.isEmpty())
            return points;
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        float[] ptsRes = nReduceListOrderedX(points, epsilon);
        if(ptsRes != null && ptsRes.length > 0 && ptsRes.length % 2 == 0) {
            int len = ptsRes.length >> 1;
            int idx = 0;
            List<IPoint2D> retList = new CopyOnWriteArrayList<>();
            while (idx < len) {
                retList.add(new Point2D(ptsRes[idx << 1], ptsRes[(idx << 1) + 1]));
                idx++;
            }
            return (List<P>) retList;
        }
        return null;
    }

    private static native float[] nReduceListOrderedX(List<? extends IPoint2D> ptsInput, float tolerance);

    static {
        System.loadLibrary("ntchart-lib");
    }

//    /**
//     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
//     *
//     * @param points
//     *          initial, ordered list of points (objects implementing the {@link IPoint2D} interface)
//     * @param epsilon
//     *          allowed margin of the resulting curve, has to be > 0
//     */
//    private static <P extends IPoint2D> List<P> reducePts(List<P> points, List<P> ret, int from, int to, float epsilon) {
//        if(points == null || points.isEmpty() || from >= points.size() || to >= points.size())
//            return points;
////        if (epsilon < 0) {
////            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
////        }
//        double furthestPointDistance = 0.0;
//        int furthestPointIndex = 0;
//        Line<P> line = new Line<P>(points.get(from), points.get(to));
//        for (int i = from + 1; i < to; i++) {
//            double distance = line.distance(points.get(i));
//            if (distance > furthestPointDistance ) {
//                furthestPointDistance = distance;
//                furthestPointIndex = i;
//            }
//        }
//        if (furthestPointDistance > epsilon) {
//            List<P> reduced1 = reducePts(points, ret, from, furthestPointIndex+2, epsilon);
//            List<P> reduced2 = reducePts(points, ret, furthestPointIndex, to + 1, epsilon);
//            if(ret == null)
//                ret = new CopyOnWriteArrayList<>();
//            ret.addAll(reduced1);
//            List<P> result = new CopyOnWriteArrayList<>(reduced1);
//            result.addAll(reduced2.subList(1, reduced2.size()));
//            return result;
//        } else {
//            return line.asList();
//        }
//    }
//
//    public static <P extends IPoint2D> List<P> reducePts(List<P> points, float epsilon) {
//        if(points == null || points.isEmpty())
//            return points;
//        if (epsilon < 0) {
//            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
//        }
//        return reducePts(points, 0, points.size() - 1, epsilon);
//    }
}
