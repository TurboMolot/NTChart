package ru.turbomolot.ntchart.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.TimeUnit;

/**
 * Created by TurboMolot on 26.09.17.
 */

public class FPSHelper {
    private final DecimalFormat format;
    private double fps;
    private double fpsCurrent;
    private double lastTime;
    private final Object lock = new Object();
    private double timeFpsNextUpd;
    private double cnt;
    private final double multiple = 1_000_000_000.0 / 2.0;
    private double inc;

    private double fpsRenderTime;
    private long sleepTime;
    private double fpsNormal;

    public FPSHelper() {
        inc = multiple / 1_000_000_000.0;
        setNormalFPS(24);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format = new DecimalFormat("#.##", symbols);
    }

    public void tick() {
        synchronized (lock) {
            cnt += inc;
            fpsCurrent += multiple / (-(lastTime - (lastTime = System.nanoTime())));
            if(timeFpsNextUpd < lastTime) {
                fps = fpsCurrent / cnt;
                timeFpsNextUpd = lastTime + multiple;
                fpsCurrent = 0;
                cnt = 0;
            }
        }
    }

    public double getFps() {
        synchronized (lock) {
            return fps;
        }
    }

    public int getFpsInt() {
        synchronized (lock) {
            return (int) Math.round(fps);
        }
    }

    public String getFpsStr() {
        return format.format(getFps());
    }

    public void sleepNormal() throws InterruptedException {
        double time = System.nanoTime() - lastTime;
        sleepTime = Math.round((time > fpsRenderTime) ? fpsRenderTime : (fpsRenderTime - time));
        if(sleepTime < 5)
            sleepTime = Math.round(fpsRenderTime);
        TimeUnit.NANOSECONDS.sleep(sleepTime);
        tick();
    }

    public final void setNormalFPS(double fpsNormal) {
        if(fpsNormal < 1 || fpsNormal > 500)
            return;
        fpsRenderTime = 1_000_000_000.0 / fpsNormal;
        this.fpsNormal = fpsNormal;
    }

    public final double getNormalFPS() {
        return fpsNormal;
    }
}
