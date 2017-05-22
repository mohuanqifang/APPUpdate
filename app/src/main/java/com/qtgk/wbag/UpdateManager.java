package com.qtgk.wbag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class UpdateManager {

    private Context mContext;
    private final static String mPath = "你的URL";
    private final static int LOADING = 0;
    private final static int LOADED = 1;

    private String mVersion_code;
    private String mVersion_name;
    private String mVersion_desc;
    private String mVersion_path;

    private AlertDialog mDownloadDialog;
    private ProgressBar mProgressBar;

    private boolean loading;

    private String mSavePath;

    public UpdateManager(Context mContext) {
        this.mContext = mContext;
    }

    private Handler mUpdateProgressHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOADED:
                    mDownloadDialog.dismiss();
                    installAPK();
                    break;
                case LOADING:
                    mProgressBar.setProgress((int)msg.obj);
                    break;
                default:
                    break;

            }
        }
    };

    public void checkUpdate() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(mPath, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            mVersion_code = jsonObject.getString("mVersion_code");
                            mVersion_name = jsonObject.getString("mVersion_name");
                            mVersion_desc = jsonObject.getString("mVersion_desc");
                            mVersion_path = jsonObject.getString("mVersion_path");
                            if (isUpdate()) {

                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(mJsonObjectRequest);
    }

    private boolean isUpdate() {
        try {
            int localVersion = mContext.getPackageManager().getPackageInfo("com.qtgk.wbag", 0).versionCode;
            return Integer.parseInt(mVersion_code) > localVersion ? true : false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showNoticeDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle("更新提示").setMessage("").
                setPositiveButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDownloadDialog();
                    }
                }).
                setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mBuilder.create().show();
    }

    private void showDownloadDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);

        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgressBar);
        mBuilder.setTitle("").setView(view).
                setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loading = false;
                        dialog.dismiss();
                    }
                });
        mDownloadDialog = mBuilder.create();
        mDownloadDialog.show();
        downloadAPK();
    }

    private void downloadAPK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mSavePath = Environment.getExternalStorageDirectory() + "/" + "";
                    File mFile = new File(mSavePath);
                    if (!mFile.exists()) mFile.mkdir();
                    try {
                        HttpsURLConnection mHttpURLConnection = (HttpsURLConnection) new URL(mVersion_path).openConnection();
                        mHttpURLConnection.connect();
                        InputStream mInputStream = mHttpURLConnection.getInputStream();
                        int length = mHttpURLConnection.getContentLength();
                        File apkFile = new File(mSavePath, mVersion_name);
                        FileOutputStream fos = new FileOutputStream(apkFile);
                        int count = 0;
                        byte[] buffer = new byte[1024];
                        while (loading) {
                            int numRead = mInputStream.read(buffer);
                            count+=numRead;
                            int mProgress = (int)(((float) count/length)*100);
                            Message msg = Message.obtain();
                            msg.what = LOADING;
                            msg.obj = mProgress;
                            mUpdateProgressHandler.sendEmptyMessage(0);
                            if(numRead<0){
                                mUpdateProgressHandler.sendEmptyMessage(LOADED);
                                break;
                            }
                            fos.write(buffer, 0, numRead);
                        }
                        fos.close();
                        mInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void installAPK(){
        File apkFile = new File(mSavePath, mVersion_name);
        if (!apkFile.exists())
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + apkFile.toString());
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
