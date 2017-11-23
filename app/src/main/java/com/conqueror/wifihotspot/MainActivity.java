package com.conqueror.wifihotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.conqueror.wifihotspot.adapter.IpAdapter;
import com.conqueror.wifihotspot.constant.Constant;
import com.conqueror.wifihotspot.socket.ClientThread;
import com.conqueror.wifihotspot.wifihot.HostWifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements CompoundButton
        .OnCheckedChangeListener {

    private EditText mSSID;
    private EditText mPassword;
    private Switch mToggle;
    private ListView mLVDevice;
    private HostWifiManager mHostWifiManager;
    private CheckBox mCBSetPsd;
    private CheckBox mCBShowPsd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();
    }

    private void initView() {

        mSSID = (EditText) findViewById(R.id.et_ssid);
        mPassword = (EditText) findViewById(R.id.et_password);
        mToggle = (Switch) findViewById(R.id.stc_wifi);
        mLVDevice = (ListView) findViewById(R.id.lv_device);
        mCBSetPsd = (CheckBox) findViewById(R.id.cb_setpsd);
        mCBShowPsd = (CheckBox) findViewById(R.id.cb_showpsd);
    }

    private void initListener() {

        mToggle.setOnCheckedChangeListener(this);
        mCBSetPsd.setOnCheckedChangeListener(this);
        mCBShowPsd.setOnCheckedChangeListener(this);

//        boolean checked = mCBSetPsd.isChecked();
//
//        if (checked) {
//            mPassword.setEnabled(true);
//            mCBShowPsd.setEnabled(true);
//        } else {
//            mPassword.setEnabled(false);
//            mCBShowPsd.setEnabled(false);
//        }


    }

    private void initData() {

        mHostWifiManager = new HostWifiManager(this);
        String ssid = mHostWifiManager.getSSID();
        mSSID.setText(ssid);
        mSSID.setSelection(ssid.length());

        IntentFilter intent = new IntentFilter();
        intent.addAction(Constant.WIFI_AP_STATE_CHANGED_ACTION);
        intent.addAction(Constant.WIFI_HOTSPOT_CLIENTS_CHANGED);
        registerReceiver(receiver, intent);

        boolean wifiApEnabled = mHostWifiManager.isWifiApEnabled();
        mToggle.setChecked(wifiApEnabled);

    }


    public void send(View view) {

        JSONObject jsonObject = new JSONObject();
        //打开WiFi热点
        String ssid = mSSID.getText().toString();
        String psd = mPassword.getText().toString();
        if (TextUtils.isEmpty(ssid)) {
            ssid = mHostWifiManager.getSSID();
        }
        if (TextUtils.isEmpty(psd)) {
            Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (psd.length() < 8) {
            Toast.makeText(getApplicationContext(), "密码不能少于8位！", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            jsonObject.put("MsgType", "Cmd");
            jsonObject.put("MsgID", 0x15);
            jsonObject.put("WifiName", ssid);
            jsonObject.put("WifiPassword", psd);
            String json = jsonObject.toString();
            new Thread(new ClientThread(getApplicationContext(), handler, json)).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        switch (compoundButton.getId()) {

            case R.id.cb_setpsd:
                if (b) {
                    mPassword.setEnabled(true);
                    mCBShowPsd.setEnabled(true);
                } else {
                    mPassword.setEnabled(false);
                    mCBShowPsd.setEnabled(false);
                }
                break;

            case R.id.cb_showpsd:

                if (b) {
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance
                            ());
                } else {
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                mPassword.setSelection(mPassword.getText().length());
                break;

            case R.id.stc_wifi:
                if (b) {
                    //打开WiFi热点
                    String ssid = mSSID.getText().toString();
                    String psd = mPassword.getText().toString();
                    if (TextUtils.isEmpty(ssid)) {
                        ssid = mHostWifiManager.getSSID();
                    }
                    Log.d("HOST", "SSID:" + ssid);
                    if (TextUtils.isEmpty(psd)) {
                        Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT)
                                .show();
                        mToggle.setChecked(false);
                        return;
                    }
                    if (psd.length() < 8) {
                        Toast.makeText(getApplicationContext(), "密码不能少于8位！", Toast.LENGTH_SHORT)
                                .show();
                        mToggle.setChecked(false);
                        return;
                    }
                    //打开热点
                    mHostWifiManager.createWifiHost(ssid, psd, false);

//                    if (TextUtils.isEmpty(psd) || !mCBSetPsd.isChecked()) {
//                        mHostWifiManager.createWifiHost(ssid, psd, true);
//                    } else {
//                        mHostWifiManager.createWifiHost(ssid, psd, false);
//                    }
                    Toast.makeText(getApplicationContext(), "热点打开", Toast.LENGTH_SHORT).show();
                } else {
                    //关闭WiFi热点
                    mHostWifiManager.closeWifiAp();
                    Toast.makeText(getApplicationContext(), "热点关闭", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(receiver);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.i("host", "msg what : " + msg.what);

            switch (msg.what) {
                case 0://服务器没有反应
                    Toast.makeText(getApplicationContext(), "设备连接失败！", Toast.LENGTH_SHORT).show();
                    break;
                case 8088:
                    Bundle data = msg.getData();
                    String json = data.getString("json");

                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String value = jsonObject.getString("Value").trim();
                        if (value.equals("OK")) {//服务连接上了
                            Toast.makeText(getApplicationContext(), "设备连接成功！", Toast
                                    .LENGTH_SHORT).show();


                            //打开wifi热点
                            String ssid = mSSID.getText().toString();
                            String psd = mPassword.getText().toString();
                            if (TextUtils.isEmpty(ssid)) {
                                ssid = mHostWifiManager.getSSID();
                            }
                            Log.d("HOST", "SSID:" + ssid);
                            if (TextUtils.isEmpty(psd)) {
                                Toast.makeText(getApplicationContext(), "密码不能为空！", Toast
                                        .LENGTH_SHORT).show();
                                return;
                            }
                            if (psd.length() < 8) {
                                Toast.makeText(getApplicationContext(), "密码不能少于8位！", Toast
                                        .LENGTH_SHORT).show();
                                return;
                            }
                            //打开热点
                            mHostWifiManager.createWifiHost(ssid, psd, false);
                            mToggle.setChecked(true);
                            Toast.makeText(getApplicationContext(), "热点打开", Toast.LENGTH_SHORT)
                                    .show();
                        } else {//服务器没有连接上

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("host", "action:" + intent.getAction());
            int wifiApState = mHostWifiManager.getWifiApState(context);
            Log.d("host", "wifi ap statte :" + wifiApState);
            ArrayList<String> wifiApClients = (ArrayList<String>) mHostWifiManager
                    .getWifiApClients();

            IpAdapter ipAdapter = new IpAdapter(getApplicationContext(), wifiApClients);
            mLVDevice.setAdapter(ipAdapter);
            ipAdapter.notifyDataSetChanged();
        }
    };

}
