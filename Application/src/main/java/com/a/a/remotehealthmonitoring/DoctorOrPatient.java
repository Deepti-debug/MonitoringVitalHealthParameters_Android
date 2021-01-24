/** This activity comes after DeviceScanActivity
 * This activity asks user, if he/she's a doctor or patient
 * If the user selects Doctor, he/she lands to - emailPasswordDoctorActivity
 * If the user selects Patient, he/she lands to - emailPasswordActivity*/

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class DoctorOrPatient extends AppCompatActivity {

    // Here we declare a couple of things
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";  // this string will store the device name obtained from the previous activity
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";  // this string will store the device address obtained from the previous activity
    private String mDeviceName, mDeviceAddress;  // strings will copy the device name and address obtained from previous strings to these new strings
    private Button doctor, patient;  // declaring 2 buttons of this activity


    @Override
    protected void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_or_patient);  // this is the XML file where you can find the layout of thos activity

        // these 3 lines of code uses the Intent component to receive the deviceName and deviceAddress from the previous activities and store them in new strings
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // here we initialise the 'Doctor' and 'Patient' buttons
        doctor = findViewById(R.id.doctor);
        patient = findViewById(R.id.patient);

        // when the 'Doctor' button is clicked execute the following function
        doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DoctorOrPatient.this, emailPasswordDoctorActivity.class);  // when the user clicks on 'Doctor' button, the Intent component takes the user to a new activity i.e. emailPasswordDoctorActivity.
                startActivity(intent);
            }
        });

        // when the 'Patient' button is clicked execute the following function
        patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DoctorOrPatient.this, emailPasswordActivity.class);  // when the user clicks on 'Patient' button, the Intent component takes the user to a new activity i.e. emailPasswordActivity.
                // intent component sends the device name and address to the new activity
                intent.putExtra(emailPasswordActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(emailPasswordActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                startActivity(intent);
            }
        });
    }

    //These two functions are executed while pressing back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {  // on pressing phone key
        if (keyCode == KeyEvent.KEYCODE_BACK) {  // if the pressed key is a back key,
            onBackPressed();  // then call 'onBackPressed' function
        }
        return super.onKeyDown(keyCode, event);
    }

    // on pressing back button, open the first activity, i.e. 'DeviceScanActivity'
    public void onBackPressed() {
        Intent intent = new Intent(DoctorOrPatient.this, DeviceScanActivity.class);
        startActivity(intent);
        finish();
    }
}

/** Next Activity is either 'emailPasswordDoctorActivity' or 'emailPasswordActivity' */
