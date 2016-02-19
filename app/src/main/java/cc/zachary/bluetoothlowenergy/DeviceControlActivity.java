package cc.zachary.bluetoothlowenergy;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 设备控制页面
 * Created by Zachary on 2016/2/17.
 */
public class DeviceControlActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    public static final String BLE_DEVICE_NAME = "bluetoothLeName";
    public static final String BLE_DEVICE_ADDRESS = "bluetoothLeAddress";

    private boolean connecting = false;
    private TextView mDeviceAddress;
    private TextView mDeviceStatus;
    private TextView mDeviceData;
    private ExpandableListView mDeviceService;
    private BluetoothDeviceService mBluetoothService;

    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothDeviceService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                LogUtil.d("无法初始化蓝牙");
                finish();
            }
            //初始化成功后连接设备
            mBluetoothService.connect(mBluetoothAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };
    private String mBluetoothAddress;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicecontrol);
        setTitle(getIntent().getStringExtra(BLE_DEVICE_NAME));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EventBus.getDefault().register(this);

        mBluetoothAddress = getIntent().getStringExtra(BLE_DEVICE_ADDRESS);

        mDeviceAddress = (TextView) findViewById(R.id.tv_deviceAddress);
        mDeviceStatus = (TextView) findViewById(R.id.tv_deviceStatus);
        mDeviceData = (TextView) findViewById(R.id.tv_deviceData);
        mDeviceService = (ExpandableListView) findViewById(R.id.elv_deviceService);
        mDeviceService.setOnChildClickListener(this);
        clearUI();
        //绑定服务
        bindService(new Intent(this, BluetoothDeviceService.class), mBluetoothServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDeviceAddress.setText(String.format(getString(R.string.ble_address), mBluetoothAddress));

        if (mBluetoothService != null) {
            mBluetoothService.connect(mBluetoothAddress);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        unbindService(mBluetoothServiceConnection);
        mDeviceService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devicecontrol, menu);
        if (!connecting) {
            menu.findItem(R.id.connect).setVisible(true);
            menu.findItem(R.id.disconnect).setVisible(false);
        } else {
            menu.findItem(R.id.connect).setVisible(false);
            menu.findItem(R.id.disconnect).setVisible(true);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.connect:
                mBluetoothService.connect(mBluetoothAddress);
                break;
            case R.id.disconnect:
                mBluetoothService.disconnect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 自身事件驱动，在UI线程中接收事件
     *
     * @param gattEvent BluetoothGattEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(BluetoothGattEvent gattEvent) {
        switch (gattEvent.event) {
            case GATT_CONNECT_SUCCESS:
                connecting = true;
                updateConnectState(R.string.ble_connect);
                invalidateOptionsMenu();
                break;
            case GATT_CONNECT_FAILED:
                connecting = false;
                updateConnectState(R.string.ble_disconnect);
                invalidateOptionsMenu();
                break;
            case GATT_DISCOVERED_SUCCESS:
                displayGattService(mBluetoothService.getSupportGattServices());

                LogUtil.d("#发现服务#");
                break;
            case GATT_RED_SUCCESS:
            case GATT_CHANGE:
                LogUtil.d("#收到数据#\t" + Arrays.toString(gattEvent.characteristic.getValue()));
                displayData(gattEvent.characteristic.getValue());
                break;
        }
    }

    protected void clearUI() {
        mDeviceAddress.setText(String.format(getString(R.string.ble_address), ""));
        mDeviceStatus.setText(String.format(getString(R.string.ble_state), ""));
        mDeviceData.setText(String.format(getString(R.string.ble_data), ""));

        mDeviceService.setAdapter((SimpleExpandableListAdapter)null);
    }


    protected void displayData(byte[] data) {
        if (data != null && data.length > 0) {
            final String stringData = Arrays.toString(data);
            mDeviceData.setText(String.format(getString(R.string.ble_data),  stringData.substring(1,stringData.length()-1)));
        }
    }

    protected void updateConnectState(@StringRes final int resourceId) {

        mDeviceStatus.setText(String.format(getString(R.string.ble_state), getString(resourceId)));
    }

    /**
     * 显示Gatt服务
     *
     * @param gattServices
     */
    protected void displayGattService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<List<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();

        mGattCharacteristics = new ArrayList<>();

        String LIST_PROPERTY = "PROPERTIES";
        String LIST_UUID = "UUID";

        for (BluetoothGattService gattService : gattServices) {
            final HashMap<String, String> currentServiceData = new HashMap<>();
            String serviceUuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_UUID, serviceUuid);

            gattServiceData.add(currentServiceData);

            final List<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> chars = new ArrayList<>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                chars.add(gattCharacteristic);
                final HashMap<String, String> currentCharaData = new HashMap<>();
                String characteristicUuid = gattCharacteristic.getUuid().toString();
                String property = gattCharacteristicPropertySwitch(gattCharacteristic);
                currentCharaData.put(LIST_PROPERTY, getString(R.string.ble_property) + property);
                currentCharaData.put(LIST_UUID, characteristicUuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(chars);
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(this,
                gattServiceData, android.R.layout.simple_expandable_list_item_1,
                new String[]{LIST_UUID},
                new int[]{android.R.id.text1},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_PROPERTY, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}) {

        };
        mDeviceService.setAdapter(gattServiceAdapter);
    }

    /**
     * 属性选择
     *
     * @param gattCharacteristic BluetoothGattCharacteristic
     * @return 属性
     */
    protected String gattCharacteristicPropertySwitch(BluetoothGattCharacteristic gattCharacteristic) {
        int stringRes;
        switch (gattCharacteristic.getProperties()) {
            case BluetoothGattCharacteristic.PROPERTY_BROADCAST:
                stringRes = R.string.ble_property_broadcast;
                break;
            case BluetoothGattCharacteristic.PROPERTY_READ:
                stringRes = R.string.ble_property_read;
                break;
            case BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE:
                stringRes = R.string.ble_property_write_no_response;
                break;
            case BluetoothGattCharacteristic.PROPERTY_WRITE:
            case BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE:
                stringRes = R.string.ble_property_write;
                break;
            case BluetoothGattCharacteristic.PROPERTY_NOTIFY:
                stringRes = R.string.ble_property_notify;
                break;
            case BluetoothGattCharacteristic.PROPERTY_INDICATE:
                stringRes = R.string.ble_property_indicate;
                break;
            default:
                stringRes = R.string.ble_property_extended_props;
                break;
        }
        return getString(stringRes);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (mGattCharacteristics == null || mGattCharacteristics.isEmpty()) {
            return false;
        }

        BluetoothGattCharacteristic notifyCharacteristic = null;
        final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
        final int properties = characteristic.getProperties();


        if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            notifyCharacteristic = characteristic;
            mBluetoothService.setCharacteristicNotification(characteristic, true);
        }

        if ((properties | BluetoothGattCharacteristic.PERMISSION_READ) > 0) { //读取属性
            if(notifyCharacteristic != null){
                mBluetoothService.setCharacteristicNotification(notifyCharacteristic,false);
                notifyCharacteristic = null;
            }

            mBluetoothService.readCharacteristic(characteristic);
        }

        return true;
    }
}
