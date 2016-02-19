package cc.zachary.bluetoothlowenergy;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * gatt事件
 * Created by Zachary on 2016/2/17.
 */
public class BluetoothGattEvent {
    public Event event;
    public BluetoothGattCharacteristic characteristic;

    public enum Event {
        GATT_CONNECT_SUCCESS,       //连接成功
        GATT_CONNECT_FAILED,        //连接断开
        GATT_DISCOVERED_SUCCESS,    //探索服务成功
        GATT_RED_SUCCESS,           //读取特征值成功
        GATT_CHANGE

    }
}
