# BinderHook
Library intended to hook Binder interface and manipulate events

# Description

This library helps to hook binder communication between your App process and system services of Android.

# Integration
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
        implementation 'com.github.ChickenHook:BinderHook:1.0'
        implementation 'com.github.ChickenHook:RestrictionBypass:2.2'
   }
```
# Usage

This chapter showcases how to use this hooking library. Also please have a look at [MainActivity.kt](app/src/main/java/org/chickenhook/chickenbinder/MainActivity.kt), [MainActivityTest.kt](androidTest/src/main/java/org/chickenhook/chickenbinder/MainActivityTest.kt) and [ServiceHooksTest.java](binderhooks/src/androidTest/java/org/chickenhook/binderhooks/ServiceHooksTest.java)

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

### Enable VERBOSE mode

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

for any transaction that went through your installed binder hook. In future we'll also add parcel dump support.

## Troubleshooting

Please create a bug report if you find any issues. This chapter will be updated then.


## Donate

If you're happy with my library please order me a cup of coffee ;) Thanks.

[![Donate with PayPal](https://raw.githubusercontent.com/stefan-niedermann/paypal-donate-button/master/paypal-donate-button.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=8UH5MBVYM3J36)