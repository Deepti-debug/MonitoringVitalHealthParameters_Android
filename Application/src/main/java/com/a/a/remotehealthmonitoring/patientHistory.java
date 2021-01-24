/** We land here from 'AfterLoginActivity' activity (when patient clicks on the 'clickForPatientHistory' button)' */
/** This activity is responsible for showing all medical prescriptions uploaded by doctor via the web page on the cloud.
 * You can choose the patient medical photo from a particular date by using the date dropdown menu */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;


public class patientHistory extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView username_textView, useremail_textView, prescription_text;  // declaring 3 textViews - first contains the userName of the logged in user, second contains the email address of the logged in user, third to store prescription
    private ImageView user_imageView;  // declaring an ImageView - to display the profile picture of the user
    private String mEmail;  // mName stores the name of the logged in patient, mEmail stores the email of the logged in patient
    private DatabaseReference mDatabaseRef_Info;  // this stores the database path of the user credentials
    private String uid;  // this stores the parent node of the user-specific key in database
    private FirebaseAuth mAuth;  // declaring firebaseAuth component - used while authenticating in the user
    public FirebaseUser user;  // declaring firebaseUser component - used to verify current user

    //Initialisations for spinner (dropdown menus)
    ArrayAdapter<String> dataAdapterDate, dataAdapterTime;
    public List<String> categoriesDate, categoriesTime;
    private Spinner HistoryDateStamp, HistoryTimeStamp;
    String dateSpinnerText, timeSpinnerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);
        Firebase.setAndroidContext(this);  // this command is useful when we refer to database locations (in line 274, 275, 276)
        mAuth = FirebaseAuth.getInstance();  //initialise the firebase authentication variable
        user = mAuth.getCurrentUser();  //current user who is logged in.
        mDatabaseRef_Info = FirebaseDatabase.getInstance().getReference().child("User_Info");  // this database path is stored in the 'mDatabaseRef_Info'
        prescription_text = findViewById(R.id.prescription_text);  // attach 'prescription_text' textView element from the xml file to the variable 'prescription_text'

        // DECLARATIONS FOR "SELECT DATE" SPINNER
        HistoryDateStamp = findViewById(R.id.HistoryDateStamp); //Spinner element
        HistoryDateStamp.setOnItemSelectedListener(patientHistory.this); // Spinner click listener
        categoriesDate = new ArrayList<String>(); // Spinner Drop down elements
        // Creating adapter for spinner
        dataAdapterDate = new ArrayAdapter<String>(patientHistory.this, android.R.layout.simple_spinner_item, categoriesDate);
        // Drop down layout style - list view with radio button
        dataAdapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        HistoryDateStamp.setAdapter(dataAdapterDate);

        // DECLARATIONS FOR "SELECT TIME" SPINNER
        HistoryTimeStamp = findViewById(R.id.HistoryTimeStamp); //Spinner element
        HistoryTimeStamp.setOnItemSelectedListener(patientHistory.this); // Spinner click listener
        categoriesTime = new ArrayList<String>(); // Spinner Drop down elements
        // Creating adapter for spinner
        dataAdapterTime = new ArrayAdapter<String>(patientHistory.this, android.R.layout.simple_spinner_item, categoriesTime);
        // Drop down layout style - list view with radio button
        dataAdapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        HistoryTimeStamp.setAdapter(dataAdapterTime);


        // first update basic user info
        // then update spinners
        mEmail = user.getEmail();  // 'mEmail' variable stores the email of the currently signed in user
        if (mEmail!=null) {  // if the 'mEmail' variable is not null,
            runOnUiThread(new Runnable() {  // 'runOnUiThread' is responsible for executing the enclosing commands without crashing the UI ('runOnUiThread' basically forms a separate thread and runs commands on top of UI)
                @Override
                public void run() {  // run the following commands on a separate thread
                    // check if the given "email", exists in the database. If it does, fetch the user UID
                    mDatabaseRef_Info.orderByChild("email").equalTo(mEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                //If user info. exists in the database, then display user info. in the app screen, by calling displayUserInfo()
                                for (com.google.firebase.database.DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    uid = childSnapshot.getKey();
                                    displayUserInfo(uid.trim());  //update basic user info

                                    // now update date spinner
                                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Prescriptions/"+uid);
                                    databaseRef.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                            String dateKey = snapshot.getKey(); //get all date values
                                            categoriesDate.add(dateKey);
                                            dataAdapterDate.notifyDataSetChanged();
                                        }
                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                            String dateKey = snapshot.getKey(); //get the added key
                                            categoriesDate.add(dateKey);
                                            dataAdapterDate.notifyDataSetChanged();
                                        }
                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                            String dateKey = snapshot.getKey(); //get the removed key
                                            categoriesDate.remove(dateKey);
                                            dataAdapterDate.notifyDataSetChanged();
                                        }
                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                            HistoryDateStamp.setAdapter(null);
                                            // attaching data adapter to spinner
                                            HistoryDateStamp.setAdapter(dataAdapterDate);
                                            String dateKey = snapshot.getKey(); //get all date values
                                            categoriesDate.add(dateKey);
                                            dataAdapterDate.notifyDataSetChanged();
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                }
                            }else { //if 'email' in database doesn't exist, show the following AlertBox to the user
                                Toast.makeText(patientHistory.this, "No such activity exists!", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(patientHistory.this);
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
                            Toast.makeText(patientHistory.this, "A database error has occurred: . "+ databaseError.getMessage() + "Please try logging in later! ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {  // if user email is not available, there might be some problem with the database. Therefore, show the following message
            Toast.makeText(patientHistory.this, "Problem occurred while fetching user information from the database. please contact the database owner", Toast.LENGTH_LONG).show();
        }
    }

    // This will display basic user info like name, email, profile pic by calling the 'displayUserInfo' function
    private void displayUserInfo(String uidUser) {  //the uppermost display of UI (showing name, email, profile picture)

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
                Picasso.with(patientHistory.this).load(value).into(user_imageView);  // now use this variable 'value' to display this url into ImageView in the UI
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {  // if the listener fails to listen to changes, then do nothing
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {  // On selecting a spinner item
        if(parent.getId() == R.id.HistoryDateStamp)
        {
            dateSpinnerText = HistoryDateStamp.getSelectedItem().toString();
            dataAdapterTime.clear(); // clear items of time spinner

            // update time spinner
            DatabaseReference databaseRef_timeSpinner = FirebaseDatabase.getInstance().getReference().child("Prescriptions/"+uid+"/"+dateSpinnerText);
            databaseRef_timeSpinner.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String timeKey = snapshot.getKey(); //get all the time values2
                    categoriesTime.add(timeKey); // add items to time spinner
                    dataAdapterTime.notifyDataSetChanged();
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String timeKey = snapshot.getKey(); //get all time values
                    categoriesTime.add(timeKey);
                    dataAdapterTime.notifyDataSetChanged();
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String timeKey = snapshot.getKey();
                    categoriesTime.remove(timeKey);
                    dataAdapterTime.notifyDataSetChanged();
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    HistoryTimeStamp.setAdapter(null);
                    // attaching data adapter to spinner
                    HistoryTimeStamp.setAdapter(dataAdapterTime);
                    String timeKey = snapshot.getKey(); //get all time values
                    categoriesTime.add(timeKey);
                    dataAdapterTime.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        if(parent.getId() == R.id.HistoryTimeStamp)
        {
            dateSpinnerText = HistoryDateStamp.getSelectedItem().toString();
            timeSpinnerText = HistoryTimeStamp.getSelectedItem().toString();

            // update the prescription in the UI as per selected date and time from the prescription
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Prescriptions").child(uid).child(dateSpinnerText).child(timeSpinnerText).child("presc");

            //Sets up UI references.
            ref.addValueEventListener(new ValueEventListener() {  // listen for any changes happening in the 'ref' database location
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {  //if the data within 'mRefName' database location changes
                    prescription_text.setText("Prescription: " + snapshot.getValue(String.class));  // then set the value of the 'uername_textView' to be this new changed data
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {  // if the listener fails to listen to changes, then do nothing
                }
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // TODO Auto-generated method stub
    }
}
