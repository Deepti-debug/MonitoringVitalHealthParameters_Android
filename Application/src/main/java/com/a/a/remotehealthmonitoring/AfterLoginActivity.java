/** We land here from - emailPasswordActivity (after the patient chooses to login into a current account)
 * After Logging In successfully, this Activity opens up!
 * It gives options to record data - It has many buttons asking user which sensor data to record
 * Whichever sensor button user clicks, it takes him to - optionsActivity */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class AfterLoginActivity extends AppCompatActivity {
    private static String TAG = AfterLoginActivity.class.getSimpleName();  // TAG just stores the name of this activity. It will be used while printing logs in the console

    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";  // this string will store the device name obtained from the previous activity
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";  // this string will store the device address obtained from the previous
    private String mDeviceName, mDeviceAddress;  // strings will copy the device name and address obtained from previous strings to these n

    public Button clickForECG, clickForEMG, clickForGSR, clickForPulse, clickForTemp, clickToRecordAll, clickToSignout, clickForPatientHistory;  // declaring 8 buttons in this activity - first to record ECG readings, second to record EMG readings, third to record GSR readings, fourth to record Pulse readings, fifth to record Temp readings, sixth to record all 4 readings simultaneously, seventh to sign out, eigth to see doctor's prescription
    private TextView username_textView, useremail_textView;  // declaring 2 textViews - first contains the userName of the logged in user, second contains the email address of the logged in user
    private ImageView user_imageView;  // declaring an ImageView - to display the profile picture of the user
    private FirebaseAuth mAuth;  // declaring firebaseAuth component - used while authenticating in the user
    public FirebaseUser user;  // declaring firebaseUser component - used to verify current user
    private DatabaseReference mDatabaseRef_Info;  // this stores the database path of the user credentials
    private String uid;  // this stores the parent node of the user-specific key in database

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);  // this is the XML file where you can find the layout of this activity
        Firebase.setAndroidContext(this);  // this command is useful when we refer to database locations (in line 274, 275, 276)
        mAuth = FirebaseAuth.getInstance();  //initialise the firebase authentication variable
        user = mAuth.getCurrentUser();  //current user who is logged in.
        mDatabaseRef_Info = FirebaseDatabase.getInstance().getReference().child("User_Info");  // this database path is stored in the 'mDatabaseRef_Info'

        clickForECG = findViewById(R.id.clickForECG);  // attach 'clickForECG' button element from the xml file to the variable 'clickForECG'
        clickForEMG = findViewById(R.id.clickForEMG);  // attach 'clickForEMG' button element from the xml file to the variable 'clickForEMG'
        clickForGSR = findViewById(R.id.clickForGSR);  // attach 'clickForGSR' button element from the xml file to the variable 'clickForGSR'
        clickForPulse = findViewById(R.id.clickForPulse);  // attach 'clickForPulse' button element from the xml file to the variable 'clickForPulse'
        clickForTemp = findViewById(R.id.clickForTemp);  // attach 'clickForTemp' button element from the xml file to the variable 'clickForTemp'
        clickToRecordAll = findViewById(R.id.clickToRecordAll);  // attach 'clickToRecordAll' button element from the xml file to the variable 'clickToRecordAll'
        clickToSignout = findViewById(R.id.signout);  // attach 'signout' button element from the xml file to the variable 'clickToSignout'
        clickForPatientHistory = findViewById(R.id.patientHistory);  // attach 'clickForPatientHistory' button element from the xml file to the variable 'clickForPatientHistory'

        final Intent intent = getIntent();
        try {
            // try to use the Intent component to receive the deviceName and deviceAddress from the previous activities and store them in new strings
            mDeviceName = Objects.requireNonNull(intent.getExtras()).getString(EXTRAS_DEVICE_NAME);
            mDeviceAddress = intent.getExtras().getString(EXTRAS_DEVICE_ADDRESS);
        } catch (Exception e){
            // if fetching deviceName and deviceAddress fails, show the following toast
            Toast.makeText(AfterLoginActivity.this, "Device name or address has empty fields", Toast.LENGTH_SHORT).show();
        }

        // Firebase databaseRef
        // this block of code assures if user's profile exists in the database or not
        final String email_string_ = user.getEmail();  // 'email_string_' variable stores the email of the currently signed in user
        if (email_string_!=null) {  // if the 'email_string_' variable is not null
            runOnUiThread(new Runnable() {  // 'runOnUiThread' is responsible for executing the enclosing commands without crashing the UI ('runOnUiThread' basically forms a separate thread and runs commands on top of UI)
                @Override
                public void run() {  // run the following commands on a separate thread
                    // check if the given "email", exists in the database. If it does, execute the following
                    mDatabaseRef_Info.orderByChild("email").equalTo(email_string_).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                //If user info. exists in the database, then display user info. in the app screen, by calling displayUserInfo()
                                for (com.google.firebase.database.DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    uid = childSnapshot.getKey();
                                    displayUserInfo(uid.trim());
                                }
                            }else { //if 'email' in database doesn't exist, show the following AlertBox to the user
                                Toast.makeText(AfterLoginActivity.this, "No such activity exists!", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(AfterLoginActivity.this);
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
                        public void onCancelled(@NonNull DatabaseError databaseError) {  // if any database error occurs. Show the following toast
                            Toast.makeText(AfterLoginActivity.this, "A database error has occured: . "+ databaseError.getMessage() + "Please try logging in later! ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {  // if user email is not available, there might be some problem with the database. Therefore, show the following message
            Toast.makeText(AfterLoginActivity.this, "Problem occurred while fetching user information from the database. please contact the database owner", Toast.LENGTH_LONG).show();
        }

        // when 'clickForECG' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickForECG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, optionsActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "ECG");  // since the user clicked on "ECG" .. intent will send the string "ECG" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickForEMG' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickForEMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, optionsActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "EMG");  // since the user clicked on "EMG" .. intent will send the string "EMG" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickForGSR' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickForGSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, optionsActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "GSR");  // since the user clicked on "GSR" .. intent will send the string "GSR" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickForPulse' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickForPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, optionsActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "Pulse");  // since the user clicked on "Pulse" .. intent will send the string "EMG" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickForTemp' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickForTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, optionsActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "Temp");  // since the user clicked on "Temp" .. intent will send the string "EMG" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickToRecordAll' button is clicked, user is prompted to move to a new activity (optionsActivity) via Intent component.
        clickToRecordAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                Intent intent = new Intent(AfterLoginActivity.this, AllAtOnceActivity.class);
                // intent component sends the device name, device address and the value of the clicked button to the new activity
                intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra("graphXaxis", "All");  // since the user clicked on "All" .. intent will send the string "EMG" to the new activity
                startActivity(intent);
            }
        });

        // when 'clickToSignout' button is clicked, user is prompted to move to a new activity (DoctorOrPatient) via Intent component.
        clickToSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                try{  // try signing out hr current user by using firebaseAuth component
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(AfterLoginActivity.this, "Logging Out!", Toast.LENGTH_LONG).show();  // show this toast notification
                } catch (Exception e) { //if someone meddled with database contents
                    Toast.makeText(AfterLoginActivity.this, "Temporary error in signing out. Please contact the database owner", Toast.LENGTH_LONG).show();
                }

                if (mAuth.getCurrentUser() == null) {  // if mAuth user is not available anymore (i.e. we have signed out successfully)
                    // use Intent component to move to a new activity (DoctorOrPatient) via Intent component.
                    final Intent intent = new Intent(AfterLoginActivity.this, DoctorOrPatient.class);
                    startActivity(intent);
                    finish();
                } else {
                    //don't do anything
                }
            }
        });

        // when 'clickForPatientHistory' button is clicked, it shows user medical prescriptions by doctor that are uploaded to the cloud
        // for this purpose, we use an Intent component to move to a new activity called - 'patientHistory'
        clickForPatientHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                final Intent intent = new Intent(AfterLoginActivity.this, patientHistory.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {  // Whenever we close this activity or pause it by opening another app, this function is called.
        super.onPause();
        Log.d(TAG, "Activity-A is paused");
    }

    @Override
    protected void onDestroy() {  // When we close the app, this function is called, it clears the app from occupying the memory
        super.onDestroy();
        Log.d(TAG, "Activity-A is Destroyed");
    }


    // This will display basic user info like name, email, profile pic by calling the 'displayUserInfo' function
    private void displayUserInfo(String uidUser) {  //the uppermost display of UI (showing name, email, profile picture)
        ((TextView) findViewById(R.id.device_name)).setText(mDeviceName);  // save the deviceName obtained from the previous intent to the text-field named 'device_name'
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);  // save the deviceAddress obtained from the previous intent to the text-field named 'device_name'

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mRefName = ref.child("User_Info").child(uidUser).child("name");  // 'mRefName' is the database location for the node 'name'
        DatabaseReference mRefEmail = ref.child("User_Info").child(uidUser).child("email");  // 'mRefEmail' is the database location for the node 'email'
        DatabaseReference mRefUrl = ref.child("User_Info").child(uidUser).child("imageUrl");  // 'mRefUrl' is the database location for the node 'imageUrl'

        username_textView = findViewById(R.id.username_textView);  // attach 'username_textView' textView element from the xml file to the variable 'username_textView'
        useremail_textView = findViewById(R.id.useremail_textView);  // attach 'useremail_textView' textView element from the xml file to the variable 'useremail_textView'
        user_imageView = findViewById(R.id.user_imageView);  // attach 'user_imageView' textView element from the xml file to the variable 'user_imageView'


        //Sets up UI references.
        mRefName.addValueEventListener(new ValueEventListener() {  // listen for any changes happening in the 'mRefName' database location
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {  //if the data within 'mRefName' database location changes
                username_textView.setText(snapshot.getValue(String.class));  // then set the value of the 'uername_textView' to be this new changed data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // if the listener fails to listen to changes, then do nothing
            }
        });

        mRefEmail.addValueEventListener(new ValueEventListener() {  // listen for any changes happening in the 'mRefEmail' database location
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {  //if the data within 'mRefEmail' database location changes
                useremail_textView.setText(snapshot.getValue(String.class));  // then set the value of the 'useremail_textView' to be this new changed data
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // if the listener fails to listen to changes, then do nothing
            }
        });

        mRefUrl.addValueEventListener(new ValueEventListener() {  // listen for any changes happening in the 'mRefUrl' database location
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {  //if the data within 'mRefUrl' database location changes
                String value = snapshot.getValue(String.class); // get this Url and store in the variable 'value'
                Picasso.with(AfterLoginActivity.this).load(value).into(user_imageView);  // now use this variable 'value' to display this url into ImageView in the UI
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // if the listener fails to listen to changes, then do nothing
            }
        });
    }

    //These two functions help to avoid opening back activities while pressing back button accidentally
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    // on pressing back button, it opens an Alert box and asks the user if they really want to leave the activity
    //if the user presses 'Yes', it leaves the activity. If the user presses 'No', it stays.
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  // constructing alert box
        builder.setTitle("Upload Or Not");  // title of Alert Box
        builder.setMessage("Do you want to leave without uploading ");  // Message of Alert Box
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {  //listen to the user Input
            public void onClick(DialogInterface dialog, int id) {  // if the user presses 'Yes'
                Intent intent = new Intent(AfterLoginActivity.this, DoctorOrPatient.class);  // go to activity 'DoctorOrPatient'
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {  //listen to the user Input
            public void onClick(DialogInterface dialog, int id) {  // if the user presses 'No'
                // do nothing
            }
        });
        builder.show();  //show the alert box
    }
}

/**Next Activity is either "optionsActivity" or "AllAtOnceActivity" */