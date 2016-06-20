package com.freerdp.freerdpcore.presentation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.freerdp.freerdpcore.R;
import com.freerdp.freerdpcore.utils.GlobelSetting;
import com.freerdp.freerdpcore.utils.appdefine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.XML;


import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class FiwoServerSetting extends Dialog implements
        android.view.View.OnClickListener {

        private Button btnCheckConnect, btnFinish;
        private EditText editFiwoServerAddr;
        private String sFiwoServerAddr;
        private boolean bConnected;
        private ProgressDialog pDialog;
        private boolean bHandshakeResponse;
        private Timer mTimer = null;
        private TimerTask mTimerTask = null;
        private static int count = 0;
        private boolean isPause = false;
        private boolean isStop = true;

        private static int delay = 15000;  //1s
        private static int period = 15000;  //1s

        public Activity context;
        private static MyHandler mHandler ;
        private OnMyDialogResult mDialogResult; // the callback

        private DownloadManager mDownloadManager;
        private long enqueueId;
        private BroadcastReceiver  mBroadcastReceiver;
        private String sFiWoUpgradePath, sFiWoUpgradeName="", sFiWoAppVersion="";

    public FiwoServerSetting(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.context = a;
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiwo_server_setting);
        if(mHandler == null)
            mHandler = new MyHandler();
        process_ui();
        /*
        try {
            process_app_update();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        */
    }

    protected void onResume()
    {
        if(mHandler == null)
            mHandler = new MyHandler();
    }

    protected void onPause()
    {
        if(mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        System.gc();
    }

    protected void onDestory(){
        if(mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        System.gc();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnFiwoServerCheckConnect) {
            process_press_checkconnect();
        }else  if (id == R.id.btnFiwoServerFinish) {
            process_press_finish();
        }
    }

    public void resetUIStatus(){
        ImageView imgStatus = (ImageView) findViewById(R.id.ImgViewFiwoServerConnectStatus);
        imgStatus.setVisibility(View.INVISIBLE);
        btnFinish.setBackgroundResource(R.drawable.btn_bg_grey);
        btnFinish.setEnabled(false);
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    private void process_ui() {

        SharedPreferences userDetails = context.getSharedPreferences("FiWoServer", Context.MODE_PRIVATE);
        String FiwoIP = userDetails.getString("ip", "");
        TextView tvTitle = (TextView) findViewById(R.id.textFiWoAddress);
        tvTitle.setText(R.string.network_server_address);
        TextView tvexample = (TextView) findViewById(R.id.textFiWoAddressExample);
        tvexample.setText(R.string.example_network);
        editFiwoServerAddr = (EditText) findViewById(R.id.editFIWOaddress);
        editFiwoServerAddr.setText(FiwoIP);
        editFiwoServerAddr.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press

                    return true;
                }
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                    // Perform action on key press

                    return true;
                }
                return false;
            }

        });
        bConnected=false;

        btnCheckConnect = (Button) findViewById(R.id.btnFiwoServerCheckConnect);
        btnCheckConnect.setText(R.string.network_check_connection);
        btnFinish = (Button) findViewById(R.id.btnFiwoServerFinish);
        btnFinish.setText(R.string.network_Finish);
        btnCheckConnect.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        btnFinish.setEnabled(false);
        // sFiwoServerAddr = editFiwoServerAddr.getText().toString();
    }

    private void process_press_checkconnect() {
        sFiwoServerAddr = editFiwoServerAddr.getText().toString();
        if (sFiwoServerAddr.equals("")) {
            editFiwoServerAddr.setError(getContext().getString(R.string.network_server_address));
            return;
        }
        bHandshakeResponse = false;
        startTimer();

        show_process_dialog(getContext().getString(R.string.loading), false);
        Thread thread = new Thread(ThreadHandshake);
        thread.start();

    }

    private void process_press_finish() {
        show_process_dialog(getContext().getString(R.string.loading), false);
        stopTimer();
        Thread thread = new Thread(ThreadDomain);
        thread.start();

    }
    public void downloadNewVersion() {
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String sAPKDownloadPath = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS) + "/" + sFiWoUpgradeName;
        File f = new File(sAPKDownloadPath);
        Boolean deleted = f.delete();
        // apkDownloadUrl 是 apk 的下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(sFiWoUpgradePath+sFiWoUpgradeName));

        request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, sFiWoUpgradeName);
        // 获取下载队列 id
        enqueueId = mDownloadManager.enqueue(request);
    }
    private void promptInstall(Uri data) {
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, "application/vnd.android.package-archive");
        // FLAG_ACTIVITY_NEW_TASK 可以保证安装成功时可以正常打开 app
        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(promptInstall);
    }

    private Runnable ThreadHandshake = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            sendHttpGetHandShake();
        }
    };

    private Runnable ThreadDomain = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            String r = sendHttpGetDomain();

            if(mHandler != null)
            {
                Message msg1 = new Message();
                msg1.what = appdefine.MSG_GET_DOMAIN;
                mHandler.sendMessage(msg1);
            }
            mDialogResult.finish(sFiwoServerAddr,r);
        }
    };
    private String sendHttpGetHandShake() {
        String strUrl = "http://";
        strUrl += sFiwoServerAddr;
        strUrl += ":";
        strUrl += GlobelSetting.sServicePort;
        strUrl += "/FiWo/Interface/rest/deskpool/app";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(strUrl);
        HttpResponse response ;
        String result = "";
        JSONObject soapDatainJsonObject = null;
        String sCurrentVersionName = null;

        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            sCurrentVersionName = pinfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int status_code = 0;
        try {
            response = client.execute(request);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);
                status_code = response.getStatusLine().getStatusCode();
            }

            if (status_code == 200) {
                soapDatainJsonObject = XML.toJSONObject(result);
                JSONObject jsonAPPInfo = soapDatainJsonObject.getJSONObject("deskpoolApp");
                sFiWoUpgradePath = jsonAPPInfo.getString("baseUrl");

                if (jsonAPPInfo.has("androidName"))
                    sFiWoUpgradeName = jsonAPPInfo.getString("androidName");
                if (jsonAPPInfo.has("androidVersion"))
                    sFiWoAppVersion = jsonAPPInfo.getString("androidVersion");


                if(mHandler != null)
                {
                    Message msg1 = new Message();
                    boolean bUpdrade = false;
                    if (sFiWoAppVersion.equals("") || sFiWoUpgradeName.equals(""))
                        bUpdrade = false;
                    else
                        bUpdrade = GlobelSetting.compareVersionNames(sCurrentVersionName, sFiWoAppVersion);

                    if (bUpdrade)
                        msg1.what = appdefine.MSG_NEED_UPGRADE;
                    else
                        msg1.what = appdefine.MSG_SHOW_CONNECTING;

                    mHandler.sendMessage(msg1);
                }
            }
            else
            {
                if(mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_CONNECTION_FAIL;
                    mHandler.sendMessage(msg1);
                }
            }
            Log.d("Response of GET request", response.toString());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        result = Integer.toString(status_code);
        return result;
    }

    private String sendHttpGetDomain() {
        String strUrl = "http://";
        strUrl += sFiwoServerAddr;
        strUrl += ":";
        strUrl += GlobelSetting.sServicePort;
        strUrl += "/FiWo/Interface/rest/deskpool/domain";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(strUrl);
        HttpResponse response ;
        JSONObject soapDatainJsonObject = null;
        String result = "";
        try {
            response = client.execute(request);

            HttpEntity resEntity = response.getEntity();

            bHandshakeResponse = true;

            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);
                soapDatainJsonObject = XML.toJSONObject(result);
            }
            Log.d("Response of GET request", response.toString());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return soapDatainJsonObject.toString();
    }

    public void show_process_dialog(String sMessage, boolean b_can_cancel)
    {
        if(pDialog != null && pDialog.isShowing())
            return;

        if(pDialog == null)
        {
            pDialog = new ProgressDialog(this.getContext());

            pDialog.setTitle("");
            pDialog.setMessage(sMessage);
        }

        pDialog.setCancelable(b_can_cancel);
        pDialog.show();
    }

    private void cancel_progressdialog()
    {
        if(pDialog != null)
        {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (bHandshakeResponse) {
                        if(mHandler != null)
                        {
                            Message msg1 = new Message();
                            msg1.what = appdefine.MSG_CONNECT_RESPONSE;
                            mHandler.sendMessage(msg1);
                        }
                    }
                    else{
                        if(mHandler != null)
                        {
                            Message msg1 = new Message();
                            msg1.what = appdefine.MSG_CONNECT_NON_RESPONSE;
                            mHandler.sendMessage(msg1);
                        }
                    }
                    count++;
                }
            };
        }
        if(mTimer != null && mTimerTask != null )
              mTimer.schedule(mTimerTask, delay, period);
    }

     private void stopTimer() {
         if (mTimer != null) {
             mTimer.cancel();
             mTimer = null;
         }

         if (mTimerTask != null) {
             mTimerTask.cancel();
             mTimerTask = null;
         }
         count = 0;

     }

    // -----------------------------------------------------------
    private class MyHandler extends Handler
    {
        public void handleMessage( Message msg)
        {

            switch( msg.what)
            {
                case appdefine.MSG_SHOW_CONNECTING: {
                    SharedPreferences keyValues = context.getSharedPreferences(("FiWoServer"), Context.MODE_PRIVATE);
                    SharedPreferences.Editor keyValuesEditor = keyValues.edit();
                    keyValuesEditor.clear();
                    keyValuesEditor.putString("ip", sFiwoServerAddr);
                    keyValuesEditor.commit();

                    ImageView imgStatus = (ImageView) findViewById(R.id.ImgViewFiwoServerConnectStatus);
                    imgStatus.setImageResource(R.drawable.icon_connect);
                    imgStatus.setVisibility(View.VISIBLE);
                    btnFinish.setBackgroundResource(R.drawable.btn_bg_green);
                    btnFinish.setEnabled(true);
                    stopTimer();
                    cancel_progressdialog();

                }
                    break;
                case appdefine.MSG_NEED_UPGRADE:{
                    stopTimer();
                    cancel_progressdialog();
                    AlertDialog.Builder b = new AlertDialog.Builder(context);
                    b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                    b.setTitle(getContext().getString(R.string.network_update));
                    b.setMessage(getContext().getString(R.string.network_version_update));
                    b.setNegativeButton(getContext().getString(R.string.ok) , new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface arg0, int arg1)
                        {
                           downloadNewVersion();
                           show_process_dialog(getContext().getString(R.string.network_downloading), false);
                           mBroadcastReceiver  = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    cancel_progressdialog();
                                    long downloadCompletedId = intent.getLongExtra(
                                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                                    // 检查是否是自己的下载队列 id, 有可能是其他应用的
                                    if (enqueueId != downloadCompletedId) {
                                        return;
                                    }
                                    DownloadManager.Query query = new DownloadManager.Query();
                                    query.setFilterById(enqueueId);
                                    Cursor c = mDownloadManager.query(query);
                                    if (c.moveToFirst()) {
                                        context.unregisterReceiver(mBroadcastReceiver);

                                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                        // 下载失败也会返回这个广播，所以要判断下是否真的下载成功
                                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                            // 获取下载好的 apk 路径
                                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));

                                            // 提示用户安装
                                            promptInstall(Uri.parse("file://" + uriString));
                                        }
                                    }
                                }
                            };
                            context.registerReceiver(mBroadcastReceiver, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        }
                    });
                    b.show();
                }
                break;
                case appdefine.MSG_CONNECTION_FAIL: {
                    stopTimer();
                    ImageView imgStatus = (ImageView) findViewById(R.id.ImgViewFiwoServerConnectStatus);
                    imgStatus.setImageResource(R.drawable.icon_disconnect);
                    imgStatus.setVisibility(View.VISIBLE);
                    cancel_progressdialog();
                }
                    break;
                case appdefine.MSG_GET_DOMAIN: {
                    cancel_progressdialog();
                    cancel();
                }
                break;
                case appdefine.MSG_CONNECT_RESPONSE: {
                   stopTimer();

                }
                break;
                case appdefine.MSG_CONNECT_NON_RESPONSE: {
                    stopTimer();
                    ImageView imgStatus = (ImageView) findViewById(R.id.ImgViewFiwoServerConnectStatus);
                    imgStatus.setImageResource(R.drawable.icon_disconnect);
                    imgStatus.setVisibility(View.VISIBLE);
                    cancel_progressdialog();
                }
                break;
                default:
                    break;
            }
        }
    }

    public interface OnMyDialogResult{
        void finish(String sFiWoAddress, String result);
    }

}
