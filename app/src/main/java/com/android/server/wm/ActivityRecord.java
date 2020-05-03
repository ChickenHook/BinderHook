package com.android.server.wm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

public class ActivityRecord {


    public static class Token implements IBinder {
        private final WeakReference<ActivityRecord> weakActivity;
        private final String name;

        public Token() {
            name = "com.android.chrome";
            weakActivity = new WeakReference<>(new ActivityRecord());
        }

        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return null;
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return false;
        }
    }
}
