package cn.nekocode.toolbox.utils;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Nekocode on 2014/11/24 0024.
 */
public class KryoUtils {
    private static Kryo kryo = new Kryo();

    public static void save(Context context, String fileName, Object obj) {
        try {
            FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            Output output = new Output(fo);

            kryo.writeObject(output, obj);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object load(Context context, String fileName, Class c) {
        Object rlt = null;
        try {
            FileInputStream fi = context.openFileInput(fileName);
            Input input = new Input(fi);
            rlt = kryo.readObject(input, c);
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rlt;
    }

    public static void delete(Context context, String fileName) {
        context.deleteFile(fileName);
    }
}
