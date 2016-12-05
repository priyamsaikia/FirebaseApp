package com.example.apps.firebase.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.apps.firebase.chat.R;
import com.example.apps.firebase.chat.beans.ChatMessageBean;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    Context mContext;
    ArrayList<ChatMessageBean> mChatMsgList;

    public MessageAdapter(Context context, ArrayList<ChatMessageBean> chatMsgList) {
        mContext = context;
        mChatMsgList = chatMsgList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_message, null);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        ChatMessageBean item = mChatMsgList.get(position);
        holder.tv_item_msg.setText(item.getMsg());
        holder.tv_item_sender_name.setText(item.getSenderName());

        boolean isPhoto = item.getPhotoUrl() != null;
        if (isPhoto) {
            holder.tv_item_msg.setVisibility(View.GONE);
            holder.imv_item_msg.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(item.getPhotoUrl()).into(holder.imv_item_msg);
        } else {
            holder.imv_item_msg.setVisibility(View.GONE);
            holder.tv_item_msg.setVisibility(View.VISIBLE);
            holder.tv_item_msg.setText(item.getMsg());
        }
    }

    @Override
    public int getItemCount() {
        return mChatMsgList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.imv_item_msg)
        ImageView imv_item_msg;

        @Bind(R.id.tv_item_msg)
        TextView tv_item_msg;

        @Bind(R.id.tv_item_sender_name)
        TextView tv_item_sender_name;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}