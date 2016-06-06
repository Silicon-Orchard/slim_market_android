package com.siliconorchard.walkitalkiechat.runnable;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by adminsiriconorchard on 5/6/16.
 */
public class RunnableReceiveVoiceChat extends RunnableBase {

    private DatagramSocket sDataGramSocket;
    public static final int DATA_PACKET_LENGTH = 16384;// 32768;
    private int channelNumber;

    private static final int SAMPLE_RATE_IN_HZ = 11025;
    private static final int MAX_GROUP_MEMBER = 20;
    private InetAddress[] mArrayInetAddress;
    private AudioTrack[] mArrayAudioTrack;
    private HashMap<InetAddress, Integer> mHashMap;

    private int numberOfMembers;

    public RunnableReceiveVoiceChat() {
        super(true);
        initArrayLists();
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public void closeSocket() {
        if(sDataGramSocket != null) {
            sDataGramSocket.close();
        }
        terminate();
    }


    @Override
    public void run() {
        try {
            sDataGramSocket = new DatagramSocket(Constant.VOICE_CHAT_PORT);
            while (isRunThread()) {
                byte[] buffer = new byte[DATA_PACKET_LENGTH];
                if(sDataGramSocket.isClosed()) {
                    break;
                }
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                sDataGramSocket.receive(datagramPacket);

                InetSocketAddress socketAddress = (InetSocketAddress)datagramPacket.getSocketAddress();
                if(socketAddress != null) {
                    InetAddress inetAddress = socketAddress.getAddress();

                    Integer index = mHashMap.get(inetAddress);
                    if(index == null) {
                        index = numberOfMembers;
                        mHashMap.put(inetAddress, index);
                        addToList(inetAddress);
                    }
                    int length = datagramPacket.getLength();
                    Log.e("TAG_LOG", "Ending message receiving from service, datagramPacket.getLength(): " + datagramPacket.getLength());
                    processData(buffer, index, length);
                } else {
                    Log.e("TAG_LOG", "socketAddress is null");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initArrayLists() {
        mArrayInetAddress = new InetAddress[MAX_GROUP_MEMBER];
        mArrayAudioTrack = new AudioTrack[MAX_GROUP_MEMBER];
        mHashMap = new HashMap<>();
    }

    private void addToList(InetAddress inetAddress) {
        mArrayInetAddress[numberOfMembers] = inetAddress;
        mArrayAudioTrack[numberOfMembers] = null;
        numberOfMembers++;
    }

    private AudioTrack initAudioTrack(int bufferSizeInBytes) {
        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM);
        return audioTrack;
    }

    private void processData(byte[] receivedData, int index, int length) {
        try {
            short[] audioData = Utils.byteArrayToShortArray(receivedData, length);
            int bufferSizeInBytes = audioData.length;

            AudioTrack audioTrack = mArrayAudioTrack[index];
            if(audioTrack == null) {
                audioTrack = initAudioTrack(bufferSizeInBytes);
                mArrayAudioTrack[index] = audioTrack;
            }
            if(audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.play();
            }
            audioTrack.write(audioData, 0, bufferSizeInBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processReceivedVoiceData(byte[] data, int index, int length) {
        new Thread(new RunnableProcessVoiceBytes(data, index, length)).start();
    }
    private class RunnableProcessVoiceBytes extends RunnableBase{
        private byte[] receivedBytes;
        private int position;
        private int length;

        public RunnableProcessVoiceBytes(byte[] data, int pos, int length) {
            super(true);
            this.receivedBytes = data;
            this.position = pos;
            this.length = length;
        }

        @Override
        public void run() {
            processData(receivedBytes, position, length);
        }
    }



    /*private synchronized void processData(byte[] receivedData) {
        isPlaying = true;
        try {
            String message = new String(receivedData);
            VoiceMessage voiceMessage = new VoiceMessage(message);
            if(voiceMessage.getChannelNumber() != channelNumber) {
                return;
            }

            byte[] voiceBytes = Base64.decode(voiceMessage.getVoiceMessage(), Base64.NO_WRAP);

            short[] audioData = Utils.byteArrayToShortArray(voiceBytes);

            int bufferSizeInBytes = (int)audioData.length;

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isPlaying = false;
    }*/
}
