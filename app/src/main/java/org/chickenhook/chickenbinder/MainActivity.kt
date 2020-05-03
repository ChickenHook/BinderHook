package org.chickenhook.chickenbinder

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.chickenhook.binderhooks.Logger.log
import org.chickenhook.binderhooks.OnBinderListener
import org.chickenhook.binderhooks.ProxyHook
import org.chickenhook.binderhooks.ServiceHooks
import org.chickenhook.restrictionbypass.helpers.Reflection.getReflective
import java.io.FileDescriptor
import java.io.PrintWriter


class MainActivity : AppCompatActivity() {

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
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    1001
                )
//                val urlString = "http://google.de"
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                intent.setPackage("com.android.chrome")

//                val intent = Intent("android.content.pm.action.REQUEST_PERMISSIONS");
//                val permissions = arrayOf(Manifest.permission.READ_CONTACTS)
//                intent.putExtra("android.content.pm.extra.REQUEST_PERMISSIONS_NAMES", permissions);
//                intent.setPackage("com.google.android.permissioncontroller");
//                try {
//                    startActivityForResult(intent, 1001)
//                } catch (exception: Exception) {
//                    log("Error while start activity", exception)
//                }
            }
        }
    }

    class MyBinder : Binder() {
        override fun getInterfaceDescriptor(): String? {
            Log.d("MyBinder", "!!!!!!!!!!! getInterfaceDescriptor     !!!!!!!!")
            return super.getInterfaceDescriptor()
        }

        override fun isBinderAlive(): Boolean {
            Log.d("MyBinder", "!!!!!!!!!!! isBinderAlive     !!!!!!!!")
            return super.isBinderAlive()
        }

        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
            Log.d("MyBinder", "!!!!!!!!!!! linkToDeath     !!!!!!!!")
            super.linkToDeath(recipient, flags)
        }

        override fun queryLocalInterface(descriptor: String): IInterface? {
            Log.d("MyBinder", "!!!!!!!!!!! queryLocalInterface     !!!!!!!!")
            return super.queryLocalInterface(descriptor)
        }

        override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) {
            Log.d("MyBinder", "!!!!!!!!!!! dumpAsync     !!!!!!!!")
            super.dumpAsync(fd, args)
        }

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            Log.d("MyBinder", "!!!!!!!!!!! onTransact     !!!!!!!!")
            return super.onTransact(code, data, reply, flags)
        }

        override fun dump(fd: FileDescriptor, args: Array<out String>?) {
            Log.d("MyBinder", "!!!!!!!!!!! dump     !!!!!!!!")
            super.dump(fd, args)
        }

        override fun dump(fd: FileDescriptor, fout: PrintWriter, args: Array<out String>?) {
            Log.d("MyBinder", "!!!!!!!!!!! dump     !!!!!!!!")
            super.dump(fd, fout, args)
        }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
            Log.d("MyBinder", "!!!!!!!!!!! unlinkToDeath     !!!!!!!!")
            return super.unlinkToDeath(recipient, flags)
        }

        override fun pingBinder(): Boolean {
            Log.d("MyBinder", "!!!!!!!!!!! pingBinder     !!!!!!!!")
            return super.pingBinder()
        }

        override fun attachInterface(owner: IInterface?, descriptor: String?) {
            super.attachInterface(owner, descriptor)
        }
    }

    fun startActivityHook(
        originalBinder: IBinder,
        code: Int,
        data: Parcel,
        reply: Parcel?,
        flags: Int, DESCRIPTOR: String
    ): Boolean {
        Log.d("MainActivity", "Manipulate start activity")
        val _data = Parcel.obtain()
        data.setDataPosition(0)
        data.writeInterfaceToken(DESCRIPTOR); // validation
        _data.writeInterfaceToken(DESCRIPTOR)
        val caller = data.readStrongBinder()
        _data.writeStrongBinder(caller)
        val callingPackage = data.readString()
        _data.writeString(callingPackage) // manipulate calling package
        //_data.writeString("com.android.chrome") // manipulate calling package
        if (Build.VERSION.CODENAME == "R") {
            val unknown = data.readString()
            _data.writeString(unknown)
        }
        val hasIntent = data.readInt()
        if (hasIntent == 0) {
            // no intent
            _data.writeInt(0)
            originalBinder.transact(code, data, reply, flags)

        } else {
            // has intent
            _data.writeInt(1)
            val intent = Intent()
            intent.readFromParcel(data)

            //val newIntent = createTrampolineIntent(loaderContext, intent)
            intent.writeToParcel(_data, 0)
        }
        _data.writeString(data.readString()); // resolved type
        val strongBinder = data.readStrongBinder()
        Log.d("MainActivity", "BinderProxy $strongBinder")
        _data.writeStrongBinder(
            strongBinder
        ); // result to
//        _data.writeStrongBinder(strongBinder); // result to
        val resultWho = data.readString()
        _data.writeString(resultWho); // result who
//        _data.writeString(resultWho);
        _data.writeInt(data.readInt()); // request code
        _data.writeInt(
            data.readInt()

//                    or (getReflective(
//                null,
//                ActivityManager::class.java,
//                "START_FLAG_DEBUG"
//            )!!)

//                    or (getReflective(
//                null,
//                ActivityManager::class.java,
//                "START_FLAG_TRACK_ALLOCATION"
//            )!!)
//
//                    or (getReflective(
//                null,
//                ActivityManager::class.java,
//                "START_FLAG_NATIVE_DEBUGGING"
//            )!!)


        ); // flags
        val hasProfileInfo = _data.readInt()
        if (hasProfileInfo == 0) {
            _data.writeInt(0)
        } else {
            _data.writeInt(1) //TODO not supported in hooks :( PRAY!!
            getReflective<Any>(
                null,
                Class.forName("android.app.ProfilerInfo"),
                "CREATOR"
            )
                ?.let {
                    val creator = it::class.java.getDeclaredMethod(
                        "createFromParcel",
                        Parcel::class.java
                    )
                    val profilerInfo = creator(data)
                    _data.writeInt(0) //TODO not supported in hooks :( PRAY!!
                } ?: run {
                throw RuntimeException("Unable to read profile Info... Abort")
            }
        }
        val hasOptions = data.readInt()
        if (hasOptions == 0) {
            _data.writeInt(0)
        } else {
            _data.writeInt(1)
            val options = data.readBundle()
            _data.writeBundle(options)
        }

        log("BinderHook [+] send manipulated startActivity() call")
        return originalBinder.transact(code, _data, reply, flags)
    }


    private fun addHooks() {
        ProxyHook.VERBOSE = true
        Log.d("MainActivity", "Add hooks")
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
    }
}
