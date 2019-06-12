package com.discovery.nettytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.discovery.nettytest.client.NettyClientManager;
import com.discovery.nettytest.entity.PackageData;
import com.discovery.nettytest.server.NettyServerManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";
    private Button mServerButton;
    private Button mClientButton;
    private Button mSendMsg;
    private Button mOpration;
    private String mString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mServerButton = findViewById(R.id.start_server);
        mClientButton = findViewById(R.id.start_client);
        mSendMsg = findViewById(R.id.send_msg);
        mServerButton.setOnClickListener(this);
        mClientButton.setOnClickListener(this);
        mSendMsg.setOnClickListener(this);

        mString = getResources().getString(R.string.data);
        TLog.d(TAG, mString);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_server:
                NettyServerManager server = NettyServerManager.instance();
                server.startServer("127.0.0.1", 10138);
                break;
            case R.id.start_client:
                NettyClientManager client = NettyClientManager.instance();
                client.startClient("127.0.0.1", 10138);
                break;
            case R.id.send_msg:

                PackageData data = new PackageData();
                data.setMsgId((short) 100);
                data.setSource((short) 0);
                data.setMsgNum((short) 101);
                data.setTarget("111111111111");
                data.setEncryp((short) 0);
                data.setBody(mString.getBytes());
                NettyClientManager.instance().sendMsg(data);
                break;

            default:
                break;
        }
    }
}
