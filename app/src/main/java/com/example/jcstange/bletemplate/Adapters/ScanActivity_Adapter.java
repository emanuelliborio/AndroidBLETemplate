package com.example.jcstange.bletemplate.Adapters;

/**
 * Created by jcstange on 18/05/2017.
 */

import android.content.Context;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jcstange.bletemplate.MainActivity;
import com.example.jcstange.bletemplate.R;
import com.example.jcstange.bletemplate.Utils.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ScanActivity_Adapter extends ArrayAdapter<MainActivity.DeviceInfo> {
    private final Context context;
    private final List<MainActivity.DeviceInfo> devicesInfo;

    public ScanActivity_Adapter(Context context,
                                List<MainActivity.DeviceInfo> devicesInfo) {
        super(context , 0);
        this.context = context;
        this.devicesInfo = devicesInfo;
    }


    static class ViewHolder {
        public TextView device_name;
        public TextView device_address;
        public TextView device_rssi;
        public LinearLayout deviceAdapter;
        public LinearLayout info;
        public LinearLayout flag_layout;
        public LinearLayout manuf_layout;
        public LinearLayout tx_layout;
        public TextView flag;
        public TextView manuf;
        public TextView tx;
        public TextView deltaT;
        public TextView scan_record;
        public Button configDevice;
        public int position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder viewHolder = new ViewHolder();

        if (convertView==null){
            convertView = inflater.inflate(R.layout.scanscreen_device_adapter, parent,false);
        }

        viewHolder.device_name = (TextView) convertView.findViewById(R.id.device_name);
        viewHolder.device_address = (TextView) convertView.findViewById(R.id.device_address);
        viewHolder.device_rssi = (TextView) convertView.findViewById(R.id.device_rssi);
        viewHolder.deviceAdapter = (LinearLayout) convertView.findViewById(R.id.device_adapter);
        viewHolder.info = (LinearLayout) convertView.findViewById(R.id.info);
        viewHolder.flag = (TextView) convertView.findViewById(R.id.flag);
        viewHolder.manuf = (TextView) convertView.findViewById(R.id.manuf);
        viewHolder.tx = (TextView) convertView.findViewById(R.id.tx);
        viewHolder.deltaT = (TextView) convertView.findViewById(R.id.device_delta);
        viewHolder.scan_record = (TextView) convertView.findViewById(R.id.scanRecord);
        viewHolder.position = position;
        viewHolder.configDevice = (Button) convertView.findViewById(R.id.config_device);
        viewHolder.flag_layout = (LinearLayout) convertView.findViewById(R.id.flag_layout);
        viewHolder.manuf_layout = (LinearLayout) convertView.findViewById(R.id.manuf_layout);
        viewHolder.tx_layout = (LinearLayout) convertView.findViewById(R.id.tx_layout);


        String deviceName = devicesInfo.get(position).device_name!=null ? devicesInfo.get(position).device_name : "Unknown";
        String deviceAdress = devicesInfo.get(position).device_address;
        String deviceRssi = ""+devicesInfo.get(position).rssi+ "    dBm ";
        String advFlag = devicesInfo.get(position).flag;
        List<ParcelUuid> serUuid = devicesInfo.get(position).uuid;
        SparseArray manuf_data = devicesInfo.get(position).manuf;
        Map<ParcelUuid, byte[]> serviceData = devicesInfo.get(position).service_data;
        int txPower = devicesInfo.get(position).tx;
        long timeStamp = devicesInfo.get(position).deltaT;


        final int pos = position;
        viewHolder.configDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //meshScannerFragment.configDevice(pos);
            }
        });

        viewHolder.device_name.setText(deviceName);
        viewHolder.device_address.setText(deviceAdress);
        viewHolder.device_rssi.setText(deviceRssi);

        ArrayList<byte[]> MD = new ArrayList<>();
        MD = asList(manuf_data);
        if (!MD.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (byte[] tmp : MD){
                message.append(Converters.getHexValue(tmp) + "\n");
            }
            viewHolder.manuf.setText(message);
        } else viewHolder.manuf_layout.setVisibility(View.GONE);
        if (advFlag != null ) viewHolder.flag.setText("" + advFlag);
        else viewHolder.flag_layout.setVisibility(View.GONE);
        viewHolder.deltaT.setText("" + timeStamp + " ms");
        viewHolder.scan_record.setText(Converters.getHexValue(devicesInfo.get(position).scanRecord));
        if (txPower > (-100)) viewHolder.tx.setText("" + txPower);
        else viewHolder.tx_layout.setVisibility(View.GONE);

        return convertView;
    }

    public static <C> ArrayList<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        ArrayList arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }
}
