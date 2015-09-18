package com.danilov.aircontrol.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.Collection;
import java.util.List;

/**
 * Created by Semyon on 18.09.2015.
 */
public class GetAvailableWiFiNetworksTask extends Task<List<ScanResult>> {

    private Context context;
    private WifiManager wifiManager;

    public GetAvailableWiFiNetworksTask(final TaskListener<List<ScanResult>> listener, final Context context) {
        super(listener);
        this.context = context;
    }

    private final BroadcastReceiver scanResultsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            context.unregisterReceiver(this);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if (scanResults != null) {
                onTaskComplete(scanResults);
            } else {
                boolean wifiEnabled = wifiManager.isWifiEnabled();
                if (!wifiEnabled) {
                    onError(new NoWiFiException());
                } else {
                    onError(new UnknownWiFiException("No idea whats wrong!"));
                }
            }
        }
    };


    private final IntentFilter scanResultsFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    public void run() {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean isWifiEnabled = wifiManager.isWifiEnabled();
        if (!isWifiEnabled) {
            onError(new NoWiFiException());
            return;
        }
        context.registerReceiver(scanResultsReceiver, scanResultsFilter);
        wifiManager.startScan();
    }

    public static class NoWiFiException extends Exception {

        public NoWiFiException() {
        }

        public NoWiFiException(final String detailMessage) {
            super(detailMessage);
        }

        public NoWiFiException(final String detailMessage, final Throwable throwable) {
            super(detailMessage, throwable);
        }

        public NoWiFiException(final Throwable throwable) {
            super(throwable);
        }

    }

    public static class UnknownWiFiException extends Exception {

        public UnknownWiFiException() {
        }

        public UnknownWiFiException(final String detailMessage) {
            super(detailMessage);
        }

        public UnknownWiFiException(final String detailMessage, final Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UnknownWiFiException(final Throwable throwable) {
            super(throwable);
        }
    }

}
