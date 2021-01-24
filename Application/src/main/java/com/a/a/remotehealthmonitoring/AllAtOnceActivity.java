/** We land here from - AfterLoginActivity (after the user clicks on "multiple-sensor" button)
 * This activity provides options (buttons) for - Recording user data, showing graph, capturing graph photo,
 * saving csv in internal storage, sending data on Firebase and sharing files with the doctor
 * multiple-sensor data (ECG, EMG, GSR and Pulse) is recorded. */

//This is the package name of our project.
//The project-related files (like CSV and images) will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


public class AllAtOnceActivity extends AppCompatActivity {
    private final static String TAG = AllAtOnceActivity.class.getSimpleName();  // TAG just stores the name of this activity. It will be used while printing logs in the console

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";  // this string will store the device name obtained from the previous activity
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";  // this string will store the device address obtained from the previous
    public String mDeviceName, mDeviceAddress;  // strings will copy the device name and address obtained from previous strings to these n

    private TextView mConnectionState;  // declaring a textView 'mConnectionState' - it contains the connection state of the bluetooth device (i.e., connected or disconnected)
    private TextView mDataField_ecg, mDataField_emg, mDataField_gsr, mDataField_pulse;   // declaring 4 textViews  - first one contains the values obtained from the ecg sensor, second contains the values obtained from the emg sensor, third contains the values obtained from the gsr sensor, fourth contains the values obtained from the pulse sensor
    private ExpandableListView mGattServicesList;  // this list is shown when we successfully connect to the BLE device. It shows various properties lie UID of BLE, etc
    private BluetoothLeService mBluetoothLeService;  // this provides various services associated with bluetooth like sending data to BLE, receiving data to BLE, etc
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();  // different GATT characteristics associated with connected BLE device are shown here
    private boolean mConnected = false;  // if the BLE device is not connected with android, 'mConnected' variable is set to false (otherwise true)
    private BluetoothGattCharacteristic mNotifyCharacteristic;  // this is useful while receiving a character from the BLE device
    public final String LIST_NAME = "NAME";  // This is used in the boiler code for defining BLE GATT characteristics
    public final String LIST_UUID = "UUID";  // This is used in the boiler code for defining BLE GATT characteristics
    private BluetoothGattCharacteristic bluetoothGattCharacteristicHM_10;  // variable 'bluetoothGattCharacteristicHM_10' holds the UID of our remote BLE deive (Bluno)  // it is initialised in 'displayGattSerivces' function

