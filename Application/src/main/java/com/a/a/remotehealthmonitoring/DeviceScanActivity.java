/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 * After this activity comes - DoctorOrPatient
 */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;


//These are the various libraries imported by us
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.Manifest;
import android.os.Build;
import android.util.Log;

// This activity will show us the list of available bluetooth devices
public class DeviceScanActivity extends ListActivity {
    //Bluetooth scanning-related Initialisations
    private LeDeviceListAdapter mLeDeviceListAdapter;  // shows the list of available bluetooth device
    private BluetoothAdapter mBluetoothAdapter;  // The BluetoothAdapter lets you perform fundamental Bluetooth tasks, such as initiate device discovery, query a list of bonded (paired) devices, instantiate a BluetoothDevice using a known MAC address, and create a BluetoothServerSocket to listen for connection requests from other devices.
    private boolean mScanning;  // is true when scanning takes place
    private Handler mHandler;  // to run bluetooth scanning continuously on User Interface

    private static final int REQUEST_ENABLE_BT = 1;  // static integer initialisation for enabling bluetooth

    private static final long SCAN_PERIOD = 10000;  // Stops scanning after 10 seconds.

    private static int PERMISSION_REQUEST_CODE = 1;  // static integer initialisation for enabling location permission
    private final static String TAG = DeviceScanActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like main() of C language.
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);  // This is to set the title at the toolbar
        mHandler = new Handler();  // mHandler object construction - to run bluetooth scanning continuously on User Interface

        Log.d(TAG, "Request Location Permissions:");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)  // If the current device's API level is > 29, then request for location permission
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)  // If the current device's API level is > 23 and less than 29, then request for location permission
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);  // requestPermission requires minimum API as 23 (M). So, for devices less than that API, it might not behave well, so, I'm including minimum API as 23
        }

/** From sdk =18 to sdk 28, allow permission all the time via dialog shown in UI. If not, manually allow location permission all the time by going to app permissions in settings*/
/** For sdk=29 (Android10), dialog only shows 'Allow only while using the app or deny'. None of them is important to us. You have to manually go to settings and 'allow location permission all the time' so that my BLE can be detected*/
/** For sdk=30 (Android11), dialog shows three options again, but all three are useless to us, so manually allow it to all the time from settings*/

        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();  // Toast shows a small notification bar to the user
            finish();
        }

        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    // Code for granting the location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if(requestCode == PERMISSION_REQUEST_CODE)
        {
            //Do something based on grantResults
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "Location permission granted");
                Toast.makeText(DeviceScanActivity.this, "Location Permission Granted! Make sure Location is turned on", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(DeviceScanActivity.this, "Please allow location permissions manually from the settings by clicking on 'Allow all the time'", Toast.LENGTH_LONG).show();
            }
        }
    }

    // On the top-right of the UI, the following function shows the option to 'STOP', 'START' bluetooth scanning
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);  // If scanning is not taking place, make the 'STOP' button invisible
            menu.findItem(R.id.menu_scan).setVisible(true);  // If scanning is not taking place, make the 'START' button visible
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);  // If scanning is taking place, make the 'STOP' button visible
            menu.findItem(R.id.menu_scan).setVisible(false);  // If scanning is taking place, make the 'START' button invisible
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:  // if from the top-right of the UI, 'START' option is chosen, then scan for availabe BLE device
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:  // if from the top-right of the UI, 'STOP' option is chosen, then stop scanning for availabe BLE device
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {  // On resume function is executed when we open this first activity after pausing it for some time
        super.onResume();

        // It checks again if Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter to show all available devices.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    // This function is called whenever startActivityForResult is called from any activity. Here it enables the bluetooth adapter.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if User chose not to enable Bluetooth via notification, come out of the app.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {  // Whenever we close the activity or pause it by opening another app, this function is called.
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();  // It clears all the scanning of bluetooth devices
    }


    // This function is executed when the user clicks on any of the bluetooth devices shown in the list
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);   // get the position of the clicked bluetooth device
        if (device == null) return;
        final Intent intent = new Intent(this, DoctorOrPatient.class);  // via Intent we open another activity (DoctorOrPatient in this case) on successfully connecting to the bluetooth device.
        intent.putExtra(DoctorOrPatient.EXTRAS_DEVICE_NAME, device.getName());  // Send the connected device's name (Bluno in this case) to the next activity
        intent.putExtra(DoctorOrPatient.EXTRAS_DEVICE_ADDRESS, device.getAddress());  // Send the connected device's address to the next activity

        if (mScanning) {  //if still bluetooth scanning is taking place, stop it
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);  // Start the next activity (DoctorOrPatient)
    }

    private void scanLeDevice(final boolean enable) {  // This function is executed when bluetooth scanning begins
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();  //Load a new menu bar in action bar (it takes the help of onCreateOptionsMenu activity)
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu(); //Load a new menu bar in action bar (it takes the help of onCreateOptionsMenu activity)
    }

    // Adapter for holding devices found through bluetooth scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();  // get the UI of this activity DeviceScanActivity
        }

        public void addDevice(BluetoothDevice device) {  // as new devices are discovered, add them in this activity
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }


        // It is called from onListItemClick function. This function makes sure to get the position of the clicked bluetooth device from the list of available devices
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        // It clears the list of available bluetooth devices, when not needed anymore
        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        // It returns the item that is clicked (1. 'START', 2. 'STOP')
        @Override
        public long getItemId(int i) {
            return i;
        }

        // we are using a custom layout to show the available ble devices. This function shows when to inflate that view or show that view on UI
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                // if the list of devices is not shown in UI yet, inflate the view
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            // it gets the device name of the clicked BLE device from the available devices and store its NAME in the deviceName field of ViewHolder class
            // it also gets the device address of the clicked BLE device from the available devices and store its ADDRESS in the deviceAddress field of ViewHolder class
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;  // show the view (list of available devices) on the UI.
        }
    }

    // Device scan callback. Callback interface shows the list of all devices resulted during a device scan.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {  // runOnUiThread contains the instructions that run continuoulsy on UI without blocking it
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);  // as new devices are discovered, keep adding them to the view
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    // This class has two fields - one for holding the deviceName of the clicked ble device from the list of available devices, another for holding the device address of that ble device
    static class ViewHolder {  // View holder class is used in getView function
        TextView deviceName;
        TextView deviceAddress;
    }

    //These two functions are executed when back button is pressed
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {  //if user presses any phone key
        if (keyCode == KeyEvent.KEYCODE_BACK) {  // check if that key is 'Back' key. If yes,
            onBackPressed();  // call this function
        }
        return super.onKeyDown(keyCode, event);
    }

    // on pressing back button
    // finish the activity
    public void onBackPressed() {
        finish();
    }
}