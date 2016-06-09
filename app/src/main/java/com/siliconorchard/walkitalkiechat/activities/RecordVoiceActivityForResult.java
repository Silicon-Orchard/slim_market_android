package com.siliconorchard.walkitalkiechat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.SendVoiceDataAsync;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.runnable.RunnableVoiceRecordProgress;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/26/16.
 */
public class RecordVoiceActivityForResult extends ActivityBase{

    private ImageView mIvRecord;
    private ImageView mIvPlay;
    private Button mBtnOk;
    private ImageView mIvCancel;


    private boolean isRecording;
    private boolean isPlaying;

    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String FOLDER_NAME = "WalkieTalkie";
    private static final String FILE_NAME = "test.mp3";
    private File mFile;
    private BroadcastReceiver mReceiverBluetoothSco;

    private MediaRecorder mRecorder = null;

    private MediaPlayer mPlayer = null;

    private RunnableVoiceRecordProgress mRecordProgress;
    private TextView mTvRecordProgress;

    private ImageView mIvSend;

    private List<HostInfo> mListHostInfo;

    private SharedPreferences mSharedPref;
    private String myIpAddress;
    private int channelNumber;

    private LinearLayout mLayoutProgress;
    private TextView mTvPercent;
    private ProgressBar mProgress;

    private static final int MAX_PROGRESS_BAR = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice_for_result);

        initView();
        initListeners();
    }

    private void initView() {
        initInfos();
        mIvRecord = (ImageView) findViewById(R.id.iv_record);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mIvCancel = (ImageView) findViewById(R.id.iv_close);
        mTvRecordProgress = (TextView) findViewById(R.id.tv_record_progress);
        mTvRecordProgress.setText("");
        mIvSend = (ImageView) findViewById(R.id.iv_send);

        mLayoutProgress = (LinearLayout) findViewById(R.id.ll_progress_bar);
        mTvPercent = (TextView) findViewById(R.id.tv_percent);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setMax(MAX_PROGRESS_BAR);
    }


    private void initInfos() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        myIpAddress = bundle.getString(Constant.KEY_MY_IP_ADDRESS, null);
        channelNumber = bundle.getInt(Constant.KEY_CHANNEL_NUMBER, 0);
        ArrayList<Parcelable> parcelableList = bundle.getParcelableArrayList(Constant.KEY_HOST_INFO_LIST);
        if(parcelableList != null && parcelableList.size()>0) {
            int size = parcelableList.size();
            mListHostInfo = new ArrayList<>();
            for(int i = 0; i<size; i++) {
                HostInfo client = (HostInfo) parcelableList.get(i);
                mListHostInfo.add(client);
            }
        }
    }
    private void initListeners() {
        mIvRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopAudioRecord();
                } else {
                    createFile(null);
                    startAudioRecord();
                }
            }
        });

        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    stopAudio();
                } else {
                    createFile(null);
                    playAudio();
                }
            }
        });

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_ABSOLUTE_FILE_PATH, mFile.getAbsolutePath());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                RecordVoiceActivityForResult.this.finish();
            }
        });

        mIvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                RecordVoiceActivityForResult.this.finish();
            }
        });

        mIvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });
    }

    private void playAudio() {
        if(mFile == null) {
            Toast.makeText(this, "No file recorded or received",Toast.LENGTH_LONG).show();
            return;
        }
        isPlaying = true;
        mIvPlay.setImageResource(R.drawable.ic_stop);
        //mIvPlay.setText("Stop");
        //PlayAudio playAudio = new PlayAudio();
        //playAudio.execute();
        onPlay(true);
    }

    private void stopAudio() {
        isPlaying = false;
        mIvPlay.setImageResource(R.drawable.ic_play);
        //mIvPlay.setText("Play");
        onPlay(false);
    }

    private void createFile(String fileName) {
        try {
            String folderPath = BASE_PATH+File.separator+FOLDER_NAME;
            File folder = new File(folderPath);
            Log.e("TAG_LOG","Folder Path: "+folderPath);
            if(!folder.exists()) {
                if(folder.mkdirs()) {
                    Log.e("TGA_LOG","Directory created");
                } else {
                    Log.e("TGA_LOG", "Directory not created");
                }
            }
            if(fileName == null) {
                mFile = new File(folderPath, FILE_NAME);
            } else {
                mFile = new File(folderPath, fileName);
            }
            if(!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            mFile = null;
            e.printStackTrace();
        }
    }

    private void startAudioRecord() {
        mIvRecord.setImageResource(R.drawable.ic_stop);
        //mIvRecord.setText("Stop");
        isRecording = true;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReceiverBluetoothSco = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.d("TAG_LOG", "Audio SCO state: " + state);

                unregisterReceiver(this);
                onRecord(true);

            }
        };
        registerReceiver(mReceiverBluetoothSco, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
        am.startBluetoothSco();
    }

    private void stopAudioRecord() {
        mIvRecord.setImageResource(R.drawable.ic_record_start);
        //mIvRecord.setText("Record");
        onRecord(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mReceiverBluetoothSco != null) {
            try{
                unregisterReceiver(mReceiverBluetoothSco);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(isRecording) {
            onRecord(false);
            stopRecording();
        }
        if(isPlaying) {
            onPlay(false);
            stopPlaying();
        }
    }



    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFile.getAbsolutePath());
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio();
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("TAG_LOG", "prepare() failed");
        }
    }

    private void stopPlaying() {
        if(mPlayer == null) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFile.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("TAG_LOG", "prepare() failed");
        }

        mRecorder.start();
        mRecordProgress = new RunnableVoiceRecordProgress(true);
        mRecordProgress.setTvProgress(mTvRecordProgress);
        new Thread(mRecordProgress).start();
    }

    private void stopRecording() {
        if(mRecorder == null) {
            return;
        }
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mRecordProgress.terminate();
        mRecordProgress = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }


    protected void sendData() {
        if(mFile == null) {
            Toast.makeText(this,"No file to send",Toast.LENGTH_LONG).show();
        }
        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        voiceMessage.setChannelNumber(channelNumber);
        try {
            SendVoiceDataAsync sendVoiceDataAsync = new SendVoiceDataAsync();
            sendVoiceDataAsync.setFile(mFile);
            sendVoiceDataAsync.setClientIPAddressList(mListHostInfo);
            sendVoiceDataAsync.setMyIpAddress(myIpAddress);
            sendVoiceDataAsync.setOnPreExecute(new SendVoiceDataAsync.OnPreExecute() {
                @Override
                public void onPreExecute() {
                    //addChatMessage("$", "Sending voice mail...");
                    mLayoutProgress.setVisibility(View.VISIBLE);
                }
            });
            sendVoiceDataAsync.setOnProgressUpdate(new SendVoiceDataAsync.OnProgressUpdate() {
                @Override
                public void onProgressUpdate(int progress) {
                    if (progress > 100) {
                        progress = 100;
                    }
                    Log.e("TAG_LOG", "Progress Value: " + progress);
                    mTvPercent.setText("" + progress + "%");
                    mProgress.setProgress(progress);
                    mProgress.setProgress(progress);
                }
            });

            sendVoiceDataAsync.setOnPostExecute(new SendVoiceDataAsync.OnPostExecute() {
                @Override
                public void onPostExecute(boolean isExecuted) {
                    if(isExecuted) {
                        //addChatMessage("$", "Voice mail sent");
                        Toast.makeText(getApplicationContext(), "Voice mail sent",Toast.LENGTH_LONG).show();
                    } else {
                        //addChatMessage("$", "Voice mail sending failed");
                        Toast.makeText(getApplicationContext(), "Voice mail sending failed",Toast.LENGTH_LONG).show();
                    }
                    mLayoutProgress.setVisibility(View.GONE);
                    //mLayoutPlay.setVisibility(View.GONE);
                    //mBtnVoice.setEnabled(true);
                    //mEtChat.setText("");
                    //mEtChat.setEnabled(true);
                }
            });
            sendVoiceDataAsync.execute(voiceMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
