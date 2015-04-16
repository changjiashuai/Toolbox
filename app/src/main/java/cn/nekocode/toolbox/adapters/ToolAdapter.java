package cn.nekocode.toolbox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.askerov.dynamicgrid.BaseDynamicGridAdapter;

import java.util.List;

import cn.nekocode.toolbox.R;
import cn.nekocode.toolbox.beans.ToolBean;
import cn.nekocode.toolbox.utils.PxUtils;

/**
 * Created by nekocode on 2015/4/9 0009.
 */
public class ToolAdapter extends BaseDynamicGridAdapter {
    public ToolAdapter(Context context, List<ToolBean> items, int columnCount) {
        super(context, items, columnCount);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheeseViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tool, null);
            holder = new CheeseViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CheeseViewHolder) convertView.getTag();
        }

        if(position < getItems().size()) {
            ToolBean tool = (ToolBean) getItem(position);
            holder.build(tool, position);
        } else {
            holder.buildPlus();
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return isInEdit ? super.getCount() : super.getCount() + 1;
    }

    private boolean isInEdit = false;
    public void setIsInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        this.notifyDataSetChanged();
    }

    private class CheeseViewHolder {
        private ImageView imageView;
        private ImageView imageView2;
        private TextView textView;

        private int position;

        private CheeseViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.imageView);
            imageView2 = (ImageView) view.findViewById(R.id.imageView2);
            textView = (TextView) view.findViewById(R.id.textView);

            imageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToolAdapter.this.remove(getItem(position));
                }
            });
        }

        void build(ToolBean toolBean, int position) {
            this.position = position;
            Picasso.with(getContext())
                    .load(toolBean.getIconUrl())
                    .centerCrop()
                    .resize(PxUtils.dip2px(getContext(), 60),
                            PxUtils.dip2px(getContext(), 60))
                    .error(android.R.drawable.stat_notify_error)
                    .into(imageView);
            textView.setText(toolBean.getTitle());

            if(isInEdit) {
                imageView2.setVisibility(View.VISIBLE);
            } else {
                imageView2.setVisibility(View.INVISIBLE);
            }
        }

        void buildPlus() {
            imageView.setImageResource(R.drawable.ic_tool_add);
            textView.setText("");

            imageView2.setVisibility(View.INVISIBLE);
        }
    }
}
