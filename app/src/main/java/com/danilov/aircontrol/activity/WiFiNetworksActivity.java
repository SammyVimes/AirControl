package com.danilov.aircontrol.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.aircontrol.R;
import com.danilov.aircontrol.core.WiFiSecurity;
import com.danilov.aircontrol.core.WiFiUtils;
import com.danilov.aircontrol.core.model.WiFiAPN;
import com.danilov.aircontrol.dialog.WiFiConnectDialog;
import com.danilov.aircontrol.task.GetAvailableWiFiNetworksTask;
import com.danilov.aircontrol.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class WiFiNetworksActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "WiFiNetworksActivity";
    private RecyclerView scanResultsView;
    private View connectionStateView;
    private ScanResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_networks);
        scanResultsView = view(R.id.scan_results);
        connectionStateView = view(R.id.connection_state);
        scanResultsView.setLayoutManager(new LinearLayoutManager(this));
        checkWifiState();
    }

    private void checkWifiState() {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            startWifiTask();
        } else {
            AppCompatDialog appCompatDialog = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.wifi_requiered));
            builder.setMessage(getString(R.string.enable_question));
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                }
            });
            builder.setPositiveButton(getString(R.string.enable), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    wifiManager.setWifiEnabled(true);
                }
            });
            appCompatDialog = builder.create();
            appCompatDialog.show();
        }
    }

    private void startWifiTask() {
        final GetAvailableWiFiNetworksTask task = new GetAvailableWiFiNetworksTask(new Task.TaskListener<List<ScanResult>>() {
            @Override
            public void onTaskComplete(@NonNull final List<ScanResult> result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<WiFiAPN> apns = new ArrayList<WiFiAPN>(result.size());
                        for (ScanResult scanResult : result) {
                            apns.add(new WiFiAPN(scanResult.SSID, WiFiUtils.getScanResultSecurity(scanResult), false));
                        }
                        adapter = new ScanResultsAdapter(apns);
                        scanResultsView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(@NonNull final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WiFiNetworksActivity.this, "Error while getting available wifi networks: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, this);
        new Thread() {
            @Override
            public void run() {
                task.run();
            }
        }.start();
    }

    @Override
    public void onClick(final View view) {
        int position = scanResultsView.getChildLayoutPosition(view);
        WiFiAPN wiFiAPN = adapter.getScanResultList().get(position);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String passkey = sharedPreferences.getString("WIFI_" + wiFiAPN.getSsid(), "");
        if ("".equals(passkey)) {
            WiFiConnectDialog wiFiConnectDialog = new WiFiConnectDialog();
            Bundle args = new Bundle();
            args.putSerializable(WiFiConnectDialog.WIFI_KEY, wiFiAPN);
            wiFiConnectDialog.setArguments(args);
            wiFiConnectDialog.show(getFragmentManager(), "connectDialog");
        } else {
            connectToAP(wiFiAPN, passkey);
        }
    }

    public void connectToAP(final WiFiAPN wiFiAPN, String passkey) {

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        targetAPN = wiFiAPN;
        this.passkey = passkey;

        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null) {
            String curSSID = connectionInfo.getSSID();
            String targetSSID = "\"" + wiFiAPN.getSsid() + "\"";
            if (targetSSID.equals(curSSID)) {
                onConnected();
                return;
            }
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        String networkSSID = wiFiAPN.getSsid();
        String networkPass = passkey;

        switch (wiFiAPN.getSecurity()) {
            case WEP:
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                wifiConfiguration.wepTxKeyIndex = 0;
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "### 1 ### add Network returned " + res);

                boolean b = wifiManager.enableNetwork(res, true);
                Log.d(TAG, "# enableNetwork returned " + b);

                wifiManager.setWifiEnabled(true);
                break;
            case OPEN:
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "# add Network returned " + res);

                b = wifiManager.enableNetwork(res, true);
                Log.d(TAG, "# enableNetwork returned " + b);

                wifiManager.setWifiEnabled(true);
                break;
        }

        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        int res = wifiManager.addNetwork(wifiConfiguration);
        Log.d(TAG, "### 2 ### add Network returned " + res);

        wifiManager.enableNetwork(res, true);

        boolean changeHappen = wifiManager.saveConfiguration();

        if(res != -1 && changeHappen){
            Log.d(TAG, "### Change happen");
        } else {
            Log.d(TAG, "*** Change NOT happen");
        }

        wifiManager.setWifiEnabled(true);

        connectionStateView.setVisibility(View.VISIBLE);
    }

    private WiFiAPN targetAPN = null;
    private String passkey = null;

    private BroadcastReceiver wifiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (!wifiManager.isWifiEnabled()) {
                return;
            }
            if (targetAPN == null) {
                startWifiTask();
                return;
            }

            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String curSSID = connectionInfo.getSSID();
                String targetSSID = "\"" + targetAPN.getSsid() + "\"";
                if (targetSSID.equals(curSSID)) {
                    onConnected();
                }
            }

        }
    };

    private Handler handler = new Handler();

    private void onConnected() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putString("WIFI_" + targetAPN.getSsid(), passkey).apply();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectionStateView.setVisibility(View.GONE);
                targetAPN = null;
                startActivity(new Intent(WiFiNetworksActivity.this, ControlActivity.class));
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION );
        registerReceiver(wifiConnectedReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiConnectedReceiver);
        super.onPause();
    }

    private class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {

        private List<WiFiAPN> scanResultList;

        private ScanResultsAdapter(final List<WiFiAPN> scanResultList) {
            this.scanResultList = scanResultList;
        }

        @Override
        public ScanResultViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_result_item, parent, false);
            v.setOnClickListener(WiFiNetworksActivity.this);
            return new ScanResultViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ScanResultViewHolder holder, final int position) {
            WiFiAPN scanResult = scanResultList.get(position);
            String name = scanResult.getSsid();
            holder.name.setText(name);
            WiFiSecurity security = scanResult.getSecurity();
            holder.security.setText(security.toString());
            boolean isKnown = scanResult.isKnown();
            holder.isKnown.setVisibility(isKnown ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return scanResultList.size();
        }

        public List<WiFiAPN> getScanResultList() {
            return scanResultList;
        }

    }

    class ScanResultViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView security;
        private TextView isKnown;

        public ScanResultViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            security = (TextView) itemView.findViewById(R.id.security);
            isKnown = (TextView) itemView.findViewById(R.id.is_known);
        }
    }

}
