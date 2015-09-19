package com.danilov.aircontrol.core.model;

import com.danilov.aircontrol.core.WiFiSecurity;

import java.io.Serializable;

/**
 * Created by Semyon on 19.09.2015.
 */
public class WiFiAPN implements Serializable {

    private static final long serialVersionUID = 4634709978937591056L;

    private String ssid;

    private WiFiSecurity security;

    private boolean isKnown;

    public WiFiAPN(final String ssid, final WiFiSecurity security, final boolean isKnown) {
        this.ssid = ssid;
        this.security = security;
        this.isKnown = isKnown;
    }

    public String getSsid() {
        return ssid;
    }

    public WiFiSecurity getSecurity() {
        return security;
    }

    public boolean isKnown() {
        return isKnown;
    }

}