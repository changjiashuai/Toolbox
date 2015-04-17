package cn.nekocode.toolbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.ViewGroup;

import cn.nekocode.toolbox.utils.PxUtils;

public class SettingActivity extends PreferenceActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        int padding = PxUtils.dip2px(this, 16);
//        ViewGroup viewGroup = (ViewGroup) this.getWindow().getDecorView();
//        viewGroup.getChildAt(0).setPadding(0, padding, 0, padding);

        addPreferencesFromResource(R.xml.setting);
    }
}
