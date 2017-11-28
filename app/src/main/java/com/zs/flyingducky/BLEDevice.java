package com.zs.flyingducky;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by zlatko on 11/26/17.
 */

public class BLEDevice {
    private String name;
    private String SSID;
    private int signalStrenght;
    private View deviceTemplate;
    BluetoothDevice bluetoothDevice;
    private Context activityContext;

    public BLEDevice(BluetoothDevice bluetoothDevice, View template, int rssi, Context context)
    {
        this.name = bluetoothDevice.getName();
        this.SSID = bluetoothDevice.getAddress();
        this.signalStrenght = rssi;
        activityContext = context;
        this.bluetoothDevice = bluetoothDevice;
        TextView nameTextView = (TextView)template.findViewById(R.id.deviceName);
        TextView idTextView = (TextView)template.findViewById(R.id.deviceId);
        TextView signalStrenghtTextView = (TextView)template.findViewById(R.id.deviceSignalStrenght);
        nameTextView.setText(this.name);
        idTextView.setText(SSID);
        signalStrenghtTextView.setText(rssi + "dB");
        template.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToDevice();
            }
        });
        deviceTemplate = template;


    }
    private void connectToDevice()
    {
        DevicesActivity devicesActivity = (DevicesActivity)activityContext;
        devicesActivity.connectToDevice(this);
    }

    public View getView() {
        return deviceTemplate;
    }
}
