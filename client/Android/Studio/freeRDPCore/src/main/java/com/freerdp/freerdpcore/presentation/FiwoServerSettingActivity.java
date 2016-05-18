package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;

import com.freerdp.freerdpcore.R;

public class FiwoServerSettingActivity extends Activity {

    private Button btnSummit, btnCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiwo_server_setting);
        process_ui();
    }

    private void process_ui() {

        btnSummit = (Button) findViewById(R.id.btnFiwoServerSummit);

        // add listener to button
        btnSummit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                process_press_summit();
            }

        });

        btnCancel = (Button) findViewById(R.id.btnFiwoServerCannel);
        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick( View v)
            {
                process_press_cancel();
            }
        });

    }

    private void process_press_summit() {

    }
    private void process_press_cancel() {

    }

}
