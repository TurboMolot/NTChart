package ru.turbomolot.ntchart.utils.generator;

import android.util.Log;

/**
 * Created by TurboMolot on 28.12.17.
 */

public class LogWnd implements ILogWindow {
    private final String TAG = "[ECG Generator]";
    @Override
    public void println(String value) {
        Log.d(TAG, value);
    }
}
