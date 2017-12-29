package ru.turbomolot.ntchart.holder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import ru.turbomolot.ntchart.utils.ConverterUtil;

/**
 * Created by XE on 07.12.2017.
 */

public class NTChartSV extends SurfaceView implements SurfaceHolder.Callback {

    private Paint paint = new Paint();

    public NTChartSV(Context context) {
        super(context);
        init();
    }

    public NTChartSV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        this.getHolder().addCallback(this);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ConverterUtil.convertDpToPixels(1, getContext()));


//        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//        setRenderer(glRenderer);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

//        this.setBackgroundColor(Color.TRANSPARENT);
//        this.setZOrderOnTop(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);

//        Paint pp = new Paint();
//        pp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        pp.setColor(Color.TRANSPARENT);

//        this.setZOrderOnTop(true);
//        getHolder().setFormat(PixelFormat.TRANSPARENT);
//        this.getHolder().addCallback(this);
//        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);

//        setBackgroundColor(0xFFFFFFFF);
//        getHolder().addCallback(this);

        // I use another SurfaceView behind to draw a grid
        // that is scroll independent with a white background
//        this.setZOrderOnTop(true);
        // Same reason here
//        this.getHolder().setFormat(PixelFormat.TRANSPARENT);
////        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        this.getHolder().addCallback(this);
//        this.getHolder().setSizeFromLayout();
//        // Tried without this line and with "software" and "none" types too
//        // and hardware acceleration on application and activity
//        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        Paint paint = new Paint();
////        paint.setColor(Color.BLUE);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.setWillNotDraw(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        setDrawingCacheEnabled(false);
        nSetSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        nSetSurface(null);
    }


    @Override
    public void draw(Canvas canvas) {
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
        super.draw(canvas);
        canvas.drawLine(canvas.getWidth() >> 1, 0,
                canvas.getWidth() >> 1, canvas.getHeight(), paint);

        canvas.drawLine(0, canvas.getHeight() >> 1,
                canvas.getWidth(), canvas.getHeight() >> 1, paint);


//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        canvas.drawColor(Color.TRANSPARENT);
    }

    //    @Override
//    protected void onStart() {
//        super.onStart();
//        nOnStart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        nOnResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        nOnPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        nOnStop();
//    }

//    @Override
//    protected void onVisibilityChanged(View changedView, int visibility) {
//        super.onVisibilityChanged(changedView, visibility);
//        if (visibility == View.VISIBLE) //onResume called
//        {}
//    else // onPause() called
//        {}
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasWindowFocus) {
//        super.onWindowFocusChanged(hasWindowFocus);
//        if (hasWindowFocus) //onresume() called
//        {}
//    else // onPause() called
//        {}
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        // onDestroy() called
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        // onCreate() called
//    }

    public static native void nOnStart();

    public static native void nOnResume();

    public static native void nOnPause();

    public static native void nOnStop();

    public static native void nSetSurface(Surface surface);

    static {
        System.loadLibrary("ntchart-lib");
    }
}
