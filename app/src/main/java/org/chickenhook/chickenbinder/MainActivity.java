package org.chickenhook.chickenbinder;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.chickenhook.binderhooks.ServiceHooks;
import org.chickenhook.binderhooks.proxyListeners.ProxyListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button packageManagerTestButton = findViewById(R.id.packageManagerTest);
        Button permissionTestButton = findViewById(R.id.permissionTest);
        Button windowManagerTestButton = findViewById(R.id.windowManagerTest);
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{
                        Manifest.permission.READ_CALENDAR
                },
                1001
        );
        permissionTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{
                                Manifest.permission.READ_CONTACTS, READ_EXTERNAL_STORAGE
                        },
                        1001
                );
            }
        });

        try {
            hook();
        } catch (Exception e) {
            Log.e("MainActivity", "Error while install hooks", e);
        }
    }

    private static final int START_FLAG_NATIVE_DEBUGGING = 1 << 1;
    private static final int START_FLAG_DEBUG = 1 << 1;
    private static final int START_FLAG_TRACK_ALLOCATION = 1 << 2;

    private void hook() throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
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


        ServiceHooks.hookActivityTaskManager(new ProxyListener() {
            @Override
            public Object invoke(Object orig, Object proxy, Method method, Object[] args) throws Throwable {
                Log.d("MainActivity", "hookActivityTaskManager [+] finally gor method call -" + method + "- on objact -" + orig + "-");

                if (method.getName().equals("startActivity")) {
                    args[args.length - 3] = ((int) args[args.length - 3]) |
                            START_FLAG_DEBUG |
                            START_FLAG_TRACK_ALLOCATION |
                            START_FLAG_NATIVE_DEBUGGING;
                }
                return method.invoke(orig, args);
            }
        });
    }
}
