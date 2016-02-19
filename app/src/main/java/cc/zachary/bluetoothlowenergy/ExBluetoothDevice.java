package cc.zachary.bluetoothlowenergy;

import android.bluetooth.BluetoothDevice;

/**
 * 扩展{@link BluetoothDevice} 将信号强度封装在一起
 * Created by Zachary on 2016/2/17.
 */
public class ExBluetoothDevice {
    public BluetoothDevice bluetoothDevice;
    public int rssi;

    public ExBluetoothDevice(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }
}
