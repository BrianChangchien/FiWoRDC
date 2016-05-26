package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


public class LoginActivity extends Activity {

    final Context context = this;
    private Spinner spinner;
    private static final String[]paths = {"cesbg.com"};
    private FiwoServerSetting fd ;
    // UI Control
    private ImageButton btn_info, btn_network;
    private Button btn_login;
    private String sDomain;
    private String sFiWoSvrAddr;
    private EditText edtAccount, edtPassword;
    private ProgressDialog pDialog;
    private JSONArray arrClient;
    private static MyHandler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(mHandler == null)
            mHandler = new MyHandler();
        process_ui();
    }


    private void process_ui() {

        edtAccount =(EditText) findViewById(R.id.editAccount);
        edtPassword =(EditText) findViewById(R.id.editPassword);

        fd =  new FiwoServerSetting(LoginActivity.this);
        fd.setTitle("伺服器設置");
        fd.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        fd.setDialogResult(new FiwoServerSetting.OnMyDialogResult() {
               public void finish(String FiWoAddr, String result) {
                                   // now you can use the 'result' on your activity
                   sFiWoSvrAddr = FiWoAddr;
                   sDomain = result;
                   if(mHandler != null) {
                       Message msg1 = new Message();
                       msg1.what = appdefine.MSG_UPDATE_SPINNER;
                       mHandler.sendMessage(msg1);
                   }

               }
       });

        spinner = (Spinner) findViewById(R.id.locationSpinner);

        btn_info = (ImageButton) findViewById(R.id.imgBtn_Info);
        btn_info.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_show_help();
            }
        });

        btn_network = (ImageButton) findViewById(R.id.imgBtn_Network);
        btn_network.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_setting_Network();
            }
        });

        btn_login = (Button) findViewById(R.id.loginbutton);
        btn_login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_login();
            }
        });
        btn_login.setEnabled(false);
    }


    private void process_login() {

        if (edtAccount.getText().toString().equals("")) {
            edtAccount.setError("請輸入帳號");
            return;
        }
        if (edtPassword.getText().toString().equals("")) {
            edtPassword.setError("請輸入密碼");
            return;
        }

        show_process_dialog(false);
        Thread thread = new Thread(ThreadGetClientInfo);
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
    /*i.putExtra(appdefine.b_activity_auto_restore_last_4_channel_in_view, false);
        i.putExtra(appdefine.b_OnePlayerView, true);
        i.putExtra(appdefine.b_autoload_channel_at_first_view, true);

        i.putExtra("URL1", m_edt1.getText().toString());
        i.putExtra("URL2", m_edt2.getText().toString());
        i.putExtra("URL3", m_edt3.getText().toString());
        i.putExtra("URL4", m_edt4.getText().toString());
        */
        try {
            JSONObject jsonLoginInfo = new JSONObject();
            jsonLoginInfo.put("account", edtAccount.getText().toString());
            jsonLoginInfo.put("password",edtPassword.getText().toString());
            jsonLoginInfo.put("domain", spinner.getSelectedItem().toString());
            i.putExtra("logininfo", jsonLoginInfo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(i);
    }

    private String sendHttpPostClient() {
        String strUrl = "http://";
        strUrl += sFiWoSvrAddr;
        strUrl += ":8080/FiWo/Interface/rest/deskpool/client/all";
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(strUrl);
        httpPost.setHeader("Content-type", "application/json");
        String sPostData = GetPostclientJson();

        try {
            httpPost.setEntity(new StringEntity(sPostData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response ;

        String result = "";
        try {
            response = client.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity);

                arrClient = ParseClientInfotoAry(result);
            }
            int nstate_code = response.getStatusLine().getStatusCode();
            if ((200 == nstate_code) || 204 == nstate_code) {
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
            jinfo.put("ip", "192.168.1.155");
            jinfo.put("mac", "00-1A-A0-97-14-29");
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

        return soapDatainJsonObject.getJSONObject("deskpools").getJSONArray("deskpool");
            /*
        for(int i=0; i<arr.length(); i++){
            JSONObject o = arr.getJSONObject(i);
            System.out.println(o);
        }
        */
        //jObjectDomains = jObject.getJSONObject("domains");
           // jObjectDomain = jObjectDomains.getJSONObject("domain");
            //sObjectDomainName = jObjectDomain.getString("domainName");



        }
    protected void process_show_help()
    {
        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
        b.setTitle(getResources().getString(R.string.dlg_login_info));
        b.setMessage(getResources().getString(R.string.dlg_login_info_content));
        b.setIcon(R.drawable.icon_title_help);
        b.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
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

        fd.show();
        fd.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_title_networking);
        fd.resetUIStatus();
    }

    private Runnable ThreadGetClientInfo = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            String r = sendHttpPostClient();

        }
    };

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
            pDialog = new ProgressDialog(this);

            pDialog.setTitle("");
            pDialog.setMessage("Loding");
        }

        pDialog.setCancelable(b_can_cancel);
        pDialog.show();
    }

    // -----------------------------------------------------------
    private class MyHandler extends Handler
    {
        public void handleMessage( Message msg)
        {
            switch( msg.what)
            {
                case appdefine.MSG_UPDATE_SPINNER: {
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
                    b.setTitle("警告");
                    b.setMessage("帳號密碼有誤,請重新輸入");
                    b.setNegativeButton("確定" , new DialogInterface.OnClickListener()
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
                    b.setTitle("警告");
                    b.setMessage("發生未知錯誤,請聯繫管理者");
                    b.setNegativeButton("確定" , new DialogInterface.OnClickListener()
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
                default:
                    break;
            }
        }
    }
}
