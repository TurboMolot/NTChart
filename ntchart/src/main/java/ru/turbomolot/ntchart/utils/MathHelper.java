package ru.turbomolot.ntchart.utils;

import java.util.List;

import ru.turbomolot.ntchart.data.DataList;
import ru.turbomolot.ntchart.data.IPoint;
import ru.turbomolot.ntchart.data.IPointLine;
import ru.turbomolot.ntchart.data.PointLine;

/**
 * Created by TurboMolot on 04.10.2017.
 */

public class MathHelper {
    /**
     * Бинарный поиск ближайшего элемента в списке
     *
     * @param points список точек
     * @param xVal   искомое значение
     * @return индекс точки предшествующий указанному значению или -1
     */
    public static int getIndexXBefore(final List<? extends IPoint> points, float xVal) {
        if (points == null
                || points.isEmpty()
                || xVal < points.get(0).getX())
            return -1;
        final int sz = points.size();
        final int lastIdx = sz - 1;
        if (xVal > points.get(lastIdx).getX())
            return lastIdx;
        int low = 0, high = sz, mid = -1;
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

    public static <P extends IPointLine> List<P> reduceToPoint(List<P> points, float width, float[] minMaxY2) {
        if(width <= 0 || points == null || points.isEmpty())
            return null;
        final int sz = points.size();
        int ptWnd = Math.round(sz / width);
        if(ptWnd <= 1) {
            if(minMaxY2 != null && minMaxY2.length == 2) {
                float[] valMinMax = getMinMaxY(points);
                if (valMinMax != null) {
                    minMaxY2[0] = valMinMax[0];
                    minMaxY2[1] = valMinMax[1];
                }
            }
            return points;
        }
        List<P> ptsRet = new DataList<>();
        int idx = 0;
        int nextWnd = ptWnd;
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        while (idx < sz) {
            P ptMin = points.get(idx);
            P ptMax = ptMin;
            while (idx < nextWnd && idx < sz) {
                P ptCur = points.get(idx);
                if(ptMin.getY() > ptCur.getY()) {
                    ptMin = ptCur;
                } else if(ptMax.getY() < ptCur.getY()) {
                    ptMax = ptCur;
                }
                idx++;
            }
            nextWnd += ptWnd;
            if(ptMin == ptMax) {
                ptsRet.add(ptMax);
            } else if(ptMin.getX() < ptMax.getY()) {
                ptsRet.add(ptMin);
                ptsRet.add(ptMax);
            } else {
                ptsRet.add(ptMax);
                ptsRet.add(ptMin);
            }
            min = Math.min(min, ptMin.getY());
            max = Math.max(max, ptMax.getY());
        }
        if(minMaxY2 != null && minMaxY2.length == 2) {
            minMaxY2[0] = min;
            minMaxY2[1] = max;
        }
        return ptsRet;
    }

    public static float[] getMinMaxY(List<? extends IPointLine> ptsInput) {
        if(ptsInput == null || ptsInput.isEmpty())
            return null;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        int idx = (ptsInput.size() % 2 == 0) ? ptsInput.size() : ptsInput.size() - 1;
        Object[] ptsArr = ptsInput.toArray(); // Плохая работа с памятью...
        float y1, y2;
        while (idx > 0) {
            y1 = ((IPointLine) ptsArr[--idx]).getY();
            y2 = ((IPointLine) ptsArr[--idx]).getY();
            minY = Math.min(Math.min(minY, y1), y2);
            maxY = Math.max(Math.max(maxY, y1), y2);
        }
        if (ptsInput.size() % 2 != 0) {
            float y = ((IPointLine) ptsArr[ptsInput.size() - 1]).getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return new float[] {minY, maxY};
    }

    public static <P extends IPointLine> List<? extends IPointLine> reduce(List<P> points, float epsilon) {
        if(points == null || points.isEmpty())
            return points;
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        float[] ptsRes = nReduceListOrderedX(points, epsilon);
        if(ptsRes != null && ptsRes.length > 0 && ptsRes.length % 2 == 0) {
            int len = ptsRes.length >> 1;
            int idx = 0;
            List<PointLine> retList = new DataList<>();
            while (idx < len) {
                retList.add(new PointLine(ptsRes[idx << 1], ptsRes[(idx << 1) + 1]));
                idx++;
            }
            return retList;
        }
        return null;
    }

    private static native float[] nReduceListOrderedX(List<? extends IPointLine> ptsInput, float tolerance);

    static {
        System.loadLibrary("nt_chart_util");
    }
}
