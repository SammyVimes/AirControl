package com.danilov.aircontrol.activity;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.aircontrol.R;
import com.danilov.aircontrol.core.WiFiSecurity;
import com.danilov.aircontrol.core.WiFiUtils;
import com.danilov.aircontrol.task.GetAvailableWiFiNetworksTask;
import com.danilov.aircontrol.task.Task;

import java.util.Collection;
import java.util.List;


public class WiFiNetworksActivity extends BaseActivity {

    private RecyclerView scanResultsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_networks);
        scanResultsView = view(R.id.scan_results);
        scanResultsView.setLayoutManager(new LinearLayoutManager(this));
        startWifiTask();
    }

    private void startWifiTask() {
        final GetAvailableWiFiNetworksTask task = new GetAvailableWiFiNetworksTask(new Task.TaskListener<List<ScanResult>>() {
            @Override
            public void onTaskComplete(@NonNull final List<ScanResult> result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanResultsView.setAdapter(new ScanResultsAdapter(result));
                    }
                });
            }

            @Override
            public void onError(@NonNull final Throwable t) {
                Toast.makeText(WiFiNetworksActivity.this, "Error while getting available wifi networks: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, this);
        new Thread() {
            @Override
            public void run() {
                task.run();
            }
        }.start();
    }

    private class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {

        private List<ScanResult> scanResultList;

        private ScanResultsAdapter(final List<ScanResult> scanResultList) {
            this.scanResultList = scanResultList;
        }

        @Override
        public ScanResultViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_result_item, parent, false);
            return new ScanResultViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ScanResultViewHolder holder, final int position) {
            ScanResult scanResult = scanResultList.get(position);
            String name = scanResult.SSID;
            holder.name.setText(name);
            WiFiSecurity security = WiFiUtils.getScanResultSecurity(scanResult);
            holder.security.setText(security.toString());
            boolean isKnown = false;
            holder.isKnown.setVisibility(isKnown ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return scanResultList.size();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wi_fi_networks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
