package org.chickenhook.binderhooks;

import android.os.Parcel;

import androidx.annotation.NonNull;

import static org.chickenhook.restrictionbypass.helpers.Reflection.getReflective;

public class ParcelEditor {

    static long getNativePtr(@NonNull Parcel parcel) throws NoSuchFieldException, IllegalAccessException {
        return getReflective(parcel, "mNativePtr");
    }

    public static void dump(@NonNull Parcel parcel) throws NoSuchFieldException, IllegalAccessException {
        long parcel_pointer = getNativePtr(parcel);
        dump(parcel_pointer, parcel.dataSize());
    }

    public static native void dump(long addr, int size);

    public static byte[] read(@NonNull Parcel parcel, int offset, int size) throws NoSuchFieldException, IllegalAccessException {
        long parcel_pointer = getNativePtr(parcel);
        return read(parcel_pointer, offset, size);
    }

    public static native byte[] read(long addr, int offset, int size);

    public static void write(@NonNull Parcel parcel, int offset, @NonNull byte[] data) throws NoSuchFieldException, IllegalAccessException {
        long parcel_pointer = getNativePtr(parcel);
        write(parcel_pointer, offset, data);
    }

    public static native void write(long addr, int offset, @NonNull byte[] data);

    static {
        System.loadLibrary("chickenbinder");
    }
}
