package cn.nekocode.toolbox.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nekocode.toolbox.R;
import cn.nekocode.toolbox.beans.ToolBean;
import cn.nekocode.toolbox.beans.TypeBean;
import cn.nekocode.toolbox.utils.PxUtils;

public class ToolToAddAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM_TYPE = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private ArrayList<Object> items;
    private ArrayList<ToolBean> userToolBeans;
    private final LayoutInflater layoutInflater;

    public ToolToAddAdapter(Context context, ArrayList<Object> items, ArrayList<ToolBean> userToolBeans) {
        this.context = context;
        this.items = items;
        this.userToolBeans = userToolBeans;

        layoutInflater = LayoutInflater.from(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = layoutInflater.inflate(R.layout.row_tool_to_add, parent, false);
            return new ItemViewHolder(v);
        }
        if (viewType == TYPE_ITEM_TYPE) {
            View v = layoutInflater.inflate(R.layout.row_tool_to_add_type, parent, false);
            return new ItemTypeViewHolder(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder vh = (ItemViewHolder) holder;
            final ToolBean toolBean = (ToolBean) getItem(position);

            Picasso.with(context)
                    .load(toolBean.getIconUrl())
                    .centerCrop()
                    .resize(PxUtils.dip2px(context, 50),
                            PxUtils.dip2px(context, 50))
                    .error(android.R.drawable.stat_notify_error)
                    .into(vh.imageView);
            vh.textView.setText(toolBean.getTitle());

            boolean find = false;
            for(ToolBean userToolBean :userToolBeans) {
                if(userToolBean.getObjectId().equals(toolBean.getObjectId())) {
                    find = true;
                    break;
                }
            }

            if(find) {
                vh.button.setText("已添加");
                vh.button.setEnabled(false);
            } else {
                vh.button.setText("添加");
                vh.button.setEnabled(true);
                vh.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToolBean toolBeanToAdd = new ToolBean(toolBean);
                        userToolBeans.add(toolBeanToAdd);

                        ((Button)v).setEnabled(false);
                        ((Button)v).setText("已添加");
                    }
                });
            }

        } else if (holder instanceof ItemTypeViewHolder) {
            ItemTypeViewHolder vh = (ItemTypeViewHolder) holder;
            TypeBean typeBean = (TypeBean) getItem(position);

            vh.textView.setText(typeBean.getName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof TypeBean ? TYPE_ITEM_TYPE : TYPE_ITEM;
    }

    private Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.imageView)
        ImageView imageView;
        @InjectView(R.id.textView)
        TextView textView;
        @InjectView(R.id.button)
        TextView button;

        ItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }


    static class ItemTypeViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.textView)
        TextView textView;

        ItemTypeViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
