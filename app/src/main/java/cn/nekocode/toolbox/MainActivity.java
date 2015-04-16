package cn.nekocode.toolbox;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.feedback.FeedbackAgent;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;

import org.askerov.dynamicgrid.DynamicGridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nekocode.toolbox.adapters.ToolAdapter;
import cn.nekocode.toolbox.beans.ToolBean;
import cn.nekocode.toolbox.utils.KryoUtils;
import cn.nekocode.toolbox.utils.PxUtils;


public class MainActivity extends ActionBarActivity implements BaseSliderView.OnSliderClickListener {
    @InjectView(R.id.toolbar)
    public Toolbar toolbar;
    @InjectView(R.id.slider)
    SliderLayout slider;
    @InjectView(R.id.custom_indicator)
    PagerIndicator customIndicator;
    @InjectView(R.id.dynamic_grid)
    DynamicGridView dynamicGrid;
    @InjectView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private ToolAdapter adapter;
    private ArrayList<ToolBean> toolBeans;
    private String sliderImageUrls[];
    private String sliderUrls[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        ImageView titleView = new ImageView(this);
        titleView.setImageResource(R.drawable.title);
        titleView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

//        TextView titleView = new TextView(this);
//        titleView.setText("工具箱");
//        titleView.setTextSize(16);
//        titleView.setGravity(Gravity.CENTER);
        titleView.setLayoutParams(new Toolbar.LayoutParams(
                PxUtils.dip2px(this, 100),
                PxUtils.dip2px(this, 50),
                Gravity.CENTER));
        toolbar.addView(titleView);

        setupPush();
        setupSlider();
        setupGrid();

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.main);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout != null)
                            swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
    }

    private void setupPush() {
        PushService.setDefaultPushCallback(this, ActionBarActivity.class);
    }

    private void setupSlider() {
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(3000);

        AVQuery query = AVQuery.getQuery("Slider");
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, AVException e) {
            }

            @Override
            protected void internalDone0(Object o, AVException e) {
                List<AVObject> avObjects = (List<AVObject>) o;
                sliderImageUrls = new String[avObjects.size()];
                sliderUrls = new String[sliderImageUrls.length];
                for (int i = 0; i < sliderImageUrls.length; i++) {
                    sliderImageUrls[i] = avObjects.get(i).getAVFile("image").getUrl();
                    sliderUrls[i] = avObjects.get(i).getString("redirect_url");
                }

                for (int i=0; i<sliderImageUrls.length; i++) {
                    DefaultSliderView sliderView = new DefaultSliderView(MainActivity.this);
                    sliderView.image(sliderImageUrls[i])
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(MainActivity.this)
                            .getBundle().putString("url", sliderUrls[i]);

                    slider.addSlider(sliderView);
                }
            }
        });
    }

    private void setupGrid() {
        toolBeans = (ArrayList<ToolBean>) KryoUtils.load(this, "toolBeans", ArrayList.class);
        if (toolBeans == null) {
            toolBeans = new ArrayList<>();
            AVQuery query = AVQuery.getQuery("URLItem");
            query.orderByAscending("sort_id");
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List list, AVException e) {
                }

                @Override
                protected void internalDone0(Object o, AVException e) {
                    List<AVObject> avObjects = (List<AVObject>) o;
                    for (AVObject object : avObjects) {
                        int sortId = object.getInt("sort_id");
                        if(sortId < 11 || sortId == 1000) {
                            toolBeans.add(new ToolBean(object.getObjectId(),
                                    object.getAVFile("image").getUrl(), object.getString("title"), object.getString("url"), ""));
                        }
                    }


                    adapter.set(toolBeans);
                    KryoUtils.save(MainActivity.this, "toolBeans", toolBeans);
                }
            });
        }

        adapter = new ToolAdapter(this, toolBeans, 4);
        dynamicGrid.setAdapter(adapter);
        dynamicGrid.setOnEditModeChangeListener(new DynamicGridView.OnEditModeChangeListener() {
            @Override
            public void onEditModeChanged(boolean inEditMode) {
                if (inEditMode) {
                    swipeRefreshLayout.setEnabled(false);
                    adapter.setIsInEdit(true);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                    adapter.setIsInEdit(false);

                    toolBeans.clear();
                    List<Object> list = adapter.getItems();
                    for(Object object : list) {
                        toolBeans.add((ToolBean) object);
                    }

                    KryoUtils.save(MainActivity.this, "toolBeans", toolBeans);
                }
            }
        });

        dynamicGrid.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
            }
        });
        dynamicGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                List list = adapter.getItems();
                if(position < list.size()) {
                    dynamicGrid.startEditMode(position);
                }
                return true;
            }
        });

        dynamicGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List list = adapter.getItems();
                if(position < list.size()) {
                    ToolBean toolBean = (ToolBean) list.get(position);

                    if(!toolBean.getLinkUrl().equals("SegueToFeedbackController")) {
                        WebViewerActivity.start(MainActivity.this, toolBean.getLinkUrl(), "打开工具:" + toolBean.getTitle());
                    } else {
                        FeedbackAgent agent = new FeedbackAgent(MainActivity.this);
                        agent.startDefaultThreadActivity();
                        AVAnalytics.onEvent(MainActivity.this, "打开工具:反馈");
                    }
                } else {
                    AddToolsActivity.start(MainActivity.this, toolBeans, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                toolBeans = data.getParcelableArrayListExtra("tools");
                adapter.set(toolBeans);
                KryoUtils.save(MainActivity.this, "toolBeans", toolBeans);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (dynamicGrid.isEditMode()) {
            dynamicGrid.stopEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        KryoUtils.save(MainActivity.this, "toolBeans", toolBeans);

        super.onDestroy();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        String url = baseSliderView.getBundle().getString("url");
        WebViewerActivity.start(this, url, "点击banner:" + url);
    }
}
