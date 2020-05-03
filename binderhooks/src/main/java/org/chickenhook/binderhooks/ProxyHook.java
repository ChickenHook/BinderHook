package org.chickenhook.binderhooks;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

import static org.chickenhook.binderhooks.Logger.log;
import static org.chickenhook.restrictionbypass.helpers.Reflection.getReflective;
import static org.chickenhook.restrictionbypass.helpers.Reflection.setReflective;

public class ProxyHook {

    public static boolean VERBOSE = false;


    /**
     * Add a binder hook for the given binder proxy
     *
     * @param binderProxy      to be hooked
     * @param onBinderListener will be called for different binder interactions
     * @return true on success
     * @throws NoSuchFieldException   if mRemote is not available
     * @throws IllegalAccessException if mRemote field cannot be accessed
     */
    public static boolean addHook(@Nullable Object binderProxy, @Nullable OnBinderListener onBinderListener) throws NoSuchFieldException, IllegalAccessException {
        if (binderProxy == null) {
            log("Unable to addHook - given binderProxy is null");
            return false;
        }

        if (onBinderListener == null) {
            log("Unable to addHook - given onBinderListener is null");
            return false;
        }

        for (Method m : binderProxy.getClass().getMethods()) {
            if (VERBOSE) log(binderProxy.getClass().getCanonicalName() + " Found method " + m);
        }

        IBinder mRemote = getReflective(binderProxy, "mRemote");
        if (mRemote == null) {
            log("Unable to addHook - retrieved mRemote is null");
            return false;
        }

        IBinder fakeBinder = new FakeBinder(binderProxy.getClass().getName(), mRemote, onBinderListener);
        setReflective(binderProxy, "mRemote", fakeBinder);
        log("Successfully added hook for <" + binderProxy.getClass().getName() + ">");
        return true;
    }

    public static class FakeBinder implements IBinder {

        private @NonNull
        OnBinderListener mOnBinderListener;
        private @NonNull
        IBinder mOriginalBinder;
        private @NonNull
        String mName;

        public FakeBinder(@NonNull String name, @NonNull IBinder originalBinder, @NonNull OnBinderListener onBinderListener) {
            mName = name;
            mOriginalBinder = originalBinder;
            mOnBinderListener = onBinderListener;
        }


        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return mOnBinderListener.getInterfaceDescriptor(mOriginalBinder);
        }

        @Override
        public boolean pingBinder() {
            return mOnBinderListener.pingBinder(mOriginalBinder);
        }

        @Override
        public boolean isBinderAlive() {
            return mOnBinderListener.isBinderAlive(mOriginalBinder);
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return mOnBinderListener.queryLocalInterface(mOriginalBinder, descriptor);
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            mOnBinderListener.dump(mOriginalBinder, fd, args);
        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            mOnBinderListener.dumpAsync(mOriginalBinder, fd, args);
        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            if (VERBOSE)
                log("Got transact call code: <" + code + "> data: <" + data + "> reply: <" + reply + "> flags: <" + flags + ">");
            if (VERBOSE) doStackTrace();
            return mOnBinderListener.transact(mOriginalBinder, code, data, reply, flags);
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
            mOnBinderListener.linkToDeath(mOriginalBinder, recipient, flags);
        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return mOnBinderListener.unlinkToDeath(mOriginalBinder, recipient, flags);
        }
    }

    static void doStackTrace() {
        try {
            throw new Exception("Trace");
        } catch (Exception e) {
            log("ProxyHook [+] trace [+] ", e);
        }
    }
}
