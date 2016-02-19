package cc.zachary.bluetoothlowenergy;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙设备列表填充器
 * Created by Zachary on 2016/2/17.
 */
public class BluetoothDeviceAdapter extends BaseAdapter {

    private final List<ExBluetoothDevice> devices;

    public BluetoothDeviceAdapter() {
        devices = new ArrayList<ExBluetoothDevice>();
    }

    public void add(ExBluetoothDevice device) {
        if (!devices.contains(device)) { //同一设备在列表中只显示一次
            devices.add(device);
        }
    }

    public void clear() {
        devices.clear();
    }


    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupHolder mGroupHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_group_bluetoothdevice, null);
            mGroupHolder = new GroupHolder();
            mGroupHolder.mDeviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
            mGroupHolder.mDeviceAddress = (TextView) convertView.findViewById(R.id.tv_deviceAddress);
            mGroupHolder.mDeviceRssi = ((TextView) convertView.findViewById(R.id.tv_deviceRssi));
            convertView.setTag(mGroupHolder);
        }

        mGroupHolder = (GroupHolder) convertView.getTag();

        ExBluetoothDevice device = devices.get(position);
        mGroupHolder.mDeviceName.setText(TextUtils.isEmpty(device.bluetoothDevice.getName()) ? "Unknown" : device.bluetoothDevice.getName());
        mGroupHolder.mDeviceAddress.setText(device.bluetoothDevice.getAddress());
        mGroupHolder.mDeviceRssi.setText(String.format(parent.getResources().getString(R.string.ble_rssi), device.rssi));
        return convertView;
    }

    class GroupHolder {
        public TextView mDeviceName;
        public TextView mDeviceAddress;
        public TextView mDeviceRssi;
    }


}
