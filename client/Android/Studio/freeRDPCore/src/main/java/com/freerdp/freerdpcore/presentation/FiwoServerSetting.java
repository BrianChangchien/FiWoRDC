package com.freerdp.freerdpcore.presentation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.freerdp.freerdpcore.R;
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

        public Activity c;
        private static MyHandler mHandler ;
        private
        OnMyDialogResult mDialogResult; // the callback

        public FiwoServerSetting(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiwo_server_setting);
        if(mHandler == null)
            mHandler = new MyHandler();
        process_ui();
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

    private Runnable ThreadHandshake = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            String r = sendHttpGetHandShake();

            if (r.equals("200") || r.equals("201")) {
                if(mHandler != null)
                {
                    Message msg1 = new Message();
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

            if (r != null)
                Log.d("Connected", r);

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
        strUrl += ":80/FiWo/Interface/rest/version";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(strUrl);
        HttpResponse response ;
        String result = "";
        JSONObject soapDatainJsonObject = null;
        int status_code = 0;

        try {
            response = client.execute(request);

            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);
            }
            status_code = response.getStatusLine().getStatusCode();
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
        strUrl += ":80/FiWo/Interface/rest/deskpool/domain";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(strUrl);
        HttpResponse response ;
        JSONObject soapDatainJsonObject = null;
        String result = "";
        try {
            response = client.execute(request);

            HttpEntity resEntity = response.getEntity();

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

        bHandshakeResponse = true;
        return soapDatainJsonObject.toString();
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

        editFiwoServerAddr = (EditText) findViewById(R.id.editFIWOaddress);
        editFiwoServerAddr.setText("10.67.54.15");
        bConnected=false;

        btnCheckConnect = (Button) findViewById(R.id.btnFiwoServerCheckConnect);
        btnFinish = (Button) findViewById(R.id.btnFiwoServerFinish);
        btnCheckConnect.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        btnFinish.setEnabled(false);
        // sFiwoServerAddr = editFiwoServerAddr.getText().toString();
    }


    private void process_press_checkconnect() {
        sFiwoServerAddr = editFiwoServerAddr.getText().toString();
        if (sFiwoServerAddr.equals("")) {
            editFiwoServerAddr.setError("請輸入FiWo Server Address");
            return;
        }
        bHandshakeResponse = false;
        startTimer();
        show_process_dialog(false);
        Thread thread = new Thread(ThreadHandshake);
        thread.start();
        //editFiwoServerAddr.setError("請輸入FiWo Server Address");
    }

    private void process_press_finish() {
        show_process_dialog(false);
        stopTimer();
        Thread thread = new Thread(ThreadDomain);
        thread.start();

    }
    private void cancel_progressdialog()
    {
        if(pDialog != null)
        {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    public void show_process_dialog( boolean b_can_cancel)
    {
        if(pDialog != null && pDialog.isShowing())
            return;

        if(pDialog == null)
        {
            pDialog = new ProgressDialog(this.getContext());

            pDialog.setTitle("");
            pDialog.setMessage("Loading");
        }

        pDialog.setCancelable(b_can_cancel);
        pDialog.show();
    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (false == bHandshakeResponse) {
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
                    ImageView imgStatus = (ImageView) findViewById(R.id.ImgViewFiwoServerConnectStatus);
                    imgStatus.setImageResource(R.drawable.icon_connect);
                    imgStatus.setVisibility(View.VISIBLE);
                    btnFinish.setBackgroundResource(R.drawable.btn_bg_green);
                    btnFinish.setEnabled(true);
                    stopTimer();
                    cancel_progressdialog();

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
