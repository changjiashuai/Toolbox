package cn.nekocode.toolbox;

import android.app.Application;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

/**
 * Created by nekocode on 2015/4/3 0003.
 */
public class MyApplication extends Application {
    public static MyApplication instant;

    @Override
    public void onCreate() {
        super.onCreate();
        instant = this;

        AVOSCloud.initialize(this, "pdz9fy8nkestylzzmfxt2hisajfr28oj0dexxnhhruiho4sy", "hpz75r4mfjyypcttrwo9zp0qqbze0vz0k13s7g5heylhmnfp");
        AVAnalytics.enableCrashReport(this, true);
    }

    public static MyApplication get() {
        return instant;
    }
}
