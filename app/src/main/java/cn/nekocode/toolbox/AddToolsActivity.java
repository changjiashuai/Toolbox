package cn.nekocode.toolbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.FindCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nekocode.toolbox.adapters.ToolToAddAdapter;
import cn.nekocode.toolbox.beans.ToolBean;
import cn.nekocode.toolbox.beans.TypeBean;
import cn.nekocode.toolbox.widgets.DividerItemDecoration;


public class AddToolsActivity extends ActionBarActivity {
    @InjectView(R.id.rv)
    RecyclerView recyclerView;
    @InjectView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<ToolBean> userToolBeans;

    private ArrayList<TypeBean> list;
    private ArrayList<Object> listForAdapter;
    private ToolToAddAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private int needToQuery = 0;

    public static void start(Activity context, ArrayList<ToolBean> list, int requestCode) {
        Intent intent = new Intent(context, AddToolsActivity.class);
        intent.putParcelableArrayListExtra("tools", list);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tools);
        ButterKnife.inject(this);

        userToolBeans = getIntent().getParcelableArrayListExtra("tools");

        Intent data = new Intent();
        data.putParcelableArrayListExtra("tools", userToolBeans);
        setResult(RESULT_OK, data);

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

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, null));

        list = new ArrayList<>();
        listForAdapter = new ArrayList<>();

        adapter = new ToolToAddAdapter(this, listForAdapter, userToolBeans);
        recyclerView.setAdapter(adapter);

        getType();
    }

    private void getType() {
        AVQuery query = AVQuery.getQuery("TagRelation");
        query.orderByAscending("sort_id");
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, AVException e) {
            }

            @Override
            protected void internalDone0(Object o, AVException e) {
                List<AVObject> avObjects = (List<AVObject>) o;
                needToQuery += avObjects.size();

                for (AVObject object : avObjects) {
                    TypeBean typeBean = new TypeBean();
                    typeBean.setObjectId(object.getObjectId());
                    typeBean.setName(object.getString("tag"));

                    ArrayList<ToolBean> toolBeans = new ArrayList<ToolBean>();
                    typeBean.setToolBeans(toolBeans);
                    getItemsFromType(toolBeans, object.getRelation("relative_item"));

                    list.add(typeBean);
                }

                if(needToQuery == 0) {
                    finishQuery();
                }
            }
        });
    }

    private void getItemsFromType(final ArrayList<ToolBean> toolBeans, AVRelation relation) {
        relation.getQuery().findInBackground(new FindCallback() {
            @Override
            public void done(List list, AVException e) {
            }

            @Override
            protected void internalDone0(Object o, AVException e) {
                List<AVObject> avObjects = (List<AVObject>) o;
                for (AVObject object : avObjects)
                    toolBeans.add(new ToolBean(object.getObjectId(),
                            object.getAVFile("image").getUrl(), object.getString("title"), object.getString("url"), ""));

                needToQuery--;
                if(needToQuery == 0) {
                    finishQuery();
                }
            }
        });
    }

    private void finishQuery() {
        for(TypeBean typeBean : list) {
            if(typeBean.getToolBeans().size() == 0)
                continue;

            listForAdapter.add(typeBean);
            listForAdapter.addAll(typeBean.getToolBeans());
        }

        adapter.notifyDataSetChanged();
    }
}
