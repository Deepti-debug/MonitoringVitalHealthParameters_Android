/** We land here from DoctorOrPatient activity (after the user clicks on Doctor Button)
 * Asks the Doctor if he/she wants to sign up or login
 * If the user chooses to signUp, he is registered and then taken to - graphModeActivity
 * If the user chooses to LogIn, he/she is authenticated and taken to - graphModeActivity */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class emailPasswordDoctorActivity extends AppCompatActivity {
    private static String TAG = emailPasswordActivity.class.getSimpleName();  // TAG just stores the name of this activity. It will be used while printing logs in the console

    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";  // this string will store the device name obtained from the previous activity
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";  // this string will store the device address obtained from the previous activity
    private String mDeviceName, mDeviceAddress;  // strings will copy the device name and address obtained from previous strings to these new strings

    private Button signup_btn, login_btn;  // declaring 2 buttons of this activity - signUp button and Login button
    private EditText email_editText, password_editText;  // declaring 2 editText fields - one for entering email address, one for entering password
    private ProgressBar mProgressBar;  // declaring a progress bar
    private FirebaseAuth mAuth;  // declaring firebaseAuth component - used while signing in the user
    private DatabaseReference mDatabaseRef_Info;  // declaring DatabaseReference component - used to locate the firebase database of the doctor's details

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like imt main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password_doctor);  // this is the XML file where you can find the layout of this activity
        mDatabaseRef_Info = FirebaseDatabase.getInstance().getReference("Doctors");  // initialise the DatabaseReference of the firebase cloud (declared earlier)

        // these 3 lines of code uses the Intent component to receive the deviceName and deviceAddress from the previous activities and store them in new strings
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // here we do couple of initialisations
        email_editText = findViewById(R.id.email);  // attach email editText from the xml file to the variable 'email_editText'
        password_editText = findViewById(R.id.password);  // attach password editText from the xml file to the variable 'password_editText'
        signup_btn = findViewById(R.id.signup);  // attach signUp button from the xml file to the variable 'signup_btn'
        login_btn = findViewById(R.id.login);  // attach logIn button from the xml file to the variable 'login_btn'
        mProgressBar = findViewById(R.id.progressBar);  // attach progressBar component from the xml file to the variable 'mProgressBar'
        mProgressBar.setVisibility(View.GONE);  // keep the progress bar invisible for now
        mAuth = FirebaseAuth.getInstance();  //initialise the firebase authentication variable

        // when signUp button is clicked, execute 'signupUserAccount' function
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                signupUserAccount();
            }
        });

        // when login button is clicked, execute 'loginUserAccount' function
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                loginUserAccount();
            }
        });
    }

    // this function is executed when sign up button is clicked
    private void signupUserAccount() {
        String email, password;
        email = email_editText.getText().toString();  //store the email entered by user in the 'email' string.
        final String modified_email = email+"@gmail.com";  //store 'email' + '@gmail.com' on a new string. This string will be used for firebase authentication purpose shortly
        password = password_editText.getText().toString();  // this string stores the password entered by the user
        if (TextUtils.isEmpty(email)) {  // if email field is empty, show the following toast notification to the user
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {  // if password field is empty, show the following toast notification to the user
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length()<6) {  // if the password length is less than 6 characters, show the following toast notification to the user
            Toast.makeText(emailPasswordDoctorActivity.this, "Password is too short!!!", Toast.LENGTH_SHORT).show();
        }
        else {  //signing Up
            try {
                mProgressBar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // if the try block somehow fails, execute the catch block
            }
            try {
                // 'createUserWithEmailAndPassword' method registers the new email and password to firebase cloud.
                // the 'onComplete' function is executed when the registration/signUp is successful
                mAuth.createUserWithEmailAndPassword(modified_email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If SignUp is successful, make progressbar invisible
                            Toast.makeText(getApplicationContext(), "Signup successful!", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                            try {
                                // If signUp is successful, launch intent component and move to another activity called 'graphModeActivity'
                                final Intent intent = new Intent(emailPasswordDoctorActivity.this, graphModeActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(emailPasswordDoctorActivity.this, "Can't move to next activity, Intent issue!", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                // If signUp is successful, then make a database entry of the user and launch intent component and move to another activity called 'AfterSignupActivity'
                                String key = mDatabaseRef_Info.push().getKey();  // a new user-specific parent key
                                mDatabaseRef_Info.child(key).child("email").setValue(modified_email);  //the key-child pair of the parent
                                // If signUp is successful, launch intent component and move to another activity called 'graphModeActivity'
                                final Intent intent = new Intent(emailPasswordDoctorActivity.this, graphModeActivity.class);
                                // intent component sends the device name and address to the new activity
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(emailPasswordDoctorActivity.this, "Can't move to next activity, Intent issue!", Toast.LENGTH_SHORT).show();
                            }
                        } else {  // If sign in fails, check if the email already existed i.e., the doctor has already signed up
                            mProgressBar.setVisibility(View.GONE);
                            mAuth.fetchSignInMethodsForEmail(modified_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
//                                    Log.d(TAG,""+task.getResult().getSignInMethods().size());
                                    try {  // the app might crash because of badly formatted email addresses. Therefore, try-catch statements are important
                                        if (task.getResult().getSignInMethods().size() == 0){  // if email doesn't not exist, then maybe user is using too many special characters in its email (firebase doesn't allow unusual email addresses). In that case, show the following message
                                            Toast.makeText(emailPasswordDoctorActivity.this, "This email address seems unusual. Please use a different one!", Toast.LENGTH_SHORT).show();
                                        }else { // if the email already existed, then display a message to the user.
                                            Toast.makeText(emailPasswordDoctorActivity.this, "This email might already be registered. Please Log In", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {  // the try statement might not work with badly formatted email addresses. In that case, show the following toast
                                        Toast.makeText(emailPasswordDoctorActivity.this, "This email address seems unusual. Please use a different one!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                // if the try block somehow fails, execute the catch block
                // Show the following toast notification to the user
                Toast.makeText(emailPasswordDoctorActivity.this, "Contact your developer, something wrong with the emailPasswordActivity's signup", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // this function is executed when login button is clicked
    private void loginUserAccount() {
        String email, password;
        email = email_editText.getText().toString();  //store the email entered by user in the 'email' string.
        final String modified_email = email+"@gmail.com";  //store 'email' + '@gmail.com' on a new string. This string will be used for firebase authentication purpose shortly
        password = password_editText.getText().toString();  // this string stores the password entered by the user

        if (TextUtils.isEmpty(email)) {  // if email field is empty, show the following toast notification to the user
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {  // if password field is empty, show the following toast notification to the user
            Toast.makeText(getApplicationContext(), "Please enter password...", Toast.LENGTH_LONG).show();
            return;
        }

        // Logging In the registered User
        try {
            // if the email string doesn't contains special symbol/character, show the progress bar and start the authentication process
            mProgressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }

        try {
            // 'signInWithEmailAndPassword' checks if the user is already registered, if yes, it authenticates or identifies that user.
            // the 'isSuccessful' function is executed when the signIn is successful
            mAuth.signInWithEmailAndPassword(modified_email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try{
                            // check if the user who tries to login is a Doctor - For that, check if his email is stored in the 'Doctors' bucket of database
                            mDatabaseRef_Info.orderByChild("email").equalTo(modified_email.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){  // if Database entry exists for this user, that means he/she is a doctor
                                        for (com.google.firebase.database.DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                            // If Login is successful, make progressbar invisible
                                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                            mProgressBar.setVisibility(View.GONE);
                                            // If LogIn is successful, launch intent component and move to another activity called 'graphModeActivity'
                                            // intent component sends the device name and address to the new activity
                                            final Intent intent = new Intent(emailPasswordDoctorActivity.this, graphModeActivity.class);
                                            intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                                            intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Toast.makeText(emailPasswordDoctorActivity.this, "Sorry, that email address belongs to a Patient!", Toast.LENGTH_SHORT).show();  // if the user entry does not exist in database, that means he/she is not a doctor
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(emailPasswordDoctorActivity.this, "Some error occurred. Please try again! " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            // do nothing
                        }
                    } else {
                        // If Login is unsuccessful, make progressbar invisible and show the following toast notification to the user
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getApplicationContext(), "The password entered is incorrect...", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                        else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // If Login is unsuccessful, and wrong password is not the reason, then show the following toast notification to the user
                            Toast.makeText(getApplicationContext(), "The userID is not registered. Please Sign Up first!", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                }
            });
        } catch (Exception e) {
            // If try block fails to execute properly, show the following toast notification
            Toast.makeText(emailPasswordDoctorActivity.this, "Contact your developer, something wrong with the emailPasswordActivity's login", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {  // On resume function is executed when we open this activity after pausing it for some time
        super.onResume();
        email_editText.setText("");  // clear the email field
        password_editText.setText("");  // clear the password field
        mProgressBar.setVisibility(View.GONE);  // hide the progressBar component
    }

    // Whenever we close this activity or pause it by opening another app, this function is called.
    @Override
    protected void onPause() {
        super.onPause();
    }

    // When we close the app, this fuction is called, it clears the app from occupying the memory
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

/** Next Activity is "graphModeActivity" */