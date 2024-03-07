package com.example.iot_kotlin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtils {
    // Connect to Wi-Fi
    public static void connectWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        NetworkRequest request = builder.build();
        connManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                connManager.bindProcessToNetwork(network);
            }
        });
    }
    // Check Wi-Fi state
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }
    // Check Wi-Fi Connected
    public static boolean isWifiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        if(currentWifi.getSSID().equals(WifiManager.UNKNOWN_SSID)) {
            return false;
        }
        return true;
    }
    // Get Wi-Fi SSID
    public static String WifiSSID(Context context) {
        String wifi_SSID = "None";
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        if(currentWifi.getSSID().equals(wifiManager.UNKNOWN_SSID)) {
            wifi_SSID = "No Wi-Fi";
        }
        else wifi_SSID = currentWifi.getSSID().substring(1, currentWifi.getSSID().length()-1);
        return  wifi_SSID;
    }
}

