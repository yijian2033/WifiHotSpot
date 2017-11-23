package com.conqueror.wifihotspot.wifihot;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.conqueror.wifihotspot.constant.Constant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yijian2033
 * @date on 2017/11/18
 * @describe TODO
 */

public class HostWifiManager {

    private Context context;
    private final WifiManager mWifiManager;

    public HostWifiManager(Context context) {

        this.context = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context
                .WIFI_SERVICE);
    }

    /**
     * 创建热点
     *
     * @param ssid   热点名称
     * @param psd    热点密码
     * @param isOpen 是否开放热点
     */
    public void createWifiHost(String ssid, String psd, boolean isOpen) {

        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        Method method1 = null;

        try {
            method1 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration
                    .class, boolean.class);

            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = ssid;
            netConfig.preSharedKey = psd;
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

            if (isOpen) {
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else {
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            }
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            method1.invoke(mWifiManager, netConfig, true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public String getSSID() {

        try {
            Method localMethod = this.mWifiManager.getClass().getDeclaredMethod
                    ("getWifiApConfiguration", new Class[0]);
            if (localMethod == null) {
                return null;
            }
            Object localObject1 = localMethod.invoke(this.mWifiManager, new Object[0]);
            if (localObject1 == null) {
                return null;
            }

            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
            if (localWifiConfiguration.SSID != null) {
                return localWifiConfiguration.SSID;
            }
            Field localField1 = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            if (localField1 == null) return null;
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(localWifiConfiguration);
            localField1.setAccessible(false);
            if (localObject2 == null) return null;
            Field localField2 = localObject2.getClass().getDeclaredField("SSID");
            localField2.setAccessible(true);
            Object localObject3 = localField2.get(localObject2);
            if (localObject3 == null) return null;
            localField2.setAccessible(false);
            String str = (String) localObject3;
            return str;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 检查是否开启Wifi热点
     *
     * @return
     */
    public boolean isWifiApEnabled() {

        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getWifiApClients() {

        List<HotspotClient> list = null;
        List<String> ips = new ArrayList<>();
        try {
            Method method = this.mWifiManager.getClass().getDeclaredMethod("getHotspotClients",
                    new Class[0]);
            if (method == null) {
                return null;
            }
            list = (List<HotspotClient>) method.invoke(this.mWifiManager, new Object[0]);
            Log.i("host", "client size:" + list.size());
            Object[] clients = list.toArray();
            for (int i = 0; i < clients.length; i++) {
                String client = clients[i].toString();
                String[] split = client.split("\n");
                Log.i("host", "client:" + split[0].trim());
                String[] split1 = split[0].split("deviceAddress:");
                Log.i("host", "client:" + split1[1].trim());
                String wifiApIp = getWifiApIp(split1[1].trim());
                Log.i("host", "wifi ap ip : " + wifiApIp);
                ips.add(wifiApIp);
            }

        } catch (NoSuchMethodException e) {
            Log.e("host", e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e("host", e.toString());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("host", e.toString());
            e.printStackTrace();
        }
        return ips;
    }

    public String getWifiApIp(String mac) {

        String ip = null;
        try {
            Method method = this.mWifiManager.getClass().getDeclaredMethod("getClientIp",
                    String.class);
            if (method == null) {
                return null;
            }
            ip = (String) method.invoke(this.mWifiManager, mac);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 关闭热点
     */
    public void closeWifiAp() {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiApEnabled()) {
            try {
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class, boolean.class);
                method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开热点手机获得其他连接手机IP的方法
     *
     * @return 其他手机IP 数组列表
     */
    public ArrayList<String> getConnectedIP() {

        ArrayList<String> connectedIp = new ArrayList<String>();
        BufferedReader br;
        BufferedReader reader = null;


        try {
            reader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = null;
            //读取第一行信息，就是IP address HW type Flags HW address Mask Device
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" +");
                if (tokens.length < 6) {
                    continue;
                }
                String ip = tokens[0]; //ip
                String t1 = tokens[1];
                String t4 = tokens[4];
                String mac = tokens[3];  //mac 地址
                String flag = tokens[2];//表示连接状态
                String device = tokens[5];//设备
//                String t6 = tokens[6];
                Log.d("host", "ip:" + ip + "--mac:" + mac + "--flag:" + flag + "--dev : " +
                        device + "--t1:" + t1 + "--t4: " + t4);
                connectedIp.add(ip);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
            }
        }


        return connectedIp;
    }

    public int getWifiApState(Context mContext) {

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            Log.i("host", "wifi state:  " + i);
            return i;
        } catch (Exception e) {
            Log.e("host", "Cannot get WiFi AP state" + e);
            return Constant.WIFI_AP_STATE_FAILED;
        }
    }

}
