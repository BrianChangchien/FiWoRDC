package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.freerdp.freerdpcore.R;
import com.freerdp.freerdpcore.application.GlobalApp;
import com.freerdp.freerdpcore.application.GlobalSettings;
import com.freerdp.freerdpcore.domain.BookmarkBase;
import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.domain.PlaceholderBookmark;
import com.freerdp.freerdpcore.domain.QuickConnectBookmark;
import com.freerdp.freerdpcore.utils.BookmarkArrayAdapter;
import com.freerdp.freerdpcore.utils.GlobelSetting;
import com.freerdp.freerdpcore.utils.SeparatedListAdapter;
import com.freerdp.freerdpcore.utils.appdefine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


public class LoginActivity extends Activity {

    final Context context = this;
    private Spinner spinner;
    private FiwoServerSetting fd ;
    // UI Control
    private ImageButton btn_info, btn_network, btn_reload;
    private Button btn_login;
    private String sDomain ="";
    private String sFiWoSvrAddr = "";
    private EditText edtAccount, edtPassword;
    private ProgressDialog pDialog;
    private JSONArray arrClient;
    private static MyHandler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        int dens=dm.densityDpi;
        double wi=(double)width/(double)dens;
        double hi=(double)height/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        if (screenInches < 6.0)
            setContentView(R.layout.activity_login_under6);
        else
            setContentView(R.layout.activity_login);

        if(mHandler == null)
            mHandler = new MyHandler();
        process_ui();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(mHandler == null)
            mHandler = new MyHandler();

