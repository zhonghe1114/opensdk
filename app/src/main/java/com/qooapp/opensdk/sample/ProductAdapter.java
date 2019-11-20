package com.qooapp.opensdk.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.qooapp.opensdk.sample.model.Product;

import java.util.List;

/**
 * An adapter to list
 * @author Vito (QooAppSDK)
 * @email devel@qoo-app.com
 */
public class ProductAdapter extends BaseAdapter{

    private Context mContext;
    private List<Product> mDataList;
    public ProductAdapter(Context context, List<Product> dataList){
        this.mContext = context;
        this.mDataList = dataList;
    }
    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_product, null);
            holder.tvName = view.findViewById(R.id.tv_name);
            holder.tvTime = view.findViewById(R.id.tv_time);
            holder.btnConsume = view.findViewById(R.id.btn_consume);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        final Product info = mDataList.get(position);
        if(info.getName() != null) {
            holder.tvName.setText(info.getName());
            holder.tvTime.setText(info.getAmount() + " " + info.getCurrency());
            holder.btnConsume.setVisibility(View.GONE);
        }else{
            holder.tvName.setText(info.getProductId() + "(" + info.getPurchase_id() + ")");
            holder.tvTime.setText("");
            holder.btnConsume.setVisibility(View.VISIBLE);
            holder.btnConsume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).consumePurchase(info.getToken(), info.getPurchase_id());
                }
            });
        }
        return view;
    }

    public void clear(){
        mDataList.clear();
    }

    private class ViewHolder{
        public TextView tvName;
        public TextView tvTime;
        public Button btnConsume;
    }

}
