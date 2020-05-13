package org.chickenhook.binderhooks;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import org.chickenhook.binderhooks.proxyListeners.ProxyListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.chickenhook.binderhooks.Logger.log;
import static org.chickenhook.restrictionbypass.helpers.Reflection.getReflective;

public class ServiceHooks {
    public static boolean hookContentResolver(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException {
        Object sContentService = getReflective(null, ContentResolver.class, "sContentService");
        return BinderHook.addHook(sContentService, binderListener);
    }

    public static boolean hookContentResolver(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException {
        Field f = ContentResolver.class.getDeclaredField("sContentService");
        f.setAccessible(true);
        return ProxyHook.addHook(null, f, f.getType() , proxyListener);
    }

    public static boolean hookNotificationManager(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException {
        Object sService = getReflective(null, NotificationManager.class, "sService");
        return BinderHook.addHook(sService, binderListener);
    }

    public static boolean hookNotificationManager(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException {
        Field f = NotificationManager.class.getDeclaredField("sService");
        f.setAccessible(true);
        return ProxyHook.addHook(null, f,f.getType(), proxyListener);
    }

    public static boolean hookActivityManager(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Object IActivityManagerSingleton = getReflective(null, ActivityManager.class, "IActivityManagerSingleton");
        if (IActivityManagerSingleton == null) {
            log("Unable to install ActivityManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Method getMethod = IActivityManagerSingleton.getClass().getMethod("get");
        getMethod.setAccessible(true);
        return BinderHook.addHook(getMethod.invoke(IActivityManagerSingleton), binderListener);
    }

    public static boolean hookActivityManager(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Object IActivityManagerSingleton = getReflective(null, ActivityManager.class, "IActivityManagerSingleton");
        if (IActivityManagerSingleton == null) {
            log("Unable to install ActivityManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Field instanceField = Class.forName("android.util.Singleton").getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        return ProxyHook.addHook(IActivityManagerSingleton, instanceField,Class.forName("android.app.IActivityManager"), proxyListener);
    }

    public static boolean hookAppOpsManager(@NonNull AppOpsManager appOpsManager, @NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException {
        Object mService = getReflective(appOpsManager, "mService");
        return BinderHook.addHook(mService, binderListener);
    }

    public static boolean hookAppOpsManager(@NonNull AppOpsManager appOpsManager, @NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Field f = appOpsManager.getClass().getDeclaredField("mService");
        f.setAccessible(true);
        return ProxyHook.addHook(appOpsManager, f,f.getType(), proxyListener);
    }


    public static boolean hookPackageManager(@NonNull PackageManager packageManager, @NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException {
        Object mPM = getReflective(packageManager, "mPM");
        return BinderHook.addHook(mPM, binderListener);
    }

    public static boolean hookPackageManager(@NonNull PackageManager packageManager, @NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException {
        Field f = packageManager.getClass().getDeclaredField("mPM");
        f.setAccessible(true);
        return ProxyHook.addHook(packageManager, f,f.getType(), proxyListener);
    }

    public static boolean hookActivityTaskManager(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Object IActivityTaskManagerSingleton = getReflective(null, Class.forName("android.app.ActivityTaskManager"), "IActivityTaskManagerSingleton");
        if (IActivityTaskManagerSingleton == null) {
            log("Unable to install ActivityTaskManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Method getMethod = IActivityTaskManagerSingleton.getClass().getMethod("get");
        getMethod.setAccessible(true);
        return BinderHook.addHook(getMethod.invoke(IActivityTaskManagerSingleton), binderListener);
    }


    public static boolean hookActivityTaskManager(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Object IActivityTaskManagerSingleton = getReflective(null, Class.forName("android.app.ActivityTaskManager"), "IActivityTaskManagerSingleton");
        if (IActivityTaskManagerSingleton == null) {
            log("Unable to install ActivityManager hook - IActivityManagerSingleton was null");
            return false;
        }
        Field instanceField = Class.forName("android.util.Singleton").getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        return ProxyHook.addHook(IActivityTaskManagerSingleton, instanceField,Class.forName("android.app.IActivityTaskManager"), proxyListener);
    }

    public static boolean hookWindowManager(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Object sWindowManagerService = getReflective(null, Class.forName("android.view.WindowManagerGlobal"), "sWindowManagerService");
        return BinderHook.addHook(sWindowManagerService, binderListener);
    }

    public static boolean hookWindowManager(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Field f = Class.forName("android.view.WindowManagerGlobal").getDeclaredField("sWindowManagerService");
        f.setAccessible(true);
        return ProxyHook.addHook(null, f,Class.forName("android.view.WindowManager"), proxyListener);
    }

    public static boolean hookWindowSession(@NonNull BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Object sWindowSession = getReflective(null, Class.forName("android.view.IWindowManagerGlobal"), "sWindowSession");
        return BinderHook.addHook(sWindowSession, binderListener);
    }

    public static boolean hookWindowSession(@NonNull ProxyListener proxyListener) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Field f = Class.forName("android.view.WindowManagerGlobal").getDeclaredField("sWindowSession");
        f.setAccessible(true);
        return ProxyHook.addHook(null, f,Class.forName("android.view.IWindowSession"), proxyListener);
    }
}