    private final ServiceConnection mServiceConnection = new ServiceConnection() {  // Code to manage Service lifecycle.
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {  // here the Android bluetooth adapter binds to the remote BLE adapter by initialising the 'BluetoothLeService' class
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();  // 'getService' function is responsible for connecting this activity with the 'BluetoothLeService' class
            if (!mBluetoothLeService.initialize()) {  // if somehow the bluetooth binding fails, show the following toast notofication
                Toast.makeText(AllAtOnceActivity.this, "Unable to initialize Bluetooth", Toast.LENGTH_SHORT).show();
                finish();  //Finish() method will destroy the current activity.
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);  // connect the android device with a BLE device having the address 'mDeviceAddress'
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {  // if the BLE is disconnected, make the 'mBluetoothLeService' as null
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {  // BroadcastReceiver is responsible for receiving data from the remote Bluno device. BroadcastReceiver function is called ever 40ms. If data is sent from arduino faster than that, it gets lost!
        @Override
        public void onReceive(Context context, Intent intent) {  // on receiving the data, execute the following
            final String action = intent.getAction();  // 'action' stores the type of activity to perform on the basis of the data received from the BLE device
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  // if the data received from the remote BLE device is a connection request, then,
                mConnected = true;  // make this boolean variable as true
                updateConnectionState(R.string.connected);  // get connected to the BLE
                invalidateOptionsMenu();  // update the options (i.e. CONNECT/DISCONNECT) on the top-right corner of toolbar
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {  // if the data received from the remote BLE device is a disconnection request, then,
                mConnected = false;  // make this boolean variable as true
                updateConnectionState(R.string.disconnected);  // get disconnected to the BLE
                invalidateOptionsMenu();  // update the options (i.e. CONNECT/DISCONNECT) on the top-right corner of toolbar
                clearUI();  // this hides all the options relating to recording data (because BLE is no more connected to android device)
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {  // if the data received from the remote BLE device are GATT characteristics, then,
                displayGattServices(mBluetoothLeService.getSupportedGattServices());  // Show all the supported services and characteristics on the user interface.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {  // if the data received from the remote BLE device is the sensor values
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));  // call the 'displayData' function and pass this received sensor reading to it
            }
        }
    };

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;  // location updates interval - 30 sec
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;  // fastest updates interval - 5 sec
    private static final int REQUEST_CHECK_SETTINGS = 100;  // to check the location permission
    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;  // to listen to location updates
    private SettingsClient mSettingsClient;  // to listen to location settings on phone
    private LocationRequest mLocationRequest;  // to request for latitude and longitude values
    private LocationSettingsRequest mLocationSettingsRequest;  // to request for location settings
    private LocationCallback mLocationCallback;  // when location values are available this is called
    private Location mCurrentLocation;  // stores the current location
    private Boolean mRequestingLocationUpdates=true;  // it is true when location permission is allowed (otherwise false)

    private Button clickForService, graphResetBtn, graphBtn, stopGraphBtn, sendToCloud, stopToCloud, sendToDoctor, capture_ecg, capture_emg, capture_gsr, capture_pulse;  // declaring 11 buttons in this activity - first to connect to BLE services, second to reset the graph, third to show the graph, fourth to stop recording the graph, fifth to send data to cloud, sixth to stop sending data to cloud, seventh to send the files to the doctor, eighth to take screenshot of the ecg graph, ninth to take screenshot of the emg graph. tenth to take screenshot of the gsr graph, eleventh to take screenshot of the pulse graph
    private int push=2;  // value of 'push' for sending to cloud is 1 and to stop sending to cloud is 0
    private String graphXaxis_string=null;  // this stores the value received from previous activity via intent ("All") - indicating that the user wants to receive the data from 4 sensors ("ECG", "EMG", "GSR", "Pulse") at once

    float n_ecg, n_emg, n_gsr, n_pulse;  // This will store the float value received from ecg, emg, gsr and pulse sensors respectively
    private LineGraphSeries<DataPoint> series_ecg;  // this initialises the graph component for storing ecg datapoints
    private LineGraphSeries<DataPoint> series_emg;  // this initialises the graph component for storing emg datapoints
    private LineGraphSeries<DataPoint> series_gsr;  // this initialises the graph component for storing gsr datapoints
    private LineGraphSeries<DataPoint> series_pulse;  // this initialises the graph component for storing pulse datapoints
    double lastX1 = 0, lastX2 = 0, lastX3 = 0, lastX4 =0;  // this stores the counts of datapoints i.e. 1,2,3 - will be used while displaying ecg, emg, gsr and pulse graphs respectively
    GraphView graph_ecg, graph_emg, graph_gsr, graph_pulse;  // initialising the graph component for ecg, emg, gsr and pulse respectively
    int count = 0;
    private String dataStr_ecg, dataStr_emg, dataStr_gsr, dataStr_pulse;  // it stores the string value of ecg, emg, gsr and pulse sensor readings respectively

    String time, currentDateString;   // to store the time string and date string
    SimpleDateFormat mSimpleDateFormat, mSimpleTimeFormat;  // to set date format and time format to store in database
    Calendar mCalendar;  // to initialise calandar

    public FirebaseAuth mAuth;  // declaring firebaseAuth component - used while authenticating the user
    private DatabaseReference mDatabaseRef_Timestamp, mDatabaseRef_Info;  // initialise the DatabaseReference of the firebase cloud
    private FirebaseUser user;  // declaring firebaseUser component - used to verify current user

    Handler handler;  // declaring handler function to run a thread without crashing UI
    int index1=0, index2=0, index3=0, index4=0;  // stores the index of the data string of ecg, emg, gsr and pulse, respectively

    File myExternalFile;  // file declaration to store the CSV in the internal storage
    String csvTime, csvCurrentDateString;  // string to store time and date, respectively, in the CSV file
    SimpleDateFormat csvSimpleDateFormat, csvSimpleTimeFormat;  // to set date format and time format to store in CSV file
    Calendar csvCalendar;  // to initialise calandar for CSV
    boolean csvRecord = false;  // if CSV button is pushed, 'csvRecord' is set to TRUE (otherwise 'FALSE)
    private Button csvBtn;  // declaring a button - to start recording values in CSV file
    private String filepath;  //This is the folder name in the phone's storage, which will be patient's local email part

    private Bitmap bitmap;  // stores the bitmap of the captured graph screenshot
    private LinearLayout captureView_ecg, captureView_emg, captureView_gsr, captureView_pulse;  // captureView_ecg is the layout where graph is displayed for ECG values, captureView_emg is the layout where graph is displayed for EMG values, captureView_gsr is the layout where graph is displayed for GSR values, captureView_pulse is the layout where graph is displayed for Pulse values

    //..... For SendToDoctor .....
    private static final int REQUEST_CODE = 6384;  // static integer initialisation - used while extracting the URI of the file selected from internal storage
    private String path="";  // stores path of the file selected from file chooser
    private Uri uri;  // stores the URI of the file to be sent to the doctor
    private String FILE="";  // this string stores the path of the file to be sent to the doctor
    private static final int PICK_FILE_REQUEST = 1;  // static integer initialisation - used while picking the file from internal storage


    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like imt main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_at_once);  // this is the XML file where you can find the layout of this activity

        Firebase.setAndroidContext(this);  // this command is useful when we refer to database locations
        mAuth = FirebaseAuth.getInstance();  //initialise the firebase authentication variable
        user = mAuth.getCurrentUser();  //current user who is logged in
        mDatabaseRef_Timestamp = FirebaseDatabase.getInstance().getReference("User_Timestamp_MultiSensor");  // initialise the DatabaseReference of the firebase cloud, containing date-time sensor values for multiple (4) sensor
        mDatabaseRef_Info =  FirebaseDatabase.getInstance().getReference().child("User_Info");  // initialise the DatabaseReference of the firebase cloud, containing user info
        handler = new Handler();  //  the handler function is used to run a thread over UI

        captureView_ecg = findViewById(R.id.captureView_ecg);  // attach 'captureView_ecg' layout from the xml file to the variable 'captureView_ecg'
        captureView_emg = findViewById(R.id.captureView_emg);  // attach 'captureView_emg' layout from the xml file to the variable 'captureView_emg'
        captureView_gsr = findViewById(R.id.captureView_gsr);  // attach 'captureView_gsr' layout from the xml file to the variable 'captureView_gsr'
        captureView_pulse = findViewById(R.id.captureView_pulse);  // attach 'captureView_pulse' layout from the xml file to the variable 'captureView_pulse'

        clickForService = findViewById(R.id.clickForService);  // attach 'clickForService' button from the xml file to the variable 'clickForService'
        clickForService.setVisibility(View.INVISIBLE);  // make the 'clickForService' button invisible
        graphBtn = findViewById(R.id.graphBtn);  // attach 'graphBtn' button from the xml file to the variable 'graphBtn'
        graphBtn.setVisibility(View.INVISIBLE);  // make the 'graphBtn' button invisible
        stopGraphBtn = findViewById(R.id.stopGraphBtn);  // attach 'stopGraphBtn' button from the xml file to the variable 'stopGraphBtn'
        stopGraphBtn.setVisibility(View.INVISIBLE);  // make the 'stopGraphBtn' button invisible
        graphResetBtn = findViewById(R.id.graphResetBtn);  // attach 'graphResetBtn' button from the xml file to the variable 'graphResetBtn'
        graphResetBtn.setVisibility(View.INVISIBLE);  // make the 'graphResetBtn' button invisible
        sendToCloud = findViewById(R.id.sendToCloud);  // attach 'sendToCloud' button from the xml file to the variable 'sendToCloud'
        sendToCloud.setVisibility(View.INVISIBLE);  // make the 'sendToCloud' button invisible
        stopToCloud = findViewById(R.id.stopToCloud); // attach 'stopToCloud' button from the xml file to the variable 'stopToCloud'
        stopToCloud.setVisibility(View.INVISIBLE);  // make the 'stopToCloud' button invisible
        csvBtn = findViewById(R.id.record_CSV);  // attach 'record_CSV' button from the xml file to the variable 'csvBtn'
        csvBtn.setVisibility(View.INVISIBLE);  // make the 'record_CSV' button invisible
        capture_ecg = findViewById(R.id.capture_ecg);  // attach 'capture_ecg' button from the xml file to the variable 'capture_ecg'
        capture_emg = findViewById(R.id.capture_emg);  // attach 'capture_emg' button from the xml file to the variable 'capture_emg'
        capture_gsr = findViewById(R.id.capture_gsr);  // attach 'capture_gsr' button from the xml file to the variable 'capture_gsr'
        capture_pulse = findViewById(R.id.capture_pulse);  // attach 'capture_pulse' button from the xml file to the variable 'capture_pulse'
        sendToDoctor = findViewById(R.id.sendToDoctor);  // attach 'sendToDoctor' button from the xml file to the variable 'sendToDoctor'
        sendToDoctor.setVisibility(View.INVISIBLE);  // make the 'sendToDoctor' button invisible
        mDataField_ecg = (TextView) findViewById(R.id.data_value_ecg);  // attach 'data_value_ecg' textView from the xml file to the variable 'mDataField_ecg'
        mDataField_emg = (TextView) findViewById(R.id.data_value_emg);  // attach 'data_value_emg' textView from the xml file to the variable 'mDataField_emg'
        mDataField_gsr = (TextView) findViewById(R.id.data_value_gsr);  // attach 'data_value_gsr' textView from the xml file to the variable 'mDataField_gsr'
        mDataField_pulse = (TextView) findViewById(R.id.data_value_pulse);  // attach 'data_value_pulse' textView from the xml file to the variable 'mDataField_pulse'

        String email_string = user.getEmail();  // variable 'email_string' will store the user email
        if (email_string != null) {  // if the variable 'email_string' is not null
            filepath = email_string.trim();  // store the email in the variable 'filepath'
        } else {  // if the variable 'email_string' is null
            Toast.makeText(AllAtOnceActivity.this, "Filepath is empty", Toast.LENGTH_SHORT).show();
        }

        // these 3 lines of code uses the Intent component to receive the deviceName and deviceAddress from the previous activities and store them in new strings
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        graphXaxis_string = intent.getStringExtra("graphXaxis");  // Here we use Intent component to receive the value received from the previous activity ("All") and store it in the 'graphXaxis_string'
        if (graphXaxis_string != null) {  // if the 'graphXaxis_string' is not null
            graphXaxis_string = graphXaxis_string.trim(); // trim() just removes any redundant space or new line statement
        } else if (graphXaxis_string == null) {  // if the 'graphXaxis_string' is null
            Toast.makeText(AllAtOnceActivity.this, "intent problem!", Toast.LENGTH_SHORT).show(); // show the given toast notification
        }

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_name)).setText(mDeviceName);  // i.e. in the UI, set the value of 'device_name' text field to be the 'mDeviceName' obtained via intent component
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);  // i.e. in the UI, set the value of 'device_address' text field to be the 'mDeviceAddress' obtained via intent component

        mConnectionState = (TextView) findViewById(R.id.connection_state);  // attach 'connection_state' text field from the xml file to the variable 'mConnectionState'
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);  // attach 'gatt_services_list' text field from the xml file to the variable 'mGattServicesList'

        Intent gattServiceIntent = new Intent(AllAtOnceActivity.this, BluetoothLeService.class);  // here we initialise the Intent component
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);  // we use the above given intent component to bind this activity with the 'BluetoothLeService' class

        // 'clickForService' button is available only when BLE device is successfully connected to our android device
        clickForService.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View v) {  // once we click on this button, execute the following
                graphBtn.setVisibility(View.VISIBLE);  // make the 'graphBtn' visible
                stopGraphBtn.setVisibility(View.VISIBLE);  // make the 'stopGraphBtn' visible
                stopGraphBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button as a very light color
                graphResetBtn.setVisibility(View.VISIBLE);  // make the 'graphResetBtn' visible
                sendToCloud.setVisibility(View.VISIBLE);  // make the 'sendToCloud' visible
                sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button as a very light color
                stopToCloud.setVisibility(View.VISIBLE);  // make the 'stopToCloud' visible
                stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button as a very light color
                csvBtn.setVisibility(View.VISIBLE);  // make the 'csvBtn' visible
                csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button as a very light color
                sendToDoctor.setVisibility(View.VISIBLE);  // make the 'sendToDoctor' visible
                sendToDoctor.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button as a very light color

                // TODO Auto-generated method stub
                if (mGattCharacteristics != null) {  // if mGattCharacteristics is not null (mGattCharacteristics are all the characteristics possessed by BLE device like device UUID, device manufacturer ID, etc)
                    final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(3).get(0);  // the .get(3).get(0) contains the important characteristic
                    clearUI(); // this hides all the options relating to recording data
                    final int charaProp = characteristic.getProperties();  // this returns an integer
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {  // only one of the properties can be activated at once - either READ data from the BLE device or WRITE/NOTIFY data to BLE device. If READ is activated,
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;  // set NOTIFY to be null
                        }
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);  // else don't set NOTIFY to be null
                    }
                }
                graphBtn.setEnabled(true);  // enable the button press on 'graphBtn'

                if (bluetoothGattCharacteristicHM_10 == null) {  // if the BLUNO device is null/missing, show the following Alert message
                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                    builder.setMessage("Your Custom Healthcare device is Missing");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();  //Finish() method will destroy the current activity.
                        }
                    });
                    builder.show();
                }
            }
        });

        // when 'graphResetBtn' button is clicked, reset the whole graph display
        graphResetBtn.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                graphBtn.setEnabled(true);  //enable this button called 'graphBtn'
                graphBtn.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color
                sendToCloud.setEnabled(false);   //enable this button called 'sendToCloud'
                sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                stopToCloud.setEnabled(false);  //disable this button that stops sending data to cloud
                stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                csvBtn.setEnabled(false);  //disable this button that records data to CSV
                csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color

                if (bluetoothGattCharacteristicHM_10 != null) {  // if the Bluno device is available
                    if(mConnected == true) {  // and if the device status is connected
                        mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristicHM_10, 'x');  // send character 'x' to the arduino (it indicates arduino, not to send any data to android)
                        graph_ecg.removeAllSeries();  // removeAllSeries() removes all the drawn datapoints of graph
                        graph_emg.removeAllSeries();
                        graph_gsr.removeAllSeries();
                        graph_pulse.removeAllSeries();
                        count = 0;  // on graph reset, set count variable = 0
                        Toast.makeText(AllAtOnceActivity.this, "Reset!", Toast.LENGTH_SHORT).show();  // set the toast notification
                    } else { // if the device status is disconnected, show the following toast notification
                        Toast.makeText(AllAtOnceActivity.this, "Your server device seems to be disconnected. Establish the connection first...", Toast.LENGTH_SHORT).show();
                        sendToCloud.setEnabled(false);  //disable this button that sends data to cloud
                        sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                        stopToCloud.setEnabled(false);  //disable this button that stops sending data to cloud
                        stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                        csvBtn.setEnabled(false);  //disable this button that records data to CSV
                        csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                    }
                }
                else if (bluetoothGattCharacteristicHM_10 == null) {  // if the Bluno device is unavailable, show the following alert box
                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                    builder.setMessage("Your Custom Healthcare device is Missing");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();  //Finish() method will destroy the current activity.
                        }
                    });
                    builder.show();
                }
            }
        });

        // when 'graphBtn' button is clicked, show the graph display in the UI
        graphBtn.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                graph_ecg.removeAllSeries(); //did this to avoid darkening graph color everytime this button is pressed
                graph_emg.removeAllSeries(); //did this to avoid darkening graph color everytime this button is pressed
                graph_gsr.removeAllSeries(); //did this to avoid darkening graph color everytime this button is pressed
                graph_pulse.removeAllSeries(); //did this to avoid darkening graph color everytime this button is pressed

                graph_ecg.addSeries(series_ecg);  // add new series (datapoints) to the graph of ecg
                graph_emg.addSeries(series_emg);  // add new series (datapoints) to the graph of emg
                graph_gsr.addSeries(series_gsr);  // add new series (datapoints) to the graph of gsr
                graph_pulse.addSeries(series_pulse);  // add new series (datapoints) to the graph of pulse

                graphBtn.setEnabled(false);  //disabling this 'graphBtn' button
                graphBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                stopGraphBtn.setEnabled(true);  //enabling this 'stopGraphBtn' button
                stopGraphBtn.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color

                sendToCloud.setEnabled(true);  //enabling this 'sendToCloud' button
                sendToCloud.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color  // setAlpha(255) sets the color of the button to a dark color
                stopToCloud.setEnabled(true);  //enabling this 'stopGraphBtn' button
                stopToCloud.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color  // setAlpha(255) sets the color of the button to a dark color
                csvBtn.setEnabled(true);  //enabling this 'csvBtn' button
                csvBtn.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color  // setAlpha(255) sets the color of the button to a dark color
                sendToDoctor.setEnabled(true);  //enabling this 'sendToDoctor' button
                sendToDoctor.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a dark color

                if(graphXaxis_string.equals("All")) {  // if the value of 'graphXaxis_string' is "All" - it means the user wants to read all four values (ecg, emg, gsr, pulse)
                    if (bluetoothGattCharacteristicHM_10 != null) { // first check if the device is available (if it is!)
                        if (mConnected == true) {  // check if the device is connected (if it is)
                            mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristicHM_10, 'a');  // send the character 'a' to the arduino - this is an indication that android wants to receive all 4 values from arduino
                        } else {  // if the device is not connected, show the following toast notification
                            Toast.makeText(AllAtOnceActivity.this, "Your server device seems to be disconnected. Establish the connection first...", Toast.LENGTH_SHORT).show();
                            sendToCloud.setEnabled(false);  // disabling the 'sendToCloud' button
                            sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                            stopToCloud.setEnabled(false);  // disabling the 'stopToCloud' button
                            stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                            csvBtn.setEnabled(false);  // disabling the 'csvBtn' button
                            csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                        }
                    } else if (bluetoothGattCharacteristicHM_10 == null) {  // if the device is unavailable, show the alert box
                        sendToCloud.setEnabled(false);  // disabling the 'sendToCloud' button
                        sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                        stopToCloud.setEnabled(false);  // disabling the 'stopToCloud' button
                        stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                        csvBtn.setEnabled(false);  // disabling the 'csvBtn' button
                        csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color

                        AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);  // syntax of showing the alert box
                        builder.setMessage("Your Custom Healthcare device is Missing");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();  // if we click the 'OK' button on alert box, it gets us out of this activity and takes us to the previous activity
                            }
                        });
                        builder.show();
                    }
                }
            }
        });

        // when 'stopGraphBtn' button is clicked, stop the graph display in the UI
        stopGraphBtn.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                graphBtn.setEnabled(true);  // enabling the 'graphBtn' button
                graphBtn.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a darker color
                stopGraphBtn.setEnabled(false);  // disabling the 'stopGraphBtn' button
                stopGraphBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color

                if (bluetoothGattCharacteristicHM_10 != null) { //this is the first time I've tried both the conditions together
                    if (mConnected == true) {  // check if the device is connected (if it is)
                        mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristicHM_10, 'x');  // send the character 'x' to the arduino - this is an indication that android wants to receive no values from arduino
                        Toast.makeText(AllAtOnceActivity.this, "Stopped Recording...", Toast.LENGTH_SHORT).show();  // set this toast notification
                    } else {  // if the device is not connected, show the following toast notification
                        Toast.makeText(AllAtOnceActivity.this, "Your server device seems to be disconnected from server. Establish the connection first...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {  // if the device is unavailable, show this toast notification
                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                    builder.setMessage("Your Custom Healthcare device is Missing");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();  //Finish() method will destroy the current activity.
                        }
                    });
                    builder.show();
                }

                sendToCloud.setEnabled(false);  // disabling the 'sendToCloud' button
                sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                stopToCloud.setEnabled(false);  // disabling the 'stopToCloud' button
                stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                csvBtn.setEnabled(false);  // disabling the 'csvBtn' button
                csvBtn.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
            }
        });

        // when 'sendToCloud' button is clicked, start sending the sensor values to the cloud
        sendToCloud.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                sendToCloud.setEnabled(false);  // disabling the 'sendToCloud' button
                sendToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                stopToCloud.setEnabled(true);  // enabling the 'stopToCloud' button
                stopToCloud.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a darker color
                push=1;  // push is set to one when 'sendToCloud' button is clicked
                Toast.makeText(AllAtOnceActivity.this, "Started Uploading to Database", Toast.LENGTH_SHORT).show();  // show the given toast notification
            }
        });

        // when 'stopToCloud' button is clicked, start sending the sensor values to the cloud
        stopToCloud.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                stopToCloud.setEnabled(false);  // disabling the 'stopToCloud' button
                stopToCloud.getBackground().setAlpha(64);  // setAlpha(64) sets the color of the button to a light color
                sendToCloud.setEnabled(true);  // enabling the 'sendToCloud' button
                sendToCloud.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a darker color
                push=0;  // push is set to 0 when 'stopToCloud' button is clicked
                Toast.makeText(AllAtOnceActivity.this, "Stopped Uploading to Database", Toast.LENGTH_SHORT).show();  // set the text of toast notification
            }
        });

        // when 'csvBtn' button is clicked, start recording the single-sensor values in the CSV file
        csvBtn.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                if (csvBtn.getText().toString().equals("Start")) {  // if the CSV button has the string "Start"
                    if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {  // check if the storage is available in the phone to store the CSV, if it is not available, show the following toast
                        Toast.makeText(AllAtOnceActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();
                        csvRecord = false;  // if CSV is not recorded, set csvRecord to false
                        csvBtn.setEnabled(true); // enable the CSV button
                        csvBtn.getBackground().setAlpha(255);  // setAlpha(255) sets the color of the button to a darker color
                    }
                    else {  // if the storage space is available,
                        Toast.makeText(AllAtOnceActivity.this, "Started writing CSV!", Toast.LENGTH_SHORT).show();  // show the following toast
                        String filename = filepath+"_"+String.valueOf(System.currentTimeMillis())+".csv";  // set the CSV file name
                        myExternalFile = new File(getExternalFilesDir(filepath), filename);  // the location where file has to be made
                        try {  // the following code starts writing the Header (or the first Row) of CSV file
                            FileOutputStream fos = new FileOutputStream(myExternalFile, true);  // make an instance of the CSV file and start writing to it
                            fos.write("Date".getBytes());  // first column is "Date"
                            fos.write(",".getBytes());
                            fos.write("Time".getBytes());  // second column is "Time"
                            fos.write(",".getBytes());
                            fos.write("ECG".getBytes());  // third column is the "ECG" value
                            fos.write(",".getBytes());
                            fos.write("EMG".getBytes());  // fourth column is the "EMG" value
                            fos.write(",".getBytes());
                            fos.write("Pulse".getBytes());  // fifth column is the "Pulse" value
                            fos.write(",".getBytes());
                            fos.write("GSR".getBytes());  // sixth column is the "GSR" value
                            fos.write("\n".getBytes());
                            fos.close();
                            csvRecord = true;  //it means csv record button has been pushed
                        } catch (Exception e) {  // if somehow, try statement fails to execute, show the following toast
                            e.printStackTrace();
                            Toast.makeText(AllAtOnceActivity.this, "Problem in writing CSV", Toast.LENGTH_SHORT).show();
                        }
                    }
                    csvBtn.setText("Stop");  // Also, set/toggle the CSV Button string to "STOP", so the next time we press the same CSV button, it will STOP writing to CSV, rather than START it
                }
                else if (csvBtn.getText().toString().equals("Stop")) {  // if the CSV button has the string "Stop"
                    csvRecord = false;  //it means csv stop button has been pushed
                    Toast.makeText(AllAtOnceActivity.this, "Your file is saved!", Toast.LENGTH_SHORT).show();  // show the toast, stating that the CSV recording has stopped and the file has been saved
                    csvBtn.setText("Start");  // Also, set/toggle the CSV Button string to "START", so the next time we press the same CSV button, it will START writing to CSV
                }
            }
        });

        // when 'sendToDoctor' button is clicked, show the various apps via which files can be sent to the doctor (whatsapp, email, etc)
        sendToDoctor.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                //strictMode solution is needed before file browsing or starting camera to avoid the error in the sendThroughOtherApps() function
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent target = MakeGraph_FileUtils.createGetContentIntent();  // Intent component helps to choose from different apps like whatsapp, email
                Intent intent = Intent.createChooser(target, "Lorem ipsum");
                try {
                    startActivityForResult(intent, REQUEST_CODE);  // when Intent is active, try calling onActivityResult
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(AllAtOnceActivity.this, "Activity Not Found!", Toast.LENGTH_SHORT).show();  // if somehow, try statement fails, display this toast
                }
            }
        });

        // when 'capture_ecg' button is clicked, capture the screenshot of the ecg graph and store it in the phone's memory
        capture_ecg.setOnClickListener(new View.OnClickListener() {  // listen for any clicks on this button
            @Override
            public void onClick(View view) {  // once we click on this button, execute the following
                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {  // if the storage space is not available on phone, show the following toast
                    Toast.makeText(AllAtOnceActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();
                } else { // if the storage space is available, capture the layout and store it in the phone
                    try {  // try executing the following
                        // the following 9 lines are responsible for capturing bitmap of the 'captureView_ecg' layout
                        captureView_ecg.measure(View.MeasureSpec.makeMeasureSpec(captureView_ecg.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(captureView_ecg.getHeight(), View.MeasureSpec.EXACTLY));
                        captureView_ecg.layout((int) captureView_ecg.getX(), (int) captureView_ecg.getY(), (int) captureView_ecg.getX() + captureView_ecg.getMeasuredWidth(), (int) captureView_ecg.getY() + captureView_ecg.getMeasuredHeight());
                        captureView_ecg.setDrawingCacheEnabled(true);
                        captureView_ecg.buildDrawingCache(true);
                        Bitmap returnedBitmap = Bitmap.createBitmap(captureView_ecg.getDrawingCache());  // first, create an empty bitmap having the dimensions of 'captureView' layout
                        Canvas canvas = new Canvas(returnedBitmap);  // now draw this bitmap on canvas
                        canvas.drawColor(Color.WHITE);  // the canvas background is set to WHITE
                        captureView_ecg.draw(canvas); // Now draw the captureView on this bitmap which is on top of a canvas
                        captureView_ecg.setDrawingCacheEnabled(false);  // after drawing everything, successfully, disable drawing

                        // the next 5 lines are responsible for storing this captured bitmap in the phone's storage
                        if(returnedBitmap!=null) {  // if the bitmap of the image is not null,
                            String filename = "ecg"+ "_" + String.valueOf(System.currentTimeMillis()) +".jpg";  // set the screenshot file name
                            myExternalFile = new File(getExternalFilesDir(filepath), filename);  // the location where file has to be made

                            FileUtil.getInstance().storeBitmap(returnedBitmap, myExternalFile.toString());  // store the image bitmap to the given file location,
                            Toast.makeText(AllAtOnceActivity.this, "Captured and Saved!" , Toast.LENGTH_LONG).show();  // show this toast notification
                        } else {  // else-if the bitmap of the image is null, show the toast
                            Toast.makeText(AllAtOnceActivity.this, "Bitmap could not be created!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {  // if the 'try' statement fails, execute the following
                        Toast.makeText(AllAtOnceActivity.this, "Can't take screenshot!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // when 'capture_emg' button is clicked, capture the screenshot of the emg graph and store it in the phone's memory
        capture_emg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                    Toast.makeText(AllAtOnceActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();
                } else { // if the storage space is available, capture the layout and store it in the phone
                    try {  // try executing the following
                        // the following 9 lines are responsible for capturing bitmap of the 'captureView' layout
                        captureView_emg.measure(View.MeasureSpec.makeMeasureSpec(captureView_emg.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(captureView_emg.getHeight(), View.MeasureSpec.EXACTLY));
                        captureView_emg.layout((int) captureView_emg.getX(), (int) captureView_emg.getY(), (int) captureView_emg.getX() + captureView_emg.getMeasuredWidth(), (int) captureView_emg.getY() + captureView_emg.getMeasuredHeight());
                        captureView_emg.setDrawingCacheEnabled(true);
                        captureView_emg.buildDrawingCache(true);
                        Bitmap returnedBitmap = Bitmap.createBitmap(captureView_emg.getDrawingCache());  // first, create an empty bitmap having the dimensions of 'captureView' layout
                        Canvas canvas = new Canvas(returnedBitmap);  // now draw this bitmap on canvas
                        canvas.drawColor(Color.WHITE);  // the canvas background is set to WHITE
                        captureView_emg.draw(canvas); // Now draw the captureView on this bitmap which is on top of a canvas
                        captureView_emg.setDrawingCacheEnabled(false);  // after drawing everything, successfully, disable drawing

                        // the next 5 lines are responsible for storing this captured bitmap in the phone's storage
                        if(returnedBitmap!=null) {  // if the bitmap of the image is not null,
                            String filename = "emg"+ "_" +String.valueOf(System.currentTimeMillis())+".jpg";    // set the screenshot file name
                            myExternalFile = new File(getExternalFilesDir(filepath), filename);  // the location where file has to be made

                            FileUtil.getInstance().storeBitmap(returnedBitmap, myExternalFile.toString());  // store the image bitmap to the given file location,
                            Toast.makeText(AllAtOnceActivity.this, "Captured and Saved!" , Toast.LENGTH_LONG).show();  // show this toast notification
                        } else {  // else-if the bitmap of the image is null, show the toast
                            Toast.makeText(AllAtOnceActivity.this, "Bitmap could not be created!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {  // if the 'try' statement fails, execute the following
                        Toast.makeText(AllAtOnceActivity.this, "Can't take screenshot!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        // when 'capture_gsr' button is clicked, capture the screenshot of the gsr graph and store it in the phone's memory
        capture_gsr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                    Toast.makeText(AllAtOnceActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();
                }
                else { // if the storage space is available, capture the layout and store it in the phone
                    try {  // try executing the following
                        // the following 9 lines are responsible for capturing bitmap of the 'captureView' layout
                        captureView_gsr.measure(View.MeasureSpec.makeMeasureSpec(captureView_gsr.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(captureView_gsr.getHeight(), View.MeasureSpec.EXACTLY));
                        captureView_gsr.layout((int) captureView_gsr.getX(), (int) captureView_gsr.getY(), (int) captureView_gsr.getX() + captureView_gsr.getMeasuredWidth(), (int) captureView_gsr.getY() + captureView_gsr.getMeasuredHeight());
                        captureView_gsr.setDrawingCacheEnabled(true);
                        captureView_gsr.buildDrawingCache(true);
                        Bitmap returnedBitmap = Bitmap.createBitmap(captureView_gsr.getDrawingCache());  // first, create an empty bitmap having the dimensions of 'captureView' layout
                        Canvas canvas = new Canvas(returnedBitmap);  // now draw this bitmap on canvas
                        canvas.drawColor(Color.WHITE);  // the canvas background is set to WHITE
                        captureView_gsr.draw(canvas); // Now draw the captureView on this bitmap which is on top of a canvas
                        captureView_gsr.setDrawingCacheEnabled(false);  // after drawing everything, successfully, disable drawing

                        // the next 5 lines are responsible for storing this captured bitmap in the phone's storage
                        if(returnedBitmap!=null) {  // if the bitmap of the image is not null,
                            String filename = "gsr"+"_"+String.valueOf(System.currentTimeMillis())+".jpg";    // set the screenshot file name
                            myExternalFile = new File(getExternalFilesDir(filepath), filename);  // the location where file has to be made

                            FileUtil.getInstance().storeBitmap(returnedBitmap, myExternalFile.toString());  // store the image bitmap to the given file location,
                            Toast.makeText(AllAtOnceActivity.this, "Captured and Saved!" , Toast.LENGTH_LONG).show();  // show this toast notification
                        } else {  // else-if the bitmap of the image is null, show the toast
                            Toast.makeText(AllAtOnceActivity.this, "Bitmap could not be created!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {  // if the 'try' statement fails, execute the following
                        Toast.makeText(AllAtOnceActivity.this, "Can't take screenshot!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // when 'capture_pulse' button is clicked, capture the screenshot of the pulse graph and store it in the phone's memory
        capture_pulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                    Toast.makeText(AllAtOnceActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();
                }
                else { // if the storage space is available, capture the layout and store it in the phone
                    try {  // try executing the following
                        // the following 9 lines are responsible for capturing bitmap of the 'captureView' layout
                        captureView_pulse.measure(View.MeasureSpec.makeMeasureSpec(captureView_pulse.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(captureView_pulse.getHeight(), View.MeasureSpec.EXACTLY));
                        captureView_pulse.layout((int) captureView_pulse.getX(), (int) captureView_pulse.getY(), (int) captureView_pulse.getX() + captureView_pulse.getMeasuredWidth(), (int) captureView_pulse.getY() + captureView_pulse.getMeasuredHeight());
                        captureView_pulse.setDrawingCacheEnabled(true);
                        captureView_pulse.buildDrawingCache(true);
                        Bitmap returnedBitmap = Bitmap.createBitmap(captureView_pulse.getDrawingCache());  // first, create an empty bitmap having the dimensions of 'captureView' layout
                        Canvas canvas = new Canvas(returnedBitmap);  // now draw this bitmap on canvas
                        canvas.drawColor(Color.WHITE);  // the canvas background is set to WHITE
                        captureView_pulse.draw(canvas); // Now draw the captureView on this bitmap which is on top of a canvas
                        captureView_pulse.setDrawingCacheEnabled(false);  // after drawing everything, successfully, disable drawing

                        // the next 5 lines are responsible for storing this captured bitmap in the phone's storage
                        if(returnedBitmap!=null) {  // if the bitmap of the image is not null,
                            String filename = "pulse"+"_"+String.valueOf(System.currentTimeMillis())+".jpg";    // set the screenshot file name
                            myExternalFile = new File(getExternalFilesDir(filepath), filename);  // the location where file has to be made

                            FileUtil.getInstance().storeBitmap(returnedBitmap, myExternalFile.toString());  // store the image bitmap to the given file location,
                            Toast.makeText(AllAtOnceActivity.this, "Captured and Saved!" , Toast.LENGTH_LONG).show();  // show this toast notification
                        } else {  // else-if the bitmap of the image is null, show the toast
                            Toast.makeText(AllAtOnceActivity.this, "Bitmap could not be created!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {  // if the 'try' statement fails, execute the following
                        Toast.makeText(AllAtOnceActivity.this, "Can't take screenshot!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        graph_ecg = findViewById(R.id.graphview_ecg);  // attach 'graphview_ecg' graph component from the xml file to the variable 'graph_ecg'
        series_ecg = new LineGraphSeries<DataPoint>();  // initialise the graph series component
        Viewport viewport1 = graph_ecg.getViewport();  // initialise the view of the graph
        viewport1.setYAxisBoundsManual(false);  // disallow to expand the y-axis manually by pinch Zoom
        viewport1.setMinY(0);  // the minimum Y-axis value could be 0
        viewport1.setMaxY(700);  // the maximum Y-axis value could be 700
        viewport1.setXAxisBoundsManual(true); // allows to expand the X-axis manually by pinch Zoom
        viewport1.setMinX(0);  // the minimum X-axis value could be 0
        viewport1.setMaxX(200);  // the maximum X-axis value could be 0
        viewport1.setScrollable(true);  // the X-axis and Y-axis scrolling is allowed
//        graph.setTitleColor(Color.rgb(255,193,7));
        graph_ecg.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graph_ecg.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel1 = graph_ecg.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel1.setHorizontalAxisTitle("                       TIME(sec)");  // Here we set the X-axis Label as 'TIME'
        gridLabel1.setVerticalAxisTitle("ecg values");  // Here we set the Y-axis Label as 'Values'
        graph_ecg.getViewport().setScalable(true);   // X-axis zooming/expansion is allowed
        graph_ecg.getViewport().setScalableY(true);   // Y-axis zooming/expansion is allowed
//        series_ecg.setDrawDataPoints(true);
        series_ecg.setTitle("ECG Data");  // This sets the Title of the graph
        series_ecg.setDrawBackground(true);  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        series_ecg.setDrawAsPath(true);  // The datapoints draw is set true
        graph_ecg.getLegendRenderer();


        // like we have done initialisations for ecg graph above, similarly we will do for emg, gsr and pulse

        graph_emg = findViewById(R.id.graphview_emg);
        series_emg = new LineGraphSeries<DataPoint>();
        Viewport viewport2 = graph_emg.getViewport();
        viewport2.setYAxisBoundsManual(false);
        viewport2.setMinY(0);
//        viewport2.setMaxY(1000);
        viewport2.setXAxisBoundsManual(true);
        viewport2.setMinX(0);
        viewport2.setMaxX(200);
        viewport2.setScrollable(true);
        graph_emg.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));
        graph_emg.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));
        GridLabelRenderer gridLabel2 = graph_emg.getGridLabelRenderer();
        gridLabel2.setHorizontalAxisTitle("                       TIME(sec)");
        gridLabel2.setVerticalAxisTitle("emg values");
        graph_emg.getViewport().setScalable(true);
        graph_emg.getViewport().setScalableY(true);
        series_emg.setTitle("EMG Data");
//        series_emg.setDrawDataPoints(true);
        series_emg.setDrawAsPath(true);
//        series_emg.setDrawBackground(true);


        graph_gsr = findViewById(R.id.graphview_gsr);
        series_gsr = new LineGraphSeries<DataPoint>();
        Viewport viewport3 = graph_gsr.getViewport();
        viewport3.setYAxisBoundsManual(false);
        viewport3.setMinY(0);
//        viewport3.setMaxY(1000);
        viewport3.setXAxisBoundsManual(true);
        viewport3.setMinX(0);
        viewport3.setMaxX(200);
        viewport3.setScrollable(true);
        graph_gsr.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));
        graph_gsr.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));
        GridLabelRenderer gridLabel3 = graph_gsr.getGridLabelRenderer();
        gridLabel3.setHorizontalAxisTitle("                       TIME(sec)");
        gridLabel3.setVerticalAxisTitle("gsr values");
        graph_gsr.getViewport().setScalable(true);
        graph_gsr.getViewport().setScalableY(true);
        series_gsr.setTitle("GSR Data");
//        series_gsr.setDrawDataPoints(true);
        series_gsr.setDrawAsPath(true);
        series_gsr.setDrawBackground(true);


        graph_pulse = findViewById(R.id.graphview_pulse);
        series_pulse = new LineGraphSeries<DataPoint>();
        Viewport viewport4 = graph_pulse.getViewport();
        viewport4.setYAxisBoundsManual(false);
        viewport4.setMinY(0);
//        viewport4.setMaxY(1000);
        viewport4.setXAxisBoundsManual(true);
        viewport4.setMinX(0);
        viewport4.setMaxX(200);
        viewport4.setScrollable(true);
        graph_pulse.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));
        graph_pulse.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));
        GridLabelRenderer gridLabel4 = graph_pulse.getGridLabelRenderer();
        gridLabel4.setHorizontalAxisTitle("                       TIME(sec)");
        gridLabel4.setVerticalAxisTitle("pulse values");
        graph_pulse.getViewport().setScalable(true);
        graph_pulse.getViewport().setScalableY(true);
//        series_pulse.setDrawDataPoints(true);
        series_pulse.setDrawAsPath(true);
        series_pulse.setTitle("Pulse Data");
//        series_pulse.setDrawBackground(true);


        // initialize the necessary libraries for accessing location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  // this listens to location updates
        mSettingsClient = LocationServices.getSettingsClient(this);  // this listens to location settings on phone (i.e., if the location on phone is turned on)
        mLocationRequest = new LocationRequest();  // this requests for latitude and longitude values
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);  // this sets the interval between two location updates
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);  // this sets the smallest interval between two location updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // this sets the accuracy of the location updates
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();  // this requests for location settings
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(AllAtOnceActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {  // if the permission has been granted by the user,
                        mRequestingLocationUpdates = true;  // set 'mRequestingLocationUpdates' as true
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {  // if the permission has been denied by the user,
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is denied permanently
                            openSettings();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        // now the location updates will take place on this separate thread so that the main UI thread is not blocked
        runOnUiThread(new Runnable() {  // while we are receiving the sensor data continuoulsy, run this thread in parallel
            @Override
            public void run() {
                try {
                    mLocationCallback = new LocationCallback() {  //LocationCallback function allows to fetch the location updates
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            // location is received
                            mCurrentLocation = locationResult.getLastLocation();  // when location callback is successful, get the current location and store it in 'mCurrentLocation' variable

                            if (mCurrentLocation != null) {
                            }
                        }
                    };
                } catch (NumberFormatException nfe) {
                }
            }
        });

        // the following code makes sure that screen remains turned ON in this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void clearUI() {  // when this function is called, it clears all the components from UI, like,
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);  // bluetooth characteristics are vanished hence, no READ and WRITE takes place
        mDataField_ecg.setText(R.string.no_data);  // the readings stored in the 'mDataField_x' are emptied
        mDataField_emg.setText(R.string.no_data);
        mDataField_gsr.setText(R.string.no_data);
        mDataField_pulse.setText(R.string.no_data);
        clickForService.setVisibility(View.INVISIBLE);  // the 'clickForService' button is made invisible
    }

    //both of them for csv file creation in phone's secondary storage
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    // this function is called, everytime a new data (sensor value) is received from the BLE device
    private void displayData(String data) {  // called for every new data - only 20 bytes at a time can be considered (order -> ecg,emg,gsr,pulse)
        if (((data.indexOf(',')!=-1) && (data.indexOf(',')<data.indexOf('/'))) && ((data.indexOf('/')!=-1) && (data.indexOf('/')<data.indexOf(':'))) && ((data.indexOf(':')!=-1) && (data.indexOf(':')<data.indexOf('\n')))) {  //so that only data with format _,_/_:_\n is accepted!
            //Log.d(TAG, "ecg index: "+ data.indexOf(','));
            //Log.d(TAG, "emg index: "+ data.indexOf('/'));
            //Log.d(TAG, "gsr index: "+ data.indexOf(':'));
            //Log.d(TAG, "pulse index: "+ data.indexOf('\n'));
            Log.d(TAG, "dataRe: " + data);

            if (((index1 = data.indexOf(',')) != -1) && ((index2 = data.indexOf('/')) != -1)  && ((index3 = data.indexOf(':')) != -1) && ((index4 = data.indexOf('\n')) != -1)) { // this is another way to check that we receive data in the specified format
                dataStr_ecg = data.substring(0, index1).trim();  // trim the ecg data until a comma (,) is encountered and store in the new string variable 'dataStr_ecg'
                //Log.d(TAG, "ecg_index: " + "0 to " + index1);
                //Log.d(TAG, "ecg: " + dataStr_ecg);

                dataStr_emg = data.substring(index1 + 1, index2).trim();  // trim the data until a forward slash (/) is encountered and store in the new string variable 'dataStr_ecmg'
                //Log.d(TAG, "emg_index: " + (index1 + 1) + " to " + index2);
                //Log.d(TAG, "emg: " + dataStr_emg);

                dataStr_gsr = data.substring(index2 + 1, index3).trim();  // trim the remaining data until a colon (:) is encountered and store in the new string variable 'dataStr_gsr'
                //Log.d(TAG, "gsr_index: " + (index2 + 1) + " to " + index3);
                //Log.d(TAG, "gsr: " + dataStr_gsr);

                dataStr_pulse = data.substring(index3 + 1, index4).trim();  // trim the remaining data until a newline (\n) is encountered and store in the new string variable 'dataStr_pulse'
                //Log.d(TAG, "pulse_index: " + (index3 + 1) + " to " + index4);
                //Log.d(TAG, "pulse: " + dataStr_pulse);

                mDataField_ecg.setText(dataStr_ecg);  // show the stored ecg value in the datafield/textView of 'mDataField_ecg'
                mDataField_emg.setText(dataStr_emg);  // show the stored emg value in the datafield/textView of 'mDataField_emg'
                mDataField_gsr.setText(dataStr_gsr);  // show the stored gsr value in the datafield/textView of 'mDataField_gsr'
                mDataField_pulse.setText(dataStr_pulse);  // show the stored pulse value in the datafield/textView of 'mDataField_pulse'

                // this block is responsible for displaying graphs
                runOnUiThread(new Runnable() {  // while we are receiving the sensor data continuously, run this thread in parallel
                    @Override
                    public void run() {
                        if (dataStr_ecg != null && !dataStr_ecg.equals("") && !dataStr_ecg.equals("!")) {  // conditions to check that ecg is in correct format
                            try {  // if we receive ecg values correctly
                                n_ecg = Integer.parseInt(dataStr_ecg);  // store the floating point equivalent of string ecg in variable 'n_ecg'
                                series_ecg.appendData(new DataPoint(lastX1++, n_ecg), true, 1000);  // pass these float sensor values in the graph series
                                if(n_ecg <=200 && n_ecg >=700) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                                    builder.setTitle("ECG sensor anomaly");
                                    builder.setMessage("There is some anomaly in ECG sensor! ");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                                    builder.show();
                                }
                            } catch (Exception e) {}
                        }

                        if (dataStr_emg != null && !dataStr_emg.equals("")) {  // conditions to check that emg is in correct format
                            try {
                                n_emg = Integer.parseInt(dataStr_emg);  // store the floating point equivalent of string emg in variable 'n_emg'
                                series_emg.appendData(new DataPoint(lastX2++, n_emg), true, 1000);  // pass these float sensor values in the graph series
                                if(n_emg <=0 && n_emg >=1023) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                                    builder.setTitle("EMG sensor anomaly");
                                    builder.setMessage("There is some anomaly in EMG sensor! ");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                                    builder.show();
                                }
                            } catch (Exception e) {}
                        }

                        if (dataStr_gsr != null && !dataStr_gsr.equals("")) {  // conditions to check that gsr is in correct format
                            try {
                                n_gsr = Integer.parseInt(dataStr_gsr);  // store the floating point equivalent of string gsr in variable 'n_gsr'
                                series_gsr.appendData(new DataPoint(lastX3++, n_gsr), true, 1000);  // pass these float sensor values in the graph series
                                if(n_gsr <=0 && n_gsr >=1023) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                                    builder.setTitle("GSR sensor anomaly");
                                    builder.setMessage("There is some anomaly in GSR sensor! ");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                                    builder.show();
                                }
                            } catch (Exception e) {}
                        }

                        if (dataStr_pulse != null && !dataStr_pulse.equals("") && !dataStr_pulse.startsWith("?")) {  // conditions to check that pulse is in correct format
                            try {
                                n_pulse = Integer.parseInt(dataStr_pulse);  // store the floating point equivalent of string pulse in variable 'n_pulse'
                                series_pulse.appendData(new DataPoint(lastX4++, n_pulse), true, 1000);  // pass these float sensor values in the graph series
                                if(n_pulse <=0 && n_pulse >=180) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                                    builder.setTitle("Pulse sensor anomaly");
                                    builder.setMessage("There is some anomaly in Pulse sensor! ");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    });
                                    builder.show();
                                }
                            } catch (Exception e) {}
                        }
                    }
                });

                // this block is responsible for writing data to CSV file
                if (csvRecord == true) {  //block will execute unless user presses stopCSV button
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!dataStr_ecg.matches("") && !dataStr_emg.matches("") && !dataStr_gsr.matches("") && !dataStr_pulse.matches("")) {  // check that data is not empty
                                csvCalendar = Calendar.getInstance();
                                csvSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");  // defines the date format to be stored
                                csvSimpleTimeFormat = new SimpleDateFormat("hh:mm:ss:Sss");  // defines the time format to be stored
                                csvCurrentDateString = csvSimpleDateFormat.getDateInstance().format(new Date());  // store the date in the variable 'csvCurrentDateString'
                                csvTime = csvSimpleTimeFormat.format(csvCalendar.getTime());  // store the time (in milliseconds) in the variable 'csvTime'

                                try {
                                    FileOutputStream fos = new FileOutputStream(myExternalFile, true);  // make an instance of the CSV file and start writing to it
                                    fos.write(csvCurrentDateString.getBytes());  // write date in the first column
                                    fos.write(",".getBytes());
                                    fos.write(csvTime.getBytes());  // write time in the second column
                                    fos.write(",".getBytes());
                                    fos.write(dataStr_ecg.getBytes());  // write ecg sensor data in the third column
                                    fos.write(",".getBytes());
                                    fos.write(dataStr_emg.getBytes());  // write emg sensor data in the fourth column
                                    fos.write(",".getBytes());
                                    fos.write(dataStr_pulse.getBytes());  // write pulse sensor data in the fifth column
                                    fos.write(",".getBytes());
                                    fos.write(dataStr_gsr.getBytes());  // write gsr sensor data in the sixth column
                                    fos.write("\n".getBytes());  // move to next line
                                    fos.close();  // stop writing
                                } catch (IOException e) {  // if CSV writing fails somehow,
                                    e.printStackTrace();  // print the error statemnet in the console
                                }
                            }
                        }
                    });
                }

                // this piece of code is responsible to send data to cloud
                if(push==1) {  // if push is set to 1, it means user has pressed the button for sending data to cloud
                    final String email_string = user.getEmail();  // store the logged in user's email ID in the string variable 'email_string'
                    mDatabaseRef_Info.orderByChild("email").equalTo(email_string).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //Database exists for this user
                                //database already has some entries of user, update more entries
                                for (com.google.firebase.database.DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    final String uid = childSnapshot.getKey();

                                    handler.postDelayed(new Runnable() {  // this handler instance runs the enclosing statement in a separate thread without blocking the main UI
                                        @Override
                                        public void run() {
                                            sendDataToCloud(uid.trim());
                                        }
                                    }, 500);
                                }
                            } else { //if 'email' in database doesn't exist
                                Toast.makeText(AllAtOnceActivity.this, "No such activity exists!", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(AllAtOnceActivity.this);
                                builder.setTitle("Contact Your Developer");
                                builder.setMessage("You Signed Up but didn't upload any Information in the cloud! ");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });
                                builder.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //Error
                        }
                    });
                }
            }
        }
    }

    // when this function is called, it posts the multi-sensor values along with latitude and longitude values in the database
    private void sendDataToCloud(String uidUser) {
        try {
            mCalendar = Calendar.getInstance();
            mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");  // defines the date format to be stored in the database
            mSimpleTimeFormat = new SimpleDateFormat("HH:mm:ss:Sss");  // defines the time format to be stored in the database

            String currentChild = uidUser;  // this stores the parent key from the database
            if(!dataStr_ecg.matches("") && !dataStr_emg.matches("") && !dataStr_gsr.matches("") && !dataStr_pulse.matches("")) { //so that null values are not stored in the cloud
                currentDateString = mSimpleDateFormat.getDateInstance().format(new Date());  // store the date in the variable 'currentDateString'
                time = mSimpleTimeFormat.format(mCalendar.getTime());  // store the time (in milliseconds) in the variable 'time'

                String lat = String.valueOf(mCurrentLocation.getLatitude());  // under the 'time' parent node, set the fifth key-child pair to be the latitude value
                String lon = String.valueOf(mCurrentLocation.getLongitude());  // under the 'time' parent node, set the sixth key-child pair to be the latitude value
                if(lat!=null && lon!=null) {
                    DatabaseReference mnew = mDatabaseRef_Timestamp.child(currentChild).child(currentDateString).child(time);  // set the database reference name such that the parent node is the local-part of user's email address and the child node is the date string and the grandchild node is the time string.
                    mnew.child("ECG").setValue(dataStr_ecg);  // under the 'time' parent node, set the first key-child pair to be the ECG value
                    mnew.child("EMG").setValue(dataStr_emg);  // under the 'time' parent node, set the second key-child pair to be the EMG value
                    mnew.child("GSR").setValue(dataStr_gsr);  // under the 'time' parent node, set the third key-child pair to be the GSR value
                    mnew.child("Pulse").setValue(dataStr_pulse);  // under the 'time' parent node, set the fourth key-child pair to be the Pulse value
                    mnew.child("Latitude").setValue(lat);  // set the child value for key 'Latitude'
                    mnew.child("Longitude").setValue(lon);  // set the child value for key 'Latitude'
                } else {
                    //do nothing
                }
            }
        } catch (NumberFormatException nfe) {  // in case the try statements fail, that might be because of wrong database reference. In that case, show the following toast
        }
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {  // listen to location permission changes
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {  // if location permissions are successfully granted
                        Log.i(TAG, "All location settings are satisfied.");  // print this in console
//                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());  // continuously provide location updates
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {  // if listening to location permission changes fails
                    @Override
                    public void onFailure(@NonNull Exception e) {  // on failing, execute the following
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:  // if the sufficient permissions are not given, show the following in console
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(AllAtOnceActivity.this, REQUEST_CHECK_SETTINGS);  // request the user to check settings and allow location permission (by showing a dialog box)
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:  // if settings are unavailable, show the following message in the console
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(AllAtOnceActivity.this, errorMessage, Toast.LENGTH_LONG).show();  // also, show the message in the toast notification
                        }
                    }
                });
    }

    public void stopLocationUpdates() {  // this function prompts the location updates to stop
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {  // this listens if the location updates have stopped or not
                    @Override
                    public void onComplete(@NonNull Task<Void> task) { // on successful stopping
//                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // This begins (& hence location updates starts) as soon as we step in OptionsActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:  // check the location settings
                switch (resultCode) {
                    case Activity.RESULT_OK:  // if the location permission is allowed
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // do nothing as, 'mRequestingLocationUpdates' is already set to true
                        break;
                    case Activity.RESULT_CANCELED:  // if the location permission is not allowed
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;  // set 'mRequestingLocationUpdates' to false
                        break;
                }
                break;
        }

        // this intent is responsible for picking a file from the phone, that will be sent to doctor via 3rd party apps
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "it is working!");
        }
        switch (requestCode) { //this switch is responsible to send csv or images to doctor, it lets the user select CSV path or image path
            case REQUEST_CODE:  // check the REQUEST_CODE
                if (resultCode == RESULT_OK) {  // if the value of resultCode is 'RESULT_OK'
                    if(data!=null) {  // check if the selected file (csv or image) is not null. If not so, then,
                        uri = data.getData();  // store the URI of the selected file in the variable 'uri'
                        Log.i(TAG, "Uri: "+uri.toString());  // print the URI in the console
                        try {
                            path = MakeGraph_FileUtils.getPath(this, uri);  //'getPath' function of 'MakeGraph_FileUtils' class, extracts the file path of the uri. Store this file path in the string variable 'path'
//                            Toast.makeText(optionsActivity.this, "File Selected: "+path, Toast.LENGTH_SHORT).show();
                            FILE = path;  // copy this path in the string variable 'FILE'
                            Toast.makeText(AllAtOnceActivity.this, FILE, Toast.LENGTH_SHORT).show();  // show this file path in the toast notification
                        } catch (Exception e) {
                            Toast.makeText(AllAtOnceActivity.this, "File not found! "+e, Toast.LENGTH_SHORT).show();
                        }
                        sendThroughOtherApps();  // this function allows us to choose from 3rd party apps that we would want to use to send our file to the doctor
                    }
                } else {  // if the value of resultCode is anything else, show the following toast notification
                    Toast.makeText(AllAtOnceActivity.this, "some problem to pick file", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void sendThroughOtherApps() {  // this is called from 'onActivityResult' function. It allows the patient to choose from 3rd party apps that we would want to use to send our file to the doctor
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);  //Intent component enables us to open 3rd party apps. 'ACTION_SEND' indicates that user intends to send some data
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"email@example.com"});  // if email app is chosen by the user, set the receiver's email as 'email@example.com'
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");  // set the subject
            emailIntent.putExtra(Intent.EXTRA_TEXT, graphXaxis_string+" values");  // set the file text

            File file = new File(FILE);  // get the selected file
            Uri uri = Uri.fromFile(file);  // get the URI of the selected file
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);  // send the file to the Intent component
            startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));  // start Intent and send the data

        } catch (Exception e) {  // If intent component fails to send data, print the following in the toast notification
            Toast.makeText(AllAtOnceActivity.this, "Some problem in sending CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {  // this prompts the user to allow location permission from the settings
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // with the help of intent, we try to open settings
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);  // this starts the intent, i.e., opens the settings from where user can make changes
    }

    private boolean checkPermissions() {  // this function is called while granting permission for collecting locating data
        int permissionState = ActivityCompat.checkSelfPermission(AllAtOnceActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onResume() {  // On resume function is executed when we open this activity after pausing it for some time
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());  // after resuming this activity, firstly register to 'broadcastReceiver'
        if (mBluetoothLeService != null) {  // if this activity is connected to bluetooth binding class, then,
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);  // connect the android device with a BLE device having the address 'mDeviceAddress'
            Log.d(TAG, "Connect request result=" + result);  // see the result in the console
        }
        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {  // if the permission for location is successfully granted
            startLocationUpdates();  // Loction information is collected on foreground only
        }
    }

    @Override
    protected void onPause() {  // Whenever we close this activity or pause it by opening another app, this function is called.
        super.onPause();
        Log.d(TAG, "Activity-A is paused");
        push=0;
        if (bluetoothGattCharacteristicHM_10 != null) {  // first check if the device is available (if it is!)
            if (mConnected == true) {  // and if the device status is connected
                mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristicHM_10, 'x');  // send character 'x' to the arduino (it indicates arduino, not to send any data to android)
//            Toast.makeText(AllAtOnceActivity.this, "Stopped recording!", Toast.LENGTH_SHORT).show();
                unregisterReceiver(mGattUpdateReceiver);
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            } else {
                Toast.makeText(AllAtOnceActivity.this, "Your server device seems to be disconnected. Establish the connection first...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {  // When we close the app, this function is called, it clears the app from occupying the memory
        super.onDestroy();
        Log.d(TAG, "Activity-A is Destroyed");
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        stopLocationUpdates(); //location information collected on foreground only
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  // this function is responsible for showing the 'Connect' and 'Disconnect' button in the top-right corner in the toolbar
        getMenuInflater().inflate(R.menu.gatt_services, menu);  // inflate the view that is responsible for showing the 'Connect' and 'Disconnect' button in the top-right corner in the toolbar
        if (mConnected) {  // if the 'mConnected' is true, it means that the BLE device is connected. In that case,
            menu.findItem(R.id.menu_connect).setVisible(false);  // do not show the 'Connect' button
            menu.findItem(R.id.menu_disconnect).setVisible(true);  // show the 'Disconnect' button
        } else {  // if the 'mConnected' is false, it means that the BLE device is disconnected. In that case,
            menu.findItem(R.id.menu_connect).setVisible(true);  // show the 'Connect' button
            menu.findItem(R.id.menu_disconnect).setVisible(false);  //do not show the 'Disconnect' button
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:  // if user selects the option 'connect' available on the top-right corner of the toolbar
                mBluetoothLeService.connect(mDeviceAddress);  // connect the android device with a BLE device having the address 'mDeviceAddress'
                return true;
            case R.id.menu_disconnect:  // if user selects the option 'disconnect' available on the top-right corner of the toolbar
                mBluetoothLeService.disconnect();  // disconnect the android device with the connected BLE device
                return true;
            case android.R.id.home:  // else
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {  // it states whether the android device and the BLE device are connected to each other or not
        runOnUiThread(new Runnable() {  // while we are receiving the sensor data continuoulsy, run this thread in parallel
            @Override
            public void run() {
                mConnectionState.setText(resourceId);  //set the value of the variable 'mConnectionState' as Connected or Disconnected, depending upon the state of connection
                if(mConnected){  // if the devices are connected, show the following toast notification
                    Toast.makeText(AllAtOnceActivity.this, "Connected to Device!", Toast.LENGTH_SHORT).show();
                }
                else if(mConnected==false){  // if the devices are disconnected, show the following toast notification
                    Toast.makeText(AllAtOnceActivity.this, "Disconnected from Device!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //........... following code is a boiler code (most of it is pre-defined code for establishing the BLE connection)
    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {

//.............
        // for Bluno device, the UUID of the device is stored in the variable 'UUID_HM_10_bluno'
        UUID UUID_HM_10_bluno = UUID.fromString(SampleGattAttributes.HM_10_bluno); //declaration for bluno
//.............

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();  //uuid = device Info Service
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
//....................
                if(uuid.equals(SampleGattAttributes.HM_10_bluno)){  // Check if the connected device is bluno
                    bluetoothGattCharacteristicHM_10 = gattService.getCharacteristic(UUID_HM_10_bluno);  // if it is, then store all the GATT characteristics of Bluno in 'bluetoothGattCharacteristicHM_10' variable
                }
//....................
            }
            mGattCharacteristics.add(charas);  // add characteristics
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(  // this shows all the characteristics to us
                AllAtOnceActivity.this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        // everytime characteristics are available, do the following
        mGattServicesList.setAdapter(gattServiceAdapter);  //every time adapter sets, make recording buttons unavailable
        clickForService.setVisibility(View.VISIBLE);  // set the 'clickForService' button visible
        graphBtn.setVisibility(View.INVISIBLE);  // set the 'graphBtn' button invisible
        stopGraphBtn.setVisibility(View.INVISIBLE);  // set the 'graphResetBtn' button invisible
        graphResetBtn.setVisibility(View.INVISIBLE);  // set the 'stopGraphBtn' button invisible
        sendToCloud.setVisibility(View.INVISIBLE);  // set the 'sendToCloud' button invisible
        stopToCloud.setVisibility(View.INVISIBLE);  // set the 'stopToCloud' button invisible
        csvBtn.setVisibility(View.INVISIBLE);  // set the 'csvBtn' button invisible
        sendToDoctor.setVisibility(View.INVISIBLE);  // set the 'screenShot' button invisible
    }

    // this method is called from onResume method
    private static IntentFilter makeGattUpdateIntentFilter() {  // this method will associate GATT characteristics with the following actions
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);  // connected to BLE device
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);  // disconnected to BLE device
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);  // BLE device is discovered
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE); // the data is available from BLE device to our android device
        return intentFilter;
    }
}

/** This is the last activity of this Application */