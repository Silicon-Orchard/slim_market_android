package com.siliconorchard.walkitalkiechat.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

/**
 * Created by adminsiriconorchard on 9/30/15.
 */
public class PopupSelectPictureOption extends PopupBase {

    private TextView mTvTitle;
    private LinearLayout mLayoutGallery;
    private LinearLayout mLayoutCamera;
    private OnClickOption mOnClickOption;

    @Nullable
    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_select_picture_option, container, false);
    }

    @Override
    protected void initView(View view) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mTvTitle = (TextView) view.findViewById(R.id.tv_popup_title);
        mLayoutGallery = (LinearLayout) view.findViewById(R.id.layout_select_gallery);
        mLayoutCamera = (LinearLayout) view.findViewById(R.id.layout_select_camera);
    }

    @Override
    protected void initListeners() {
        mLayoutGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOptions(Options.GALLERY);
            }
        });
        mLayoutCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOptions(Options.CAMERA);
            }
        });
    }

    private void onClickOptions(Options option) {
        if(mOnClickOption != null) {
            mOnClickOption.onClickOptions(option);
        }
        this.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        //int width = Utils.getScreenWidth(getActivity())*80/100;
        //int height = Utils.getScreenHeight(getActivity())*50/100;
        //getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationBounce;
    }


    public void setOnClickOption(OnClickOption onClickOption) {
        this.mOnClickOption = onClickOption;
    }

    public static interface OnClickOption {
        public void onClickOptions(Options option);
    }

    public static enum Options{
        GALLERY,
        CAMERA
    }
}
