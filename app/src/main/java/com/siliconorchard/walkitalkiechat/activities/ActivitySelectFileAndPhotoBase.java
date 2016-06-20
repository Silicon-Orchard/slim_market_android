package com.siliconorchard.walkitalkiechat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

    private Uri mUriImage;
    protected File mFile;

    protected void storeAndPreviewPhoto(Intent data, ImageView imageView, int requiredHeightWidth) {
        try {
            Bitmap selectedPhotoBitmap;
            requiredHeightWidth = 700;
            if(mUriImage == null) {
                mUriImage = data.getData();
                mFile = new File(Utils.getRealPathFromURI(this, mUriImage));
                selectedPhotoBitmap = Utils.decodeFile(mFile, requiredHeightWidth);
                mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ramen",
                        String.valueOf(System.currentTimeMillis()) + ".jpg");
            } else {
                selectedPhotoBitmap = Utils.decodeFile(mFile, requiredHeightWidth);
            }

            //Bitmap picture = (Bitmap) data.getExtras().get("data");

            if(selectedPhotoBitmap != null) {
                imageView.setImageBitmap(selectedPhotoBitmap);
                imageView.setVisibility(View.VISIBLE);
                saveBitmapToFile(selectedPhotoBitmap, mFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("TAG_LOG", e.getMessage());
            e.printStackTrace();
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
                        //Toast.makeText(getActivity(), "Under Construction", Toast.LENGTH_LONG).show();
                        capturePicture();
                        break;
                }
            }
        });
        popupSelectPictureOption.show(fm,"popup_select_picture_options");
    }

    private void selectPicture() {
        mUriImage = null;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constant.REQUEST_CODE_SELECT_SINGLE_PICTURE);
    }

    private void capturePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File tempFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/ramen");
        tempFolder.mkdir();
        if (! tempFolder.exists()){
            if (! tempFolder.mkdirs()){
                Log.e("TAG_LOG", "failed to create directory");
            }
        }
        mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ramen",
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mUriImage = Uri.fromFile(mFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriImage);
        //startActivityForResult(intent, TAKE_PICTURE);
        startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_SINGLE_PICTURE);
    }

    private void saveBitmapToFile(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