        if (fd != null) {
            fd.onResume();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        cancel_progressdialog();

        if(mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (fd != null) {
            fd.onPause();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (fd != null)
            fd.onDestory();
    }

    // -----------

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();

        cancel_progressdialog();

        // ------------------------
        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
        b.setIcon(R.drawable.ic_dialog_alert_holo_light);
        String strTitle = "<font color='#465dbf'>";
        strTitle += this.getString(R.string.exit);
        strTitle +="</font>";
        b.setTitle(Html.fromHtml(strTitle));
        b.setMessage(this.getString(R.string.exit_message));
        b.setPositiveButton((this.getString(R.string.exit)), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which)
            {
                SharedPreferences keyValues = context.getSharedPreferences(("FiWoServer"), Context.MODE_PRIVATE);
                SharedPreferences.Editor keyValuesEditor = keyValues.edit();
                keyValuesEditor.clear();
                keyValuesEditor.putString("ip", "");
                keyValuesEditor.commit();

                System.gc();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        b.setNegativeButton((this.getString(R.string.cancel)), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface arg0, int arg1)
            {
                // i_back_key_count = 0;

                arg0.dismiss();
            }
        });
        b.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_ENTER:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    // Perform action on key press
                    if (sFiWoSvrAddr.isEmpty() || sDomain.isEmpty())
                    {
                        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                        b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                        b.setTitle(getString(R.string.warning));
                        b.setMessage(getString(R.string.login_alert_non_setting_address));
                        b.setNegativeButton(getString(R.string.ok) , new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface arg0, int arg1)
                            {
                                arg0.dismiss();
                            }
                        });
                        b.show();
                    }
                    else
                        process_login();

                    return true;
                }
                break;
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    // Perform action on key press
                    if (sFiWoSvrAddr.isEmpty() || sDomain.isEmpty())
                    {
                        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                        b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                        b.setTitle(getString(R.string.warning));
                        b.setMessage(getString(R.string.login_alert_non_setting_address));
                        b.setNegativeButton(getString(R.string.ok) , new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface arg0, int arg1)
                            {
                                arg0.dismiss();
                            }
                        });
                        b.show();
                    }
                    else
                        process_login();

                    return true;
                }
                break;
            default:
                return super.dispatchKeyEvent(event);
        }
        return true;
    }

    private void process_ui() {

        edtAccount = (EditText) findViewById(R.id.editAccount);
        edtPassword = (EditText) findViewById(R.id.editPassword);

        edtAccount.setHint(R.string.account);
        edtPassword.setHint(R.string.password);

        fd = new FiwoServerSetting(LoginActivity.this);
        String strTitle = "<font color='#465dbf'>";
        strTitle += this.getString(R.string.dlg_network_setting);
        strTitle +="</font>";
        fd.setTitle(Html.fromHtml(strTitle));

        fd.requestWindowFeature(Window.FEATURE_LEFT_ICON);

        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    fd.processEnterKeyEvent();
                    return true;
                }
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                    // Perform action on key press
                    fd.processEnterKeyEvent();
                    return true;
                }
                return false;
            }

        };
        fd.setOnKeyListener(keyListener);

        fd.setDialogResult(new FiwoServerSetting.OnMyDialogResult() {
            public void finish(String FiWoAddr, String result) {
                // now you can use the 'result' on your activity
                sFiWoSvrAddr = FiWoAddr;
                sDomain = result;

                if (mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_UPDATE_SPINNER;
                    mHandler.sendMessage(msg1);
                }

            }
        });

        spinner = (Spinner) findViewById(R.id.locationSpinner);

        btn_info = (ImageButton) findViewById(R.id.imgBtn_Info);
        btn_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                process_show_help();
            }
        });

        btn_network = (ImageButton) findViewById(R.id.imgBtn_Network);
        btn_network.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                process_setting_Network();
            }
        });

        btn_reload = (ImageButton) findViewById(R.id.ImgBtnReload);
        btn_reload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                process_setting_Reload();
            }
        });

        btn_login = (Button) findViewById(R.id.loginbutton);
        btn_login.setText(R.string.login);
        btn_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                process_login();
            }
        });
        btn_login.setEnabled(false);


    }

    private void process_login() {

        if (edtAccount.getText().toString().equals("")) {
            edtAccount.setError(this.getString(R.string.account_empty));
            return;
        }
        if (edtPassword.getText().toString().equals("")) {
            edtPassword.setError(this.getString(R.string.password_empty));
            return;
        }

        show_process_dialog(false);
        Thread thread = new Thread(ThreadGetClientInfo);
        thread.start();

    }
    protected void process_show_help()
    {
        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);

        String sTitle="<font color='#465dbf'>";
        sTitle += getString(R.string.dlg_login_info);
        sTitle += "</font>";
        b.setTitle(Html.fromHtml(sTitle));

        b.setMessage(getString(R.string.dlg_login_info_content));
        b.setIcon(R.drawable.icon_title_help);
        b.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface arg0, int arg1)
            {
                arg0.dismiss();
            }
        });
        b.show();

    }

    protected void process_setting_Network() {
        // custom dialog
        /*
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.activity_fiwo_server_setting);
        dialog.setTitle("伺服器設置");
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_title_networking);
        dialog.setCancelable(true);*/

        // fd.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_title_networking);
        //set up button
        ImageButton imgbtnReload = (ImageButton) findViewById(R.id.ImgBtnReload);
        imgbtnReload.setVisibility(View.INVISIBLE);


        fd.show();
        fd.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_title_networking);
        fd.resetUIStatus();
    }

    protected void process_setting_Reload() {

        show_process_dialog(false);
        Thread thread = new Thread(ThreadDomain);
        thread.start();
    }

    private void goto_next_activity(Class<?> cls) {
        Intent i = new Intent();
        if (arrClient.length() > 0) {
            JSONArray arrDeskpools = new JSONArray();
            arrDeskpools = arrClient;
            i.putExtra("deskpool", arrDeskpools.toString());
        }
        i.setClass(context, cls);

        try {
            JSONObject jsonLoginInfo = new JSONObject();
            jsonLoginInfo.put("account", edtAccount.getText().toString());
            jsonLoginInfo.put("password",edtPassword.getText().toString());
            jsonLoginInfo.put("domain", spinner.getSelectedItem().toString());
            jsonLoginInfo.put("FiWoAddress", sFiWoSvrAddr);
            i.putExtra("logininfo", jsonLoginInfo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(i);
    }

    private String sendHttpPostClient() {
        String strUrl = "http://";
        strUrl += sFiWoSvrAddr;
        strUrl += ":";
        strUrl += GlobelSetting.sServicePort;
        strUrl += "/FiWo/Interface/rest/deskpool/client/all";
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(strUrl);
        httpPost.addHeader("Content-type", "application/json");
        httpPost.addHeader("charset", HTTP.UTF_8);
        String sPostData = GetPostclientJson();

        try {
            httpPost.setEntity(new StringEntity(sPostData, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response ;

        String result = "";
        try {
            response = client.execute(httpPost);
            int nstate_code = response.getStatusLine().getStatusCode();
            if ((200 == nstate_code) || 204 == nstate_code) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "UTF-8");

                    arrClient = ParseClientInfotoAry(result);
                }
                if(mHandler != null)
                {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_LOGIN_GET_CLIENT_SUCCESS;
                    mHandler.sendMessage(msg1);
                }
            }
            else if (401 == nstate_code)
            {
                if(mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_LOGIN_AUTH_ERROR;
                    mHandler.sendMessage(msg1);
                }
            }
            else {
                if(mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_LOGIN_GET_CLIENT_ERROR;
                    mHandler.sendMessage(msg1);
                }
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String GetPostclientJson() {
        JSONObject jinfo = null;
        JSONObject jDomain = null;
        JSONObject jDomains = null;
        JSONArray jarysource = null;
        try {
            jinfo = new JSONObject();
            jinfo.put("ip", getIPAddress(this));
            jinfo.put("mac", getMacAddress(this));
            jarysource = new JSONArray();
            jarysource.put(jinfo);

            jDomain = new JSONObject();
            jDomain.put("domainName", spinner.getSelectedItem().toString());

            jDomain.put("username", edtAccount.getText().toString() + "@" +spinner.getSelectedItem().toString());
            jDomain.put("password", edtPassword.getText().toString());
            jDomain.put("source", jinfo);

            jDomains = new JSONObject();
            jDomains.put("domain", jDomain);
        }
       catch (JSONException e){

       }

        return jDomains.toString();
    }

    private JSONArray ParseClientInfotoAry(String sClientInfo) throws JSONException {
        JSONObject soapDatainJsonObject = null;
        soapDatainJsonObject = XML.toJSONObject(sClientInfo);
        JSONObject DeskpoolJsonObject = soapDatainJsonObject.getJSONObject("deskpools");
        JSONArray  interventionJsonArray = new JSONArray();
        JSONObject interventionObject = new JSONObject();

        Object intervention = DeskpoolJsonObject.get("deskpool");
        if (intervention instanceof JSONArray) {
            // It's an array
            interventionJsonArray = (JSONArray)intervention;
        }
        else if (intervention instanceof JSONObject) {
            // It's an object
            interventionObject = (JSONObject)intervention;
            interventionJsonArray.put(interventionObject);
        }
        else {
            // It's something else, like a string or number
        }

         return interventionJsonArray;

     }

    private Runnable ThreadGetClientInfo = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            String r = sendHttpPostClient();

        }
    };
    private Runnable ThreadDomain = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            String r = sendHttpGetDomain();
            sDomain = r;

            if(mHandler != null)
            {
                Message msg1 = new Message();
                msg1.what = appdefine.MSG_LOGIN_GET_DOMAIN;
                mHandler.sendMessage(msg1);
            }
        }
    };
    private String sendHttpGetDomain() {
        String strUrl = "http://";
        strUrl += sFiWoSvrAddr;
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
    private  void updateDomainControl() {
        Spinner Callbackspinner = (Spinner) findViewById(R.id.locationSpinner);
        Callbackspinner.setAdapter(null);

        JSONObject jObjectDomains = null;
        JSONObject jObjectDomain  = null;
        String sObjectDomainName = null;
        try {
            JSONObject jObject = new JSONObject(sDomain);
            jObjectDomains = jObject.getJSONObject("domains");
            jObjectDomain = jObjectDomains.getJSONObject("domain");
            sObjectDomainName = jObjectDomain.getString("domainName");

            List<String> list;
            list = new ArrayList<String>();
            list.add(sObjectDomainName);
            ArrayAdapter<String> adapterLocationType = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.login_spinner_item, list);
            adapterLocationType.setDropDownViewResource(R.layout.login_spinner_pop_item);
            spinner.setAdapter(adapterLocationType);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void show_process_dialog( boolean b_can_cancel)
    {
        if(pDialog != null && pDialog.isShowing())
            return;

        if(pDialog == null)
        {
            pDialog = new ProgressDialog(this);

            pDialog.setTitle("");
            pDialog.setMessage(getString(R.string.loading));
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


    public static String getMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        return wifiInf.getMacAddress();
    }

    public static String getIPAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        long ip = wifiInf.getIpAddress();
        if( ip != 0 )
            return String.format( "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "0.0.0.0";
    }


    // -----------------------------------------------------------
    private class MyHandler extends Handler
    {
        public void handleMessage( Message msg)
        {
            switch( msg.what)
            {
                case appdefine.MSG_UPDATE_SPINNER: {

                    ImageButton imgBtnReload = (ImageButton) findViewById(R.id.ImgBtnReload);
                    imgBtnReload.setVisibility(View.VISIBLE);

                    updateDomainControl();
                    EditText editAccount = (EditText) findViewById(R.id.editAccount);
                    EditText editPwd = (EditText) findViewById(R.id.editPassword);
                    String sAccount = editAccount.getText().toString();
                    String sPwd = editPwd.getText().toString();

                    Button loginbtn = (Button) findViewById(R.id.loginbutton);
                    loginbtn.setBackgroundResource(R.drawable.btn_login);
                    loginbtn.setEnabled(true);
                }
                break;
                case appdefine.MSG_LOGIN_AUTH_ERROR: {
                    cancel_progressdialog();
                    // ------------------------
                    AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                    b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                    b.setTitle(getString(R.string.warning));
                    b.setMessage(getString(R.string.account_password_error));
                    b.setNegativeButton(getString(R.string.ok) , new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface arg0, int arg1)
                        {
                            arg0.dismiss();
                        }
                    });
                    b.show();
                }
                break;
                case appdefine.MSG_LOGIN_GET_CLIENT_ERROR: {
                    cancel_progressdialog();
                    AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                    b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                    b.setTitle(getString(R.string.warning));
                    b.setMessage(getString(R.string.login_unknown_error));
                    b.setNegativeButton(getString(R.string.ok) , new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface arg0, int arg1)
                        {
                            arg0.dismiss();
                        }
                    });
                    b.show();
                }
                break;
                case appdefine.MSG_LOGIN_GET_CLIENT_SUCCESS: {
                    goto_next_activity(DesktopPoolActivity.class);
                    cancel_progressdialog();
                }
                break;
                case appdefine.MSG_LOGIN_GET_DOMAIN:{
                    cancel_progressdialog();
                    updateDomainControl();
                }
                break;
                default:
                    break;
            }
        }
    }
}
