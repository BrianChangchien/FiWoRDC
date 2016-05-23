package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.freerdp.freerdpcore.R;

public class DesktopPoolActivity extends Activity {

    private ImageButton btn_info, btn_logout;
    private View PreviousView, DefaultView ;
    GridView grid;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop_pool);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        process_ui();
    }

    private void process_ui() {
        btn_info = (ImageButton) findViewById(R.id.imgBtn_deskpool_info);
        btn_info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_show_help();
            }
        });

        btn_logout = (ImageButton) findViewById(R.id.imgBtn_Logout);
        btn_logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_setting_Logout();
            }
        });

        process_grid_deskpool();
    }

    private void process_grid_deskpool() {
        DeskpoolGrid adapter = new DeskpoolGrid(DesktopPoolActivity.this, web, imageId);
        grid=(GridView)findViewById(R.id.gridView_deskpool);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(DesktopPoolActivity.this, "You Clicked at " +web[+ position], Toast.LENGTH_SHORT).show();

                ImageView iv = (ImageView) view.findViewById(R.id.grid_single_image);
                if (view.equals(DefaultView))
                    iv.setImageResource(R.drawable.icon_pool_default_select);
                else
                    iv.setImageResource(R.drawable.icon_pool_select_not_default);

                Button btnConnect = (Button) view.findViewById(R.id.grid_btn_connect);
                btnConnect.setVisibility(View.VISIBLE);
                if (null != PreviousView)
                {
                    ImageView ivPre = (ImageView) PreviousView.findViewById(R.id.grid_single_image);
                    if (PreviousView.equals(DefaultView))
                        ivPre.setImageResource(R.drawable.icon_pool_default);
                    else
                        ivPre.setImageResource(R.drawable.icon_pool_not_default);

                    Button btnConnectPre = (Button) PreviousView.findViewById(R.id.grid_btn_connect);
                    btnConnectPre.setVisibility(View.INVISIBLE);
                }
                PreviousView = view;
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DesktopPoolActivity.this, "You Long Clicked at " +web[+ position], Toast.LENGTH_SHORT).show();
                if (null != DefaultView) {
                    ImageView ivPre = (ImageView) DefaultView.findViewById(R.id.grid_single_image);
                    ivPre.setImageResource(R.drawable.icon_pool_not_default);
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
    }
}
