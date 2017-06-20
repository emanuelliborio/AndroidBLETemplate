package com.example.jcstange.bletemplate;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.jcstange.bletemplate.Adapters.ScanActivity_Adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    List<DeviceInfo> scannedDevices;
    ListView scannedList;
    ScanActivity_Adapter devicesAdapter;
    BluetoothManager bluetoothManager;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothAdapter bluetoothAdapter;
    MenuItem scan;
    Boolean scanning = false;
    LinearLayout location_enable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scannedDevices = new ArrayList<>();
        scannedList = (ListView) findViewById(R.id.scannedList);
        devicesAdapter = new ScanActivity_Adapter(getApplicationContext(), scannedDevices);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        location_enable = (LinearLayout) findViewById(R.id.location_enable);


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            final LinearLayout bluetooth_enable = (LinearLayout) findViewById(R.id.bluetooth_enable);
            bluetooth_enable.setVisibility(View.VISIBLE);
            Button bluetooth_enable_btn = (Button) findViewById(R.id.bluetooth_enable_btn);
            bluetooth_enable_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothAdapter.getDefaultAdapter().enable();
                    bluetooth_enable.setVisibility(View.GONE);
                }
            });
        }


        if (!isLocationEnabled(getApplicationContext())) {

            location_enable.setVisibility(View.VISIBLE);
            Button location_enable_btn = (Button) findViewById(R.id.location_enable_btn);
            location_enable_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    location_enable.setVisibility(View.GONE);
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    if(Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);

                }
            });
        } else location_enable.setVisibility(View.GONE);
    }

    /*********************************************
     * Check if the Location Services is enabled *
     *********************************************/
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("onRequestPermissions","coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        scan = menu.findItem(R.id.scan);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.scan:
                if (!scanning) {
                    startScan();
                    scanning = true;
                } else {
                    stopScan();
                    scanning = false;
                }
                return true;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void startScan(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) bluetoothLeScanner.startScan(scanCallback);
        //else bluetoothAdapter.startLeScan(scanLeCallback);
        scan.setTitle("STOP");
        Log.d("startScanLeDevice", "Scan Started");
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            createDeviceObject(result);


        }
    };

    private void createDeviceObject(ScanResult result){

        DeviceInfo deviceInfo = new DeviceInfo();

        deviceInfo.device = result.getDevice();
        deviceInfo.device_address = result.getDevice().getAddress();
        deviceInfo.device_name = result.getDevice().getName();
        deviceInfo.rssi = result.getRssi();
        deviceInfo.flag = Integer.toBinaryString(result.getScanRecord().getAdvertiseFlags());
        deviceInfo.uuid =  result.getScanRecord().getServiceUuids();
        deviceInfo.manuf = result.getScanRecord().getManufacturerSpecificData();
        deviceInfo.service_data = result.getScanRecord().getServiceData();
        deviceInfo.tx = result.getScanRecord().getTxPowerLevel();
        deviceInfo.timestamp = result.getTimestampNanos();
        deviceInfo.scanRecord = result.getScanRecord().getBytes();

        boolean contains = false;

        if (scannedDevices!=null && scannedDevices.isEmpty()){
            deviceInfo.position = 0;
            scannedDevices.add(deviceInfo);
            Log.d("","Added - " + scannedDevices.get(0).position + " - " + scannedDevices.get(0).device_address);
        } else {
            for (DeviceInfo devInfo : scannedDevices) {
                if (devInfo.device_address.equals(deviceInfo.device_address)) {
                    contains = true;
                    int index = scannedDevices.indexOf(devInfo);
                    scannedDevices.get(index).position = index;
                    scannedDevices.get(index).flag = deviceInfo.flag;
                    scannedDevices.get(index).uuid = deviceInfo.uuid;
                    scannedDevices.get(index).rssi = deviceInfo.rssi;
                    scannedDevices.get(index).manuf = deviceInfo.manuf;
                    scannedDevices.get(index).service_data = deviceInfo.service_data;
                    scannedDevices.get(index).deltaT = (deviceInfo.timestamp - devInfo.timestamp)/1000000;
                    scannedDevices.get(index).scanRecord = deviceInfo.scanRecord;
                    scannedDevices.get(index).timestamp = deviceInfo.timestamp;
                    Log.d("","Updated - " + scannedDevices.get(index).position + " - " + scannedDevices.get(index).device_address);
                }
            }
            if (!contains){
                deviceInfo.position = scannedDevices.size();
                scannedDevices.add(deviceInfo);
                Log.d("","Added - " + scannedDevices.get(scannedDevices.size()-1).position + " - " + scannedDevices.get(scannedDevices.size()-1).device_address);

            }

        }

        Collections.sort(scannedDevices, comparator);

        setDevicesAdapter();
    }

    private final Comparator<DeviceInfo> comparator = new Comparator<DeviceInfo>() {
        @Override
        public int compare(DeviceInfo lhs, DeviceInfo rhs) {
            final int lrssi = lhs.rssi;
            final int rrssi = rhs.rssi;
            return ((Integer)rrssi).compareTo(lrssi);
        }
    };

    private void setDevicesAdapter() {

        Log.d("devicesAdapter", "isSet");
        devicesAdapter.clear();
        devicesAdapter.addAll(scannedDevices);
        scannedList.setAdapter(devicesAdapter);

        scannedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (scanning) stopScan();
                LinearLayout info = (LinearLayout) view.findViewById(R.id.info);
                if (info.getVisibility() == View.GONE){
                    Log.d("OnClick","Visible");
                    info.setVisibility(View.VISIBLE);
                } else {
                    Log.d("OnClick","Invisible");
                    info.setVisibility(View.GONE);
                }

            }
        });
    }

    private void stopScan(){

            if (Build.VERSION.SDK_INT >= 21) bluetoothLeScanner.stopScan(scanCallback);
            //else bluetoothAdapter.stopLeScan(leScanCallback);
            Log.d("stopScan", "Called");
            //bluetoothLeScanner = null;
            scan.setTitle("SCAN");

    }

    public class DeviceInfo {
        public BluetoothDevice device;
        public String device_name;
        public String device_address;
        public int rssi;
        public String flag;
        public List<ParcelUuid> uuid;
        public SparseArray<byte[]> manuf;
        public Map<ParcelUuid, byte[]> service_data;
        public int tx;
        public long timestamp;
        public long deltaT = 0;
        public int position = 0;
        public byte[] scanRecord;

    }


}
