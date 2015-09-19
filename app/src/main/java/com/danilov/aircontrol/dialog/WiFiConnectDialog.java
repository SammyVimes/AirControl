package com.danilov.aircontrol.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.danilov.aircontrol.R;
import com.danilov.aircontrol.activity.WiFiNetworksActivity;
import com.danilov.aircontrol.core.model.WiFiAPN;

import java.io.Serializable;

/**
 * Created by Semyon on 19.09.2015.
 */
public class WiFiConnectDialog extends DialogFragment {

    public static final String WIFI_KEY = "WIFI";

    private WiFiAPN wiFiAPN = null;
    private EditText passwordEditText = null;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        wiFiAPN = (WiFiAPN) arguments.getSerializable(WIFI_KEY);
        final WiFiNetworksActivity activity = (WiFiNetworksActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View contentView = inflater.inflate(R.layout.dialog_connect_wifi, null);
        passwordEditText = (EditText) contentView.findViewById(R.id.password);
        builder.setView(contentView);
        builder.setTitle("Connecting to WiFi");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                activity.connectToAP(wiFiAPN, passwordEditText.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dismiss();
            }
        });
        return builder.create();
    }

}