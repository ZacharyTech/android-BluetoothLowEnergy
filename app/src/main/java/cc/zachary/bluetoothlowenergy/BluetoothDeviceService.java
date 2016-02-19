package cc.zachary.bluetoothlowenergy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.UUID;

/**
 * Created by Zachary on 2016/2/17.
 */
public class BluetoothDeviceService extends Service {
    private final LocalBinder mBinder;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBleBluetoothDevice;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public BluetoothDeviceService() {
        mBinder = new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public BluetoothDeviceService getService() {
            return BluetoothDeviceService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    //Gatt服务 事件回调。连接状态变化、数据断开、特征值读取和变化
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) { //连接成功
                LogUtil.d("连接到Gatt服务");
                gatt.discoverServices(); //发现服务

                post(BluetoothGattEvent.Event.GATT_CONNECT_SUCCESS);

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) { //连接断开
                LogUtil.d("Gatt服务连接断开");
                post(BluetoothGattEvent.Event.GATT_CONNECT_FAILED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) { //探索服务成功
                post(BluetoothGattEvent.Event.GATT_DISCOVERED_SUCCESS);
            } else {
                LogUtil.d("onServicesDiscovered received:\t"+status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) { //读取特征值成功
                post(BluetoothGattEvent.Event.GATT_RED_SUCCESS, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            post(BluetoothGattEvent.Event.GATT_CHANGE,characteristic); //特征值改变，收到外围设备通知
        }
    };

    /**
     * 使用EventBus传递消息
     * @param event 事件
     */
    public void post(BluetoothGattEvent.Event event) {
        post(event,null);
    }

    /**
     * 使用EventBus传递消息
     * @param event 事件
     * @param characteristic 特征值
     */
    public void post(BluetoothGattEvent.Event event,BluetoothGattCharacteristic characteristic) {
        BluetoothGattEvent bluetoothGattEvent = new BluetoothGattEvent();
        bluetoothGattEvent.event = event;
        bluetoothGattEvent.characteristic = characteristic;
        EventBus.getDefault().post(bluetoothGattEvent);

    }

    /** 初始化BluetoothAdapter */
    public boolean initialize(){
       final BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        if(bm == null){
            LogUtil.d("初始化蓝牙失败");
            return false;
        }

        mBluetoothAdapter = bm.getAdapter();
        if(mBluetoothAdapter == null){
            LogUtil.d("初始化蓝牙失败");
            return false;
        }

        return true;
    }

    /**
     * 连接到Ble设备的Gatt 服务
     * @param address mac
     */
    public boolean connect(String address){
        if(mBluetoothAdapter == null || TextUtils.isEmpty(address)){
            LogUtil.d("BluetoothAdapter 没有初始化或者 mac地址无效");
            return false;
        }

        if(mBleBluetoothDevice != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null ){
            LogUtil.d("尝试使用已存在的BluetoothGatt连接");
            if(mBluetoothGatt.connect()){
                return  true;
            }else{
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            LogUtil.d("连接失败,没有找到设备");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this,true,mGattCallback);
        LogUtil.d("创建一个新的连接");
        mBluetoothDeviceAddress = address;
        return true;

    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        close();
    }

    /** 断开连接 */
    public void disconnect(){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            return;
        }

        mBluetoothGatt.disconnect();
    }

    /** 关闭连接 */
    public void close(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * 主动请求读取给定特征值.读取结果将通过 {@link BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)}异步回调
     * @param characteristic 操作的特征值
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(mBluetoothAdapter != null && mBluetoothGatt != null){
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    /**
     * 开启或关闭给定特征值的通知
     * @param characteristic 操作的特征值
     * @param enable true 开启,false 关闭
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enable){
        if(characteristic == null || mBluetoothGatt == null){
            LogUtil.d("BluetoothAdapter 没有初始化或特征值无效");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic,enable);
        if(characteristic.getUuid().equals(UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT))){
            final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 写出给定的特征值
     * @param characteristic 写出的特征值
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if(characteristic == null || mBluetoothGatt == null){
            LogUtil.d("BluetoothAdapter 没有初始化或特征值无效");
            return;
        }

        if(characteristic.getUuid().equals(UUID.fromString(GattAttributes.WRITE))){
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * 获取连接设备支持的Gatt服务，需要执行完成一次 {@code BluetoothGatt#discoverServices()}
     * @return 支持的Gatt服务的集合
     */
    public List<BluetoothGattService> getSupportGattServices(){
        if(mBluetoothGatt == null){return null;}

        return mBluetoothGatt.getServices();
    }

}
