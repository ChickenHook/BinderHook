# BinderHook
Library intended to hook Binder interface and manipulate events

# Table of Contents
* [Description](#description)
* [Integration](#integration)
* [Usage](#usage)
<br>     => [Android Services](#android-services)
<br>     => [Interface Hooking](#interface-hooking)
<br>     => [Verbose mode](#verbose-mode)
<br>     => [Parcel](#parcel)
<br>     => [Parcel](#parcel)
* [Troubleshooting](#troubleshooting)
* [Other projects](#other-projects)

# <a name="description"/> Description

This library helps to hook binder communication between your App process and system services of Android.

# <a name="integration"/> Integration
Just include the maven repository

1) In your root build.gradle:
```groovy
allprojects {
        repositories {
            [..]
            jcenter()
            maven { url "https://jitpack.io" }
        }
   }
```
2) In your library/build.gradle add:
```groovy
   dependencies {
        implementation 'com.github.ChickenHook:BinderHook:3.0'
        implementation 'com.github.ChickenHook:RestrictionBypass:2.2'
   }
```
# <a name="usage"/> Usage

This chapter showcases how to use this hooking library. Also please have a look at [MainActivity.kt](app/src/main/java/org/chickenhook/chickenbinder/MainActivity.kt), [MainActivityTest.kt](androidTest/src/main/java/org/chickenhook/chickenbinder/MainActivityTest.kt) and [ServiceHooksTest.java](binderhooks/src/androidTest/java/org/chickenhook/binderhooks/ServiceHooksTest.java)

## <a name="android-services"/>  Android Services

### Package Manager

Please check [AIDL Implementation](https://cs.android.com/android/platform/superproject/+/master:out/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/xref28/srcjars.xref/frameworks/base/core/java/android/content/pm/IPackageManager.java;bpv=1;bpt=0)
in order to learn parcel encode / decode instructions


```kt
        ServiceHooks.hookPackageManager(packageManager, object :
            OnBinderListener() {

            val DESCRIPTOR = "android.content.pm.IPackageManager"

            override fun transact(
                originalBinder: IBinder,
                code: Int,
                data: Parcel,
                reply: Parcel?,
                flags: Int
            ): Boolean {

                if (code == 193) { // setHarmfulAppWarning
                    log("Manipulate setHarmfulAppWarning")
                    data.setDataPosition(0)
                    val _data = Parcel.obtain()
                    _data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInterfaceToken(DESCRIPTOR)
                    val packageName = data.readString()
                    _data.writeString(packageName)

                    var message: CharSequence?
                    if (data.readInt() != 0) {
                        message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data)
                        _data.writeInt(1)
                        TextUtils.writeToParcel(message, _data, 0)
                    } else {
                        _data.writeInt(0)
                        message = null
                    }

                    val _flags = data.readInt()
                    _data.writeInt(1)
                    return originalBinder.transact(code, _data, reply, Binder.FLAG_ONEWAY)

                }

                if (code == 56) { // get installed packages


                    // try to manipulate this call
                    val _data = Parcel.obtain()
                    _data.writeInterfaceToken(DESCRIPTOR)
                    _data.writeInt(0)
                    _data.writeInt(0) // simulate system user?
                    return originalBinder.transact(code, _data, reply, flags)
                }

                return originalBinder.transact(code, data, reply, flags)
            }
        })
```

### ActivityManager

Please check [AIDL Implementation](https://cs.android.com/android/platform/superproject/+/master:out/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/xref28/srcjars.xref/frameworks/base/core/java/android/app/IActivityManager.java;bpv=1;bpt=0)
in order to learn parcel encode / decode instructions

```kt
        ServiceHooks.hookActivityManager(object :
            OnBinderListener() {
            val DESCRIPTOR = "android.app.IActivityManager"
            override fun transact(
                originalBinder: IBinder,
                code: Int,
                data: Parcel,
                reply: Parcel?,
                flags: Int
            ): Boolean {
                if (code == 6) {// start activity
                    return startActivityHook(
                        originalBinder,
                        code,
                        data,
                        reply,
                        flags,
                        DESCRIPTOR
                    )
                }
                return originalBinder.transact(code, data, reply, flags)
            }
        })
```

### ActivityTaskManager

Please check [AIDL Implementation](https://cs.android.com/android/platform/superproject/+/master:out/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/xref28/srcjars.xref/frameworks/base/core/java/android/app/IActivityManager.java;bpv=1;bpt=0)
in order to learn parcel encode / decode instructions

```kt
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ServiceHooks.hookActivityTaskManager(object :
                OnBinderListener() {
                val DESCRIPTOR = "android.app.IActivityTaskManager"

                override fun transact(
                    originalBinder: IBinder,
                    code: Int,
                    data: Parcel,
                    reply: Parcel?,
                    flags: Int
                ): Boolean {

                    if (code == 1) { // start activity
                        return startActivityHook(
                            originalBinder,
                            code,
                            data,
                            reply,
                            flags,
                            DESCRIPTOR
                        )
                    }

                    return originalBinder.transact(code, data, reply, flags)
                }
            })
```

### WindowManager

Please check [AIDL Implementation](https://cs.android.com/android/platform/superproject/+/master:out/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/xref28/srcjars.xref/frameworks/base/core/java/android/view/IWindowManager.java;bpv=1;bpt=0)
in order to learn parcel encode / decode instructions

```
        ServiceHooks.hookWindowManager(object :
            OnBinderListener() {
            override fun transact(
                originalBinder: IBinder,
                code: Int,
                data: Parcel,
                reply: Parcel?,
                flags: Int
            ): Boolean {
                return originalBinder.transact(code, data, reply, flags)
            }
        })
```

### Other services

A lot of other service hooks are also present. However some are missing and will come in future releases.

|Service Name|ImplementationStatus|
|---|---|
|ContentResolver|OK|
|NotificationManager|OK|
|ActivityManager|OK|
|AppOpsManager|OK|
|PackageManager|OK|
|WindowManager|OK|
|ActivityTaskManager|OK|
|AlarmManager|WIP|
|WallpaperManager|WIP|
|RoleService|WIP|
|CameraService|WIP|
|PrintService|WIP|
|TrustService|WIP|
|UsageStatsService|WIP|
|...|Feel free to submit feature requests if you need more services|

## <a name="interface-hooking"/> Interface Hooking
If you just wanna change some args and not manipulate Parcel directly you can use ProxyListener:

```
        ServiceHooks.hookActivityManager(new ProxyListener() {
            @Override
            public Object invoke(Object orig, Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("startActivity") && args.length == 10) {
                    args[args.length - 3] = ((int) args[args.length - 3]) |
                            START_FLAG_DEBUG |
                            START_FLAG_TRACK_ALLOCATION |
                            START_FLAG_NATIVE_DEBUGGING;
                }
                return method.invoke(orig, args);
            }
        });
```

## <a name="verbose-mode"/> Enable VERBOSE mode

By adding these lines of code you can enable the verbose mode

```kt
        ProxyHook.VERBOSE = true
```

This will create output like:

```
2020-05-03 22:03:40.309 25785-25785/org.chickenhook.chickenbinder I/BinderHook: ProxyHook [+] trace [+]
    java.lang.Exception: Trace
        at org.chickenhook.binderhooks.ProxyHook.doStackTrace(ProxyHook.java:128)
        at org.chickenhook.binderhooks.ProxyHook$FakeBinder.transact(ProxyHook.java:111)
        at android.content.pm.IPackageManager$Stub$Proxy.setHarmfulAppWarning(IPackageManager.java:8654)
        at android.app.ApplicationPackageManager.setHarmfulAppWarning(ApplicationPackageManager.java:3234)
        at java.lang.reflect.Method.invoke(Native Method)
        at org.chickenhook.chickenbinder.MainActivity$onCreate$$inlined$apply$lambda$1.onClick(MainActivity.kt:40)
        at android.view.View.performClick(View.java:7356)
        at android.view.View.performClickInternal(View.java:7333)
        at android.view.View.access$3600(View.java:807)
        at android.view.View$PerformClick.run(View.java:28200)
        at android.os.Handler.handleCallback(Handler.java:907)
        at android.os.Handler.dispatchMessage(Handler.java:99)
        at android.os.Looper.loop(Looper.java:223)
        at android.app.ActivityThread.main(ActivityThread.java:7476)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:549)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:939)
```

and

```
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/chickenbinder: dump parcel <0x7444767700>
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/chickenbinder: ---------------------------------------------------------------------------------------------------------------------------------------------
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/parcel: HEX (76 bytes):
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/parcel: 04:00:00:C2:FF:FF:FF:FF:1C:00:00:00:61:00:6E:00:64:00:72:00:6F:00:69:00:64:00:2E:00:61:00:70:00:70:00:2E:00:49:00:41:00:63:00:74:00:69:00:76:00:69:00
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/parcel: 74:00:79:00:4D:00:61:00:6E:00:61:00:67:00:65:00:72:00:00:00:00:00:61:13:00:00
2020-05-07 10:44:55.603 4911-4911/org.chickenhook.chickenbinder D/chickenbinder: ---------------------------------------------------------------------------------------------------------------------------------------------
```

for any transaction that went through your installed binder hook.

## <a name="parcel"/> Parcel

In order to work with existing parcels and be able to modify also restricted content like flattened Binder objects (aka writeStrongBinder, readStrongBinder) we added some low level edit functions.

### dump

This function dumps the content of the parcel as hexdump to logcat

```
                    ParcelEditor.dump(parcel);
```

### write

Write a bunch of bites at the given offset

```Java
                    byte[] bytes = new byte[]{
                            1, 2, 3
                    };
                    ParcelEditor.write(parcel, 0, bytes);
```

### read

Read a bunch of bytes from the given offset

```Java
                    byte[] content = ParcelEditor.read(parcel, 0, parcel.dataSize());
```


## <a name="troubleshooting"/> Troubleshooting

Please create a bug report if you find any issues. This chapter will be updated then.


## <a name="other-projects"/> Other Projects

| Project | Description |
|---------|-------------|
| [ChickenHook](https://github.com/ChickenHook/ChickenHook) | A linux / android / MacOS hooking framework  |
| [BinderHook](https://github.com/ChickenHook/BinderHook) | Library intended to hook Binder interface and manipulate events |
| [RestrictionBypass](https://github.com/ChickenHook/RestrictionBypass) |  Android API restriction bypass for all Android Versions |
| [AndroidManifestBypass](https://github.com/ChickenHook/AndroidManifestBypass) |  Android API restriction bypass for all Android Versions |
| .. | |

## Sponsor

If you're happy with my library please order me a cup of coffee ;) Thanks.

