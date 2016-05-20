package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.freerdp.freerdpcore.R;

public class DesktopPoolActivity extends Activity {

    private ImageButton btn_info, btn_logout;
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
