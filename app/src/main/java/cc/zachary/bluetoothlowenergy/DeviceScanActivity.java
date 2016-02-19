package cc.zachary.bluetoothlowenergy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Bluetooth Low Energy 演示
 * Crate by Zachary on 2016/2/17
 */
public class DeviceScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /** 扫描时长 */
    public static final int SCAN_DURATION = 10000;
    private Handler mHandler;
    private boolean scanning = false;
    private BluetoothDeviceAdapter mDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicescan);

        init();
    }

    private void init() {

        mHandler = new Handler();
        mDeviceAdapter = new BluetoothDeviceAdapter();
        ListView mDeviceList = (ListView) findViewById(R.id.lv_deviceList);
        mDeviceList.setOnItemClickListener(this);
        mDeviceList.setAdapter(mDeviceAdapter);
        checkSupport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * 检测设备是否支持Bluetooth和Bluetooth low energy
     * 如不支持将直接结束
     */
    public void checkSupport() {
        //因为在清单文件中设置App适用于不支持BLE的设备，这里需要对不支持BLE的设备进行相应处理
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bm.getAdapter();
        //设备不支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devicescan, menu);

        if (!scanning) {
            menu.findItem(R.id.scan).setVisible(true);
            menu.findItem(R.id.stop).setVisible(false);
            menu.findItem(R.id.refresh).setActionView(null);

        } else {
            menu.findItem(R.id.scan).setVisible(false);
            menu.findItem(R.id.stop).setVisible(true);
            menu.findItem(R.id.refresh).setActionView(R.layout.toolbar_progress);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        scan(item.getItemId() == R.id.scan);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Ble 扫描回调<br>
     * 扫描到的设备通过{@link android.bluetooth.BluetoothAdapter.LeScanCallback#onLeScan(BluetoothDevice, int, byte[])} 回调
     * <p/>
     * note: 厂商不同可能会出现回调所在的线程不同，故统一到主线程处理
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.add(new ExBluetoothDevice(device, rssi)); //rssi信号强度
                    mDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    /**
     * 扫描Bluetooth low energy 设备
     *
     * @param b true 扫描,false 停止扫描
     */
    public void scan(boolean b) {
        if (b) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_DURATION); //在指定时长后停止扫描

            scanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       final ExBluetoothDevice device = (ExBluetoothDevice) mDeviceAdapter.getItem(position);
        if(device == null) return;

        Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.BLE_DEVICE_NAME, device.bluetoothDevice.getName());
        intent.putExtra(DeviceControlActivity.BLE_DEVICE_ADDRESS, device.bluetoothDevice.getAddress());
        scan(false);
        startActivity(intent);

    }
}
