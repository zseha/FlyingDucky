package com.zs.flyingducky;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class DevicesActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private List<BLEDevice> devices;
    private boolean mScanning;
    private Handler mHandler;
    private ScanSettings bleSettings;
    private List<ScanFilter> bleFilters;
    private BluetoothLeScanner mLEScanner;
    private BluetoothGatt mGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();
        bleFilters = new ArrayList<ScanFilter>();
        devices = new ArrayList<>();

        bleSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);//request enable bt
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            }
            else
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},13);
            }
        }
        enableScanning(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Scanning devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(!mScanning)
                    enableScanning(true);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void enableScanning(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  // mScanning = false;
                  // mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 15000);//15 seconds

            mScanning = true;
            mLEScanner.startScan(bleFilters, bleSettings, mScanCallback);
        } else {
            mScanning = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            devices.add(new BLEDevice(btDevice,  getLayoutInflater().inflate(R.layout.discovered_device,
                    null),result.getRssi(),DevicesActivity.this));
            updateView();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private void updateView() {
        LinearLayout deviceListLayout = (LinearLayout)findViewById(R.id.deviceList);
        for(BLEDevice device : devices)
        {
            deviceListLayout.addView(device.getView());
        }
    }

    public void connectToDevice(BLEDevice bleDevice) {
        if (mGatt == null) {
            mGatt = bleDevice.bluetoothDevice.connectGatt(this, false, gattCallback);
            //scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    //gatt.discoverServices();
                    if(handshake())
                    {
                        startActivity(new Intent(getApplicationContext(), AttackListActivity.class));
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }
    };

    private boolean handshake() {
        return true;
    }
}
