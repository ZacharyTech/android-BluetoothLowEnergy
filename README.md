# Bluetooth Low Energy

### 前言 ###
  - Bluetooth SIG宣布正式采用蓝牙核心规格版本4.0时，它包括标志性的Bluetooth® Smart（低功耗）功能。采用流程的完成开启了所有蓝牙产品类型进行4.0版资格认证的大门。

- 蓝牙智能（低功耗）无线技术的主要特点包括：
  1. 超低峰值、平均值和待机模式能耗
  2. 标准纽扣电池能让设备运行数年
  3. 低成本​​
  4. 多供应商互操作性​​
  5.  增强射程
<p>
- 蓝牙智能（低功耗）技术允许对采用蓝牙无线技术的手表、牙刷或玩具等设备进行增强。它还使开发人员能够在已采用蓝牙技术的设备，如运动及健身、医疗保健、人机界面(HID)和娱乐设备中加入新的功能。例如，计步器和血糖仪中的传感器采用低耗能技术就可运行。4.0版具备的节能以及低成本应用特点将为这些单模式设备带来优势。手表可以同时使用低耗能技术（从身体上穿戴的健身传感器收集数据)和传统蓝牙技术(将数据发送至个人电脑或无线连接至智能手机以显示来电信息）。智能手机及个人电脑利用双模式方案，同时运行传统、低耗能及高速蓝牙技术，从而能够支持该规格所能实现的最广泛用例

- Android于 4.3(Api 18)开始引入对低功耗蓝牙(Bluetooth Low Energy)的支持，结束了之前各Android厂商对低功耗蓝牙支持的乱象。目前市面上常见的可穿戴设备都是基于低功耗蓝牙与手机通信，如 手环、智能手表等

### 相关术语 ###

- 通用属性配置文件(GATT)：  GATT配置文件是一个通用规范，用于通过BLE链路发送和接收`属性`数据。目前所有低功耗蓝牙都遵循GATT 协议
- 属性协议(ATT)： GATT建立在属性协议(ATT)的顶部,也被称为GATT/ATT。ATT为在BLE设备上运行进行了优化，为此它最好尽可能的使用最少的字节。每个属性唯一的标识由一个通用唯一标识符(UUID),唯一标识信息使用128位UUID格式。
- 特征(Characteristic )： 特征包含描述特征值的单个值和0-n个描述符
- 描述 符(Descriptors)：  描述特征值的属性。
- 服务(Service)： 服务是特征的集合。

### 权限 ###

	<uses-permission android:name="android.permission.BLUETOOTH"/>
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
	
	<!-- 设置为true时App仅适用于支持低功耗蓝牙的设备，false时App适用于所有设备 -->
	<uses-feature android:name="android.hardware.bluetooth_le" android:required="false"/>

### 开发 ###

 1. 检测设备对Bluetooth 以及Bluetooth low energy 的支持

			//因为在清单文件中设置App适用于不支持BLE的设备，这里需要对不支持BLE的设备进行相应处理
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
				finish();
			}

			//检测设备是否支持蓝牙
			BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			BluetoothAdapter  mBluetoothAdapter = bm.getAdapter();
			//设备不支持蓝牙
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
				finish();
			}
 2. 扫描设备
			
		if (b) {
			mDeviceAdapter.clear();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					scanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_DURATION); //在指定时长后停止扫描
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		

 3. 连接设备
		
		//第二个参数如设置为true 表示自动连接
		mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
		//mBluetoothGatt.connect(); //手动连接
 4. 发现服务
 
		gatt.discoverServices();//探索服务
 5. 发送特征值/接收特征值/读取特征 值
 
		BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
 6. 关闭连接
 
		gatt.disconnect();
		gatt.close();

### Ref ###

[http://developer.bluetooth.cn/](http://developer.bluetooth.cn "Bluetooth 开发者门户")
[http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth-le.html](http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth-le.html "Android API指南")
