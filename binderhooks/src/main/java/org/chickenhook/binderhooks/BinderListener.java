package org.chickenhook.binderhooks;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;

public abstract class BinderListener {
    @Nullable
    protected String getInterfaceDescriptor(@NonNull IBinder originalBinder) throws RemoteException {
        return originalBinder.getInterfaceDescriptor();
    }

    protected boolean pingBinder(@NonNull IBinder originalBinder) {
        return originalBinder.pingBinder();
    }

    protected boolean isBinderAlive(@NonNull IBinder originalBinder) {
        return originalBinder.isBinderAlive();
    }

    @Nullable
    protected IInterface queryLocalInterface(@NonNull IBinder originalBinder, @NonNull String descriptor) {
        return originalBinder.queryLocalInterface(descriptor);
    }

    protected void dump(@NonNull IBinder originalBinder, @NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        originalBinder.dump(fd, args);
    }

    protected void dumpAsync(@NonNull IBinder originalBinder, @NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        originalBinder.dumpAsync(fd, args);
    }

    protected abstract boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException;

    protected void linkToDeath(@NonNull IBinder originalBinder, @NonNull IBinder.DeathRecipient recipient, int flags) throws RemoteException {
        originalBinder.linkToDeath(recipient, flags);
    }

    protected boolean unlinkToDeath(@NonNull IBinder originalBinder, @NonNull IBinder.DeathRecipient recipient, int flags) {
        return originalBinder.unlinkToDeath(recipient, flags);
    }
}
