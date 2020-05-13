package org.chickenhook.chickenbinder

import android.Manifest
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.chickenhook.binderhooks.BinderHook
import org.chickenhook.binderhooks.BinderListener
import org.chickenhook.binderhooks.Logger.log
import org.chickenhook.binderhooks.ServiceHooks


class MainActivityKotlin : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addHooks()
        findViewById<Button>(R.id.packageManagerTest)?.apply {
            setOnClickListener {
//                context.packageManager.getInstalledApplications(0)?.forEach { pkg ->
//                    Log.d("MainActivity", "Found package $pkg")
//                    pkg.loadIcon(context.packageManager)
//                }
                val m = packageManager::class.java.getDeclaredMethod(
                    "setHarmfulAppWarning",
                    String::class.java,
                    CharSequence::class.java
                )
                m.isAccessible = true
                m.invoke(packageManager, "com.android.chrome", "Warning this app is dangerous!")
            }
        }

        findViewById<Button>(R.id.permissionTest)?.apply {
            setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@MainActivityKotlin,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    1001
                )
            }
        }

        findViewById<Button>(R.id.windowManagerTest)?.setOnClickListener {
            //pool.execute(WindowBruterTask())
        }
    }


    private fun addHooks() {
        BinderHook.VERBOSE = true
        Log.d("MainActivity", "Add hooks")


        ServiceHooks.hookPackageManager(packageManager, object :
            BinderListener() {

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


                    // https://cs.android.com/android/platform/superproject/+/master:out/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/xref28/srcjars.xref/frameworks/base/core/java/android/content/pm/IPackageManager.java;bpv=1;bpt=0
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

        ServiceHooks.hookNotificationManager(object :
            BinderListener() {
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

        ServiceHooks.hookWindowManager(object :
            BinderListener() {
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
//        ANDROID R
//        Handler().postDelayed(object : Runnable {
//            override fun run() {
//                // session will established later todo use windowManager to determine when session was created
//                ServiceHooks.hookWindowSession(object :
//                    BinderListener() {
//                    override fun transact(
//                        originalBinder: IBinder,
//                        code: Int,
//                        data: Parcel,
//                        reply: Parcel?,
//                        flags: Int
//                    ): Boolean {
//
//                        if (code == 1) {
//                            return hookAddToDisplay(originalBinder, code, data, reply, flags)
//                        }
//
//                        return originalBinder.transact(code, data, reply, flags)
//                    }
//                })
//            }
//
//        }, 1000)

    }
}
