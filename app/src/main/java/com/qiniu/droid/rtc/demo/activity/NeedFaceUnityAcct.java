package com.qiniu.droid.rtc.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.qiniu.droid.rtc.demo.R;
import com.qiniu.droid.rtc.demo.RTCApplication;
import com.qiniu.droid.rtc.demo.utils.PreferenceUtil;

public class NeedFaceUnityAcct extends AppCompatActivity {
    // 是否使用FaceUnity
    private boolean mIsFuOn = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_faceunity);

        final Button button = (Button) findViewById(R.id.btn_set);
        String isOpen = PreferenceUtil.getString(RTCApplication.getInstance(), PreferenceUtil.KEY_FACEUNITY_ISON);
        mIsFuOn = !TextUtils.isEmpty(isOpen) && !PreferenceUtil.FU_BEAUTY_OFF.equals(isOpen);
        button.setText(mIsFuOn ? "On" : "Off");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFuOn = !mIsFuOn;
                button.setText(mIsFuOn ? "On" : "Off");
            }
        });

        Button btnToMain = (Button) findViewById(R.id.btn_to_main);
        btnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NeedFaceUnityAcct.this, WelcomeActivity.class);
                PreferenceUtil.persistString(RTCApplication.getInstance(), PreferenceUtil.KEY_FACEUNITY_ISON,
                        mIsFuOn ? PreferenceUtil.FU_BEAUTY_ON : PreferenceUtil.FU_BEAUTY_OFF);
                startActivity(intent);
                finish();
            }
        });
    }
}
