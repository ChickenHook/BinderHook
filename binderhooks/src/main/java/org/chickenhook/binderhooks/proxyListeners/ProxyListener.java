package org.chickenhook.binderhooks.proxyListeners;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Listener for interface hooking
 */
public abstract class ProxyListener implements InvocationHandler {

    private Object obj;

    public void setObject(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke(obj, proxy, method, args);
    }

    public abstract Object invoke(Object original, Object proxy, Method method, Object[] args) throws Throwable;
}
