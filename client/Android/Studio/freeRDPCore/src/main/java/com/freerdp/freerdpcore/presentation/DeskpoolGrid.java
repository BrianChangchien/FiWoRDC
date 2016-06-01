package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.freerdp.freerdpcore.R;

import org.json.JSONException;


/**
 * Created by Brian_NB on 2016/5/20.
 */
public class DeskpoolGrid extends BaseAdapter{
    private Context mContext;
    private final String[] web;
    private final int[] Imageid;
    private final String sDefaultViewPos;
    public DeskpoolGrid(Context c,String[] web,int[] Imageid, String sdefaultPos ) {
        mContext = c;
        this.Imageid = Imageid;
        this.web = web;
        sDefaultViewPos = sdefaultPos;
    }

    @Override
    public int getCount() {
        return web.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_deskpool_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_single_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_single_image);
            Button BtnConnect = (Button)grid.findViewById(R.id.grid_btn_connect);
            BtnConnect.setOnClickListener(new ItemButton_Click((Activity) mContext , position));

            textView.setText(web[position]);
            imageView.setImageResource(Imageid[position]);

            if (sDefaultViewPos.equals(Integer.toString(position)) ){
                DesktopPoolActivity dpActivity = (DesktopPoolActivity)mContext;
                dpActivity.SetDefaultDeskpoolGridView(grid);
            }

        } else {
            grid = (View) convertView;
        }
        return grid;

    }

    //自訂按鈕監聽事件
    class ItemButton_Click implements View.OnClickListener {
        private int position;
        private Activity mainActivity;

        ItemButton_Click(Activity context, int pos) {
            this.mainActivity = context;
            position = pos;
        }

        public void onClick(View v) {
            String sPos = "Position : ";
            sPos += Integer.toString(position);
            //Toast.makeText(mainActivity, sPos + "Connect Button clicked", Toast.LENGTH_SHORT).show();
            try {
                ((DesktopPoolActivity) mContext).process_connect_deskpool_vm(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



}
