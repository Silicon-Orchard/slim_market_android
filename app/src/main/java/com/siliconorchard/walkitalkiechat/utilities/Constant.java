package com.siliconorchard.walkitalkiechat.utilities;

import android.os.Environment;

import java.io.File;

/**
 * Created by siliconorchard on 4/11/2016.
 */
public class Constant {

    public static final String SHARED_PREF_NAME = "com.siliconorchard.walkitalkiechat";
    public static final String SELF_PACKAGE_NAME = "com.siliconorchard.walkitalkiechat";
    public static final String KEY_DEVICE_ID = "key_device_id";
    public static final String KEY_SERVER_IP_ADDRESS = "key_server_ip_address";
    public static final String KEY_MY_IP_ADDRESS = "key_my_ip_address";

    public static final String KEY_MY_DEVICE_NAME = "key_my_device_name";
    public static final String KEY_CLIENT_MESSAGE = "key_client_message";
    public static final String KEY_HOST_INFO = "key_host_info";
    public static final String KEY_HOST_INFO_LIST = "key_host_info_list";
    public static final String KEY_CHANNEL_NUMBER = "key_channel_number";

    public static final String KEY_IS_CONTACT_MODIFIED = "key_is_contact_added";

    public static final String KEY_ABSOLUTE_FILE_PATH = "key_absolute_file_path";

    public static final String SERVER_SERVICE_NAME = "com.siliconorchard.walkitalkiechat.service";

    public static final String SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND = "com.siliconorchard.walkitalkiechat.service.receiver.foreground";
    public static final String SERVICE_NOTIFICATION_STRING_CHAT_BACKGROUND = "com.siliconorchard.walkitalkiechat.service.receiver.background";
    public static final String RECEIVER_NOTIFICATION_CONTACT_LIST_MODIFIED = "com.siliconorchard.walkitalkiechat.receiver.contact_modified";
    public static final String RECEIVER_NOTIFICATION_CHAT_REQUEST = "com.siliconorchard.walkitalkiechat.receiver.chat_request";
    public static final String RECEIVER_NOTIFICATION_CHAT_ACCEPT = "com.siliconorchard.walkitalkiechat.receiver.chat_accept";

    public static final int FIRST_SERVER_PORT = 43321;

    public static final int READ_PHONE_STATE_PERMISSION = 111;

    public static final String DEVICE_ID_UNKNOWN = "UNKNOWN";

    public static final int ACTIVITY_RESULT_ADD_CLIENT = 1111;
    public static final int ACTIVITY_RESULT_RECORD_VOICE = 1112;
    public static final int REQUEST_CODE_SELECT_SINGLE_PICTURE = 4551;
    public static final int REQUEST_CODE_SELECT_ANY_FILE = 4552;

    public static final long WAITING_TIME = 2000;
    public static final int PUBLIC_CHANNEL_NUMBER_A = 1;
    public static final int PUBLIC_CHANNEL_NUMBER_B = 2;


    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "WalkieTalkie"+ File.separator;
    public static final String FILE_NAME = "test.mp3";

    public static final int VOICE_SERVER_PORT = 43322;
    public static final int VOICE_CHAT_PORT = 43323;

    public static final int FILE_TYPE_AUDIO = 1;
    public static final int FILE_TYPE_VIDEO = 2;
    public static final int FILE_TYPE_PHOTO = 3;
    public static final int FILE_TYPE_OTHERS = 4;

    public static final String FOLDER_NAME_AUDIO = "audio";
    public static final String FOLDER_NAME_VIDEO = "video";
    public static final String FOLDER_NAME_PHOTO = "photo";
    public static final String FOLDER_NAME_OTHER = "other";
    public static final String FOLDER_PROFILE_PIC = "profile_pic";
    public static final String PROFILE_PIC_NAME = "profile.png";
    public static final String KEY_USER_STATUS = "user_status";

    //Hash values for audio formats
    public static final int[] AUDIO_FORMAT_HASH_VALUES = {
            1335, //aac
            295527, //flac
            17454, //mp3
            17965, //m4a
            17176, //mid
            31578, //xmf
            20161, //ota
            12157, //imy
            19699, //ogg
            29866, //wav
            1782 //amr
    };


    //Hash values for image formats
    public static final int[] IMAGE_FORMAT_HASH_VALUES = {
            13543, //jpg
            9402, //gif
            21247, //png
            3076, //bmp
            1079656 //webp
    };

    //Hash values for video formats
    public static final int[] VIDEO_FORMAT_HASH_VALUES = {
            39148, //3gp
            17455, //mp4
            739, //ts
            1079653, //webm
            17266, //mkv
            8230, //flv
            30298, //wmv
            17431, //mpg
            627451 //mpeg
    };



}
