package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessageHistory;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 6/8/16.
 */
public class AdapterChatHistory extends BaseAdapter{

    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<ChatMessageHistory> mListChat;


    public AdapterChatHistory(Activity activity, List<ChatMessageHistory> chatMessageList) {
        mActivity = activity;
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
        final ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_chat_history, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
            viewHolder.tvMsg = (TextView)convertView.findViewById(R.id.tv_msg);
            viewHolder.llPlay = (LinearLayout) convertView.findViewById(R.id.ll_play);
            viewHolder.ivPlay = (ImageView) convertView.findViewById(R.id.iv_play);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ChatMessageHistory chatMessage = mListChat.get(position);
        String name = chatMessage.getDeviceName();
        if(chatMessage.isSent()) {
            viewHolder.tvName.setGravity(Gravity.RIGHT);
            viewHolder.tvMsg.setGravity(Gravity.RIGHT);
        } else {
            viewHolder.tvName.setGravity(Gravity.LEFT);
            viewHolder.tvMsg.setGravity(Gravity.LEFT);
        }
        viewHolder.tvName.setText(name+":");
        viewHolder.tvMsg.setText(chatMessage.getMessage());
        viewHolder.llPlay.setVisibility(View.GONE);
        if(chatMessage.getFileType() != 0) {
            switch (chatMessage.getFileType()) {
                case Constant.FILE_TYPE_AUDIO:
                    initAudioLayout(viewHolder, position);
                    break;
                case Constant.FILE_TYPE_VIDEO:
                    initVideoLayout(viewHolder, position);
                    break;
                case Constant.FILE_TYPE_PHOTO:
                    initPhotoLayout(viewHolder, position);
                    break;
                case Constant.FILE_TYPE_OTHERS:
                    initOtherFileLayout(viewHolder, position);
                    break;
            }
            viewHolder.llPlay.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    private void initAudioLayout(ViewHolder viewHolder, int position) {
        viewHolder.player = null;
        viewHolder.llPlay.setTag(viewHolder);
        viewHolder.ivPlay.setTag(position);
        viewHolder.ivPlay.setImageResource(R.drawable.ic_play);
        viewHolder.llPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder playHolder = (ViewHolder) v.getTag();
                if(playHolder.player != null && playHolder.player.isPlaying()) {
                    playHolder.ivPlay.setImageResource(R.drawable.ic_play);
                    stopAudio(playHolder);
                } else {
                    playHolder.player = new MediaPlayer();
                    playHolder.ivPlay.setImageResource(R.drawable.ic_stop);
                    playAudio(playHolder);
                }
            }
        });
    }

    private void initPhotoLayout(ViewHolder viewHolder, int position) {
        String filePath = mListChat.get(position).getFilePath();
        if(filePath == null) {
            return;
        }
        try {
            viewHolder.ivPlay.setImageBitmap(Utils.decodeFile(filePath, Utils.dpToPx(50)));
            viewHolder.llPlay.setTag(filePath);
            viewHolder.llPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
                    if (path == null) {
                        showToast();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + path), "image/*");
                    mActivity.startActivity(intent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initVideoLayout(ViewHolder viewHolder, int position) {
        String filePath = mListChat.get(position).getFilePath();
        if(filePath == null) {
            return;
        }
        try {
            viewHolder.ivPlay.setImageResource(R.drawable.ic_play);
            viewHolder.llPlay.setTag(filePath);
            viewHolder.llPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
                    if (path == null) {
                        showToast();
                        return;
                    }

                    String videoPath = "file://" + path;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
                    intent.setDataAndType(Uri.parse(videoPath), "video/*");
                    mActivity.startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initOtherFileLayout(ViewHolder viewHolder, int position) {
        String filePath = mListChat.get(position).getFilePath();
        if(filePath == null) {
            return;
        }
        try {
            viewHolder.ivPlay.setImageResource(R.drawable.ic_file);
            viewHolder.llPlay.setTag(filePath);
            viewHolder.llPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
                    if (path == null) {
                        showToast();
                        return;
                    }
                    String filePath = "file://" + path;
                    openUnknownFile(filePath);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUnknownFile(String filePath) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(filePath).substring(1));
        newIntent.setDataAndType(Uri.parse(filePath),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mActivity.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public void addMessage(ChatMessageHistory chatMessage) {
        if(mListChat == null) {
            mListChat = new ArrayList<>();
        }
        mListChat.add(chatMessage);
        notifyDataSetChanged();
    }



    private void onPlay(boolean start, ViewHolder viewHolder, String filePath) {
        if (start) {
            startPlaying(viewHolder, filePath);
        } else {
            stopPlaying(viewHolder);
        }
    }

    private void startPlaying(final ViewHolder viewHolder, String filePath) {
        try {
            Log.e("TAG_LOG","File Path: "+filePath);
            viewHolder.player.setDataSource(filePath);
            viewHolder.player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    viewHolder.ivPlay.setImageResource(R.drawable.ic_play);
                    return false;
                }
            });
            viewHolder.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio(viewHolder);
                    viewHolder.ivPlay.setImageResource(R.drawable.ic_play);
                }
            });
            viewHolder.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    viewHolder.player.start();
                }
            });
            viewHolder.player.prepareAsync();
            //viewHolder.player.prepare();
            //viewHolder.player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying(ViewHolder viewHolder) {
        if(viewHolder.player == null) {
            return;
        }
        viewHolder.player.stop();
    }


    protected void playAudio(ViewHolder viewHolder) {
        String filePath = mListChat.get((int)viewHolder.ivPlay.getTag()).getFilePath();
        if(filePath == null) {
            showToast();
            viewHolder.ivPlay.setImageResource(R.drawable.ic_play);
            return;
        }
        //isPlaying = true;
        onPlay(true, viewHolder, filePath);
    }

    protected void stopAudio(ViewHolder viewHolder) {
        //isPlaying = false;
        //mBtnPlay.setText("Play");
        onPlay(false, viewHolder, null);
    }

    private void showToast() {
        Toast.makeText(mActivity, "No file recorded or received",Toast.LENGTH_LONG).show();
    }

    class ViewHolder {
        TextView tvName;
        TextView tvMsg;
        LinearLayout llPlay;
        ImageView ivPlay;
        MediaPlayer player;
    }
}
