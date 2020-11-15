package com.example.tjarnav.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.example.tjarnav.R;

import java.util.List;

/**
 * 输入提示adapter，展示item名称和地址
 * Created by ligen on 16/11/25.
 */
public class InputTipsAdapter extends RecyclerView.Adapter<InputTipsAdapter.MyViewHolder> {
    private Context mContext;
    private List<Tip> mListTips;
    private OnItemClickListener mOnItemClickListener;

    public InputTipsAdapter(Context context, List<Tip> tipList) {
        mContext = context;
        mListTips = tipList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(
                LayoutInflater.from(mContext).
                        inflate(R.layout.adapter_inputtips,
                                parent,
                                false));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(view);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mName.setText(mListTips.get(position).getName());

        String address = mListTips.get(position).getAddress();
        Log.d("hgy", "onBindViewHolder: +" + mListTips.get(position).getName() + "," + mListTips.get(position).getAdcode());

        if (address == null || address.equals("")) {
            holder.mAddress.setVisibility(View.GONE);
        } else {
            holder.mAddress.setVisibility(View.VISIBLE);
            holder.mAddress.setText(address);
        }
    }

    @Override
    public int getItemCount() {
        return mListTips.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mAddress;

        public MyViewHolder(@NonNull View view) {
            super(view);
            mName = view.findViewById(R.id.name);
            mAddress = view.findViewById(R.id.adress);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}