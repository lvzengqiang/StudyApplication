package com.sxt.chat.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

import com.sxt.chat.R;
import com.sxt.chat.base.HeaderActivity;
import com.sxt.chat.task.BinderService;
import com.sxt.chat.task.TaskService;

/**
 * Created by sxt on 2018/7/30.
 */

public class BinderActivity extends HeaderActivity {

    private boolean flag;
    private boolean bindFlag;
    private ServiceConnection serviceConnection;
    private final String TAG = this.getClass().getName();
    private BluetoothReceiver receiver;
    private final int REQUEST_CODE_BLUETOOTH = 100;
    private final int REQUEST_CODE_LOCATION = 101;
    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biner);
        startService(new Intent(this, TaskService.class));
    }

    public void startService(View view) {
        flag = !flag;
        if (flag) {
            startService(new Intent(this, BinderService.class));
        } else {
            stopService(new Intent(this, BinderService.class));
        }
    }

    public void bindService(View view) {
        bindFlag = !bindFlag;
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "onServiceConnected --> ComponentName : " + name.getClassName());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "onServiceDisconnected --> ComponentName : " + name.getClassName());
                }
            };
        }
        if (bindFlag) {
            bindService(new Intent(this, BinderService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            unbindService(serviceConnection);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void open(View view) {
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            receiver = new BluetoothReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(receiver, filter);
        }
        openBluetooth();
    }

    @Override
    public void onPermissionsaAlowed(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsaAlowed(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            checkPermission(REQUEST_CODE_BLUETOOTH, Manifest.permission.BLUETOOTH, new String[]{
                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN});
        }
    }

    @Override
    public void onPermissionsRefused(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsRefused(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsRefusedNever(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsRefusedNever(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openBluetooth() {
        if (bluetoothManager != null && bluetoothManager.getAdapter() != null) {
            if (!bluetoothManager.getAdapter().enable()) {
                checkPermission(REQUEST_CODE_LOCATION, Manifest.permission_group.LOCATION, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

            } else {
                Log.i(TAG, "Bluetooth Is Alreadly Opend ");
            }
        }
    }

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        stopService(new Intent(this, BinderService.class));
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
