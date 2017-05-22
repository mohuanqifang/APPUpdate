package com.qtgk.wbag;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences("versionInfor", Context.MODE_PRIVATE);
        boolean autoUpdate = mSharedPreferences.getBoolean("autoUpdate", false);
        if (autoUpdate) {
           new UpdateManager(this).checkUpdate();
        }
    }
}
