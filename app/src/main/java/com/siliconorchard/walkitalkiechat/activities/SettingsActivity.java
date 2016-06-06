package com.siliconorchard.walkitalkiechat.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

/**
 * Created by adminsiriconorchard on 4/17/16.
 */
public class SettingsActivity extends ActivityBase {

    private EditText mEtName;
    private Button mBtnSave;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
        initListener();
    }

    private void initView() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        mEtName = (EditText) findViewById(R.id.et_my_name);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mEtName.setText(Utils.getDeviceName(mSharedPref));
    }

    private void initListener() {
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });
    }

    private void saveName() {
        String name = mEtName.getText().toString().trim();
        if(name != null && name.length()>0) {
            Utils.setDeviceName(mSharedPref, name);
            SettingsActivity.this.finish();
        }
    }
}
