package com.siliconorchard.walkitalkiechat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.runnable.RunnableVoiceRecordProgress;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.io.File;
import java.io.IOException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice_for_result);

        initView();
        initListeners();
    }

    private void initView() {
        mIvRecord = (ImageView) findViewById(R.id.iv_record);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mIvCancel = (ImageView) findViewById(R.id.iv_close);
        mTvRecordProgress = (TextView) findViewById(R.id.tv_record_progress);
        mTvRecordProgress.setText("");
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
}
