package org.chickenhook.binderhooks;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.chickenhook.binderhooks.proxyListeners.ProxyListener;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.chickenhook.binderhooks.Logger.log;

public class ProxyHook {

    public static boolean addHook(@Nullable Object host, @NonNull Field field, @NonNull Class<?> type, @NonNull ProxyListener proxyListener) throws IllegalAccessException {
        proxyListener.setObject(field.get(host));
        Object proxy = Proxy.newProxyInstance(Context.class.getClassLoader(), new Class[]{type}, proxyListener);
        try {
            field.set(host, proxy);
            log("ProxyHook [-] successfully added hook for <" + type + ">");
            return true;
        } catch (IllegalAccessException e) {
            log("ProxyHook [-] error while place proxy hook", e);
            return false;
        }
    }
}
