package org.chickenhook.binderhooks;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.system.Os;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class ServiceHooksTest {

    @Test
    public void hookContentResolver() throws Exception {
        InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(); // let android establish a connection to the service first


        assertTrue(ServiceHooks.hookContentResolver(new BinderListener() {
            @Override
            protected boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                return originalBinder.transact(code, data, reply, flags);
            }
        }));
    }

    @Test
    public void hookNotificationManager() throws Exception {
        NotificationManager notificationManager = (NotificationManager) InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll(); // let android establish a connection to the service first


        assertTrue(ServiceHooks.hookNotificationManager(new BinderListener() {
            @Override
            protected boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                return originalBinder.transact(code, data, reply, flags);
            }
        }));
    }

    @Test
    public void hookActivityManager() throws Exception {
        assertTrue(ServiceHooks.hookActivityManager(new BinderListener() {
            @Override
            protected boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                return originalBinder.transact(code, data, reply, flags);
            }
        }));
    }

    @Test
    public void hookAppOpsManager() throws Exception {
        AppOpsManager appOpsManager = (AppOpsManager) InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(Context.APP_OPS_SERVICE);
        appOpsManager.checkPackage(Os.getuid(), InstrumentationRegistry.getInstrumentation().getContext().getPackageName());  // let android establish a connection to the service first


        assertTrue(ServiceHooks.hookAppOpsManager(appOpsManager, new BinderListener() {
            @Override
            protected boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                return originalBinder.transact(code, data, reply, flags);
            }
        }));
    }

    @Test
    public void hookPackageManager() throws Exception {
        assertTrue(ServiceHooks.hookPackageManager(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageManager(), new BinderListener() {
            @Override
            protected boolean transact(@NonNull IBinder originalBinder, int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                return originalBinder.transact(code, data, reply, flags);
            }
        }));
    }
}