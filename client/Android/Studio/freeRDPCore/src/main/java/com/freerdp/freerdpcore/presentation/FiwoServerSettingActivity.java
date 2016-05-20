package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.freerdp.freerdpcore.R;

public class FiwoServerSettingActivity extends Activity {

    private Button btnCheckConnect, btnFinish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiwo_server_setting);
        process_ui();
    }

    private void process_ui() {

        btnCheckConnect = (Button) findViewById(R.id.btnFiwoServerCheckConnect);

        // add listener to button
        btnCheckConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                process_press_checkconnect();
            }

        });

        btnFinish = (Button) findViewById(R.id.btnFiwoServerFinish);
        btnFinish.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_press_finish();
            }
        });

    }

    private void process_press_checkconnect() {

    }
    private void process_press_finish() {

    }

}
