package ru.turbomolot.ntchart.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TurboMolot on 26.09.17.
 */

public class ForegroundDetector implements Application.ActivityLifecycleCallbacks {
    private final static String LOG_TAG = ForegroundDetector.class.getName();

    public interface ForegroundListener {
        void onBecameForeground();
        void onBecameBackground();
    }
    /**
     * How long to wait before checking onStart()/onStop() count to determine if the app has been
     * backgrounded.
     */
    public static final long BACKGROUND_CHECK_DELAY_MS = 500;

    private static ForegroundDetector instance;

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private boolean isForeground = true;
    private int count;
    private List<ForegroundListener> listeners = new CopyOnWriteArrayList<>();

    public static void init(final Application application) {
        if (instance == null) {
            instance = new ForegroundDetector();
            application.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static ForegroundDetector getInstance() {
        return instance;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public boolean isBackground() {
        return !isForeground;
    }

    public void addListener(ForegroundListener listener){
        listeners.add(listener);
    }

    public void removeListener(ForegroundListener listener){
        listeners.remove(listener);
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        count++;

        // Remove posted Runnables so any Meteor disconnect is cancelled if the user comes back to
        // the app before it runs.
        mainThreadHandler.removeCallbacksAndMessages(null);

        if (!isForeground) {
            isForeground = true;
            for (ForegroundListener l : listeners) {
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    Log.e(LOG_TAG, "ForegroundListener threw exception!", exc);
                }
            }
        }
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        count--;

        // A transparent Activity like community user profile won't stop the Activity that launched
        // it. If you launch another Activity from the user profile or hit the Android home button,
        // there are two onStops(). One for the user profile and one for its parent. Remove any
        // posted Runnables so we don't get two session ended events.
        mainThreadHandler.removeCallbacksAndMessages(null);
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (count == 0) {
                    isForeground = false;
                    for (ForegroundListener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.e(LOG_TAG, "ForegroundListener threw exception!", exc);
                        }
                    }
                }
            }
        }, BACKGROUND_CHECK_DELAY_MS);
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {

    }

    @Override
    public void onActivityResumed(final Activity activity) {

    }

    @Override
    public void onActivityPaused(final Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(final Activity activity) {

    }
}
