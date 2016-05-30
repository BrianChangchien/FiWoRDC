package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.freerdp.freerdpcore.R;
import com.freerdp.freerdpcore.utils.appdefine;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DesktopPoolActivity extends Activity {

    private ImageButton btn_info, btn_logout;
    private View PreviousView, DefaultView;
    private String sPreviousViewType;
    private JSONArray arrDeskpools;
    private JSONObject jsonLoginInfo, jsonPublicDeskpool, jsonPublicInstanceInfo;
    GridView grid;
    String[] web;
    int[] imageId;
    private static MyHandler mHandler;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    /*
    String[] web = {
            "Google",
            "Github",
            "Instagram",
            "Facebook",
            "Twitter",
            "Vimeo",
            "WordPress",
            "Youtube"
    } ;
    int[] imageId = {
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon,
            R.drawable.help_icon
    };
   */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop_pool);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        sPreviousViewType = "";
        try {
            JSONArray jsonArray = new JSONArray(getIntent().getStringExtra("deskpool"));
            arrDeskpools = jsonArray;
            JSONObject jsonLogin = new JSONObject(getIntent().getStringExtra("logininfo"));
            jsonLoginInfo = jsonLogin;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(mHandler == null)
            mHandler = new MyHandler();
        process_ui();

    }

    private void process_ui() {

        btn_info = (ImageButton) findViewById(R.id.imgBtn_deskpool_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                process_show_help();
            }
        });

        btn_logout = (ImageButton) findViewById(R.id.imgBtn_Logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                process_setting_Logout();
            }
        });

        process_grid_deskpool();
    }

    private void process_grid_deskpool() {
        web = new String[arrDeskpools.length()];
        imageId = new int[arrDeskpools.length()];
        for (int i = 0; i < arrDeskpools.length(); i++) {
            JSONObject jsondeskpool = null;
            try {
                jsondeskpool = arrDeskpools.getJSONObject(i);
                String sName;
                String sPoolType;
                if (jsondeskpool.getString("type").equals("private")) {
                    sName = jsondeskpool.getString("osName");
                    sPoolType = "<私有虛擬機>";
                    sName += "\n";
                    sName += sPoolType + "\n";
                    sName += "(" + jsondeskpool.getString("container") + ")";
                    imageId[i] = R.drawable.icon_pool_not_default;
                } else {
                    sName = jsondeskpool.getString("name");
                    sPoolType = "<公有桌面池>";
                    sName += "\n";
                    sName += sPoolType + "\n";
                    sName += "(" + jsondeskpool.getString("container") + ")";
                    imageId[i] = R.drawable.icon_public_not_default;
                }
                web[i] = sName;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        DeskpoolGrid adapter = new DeskpoolGrid(DesktopPoolActivity.this, web, imageId);
        grid = (GridView) findViewById(R.id.gridView_deskpool);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(DesktopPoolActivity.this, "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();

                ImageView iv = (ImageView) view.findViewById(R.id.grid_single_image);
                try {
                    JSONObject jsondeskpool = arrDeskpools.getJSONObject(position);
                    if (view.equals(DefaultView)) {
                        if (jsondeskpool.getString("type").equals("private"))
                            iv.setImageResource(R.drawable.icon_pool_default_select);
                        else
                            iv.setImageResource(R.drawable.icon_public_select_default);
                    }else {
                        if (jsondeskpool.getString("type").equals("private"))
                            iv.setImageResource(R.drawable.icon_pool_select_not_default);
                        else
                            iv.setImageResource(R.drawable.icon_public_select_not_default);
                    }

                    Button btnConnect = (Button) view.findViewById(R.id.grid_btn_connect);
                    btnConnect.setVisibility(View.VISIBLE);
                    if (null != PreviousView) {
                        ImageView ivPre = (ImageView) PreviousView.findViewById(R.id.grid_single_image);
                        if (PreviousView.equals(DefaultView)) {
                            if (sPreviousViewType.equals("private"))
                                ivPre.setImageResource(R.drawable.icon_pool_default);
                            else
                                ivPre.setImageResource(R.drawable.icon_public_default);
                        }else {
                            if (sPreviousViewType.equals("private"))
                                ivPre.setImageResource(R.drawable.icon_pool_not_default);
                            else
                                ivPre.setImageResource(R.drawable.icon_public_not_default);
                        }
                        Button btnConnectPre = (Button) PreviousView.findViewById(R.id.grid_btn_connect);
                        btnConnectPre.setVisibility(View.INVISIBLE);
                    }
                    PreviousView = view;
                    sPreviousViewType = jsondeskpool.getString("type");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(DesktopPoolActivity.this, "You Long Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                if (null != DefaultView) {
                    ImageView ivPre = (ImageView) DefaultView.findViewById(R.id.grid_single_image);
                    try {
                        JSONObject jsondeskpool = arrDeskpools.getJSONObject(position);
                        if (jsondeskpool.getString("type").equals("private"))
                            ivPre.setImageResource(R.drawable.icon_pool_not_default);
                        else
                            ivPre.setImageResource(R.drawable.icon_public_not_default);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (view.equals(DefaultView)) {
                        DefaultView = null;
                        return true;
                    }
                }
                DefaultView = view;
                return false;
            }
        });
    }

    private void process_setting_Logout() {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

    private void process_show_help() {

        AlertDialog.Builder b = new AlertDialog.Builder(DesktopPoolActivity.this);

        String sTitle="<font color='#465dbf'>";
        sTitle += getString(R.string.dlg_login_info);
        sTitle += "</font>";
        b.setTitle(Html.fromHtml(sTitle));

        b.setMessage(getResources().getString(R.string.dlg_deskpool_info_content));
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

    protected void process_connect_deskpool_vm(int nPos) throws JSONException {
        /*
        JSONArray arrDeskpools = new JSONArray();
        arrDeskpools = arrClient;
        i.putExtra("deskpool", arrDeskpools.toString());
        */
        JSONObject jsonConnectObj = arrDeskpools.getJSONObject(nPos);
        if (jsonConnectObj.getString("type").equals("private")) {
            String refStr = "";
            Bundle bundle = new Bundle();
            bundle.putString(SessionActivity.PARAM_CONNECTION_REFERENCE, refStr);

            Intent sessionIntent = new Intent(this, SessionActivity.class);
            sessionIntent.putExtras(bundle);
            sessionIntent.putExtra("connectObj", jsonConnectObj.toString());
            sessionIntent.putExtra("loginObj", jsonLoginInfo.toString());
            startActivity(sessionIntent);
        } else {
            jsonPublicDeskpool = jsonConnectObj;
            if (mHandler != null) {
                Message msg1 = new Message();
                msg1.what = appdefine.MSG_DESKPOOL_GET_PUBLIC_CLIENT;
                mHandler.sendMessage(msg1);
            }
        }
    }
    private  void process_connect_public_instance(){
        String refStr = "";
        Bundle bundle = new Bundle();
        bundle.putString(SessionActivity.PARAM_CONNECTION_REFERENCE, refStr);
        try {
            JSONObject jsonConnectObj = new JSONObject();
            jsonConnectObj.put("ip", jsonPublicInstanceInfo.getString("ip"));
            jsonConnectObj.put("port", jsonPublicInstanceInfo.getString("port"));
            Intent sessionIntent = new Intent(this, SessionActivity.class);
            sessionIntent.putExtras(bundle);
            sessionIntent.putExtra("connectObj", jsonConnectObj.toString());
            sessionIntent.putExtra("loginObj", jsonLoginInfo.toString());
            startActivity(sessionIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private Runnable ThreadGetPublicInstanceInfo = new Runnable() {
        public void run() {
            // 運行網路連線的程式
            sendHttpPostPublicClient();

        }
    };

    private void sendHttpPostPublicClient()  {
        String sFiWoAddressIP = "";
        String sPublicDeskpoolID = "";
        try {
            sFiWoAddressIP = jsonLoginInfo.getString("FiWoAddress");
            sPublicDeskpoolID = jsonPublicDeskpool.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String strUrl = "http://";
        strUrl += sFiWoAddressIP;
        strUrl += ":8080/FiWo/Interface/rest/deskpool/";
        strUrl += sPublicDeskpoolID;
        strUrl += "/client/public/ip";
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(strUrl);
        httpPost.setHeader("Content-type", "application/json");
        String sPostData = GetPostclientJson();

        try {
            httpPost.setEntity(new StringEntity(sPostData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response;

        String result = "";
        try {
            response = client.execute(httpPost);
            int nstate_code = response.getStatusLine().getStatusCode();
            if ((200 == nstate_code) ) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity);
                    JSONObject soapDatainJsonObject = null;
                    soapDatainJsonObject = XML.toJSONObject(result);
                    jsonPublicInstanceInfo = soapDatainJsonObject.getJSONObject("deskpool");
                }
                if (mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_DESKPOOL_GET_PUBLIC_SUCCESS;
                    mHandler.sendMessage(msg1);
                }
            } else if (204 == nstate_code) {
                if (mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_DESKPOOL_GET_PUBLIC_NON_CONTENT;
                    mHandler.sendMessage(msg1);
                }
            } else {
                if (mHandler != null) {
                    Message msg1 = new Message();
                    msg1.what = appdefine.MSG_DESKPOOL_GET_PUBLIC_ERROR;
                    mHandler.sendMessage(msg1);
                }
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GetPostclientJson() {
        JSONObject jinfo = null;
        JSONObject jDomain = null;
        JSONObject jDomains = null;
        JSONArray jarysource = null;
        try {

            jDomain = new JSONObject();
            jDomain.put("domainName", jsonLoginInfo.getString("domain"));

            jDomain.put("username", jsonLoginInfo.getString("account") + "@" + jsonLoginInfo.getString("domain"));
            jDomain.put("password", jsonLoginInfo.getString("password"));

            jDomains = new JSONObject();
            jDomains.put("domain", jDomain);
        }
        catch (JSONException e){

        }

        return jDomains.toString();
    }
    // -----------------------------------------------------------
    private class MyHandler extends Handler
    {
        public void handleMessage( Message msg)
        {
            switch( msg.what)
            {
                case appdefine.MSG_DESKPOOL_GET_PUBLIC_CLIENT: {
                    Thread thread = new Thread(ThreadGetPublicInstanceInfo);
                    thread.start();
                }
                break;
                case appdefine.MSG_DESKPOOL_GET_PUBLIC_SUCCESS: {
                    process_connect_public_instance();
                }
                break;
                case appdefine.MSG_DESKPOOL_GET_PUBLIC_ERROR: {
                    // ------------------------
                    AlertDialog.Builder b = new AlertDialog.Builder(DesktopPoolActivity.this);
                    b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                    b.setTitle("警告");
                    b.setMessage("發生未預知的結果,請聯絡管理者");
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
                case appdefine.MSG_DESKPOOL_GET_PUBLIC_NON_CONTENT: {
                    //();
                    AlertDialog.Builder b = new AlertDialog.Builder(DesktopPoolActivity.this);
                    b.setIcon(R.drawable.ic_dialog_alert_holo_light);
                    b.setTitle("警告");
                    b.setMessage("無可用虛擬機器, 請聯絡管理者");
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
                default:
                    break;
            }
        }
    }
}
