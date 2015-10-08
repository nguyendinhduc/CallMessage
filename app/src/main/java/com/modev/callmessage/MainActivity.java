package com.modev.callmessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.modev.common.CommonVL;
import com.modev.common.MyService;
import com.modev.common.MySharedPreferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button btnStartBroadcastCall, btnStartBroadcastMessage;
    private MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService();
        btnStartBroadcastCall = (Button) findViewById(R.id.btnStartBroadcastCall);
        btnStartBroadcastCall.setOnClickListener(this);
        initComponent();
    }

    private void initComponent() {
        mySharedPreferences = new MySharedPreferences(this);
        MyService.checkRegisterBroadcastCall = mySharedPreferences.isCheckRegisterBroadcastCall();
        MyService.checkRegisterBroadcastMessage = mySharedPreferences.isCheckRegisterBroadcastMessage();
        btnStartBroadcastCall = (Button) findViewById(R.id.btnStartBroadcastCall);
        btnStartBroadcastCall.setOnClickListener(this);
        btnStartBroadcastMessage = (Button) findViewById(R.id.btnStartBroadcastMessage);
        btnStartBroadcastMessage.setOnClickListener(this);
        changeStateBroadcastCall();
        changeStateBroadcastMessage();

    }

    private void startService() {
        Log.i(TAG, "startService ");
        Intent intent = new Intent();
        intent.setClassName("com.modev.callmessage", "com.modev.common.MyService");
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartBroadcastCall:
                MyService.checkRegisterBroadcastCall = !mySharedPreferences.isCheckRegisterBroadcastCall();
                mySharedPreferences.setCheckRegisterBroadcastCall(MyService.checkRegisterBroadcastCall);
                changeStateBroadcastCall();
                break;
            case R.id.btnStartBroadcastMessage:
                MyService.checkRegisterBroadcastMessage = !mySharedPreferences.isCheckRegisterBroadcastMessage();
                mySharedPreferences.setCheckRegisterBroadcastMessage(MyService.checkRegisterBroadcastMessage);
                changeStateBroadcastMessage();
                break;
        }
        mySharedPreferences.setCheckRegisterBroadcastCall(MyService.checkRegisterBroadcastCall);
    }

    private void changeStateBroadcastCall() {
//        mySharedPreferences.setCheckRegisterBroadcastCall(MyService.checkRegisterBroadcastCall);
        if (MyService.checkRegisterBroadcastCall) {
            btnStartBroadcastCall.setText("Stop broadcast call");
            Intent intent = new Intent();
            intent.setAction(CommonVL.REGISTER_BROADCAST_CALL);
            sendBroadcast(intent);
        } else {
            btnStartBroadcastCall.setText("Start broadcast call");
            Intent intent = new Intent();
            intent.setAction(CommonVL.UNREGISTER_BROADCAST_CALL);
            sendBroadcast(intent);
        }


    }

    private void changeStateBroadcastMessage() {
//        mySharedPreferences.setCheckRegisterBroadcastMessage(MyService.checkRegisterBroadcastMessage);
        if (MyService.checkRegisterBroadcastMessage) {
            btnStartBroadcastMessage.setText("Stop broadcast message");
            Intent intent = new Intent();
            intent.setAction(CommonVL.REGISTER_BROADCAST_MESSAGE);
            sendBroadcast(intent);
        } else {
            btnStartBroadcastMessage.setText("Start broadcast message");
            Intent intent = new Intent();
            intent.setAction(CommonVL.UNREGISTER_BROADCAST_MESSAGE);
            sendBroadcast(intent);
        }
    }
}
