package com.siliconorchard.walkitalkiechat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.siliconorchard.walkitalkiechat.dialog.PopupSelectPictureOption;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by adminsiriconorchard on 6/20/16.
 */
public class ActivitySelectFileAndPhotoBase extends ActivityBase {

    protected File mSelectedFile;
    private static final String FILE_PATH = Constant.BASE_PATH+Constant.FOLDER_NAME_PHOTO;
    private String uriPath;

    protected void initUriAndFile(Intent data) {
        if(uriPath == null) {
            Uri uriImage = data.getData();
            mSelectedFile = new File(Utils.getRealPathFromURI(this, uriImage));
        } else {
            mSelectedFile = new File(uriPath);
        }
    }

    protected void selectPictureOption() {
        FragmentManager fm = getFragmentManager();
        PopupSelectPictureOption popupSelectPictureOption = new PopupSelectPictureOption();
        popupSelectPictureOption.setOnClickOption(new PopupSelectPictureOption.OnClickOption() {
            @Override
            public void onClickOptions(PopupSelectPictureOption.Options option) {
                switch (option) {
                    case GALLERY:
                        selectPicture();
                        break;
                    case CAMERA:
                        capturePicture();
                        break;
                }
            }
        });
        popupSelectPictureOption.show(fm, "popup_select_picture_options");
    }

    private void selectPicture() {
        uriPath = null;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constant.REQUEST_CODE_SELECT_SINGLE_PICTURE);
    }

    private void capturePicture() {
        File tempFolder = new File(FILE_PATH);
        tempFolder.mkdir();
        if (! tempFolder.exists()){
            if (! tempFolder.mkdirs()){
                Log.e("TAG_LOG", "failed to create directory");
            }
        }
        mSelectedFile = new File(FILE_PATH,
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        uriPath = mSelectedFile.getAbsolutePath();
        Uri uriImage = Uri.fromFile(mSelectedFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
        startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_SINGLE_PICTURE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (uriPath != null) {
            outState.putString("cameraImageUri", uriPath);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            uriPath = savedInstanceState.getString("cameraImageUri");
        }
    }
}
