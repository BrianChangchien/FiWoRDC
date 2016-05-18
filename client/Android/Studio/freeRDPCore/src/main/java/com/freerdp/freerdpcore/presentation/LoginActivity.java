package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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



public class LoginActivity extends Activity {

    final Context context = this;
    private Spinner spinner;
    private static final String[]paths = {"cesbg.com"};
    // UI Control
    private ImageButton btn_info, btn_network;

    ArrayAdapter<String> adapterLocationType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        process_ui();
    }


    private void process_ui() {

        spinner = (Spinner) findViewById(R.id.locationSpinner);
        adapterLocationType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, paths);
        adapterLocationType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterLocationType);

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
    }

    protected void process_show_help()
    {
        AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);


        b.setTitle(getResources().getString(R.string.dlg_login_info));
        b.setMessage(getResources().getString(R.string.dlg_login_info_content));

        b.setNegativeButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener()
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
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.activity_fiwo_server_setting);
        dialog.setTitle("伺服器設置");
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_title_networking);
        dialog.show();

    }
}
