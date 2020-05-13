package org.chickenhook.binderhooks;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;

import static org.chickenhook.binderhooks.Logger.log;
import static org.chickenhook.restrictionbypass.helpers.Reflection.getReflective;
import static org.chickenhook.restrictionbypass.helpers.Reflection.setReflective;

public class BinderHook {

    public static boolean VERBOSE = false;


    /**
     * Add a binder hook for the given binder proxy
     *
     * @param binderProxy      to be hooked
     * @param binderListener will be called for different binder interactions
     * @return true on success
     * @throws NoSuchFieldException   if mRemote is not available
     * @throws IllegalAccessException if mRemote field cannot be accessed
     */
    public static boolean addHook(@Nullable Object binderProxy, @Nullable BinderListener binderListener) throws NoSuchFieldException, IllegalAccessException {
        if (binderProxy == null) {
            log("Unable to addHook - given binderProxy is null");
            return false;
        }

        if (binderListener == null) {
            log("Unable to addHook - given onBinderListener is null");
            return false;
        }

        /*for (Method m : binderProxy.getClass().getMethods()) {
            if (VERBOSE) log(binderProxy.getClass().getCanonicalName() + " Found method " + m);
        }*/

        IBinder mRemote = getReflective(binderProxy, "mRemote");
        if (mRemote == null) {
            log("Unable to addHook - retrieved mRemote is null");
            return false;
        }

        IBinder fakeBinder = new FakeBinder(binderProxy.getClass().getName(), mRemote, binderListener);
        setReflective(binderProxy, "mRemote", fakeBinder);
        log("Successfully added hook for <" + binderProxy.getClass().getName() + ">");
        return true;
    }

    public static class FakeBinder implements IBinder {

        private @NonNull
        BinderListener mBinderListener;
        private @NonNull
        IBinder mOriginalBinder;
        private @NonNull
        String mName;

        public FakeBinder(@NonNull String name, @NonNull IBinder originalBinder, @NonNull BinderListener binderListener) {
            mName = name;
            mOriginalBinder = originalBinder;
            mBinderListener = binderListener;
        }


        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return mBinderListener.getInterfaceDescriptor(mOriginalBinder);
        }

        @Override
        public boolean pingBinder() {
            return mBinderListener.pingBinder(mOriginalBinder);
        }

        @Override
        public boolean isBinderAlive() {
            return mBinderListener.isBinderAlive(mOriginalBinder);
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return mBinderListener.queryLocalInterface(mOriginalBinder, descriptor);
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            mBinderListener.dump(mOriginalBinder, fd, args);
        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            mBinderListener.dumpAsync(mOriginalBinder, fd, args);
        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            if (VERBOSE) {
                try {
                    log("Got transact call code: <" + code + "> data: <" + data + "> reply: <" + reply + "> flags: <" + flags + ">");
                    doStackTrace();
                    ParcelEditor.dump(data);
                } catch (Exception e) {
                    log("Error while dump parcel", e);
                }
            }
            boolean res = mBinderListener.transact(mOriginalBinder, code, data, reply, flags);
            if (VERBOSE) {
                try {
                    if (reply != null) {
                        log("Got reply call code: <" + code + "> data: <" + data + "> reply: <" + reply + "> flags: <" + flags + ">");
                        ParcelEditor.dump(reply);
                    }
                } catch (Exception e) {
                    log("Error while dump parcel", e);
                }
            }
            return res;
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
            mBinderListener.linkToDeath(mOriginalBinder, recipient, flags);
        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return mBinderListener.unlinkToDeath(mOriginalBinder, recipient, flags);
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
