package cn.nekocode.toolbox;

import java.net.MalformedURLException;
import java.net.URL;

import com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import cn.nekocode.toolbox.utils.NinePatchUtils;
import cn.nekocode.toolbox.utils.X5WebViewScrollbarDrawable;
import cn.nekocode.toolbox.widgets.X5WebView;

public class WebViewerActivity extends Activity {
    private X5WebView mWebView;
    private ViewGroup mViewParent;
    private ImageButton mBack;
    private ImageButton mForward;
    private ImageButton mRefresh;
    private ImageButton mExit;
    private ImageButton mShare;
    private static final String mHomeUrl = "http://app.html5.qq.com/navi/index";

    private static final String TAG = "SdkDemo";
    private static final int MAX_LENGTH = 14;

    private URL mIntentUrl;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, WebViewerActivity.class);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFormat(PixelFormat.RGBA_8888);

        Log.e("MainActivity", "QQBrowserSDK core version is " + WebView.getQQBrowserCoreVersion(this));

        //if (WebView.getQQBrowserCoreVersion(this) == 0) QbSdk.forceSysWebView();
        Intent intent = getIntent();
        if (intent != null) {
            try {
                mIntentUrl = new URL(intent.getData().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 在条件满足时开启硬件加速
        try {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_web_viewer);
        mViewParent = (ViewGroup) findViewById(R.id.webView1);
        init();
        initBtnListenser();
    }

    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    protected FrameLayout mFullscreenContainer;
    private boolean mIsH5VideoFullScreen = false;

    private void init() {
        // ========================================================
        // 创建WebView
        mWebView = new X5WebView(this);
        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

        // 设置Client
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                resetBackAndForward();
            }

            //@Override
            //public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String captureType)
            //{
            //	MainActivity.this.uploadFile = uploadFile;
            //	Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            //	i.addCategory(Intent.CATEGORY_OPENABLE);
            //	i.setType("*/*");
            //	MainActivity.this.startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.choose_uploadfile)), 0);
            //}

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomViewCallback = callback;

                mOriginalOrientation = WebViewerActivity.this.getRequestedOrientation();
                FrameLayout decor = (FrameLayout) WebViewerActivity.this.getWindow().getDecorView();
                mFullscreenContainer = new FullscreenHolder(WebViewerActivity.this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                mFullscreenContainer.addView(view, lp);
                decor.addView(mFullscreenContainer, lp);
                mCustomView = view;
                //				FullScreenManager.getInstance().request(null, FullScreenManager.VIDEO_FULLSCREEN_REQUEST);
                WebViewerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                WebViewerActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                WebViewerActivity.this.getApplicationContext().startActivity(intent);
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) {
                    return;
                }

                FrameLayout decor = (FrameLayout) WebViewerActivity.this.getWindow().getDecorView();
                decor.removeView(mFullscreenContainer);
                mFullscreenContainer.removeAllViews();
                mFullscreenContainer = null;
                WebViewerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mCustomView = null;
                WebViewerActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mCustomViewCallback.onCustomViewHidden();
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String arg0, String arg1, String arg2, String arg3, long arg4) {
                Log.d(TAG, "url: " + arg0);
                new AlertDialog.Builder(WebViewerActivity.this).setTitle("是否下载").setPositiveButton("yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(WebViewerActivity.this, "fake message: i'll download...", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("no", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Toast.makeText(WebViewerActivity.this, "fake message: refuse download...", Toast.LENGTH_SHORT).show();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        Toast.makeText(WebViewerActivity.this, "fake message: refuse download...", Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
        // 各种设置
        if (mWebView.getX5WebViewExtension() != null) {
            Log.e("robins", "CoreVersion_FromSDK::" + mWebView.getX5WebViewExtension().getQQBrowserVersion());
            mWebView.getX5WebViewExtension().setWebViewClientExtension(new ProxyWebViewClientExtension() {
                @Override
                public Object onMiscCallBack(String method, Bundle bundle) {
                    if (method == "onSecurityLevelGot") {
                        Toast.makeText(WebViewerActivity.this, "Security Level Check: \nit's level is " + bundle.getInt("level"), Toast.LENGTH_SHORT).show();
                    }
                    return null;
                }
            });
        } else {
            Log.e("robins", "CoreVersion");
        }
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", Context.MODE_PRIVATE).getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        //最重要的方法，一定要设置，这就是出不来的主要原因
        webSetting.setDomStorageEnabled(true);

        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mWebView.loadUrl(mHomeUrl);
        } else {
            mWebView.loadUrl(mIntentUrl.toString());
        }
        Log.d("time-cost", "cost time: " + (System.currentTimeMillis() - time));
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
    }

    private void resetBackAndForward() {
        if (mWebView != null && mWebView.canGoBack()) {
            mBack.setImageResource(R.drawable.ic_left);
            mBack.setEnabled(true);
        } else {
            mBack.setImageResource(R.drawable.ic_left_disable);
            mBack.setEnabled(false);
        }

        if (mWebView != null && mWebView.canGoForward()) {
            mForward.setImageResource(R.drawable.ic_right);
            mForward.setEnabled(true);
        } else {
            mForward.setImageResource(R.drawable.ic_right_disable);
            mForward.setEnabled(false);
        }
    }

    private void initBtnListenser() {
        mBack = (ImageButton) findViewById(R.id.btnBack);
        mForward = (ImageButton) findViewById(R.id.btnForward);
        mRefresh = (ImageButton) findViewById(R.id.btnRefresh);
        mExit = (ImageButton) findViewById(R.id.btnExit);
        mShare = (ImageButton) findViewById(R.id.btnHome);

        mBack.setImageResource(R.drawable.ic_left_disable);
        mBack.setEnabled(false);
        mBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                    resetBackAndForward();
                }
            }
        });

        mForward.setImageResource(R.drawable.ic_right_disable);
        mForward.setEnabled(false);
        mForward.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mWebView != null && mWebView.canGoForward()) {
                    mWebView.goForward();
                    resetBackAndForward();
                }
            }
        });

        mRefresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mWebView != null)
                    mWebView.reload();
            }
        });

        mExit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mWebView.getX5WebViewExtension() != null)
                    mWebView.getX5WebViewExtension().onAppExit();

                if(mWebView.getX5WebViewExtension() == null) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    WebViewerActivity.this.finish();
                }

            }
        });

        mShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    private enum TEST_ENUM_INDEX {
        CONST_IDX_SOLAR,
        CONST_IDX_PAGE_SOLAR,
        CONST_IDX_NO_PIC,
        CONST_IDX_PAGE_PC_2_PHONE,
        CONST_IDX_FIT_FIRST,
        CONST_IDX_PRE_READ,
        CONST_IDX_USE_SELF_UI
    }

    ;

    final String[] items = new String[]{"日间模式", "页面入夜间模式", "无图模式", "电脑页转手机版面", "优先访问简版", "预读", "自定义UI"};

    boolean[] m_selected = new boolean[]{true, true, true, true, false, false, true};

    private void testSettings() {
        // AlertDialog.Builder(this);
        new AlertDialog.Builder(WebViewerActivity.this).setTitle("浏览器配置开关")
                .setMultiChoiceItems(items, m_selected, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // TODO Auto-generated method stub
                        m_selected[which] = isChecked;
                    }
                }).setPositiveButton("测试", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (mWebView.getX5WebViewExtension() != null) {
                    //mWebView.getSettingsExtension().setDayOrNight(m_selected[TEST_ENUM_INDEX.CONST_IDX_SOLAR.ordinal()]);
                    mWebView.getSettingsExtension().setPageSolarEnableFlag(m_selected[TEST_ENUM_INDEX.CONST_IDX_PAGE_SOLAR.ordinal()]);
                    mWebView.getSettings().setLoadsImagesAutomatically(!m_selected[TEST_ENUM_INDEX.CONST_IDX_NO_PIC.ordinal()]);
                    mWebView.getSettingsExtension().setPreFectch(m_selected[TEST_ENUM_INDEX.CONST_IDX_PRE_READ.ordinal()]);
                    mWebView.getSettingsExtension().setWapSitePreferred(m_selected[TEST_ENUM_INDEX.CONST_IDX_FIT_FIRST.ordinal()]);
                    mWebView.getSettingsExtension().setFitScreen(m_selected[TEST_ENUM_INDEX.CONST_IDX_PAGE_PC_2_PHONE.ordinal()]);
                    setUIStyle(mWebView, m_selected[TEST_ENUM_INDEX.CONST_IDX_USE_SELF_UI.ordinal()]);
                    // mWebView.getSettingsExtension().setPageSolarEnableFlag(selected[TEST_ENUM_INDEX.CONST_IDX_CLOUND.ordinal()]);
                            /*
							 * mWebView.getSettingsExtension().
							 * setPageSolarEnableFlag
							 * (selected[TEST_ENUM_INDEX.CONST_IDX_FULL_SCREEN
							 * .ordinal()]);
							 * mWebView.getSettingsExtension().
							 * setPageSolarEnableFlag
							 * (selected[TEST_ENUM_INDEX.CONST_IDX_CLOUND
							 * .ordinal()]);
							 * mWebView.getSettingsExtension().
							 * setPageSolarEnableFlag
							 * (selected[TEST_ENUM_INDEX.CONST_IDX_PAGE_PC_2_PHONE
							 * .ordinal()]);
							 * mWebView.getSettingsExtension().
							 * setPageSolarEnableFlag
							 * (selected[TEST_ENUM_INDEX.CONST_IDX_FIT_FIRST
							 * .ordinal()]);
							 * mWebView.getSettingsExtension().
							 * setPageSolarEnableFlag
							 * (selected[TEST_ENUM_INDEX.CONST_IDX_VERTICAL_ONLY
							 * .ordinal()]);
							 */
                    //mWebView.reload();
                }

                mWebView.setDayOrNight(m_selected[TEST_ENUM_INDEX.CONST_IDX_SOLAR.ordinal()]);
                //mWebView.reload();

            }
        }).setNegativeButton("取消", null).show();

    }

    private enum TEST_ENUM_FONTSIZE {
        FONT_SIZE_SMALLEST,
        FONT_SIZE_SMALLER,
        FONT_SIZE_NORMAL,
        FONT_SIZE_LARGER,
        FONT_SIZE_LARGEST
    }

    ;

    private TEST_ENUM_FONTSIZE m_font_index = TEST_ENUM_FONTSIZE.FONT_SIZE_NORMAL;

    private void testSettingsFontSize() {
        final String[] items = new String[]{"超小号", "小号", "中号", "大号", "超大号"};

        // AlertDialog.Builder(this);
        new AlertDialog.Builder(WebViewerActivity.this).setTitle("设置字体大小")
                .setSingleChoiceItems(items, m_font_index.ordinal(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        m_font_index = TEST_ENUM_FONTSIZE.values()[which];
                    }
                }).setPositiveButton("测试", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (m_font_index) {
                    case FONT_SIZE_SMALLEST:
                        mWebView.getSettings().setTextSize(WebSettings.TextSize.SMALLEST);
                        break;
                    case FONT_SIZE_SMALLER:
                        mWebView.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
                        break;
                    case FONT_SIZE_NORMAL:
                        mWebView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
                        break;
                    case FONT_SIZE_LARGER:
                        mWebView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
                        break;
                    case FONT_SIZE_LARGEST:
                        mWebView.getSettings().setTextSize(WebSettings.TextSize.LARGEST);
                        break;
                }
                mWebView.reload();

            }
        }).setNegativeButton("取消", null).show();
    }

    private void setUIStyle(WebView x5Webview, boolean isSelfStyle) {
        if (!isSelfStyle) {
            if (!x5Webview.isDayMode()) {
                Log.e("MainActivity", "Please set different style.");
            }
            Drawable drawable;
            if ((drawable = NinePatchUtils.getDrawableFromDefaultSkin(x5Webview.getContext(), "theme_scrollbar_horizontal_fg_normal.9.png")) != null) {
                x5Webview.getX5WebViewExtension().setHorizontalScrollBarDrawable(new X5WebViewScrollbarDrawable(false, drawable, 0, 6, 100));
            }
            if ((drawable = NinePatchUtils.getDrawableFromDefaultSkin(x5Webview.getContext(), "theme_scrollbar_horizontal_fg_normal.9.png")) != null) {
                x5Webview.getX5WebViewExtension().setVerticalScrollBarDrawable(new X5WebViewScrollbarDrawable(true, drawable, 0, 6, 100));
            }

            if ((drawable = NinePatchUtils.getWepbFromDefaultSkin(x5Webview.getContext(), "fast_scroller.webp")) != null) {
                x5Webview.getX5WebViewExtension().setVerticalTrackDrawable(drawable);
            }
            x5Webview.getX5WebViewExtension().setScrollBarDefaultDelayBeforeFade(1000);
        } else {

            if (!x5Webview.isDayMode()) {
                Log.e("MainActivity", "Please set different style.");
            }
            Drawable drawable;
            if ((drawable = NinePatchUtils.getDrawableFromDefaultSkin(x5Webview.getContext(), "theme_scrollbar_horizontal_fg_normal_self.9.png")) != null) {
                x5Webview.getX5WebViewExtension().setHorizontalScrollBarDrawable(new X5WebViewScrollbarDrawable(false, drawable, 0, 6, 100));
            }
            if ((drawable = NinePatchUtils.getDrawableFromDefaultSkin(x5Webview.getContext(), "theme_scrollbar_horizontal_fg_normal_self.9.png")) != null) {
                x5Webview.getX5WebViewExtension().setVerticalScrollBarDrawable(new X5WebViewScrollbarDrawable(true, drawable, 0, 6, 100));
            }

            if ((drawable = NinePatchUtils.getWepbFromDefaultSkin(x5Webview.getContext(), "fast_scroller_self.webp")) != null) {
                x5Webview.getX5WebViewExtension().setVerticalTrackDrawable(drawable);
            }
            x5Webview.getX5WebViewExtension().setScrollBarDefaultDelayBeforeFade(1000);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK  && event.getRepeatCount() == 0) {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        if (intent == null || mWebView == null || intent.getData() == null)
            return;
        mWebView.loadUrl(intent.getData().toString());
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null)
            mWebView.destroy();
        super.onDestroy();
    }

    private class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context context) {
            super(context);
            setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
        }

    }

}
