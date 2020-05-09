package org.chickenhook.binderhooks;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.chickenhook.binderhooks.Logger.log;
import static org.chickenhook.restrictionbypass.helpers.Reflection.getReflective;

public class ServiceHooks {
    public static boolean hookContentResolver(@NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException {
        Object sContentService = getReflective(null, ContentResolver.class, "sContentService");
        return ProxyHook.addHook(sContentService, onBinderListener);
    }

    public static boolean hookNotificationManager(@NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException {
        Object sService = getReflective(null, NotificationManager.class, "sService");
        return ProxyHook.addHook(sService, onBinderListener);
    }

    public static boolean hookActivityManager(@NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Object IActivityManagerSingleton = getReflective(null, ActivityManager.class, "IActivityManagerSingleton");
        if (IActivityManagerSingleton == null) {
            log("Unable to install ActivityManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Method getMethod = IActivityManagerSingleton.getClass().getMethod("get");
        getMethod.setAccessible(true);
        return ProxyHook.addHook(getMethod.invoke(IActivityManagerSingleton), onBinderListener);
    }

    public static boolean hookAppOpsManager(@NonNull AppOpsManager appOpsManager, @NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException {
        Object mService = getReflective(appOpsManager, "mService");
        return ProxyHook.addHook(mService, onBinderListener);
    }


    public static boolean hookPackageManager(@NonNull PackageManager packageManager, @NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException {
        Object mPM = getReflective(packageManager, "mPM");
        return ProxyHook.addHook(mPM, onBinderListener);
    }

    public static boolean hookActivityTaskManager(@NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Object IActivityTaskManagerSingleton = getReflective(null, Class.forName("android.app.ActivityTaskManager"), "IActivityTaskManagerSingleton");
        if (IActivityTaskManagerSingleton == null) {
            log("Unable to install ActivityTaskManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Method getMethod = IActivityTaskManagerSingleton.getClass().getMethod("get");
        getMethod.setAccessible(true);
        return ProxyHook.addHook(getMethod.invoke(IActivityTaskManagerSingleton), onBinderListener);
    }

    public static boolean hookWindowManager(@NonNull OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Object sWindowManagerService = getReflective(null, Class.forName("android.view.WindowManagerGlobal"), "sWindowManagerService");
        return ProxyHook.addHook(sWindowManagerService, onBinderListener);
    }
}
