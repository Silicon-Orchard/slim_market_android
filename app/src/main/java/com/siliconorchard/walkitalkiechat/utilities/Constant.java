package com.siliconorchard.walkitalkiechat.utilities;

import android.os.Environment;

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

    public static final long WAITING_TIME = 2000;
    public static final int PUBLIC_CHANNEL_NUMBER_A = 1;
    public static final int PUBLIC_CHANNEL_NUMBER_B = 2;


    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String FOLDER_NAME = "WalkieTalkie";
    public static final String FILE_NAME = "test.mp3";

    public static final int VOICE_SERVER_PORT = 43322;
    public static final int VOICE_CHAT_PORT = 43323;
}
