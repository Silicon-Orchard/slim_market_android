package com.siliconorchard.walkitalkiechat.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.siliconorchard.walkitalkiechat.R;

/**
 * Created by adminsiriconorchard on 8/31/15.
 */
public abstract class PopupBase extends DialogFragment {

    private LinearLayout mLayoutClose;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = createView(inflater, container, savedInstanceState);
        mLayoutClose = (LinearLayout) view.findViewById(R.id.layout_close);
        if(mLayoutClose != null) {
            mLayoutClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupBase.this.dismiss();
                }
            });
        }
        initView(view);
        initListeners();
        return view;
    }

    @Nullable
    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
    protected abstract void initView(View view);
    protected abstract void initListeners();
}
