package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 6/8/16.
 */
public class AdapterChatHistory extends BaseAdapter{

    private LayoutInflater mInflater;
    private List<ChatMessage> mListChat;

    public AdapterChatHistory(Activity activity, List<ChatMessage> chatMessageList) {
        mInflater = LayoutInflater.from(activity);
        mListChat = chatMessageList;
    }

    @Override
    public int getCount() {
        if(mListChat != null) {
            return mListChat.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mListChat != null) {
            return mListChat.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_chat_history, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
            viewHolder.tvMsg = (TextView)convertView.findViewById(R.id.tv_msg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ChatMessage chatMessage = mListChat.get(position);
        String name = chatMessage.getDeviceName();
        if(name.charAt(0) == '$') {
            viewHolder.tvName.setText("Me:");
            viewHolder.tvName.setGravity(Gravity.RIGHT);
            viewHolder.tvMsg.setGravity(Gravity.RIGHT);
        } else {
            viewHolder.tvName.setText(name+":");
            viewHolder.tvName.setGravity(Gravity.LEFT);
            viewHolder.tvMsg.setGravity(Gravity.LEFT);
        }
        viewHolder.tvMsg.setText(chatMessage.getMessage());
        return convertView;
    }

    public void addMessage(ChatMessage chatMessage) {
        if(mListChat == null) {
            mListChat = new ArrayList<>();
        }
        mListChat.add(chatMessage);
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView tvName;
        TextView tvMsg;
    }
}
