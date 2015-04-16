package cn.nekocode.toolbox.utils;

import android.app.Application;
import android.widget.Toast;

import cn.nekocode.toolbox.MyApplication;

/**
 * Created by nekocode on 2015/4/14 0014.
 */
public class T {
    public static void show(String message) {
        Toast.makeText(MyApplication.get().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
