package com.jpfeng.bingswitchdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jpfeng.bingswitch.BingSwitch;

public class MainActivity extends AppCompatActivity {
    BingSwitch bs;
    Switch sw;
    TextView tv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bs = (BingSwitch) findViewById(R.id.bs_1);
        sw = (Switch) findViewById(R.id.sw_1);
        tv = (TextView) findViewById(R.id.tv_1);
        btn = (Button) findViewById(R.id.btn_1);

        bs.printDebugLog(false);

        bs.setOnCheckedChangeListener(new BingSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(boolean isChecked) {
                sw.setChecked(isChecked);
                tv.setText("Switch：" + (isChecked ? "On" : "Off"));
            }
        });

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (bs.getChecked() != isChecked) {
                    bs.setChecked(isChecked);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bs.toggle();
            }
        });
    }
}
