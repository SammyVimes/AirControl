package com.danilov.aircontrol.core;

import android.net.wifi.ScanResult;

/**
 * Created by Semyon on 18.09.2015.
 */
public class WiFiUtils {

    public static WiFiSecurity getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;

        WiFiSecurity[] values = WiFiSecurity.values();
        for (int i = values.length - 1; i >= 0; i--) {
            if (cap.contains(values[i].toString())) {
                return values[i];
            }
        }
        return WiFiSecurity.OPEN;
    }

    private WiFiUtils() {
    }
}
