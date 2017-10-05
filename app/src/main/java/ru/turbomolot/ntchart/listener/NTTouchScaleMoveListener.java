package ru.turbomolot.ntchart.listener;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import ru.turbomolot.ntchart.charts.NTChart;

/**
 * Created by TurboMolot on 04.10.17.
 */

public class NTTouchScaleMoveListener implements View.OnTouchListener {
    // these matrices will be used to move and zoom
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private WeakReference<NTChart> wChart;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
    private long lastClickTime = 0;

    public NTTouchScaleMoveListener(NTChart chart) {
        if(chart == null)
            throw new NullPointerException("chart can not be null");
        this.wChart = new WeakReference<>(chart);
    }

    protected NTChart getChart() {
        return wChart.get();
    }

    protected boolean applyMatrix(PointF startPoint, Matrix matrix) {
        NTChart chart = getChart();
        if(chart != null) {
            chart.applyMatrix(startPoint, matrix);
            return true;
        }
        return false;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void reset(PointF startPoint) {
        matrix.reset();
        applyMatrix(startPoint, matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(), event.getY());
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                    reset(start);
                    return true;
                }
                lastClickTime = clickTime;

                savedMatrix.set(matrix);
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        NTChart chart = getChart();
                        if(chart == null)
                            return false;
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (chart.getWidth() >> 1) * sx;
                        float yc = (chart.getHeight() >> 1) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }
        return applyMatrix(start, matrix);
    }
}
