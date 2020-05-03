package org.chickenhook.binderhooks;

import android.util.Log;

import androidx.annotation.NonNull;

public class Logger {

    public static void log(@NonNull String message) {
        Log.i("BinderHook", message);
    }

    public static void log(@NonNull String message, @NonNull Exception exception) {
        Log.i("BinderHook", message, exception);
    }
}
