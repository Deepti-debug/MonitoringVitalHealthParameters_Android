/** We land here from emailPasswordActivity
 * Here patient fill all his/her details to register to database
 * This activity sends the user info (name, emailId, photo) to firebase database/storage
 */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import id.zelory.compressor.Compressor;

public class AfterSignupActivity extends AppCompatActivity {

    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";// this string will store the device name obtained from the previous activity
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";  // this string will store the device address obtained from the previous
    private String mDeviceName, mDeviceAddress;  // strings will copy the device name and address obtained from previous strings to these n

    public FirebaseAuth mAuth;  // declaring firebaseAuth component - used while authenticating in the user
    private FirebaseUser user;  // declaring firebaseUser component - used to verify current user
    private StorageReference mStorageRef;  // declaring the StorageReference component - used to locate firebase storage
    private DatabaseReference mDatabaseRef_Info;  // declaring DatabaseReference component - used to locate the firebase database
    private StorageTask mUploadTask;  // mUploadTask for uploading compressed image to the firebase cloud

    private static final int CAMERA_REQUEST = 1888;  //initialising a static integer - will be used when user requests to open camera to click a picture
    public Button mButtonChooseImage, mButtonUpload, mButtonSignout;  // declaring 3 buttons in this activity - first to choose image, second to upload that image on the cloud, third to sign out
    public TextView email_textView;  // This textView contains the emailAddress of the authenticated user
    private EditText nameOfPatient_editText, ageOfPatient_editText, bloodGroupOfPatient_editText;   // declaring 3 editText fields - one for entering patient's name, one for entering patient's age, one for entering patient's blood group
    private ImageView mImageView;  // declaring an ImageView - to display the profile picture chosen by user from the gallary
    private ProgressBar mProgressBar;  // declaring a progress bar component
    private Uri downloadUrl, resultUri;  //resultUri is the URI of the cropped image, downloadUrl is the URI of the image to be uploaded to the cloud
    private Bitmap photoBitmap;  // this stores the bitmap of the photo captured using device camera
    private String email_string;  // this stores the email address of the currently signed in user
    private int uploadIsClicked=0;  // uploadIsClicked = 0 means that 'mButtonUpload' has not been clicked yet. Once 'mButtonUpload' is clicked, uploadIsClicked becomes 1

    Bitmap thumb_bitmap = null;  //  it is the bitmap of the image (profile picture) loaded from the gallary
    File myExternalFile;  // this is the file location - used to store basic user info in the internal storage of the phone
    File myExternalFile_temporary;  // this is the location of the image captured via camera (it is temporary because right agter the image is uploaded to cloud, we delete it from the phone storage as it is no longer needed)
    int storeOnlyOnce = 1;  // this makes sure that the basic user info is stored in the internal storage of the phone only once

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_signup);  // this is the XML file where you can find the layout of thos activity
        mAuth = FirebaseAuth.getInstance();  //initialise the firebase authentication variable
        user = mAuth.getCurrentUser();  //current user who is logged in.


        if(user.getEmail()!=null) {  // if there is some user logged in, store his email address in the variable 'email_string'
            email_string = user.getEmail();
        }
        else {  // if there is no user logged in, show the following toast notification
            Toast.makeText(AfterSignupActivity.this, "Email is empty", Toast.LENGTH_SHORT).show();
        }

        // these 3 lines of code uses the Intent component to receive the deviceName and deviceAddress from the previous activities and store them in new strings
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Initialisations
        mButtonChooseImage = findViewById(R.id.button_choose_image);  // attach 'button_choose_image' button element from the xml file to the variable 'mButtonChooseImage'
        mButtonUpload = findViewById(R.id.button_upload);  // attach 'button_upload' button element from the xml file to the variable 'mButtonUpload'
        mButtonSignout = findViewById(R.id.button_signout);  // attach 'button_signout' button element from the xml file to the variable 'mButtonSignout'
        email_textView = findViewById(R.id.email_textView);  // attach 'email_textView' textView element from the xml file to the variable 'email_textView'
        email_textView.setText(email_string);  // set the value of 'email_textView' to be the email address of the current logged in user
        nameOfPatient_editText = findViewById(R.id.nameOfPatient_editText);  // attach 'nameOfPatient_editText' editText field from the xml file to the variable 'nameOfPatient_editText'
        ageOfPatient_editText = findViewById(R.id.ageOfPatient_editText);  // attach 'ageOfPatient_editText' editText field from the xml file to the variable 'ageOfPatient_editText'
        bloodGroupOfPatient_editText = findViewById(R.id.bloodGroupOfPatient_editText);  // attach 'bloodGroupOfPatient_editText' editText field from the xml file to the variable 'bloodGroupOfPatient_editText'
        mImageView = findViewById(R.id.image_view);  // attach 'image_view' imageView element from the xml file to the variable 'mImageView'
        mProgressBar = findViewById(R.id.progress_bar);  // attach 'progress_bar' progressBar element from the xml file to the variable 'mProgressBar'
        mStorageRef = FirebaseStorage.getInstance().getReference();  // initialise the StorageReference of the firebase cloud (declared earlier)
        mDatabaseRef_Info = FirebaseDatabase.getInstance().getReference("User_Info");  // initialise the DatabaseReference of the firebase cloud (declared earlier)

        // when 'mButtonChooseImage' button is clicked, user is prompted to choose an image from the gallary
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // see if user granted camera permission. If not, show the dialog asking the user to grant the permission
                if (ContextCompat.checkSelfPermission(AfterSignupActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(AfterSignupActivity.this, new String[] {Manifest.permission.CAMERA}, 100);
                }

                // only when camera permission is Granted by user, execute the following
                if (ContextCompat.checkSelfPermission(AfterSignupActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                    // show an alert box, asking user whether he/she wants to select image from gallery or camera
                    AlertDialog.Builder alert = new AlertDialog.Builder(AfterSignupActivity.this);
                    alert.setTitle("Choose the Image Source");
                    alert.setIcon(android.R.drawable.ic_dialog_alert);

                    // if the user clicks on Gallery option, execute the following and open 'onActivityRequest'
                    alert.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // to take photo from gallery
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1); // one can be replaced with any requestcode
                        }
                    });

                    // if the user clicks on Camera option, execute the following and open 'onActivityRequest'
                    alert.setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // to take picture from the camera
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, CAMERA_REQUEST);  // requestCode
                        }
                    });
                    alert.show();

                } else { // if the user denies camera permission, show the following toast
                    Toast.makeText(AfterSignupActivity.this, "First, grant CAMERA permission from the Settings!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // when 'mButtonUpload' button is clicked, all the user specific credentials are uploaded to firebase cloud and are also stored in phone storage
        mButtonUpload.setOnClickListener(new View.OnClickListener() {   //Click this button to upload info to cloud
            @Override
            public void onClick(View view) {
                mButtonUpload.setEnabled(false);  // Disabling the 'upload' button to avoid multiple presses
                Toast.makeText(AfterSignupActivity.this, "Please Wait!", Toast.LENGTH_SHORT).show();
                String naMe = nameOfPatient_editText.getText().toString();
                String eMail = user.getEmail();
                String aGe = ageOfPatient_editText.getText().toString();
                String bloodGroup = bloodGroupOfPatient_editText.getText().toString();
                String filename = "info.txt";  // this is the name of the text file, which will store user-specific credentials in the internal storage of phone
                String filepath = eMail.trim();  // make a user-specific folder in the internal storage. The folder name will be the email address of the user
                myExternalFile = new File(getExternalFilesDir(filepath), filename);  // all the user specific credentials are also stored in the internal storage with the file name 'info.txt'

                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {  // if the storage is not available in the phone, show the following toast message
                    Toast.makeText(AfterSignupActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();

                }
                else {  // if the storage is available in the phone, all the user specific credentials are stored in the internal storage with the file name 'info.txt'
                    if (storeOnlyOnce == 1) {  // this assures that the info.txt is created only once in the internal storage
                        try {
                            FileOutputStream fos = new FileOutputStream(myExternalFile, true);  // this function makes a file in the internal storage (in this case 'info.txt')
                            fos.write("Name: ".getBytes());  // the file contains user Name
                            fos.write(naMe.getBytes());
                            fos.write("\n".getBytes());
                            fos.write("Age: ".getBytes());  // the file contains user Age
                            fos.write(aGe.getBytes());
                            fos.write("\n".getBytes());
                            fos.write("Blood Group: ".getBytes());  // the file contains user bloodgroup
                            fos.write(bloodGroup.getBytes());
                            fos.write("\n".getBytes());
                            fos.write("Email: ".getBytes());  // the file contains user Email address
                            fos.write(eMail.getBytes());
                            fos.write("\n".getBytes());
                            fos.close();
                            storeOnlyOnce++;
                        } catch (IOException e) {
                        }
                    }
                }

                // Simultaneously, if the user credentials are being uploaded to the cloud, show the following toast notification to the user
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(AfterSignupActivity.this, "Upload in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    //if somehow the user credentials are not being uploaded to the cloud, call the following method
                    uploadFile();  // this method begins to upload the user credentials on the cloud
                }
            }
        });

        // when 'mButtonSignout' button is clicked,
        mButtonSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploadIsClicked==1) {  // if the data upload to cloud has completed successfully, then
                    FirebaseAuth.getInstance().signOut();  // sign out the user and show the following toast notification
                    Toast.makeText(AfterSignupActivity.this, "Signed Out!", Toast.LENGTH_LONG).show();
                    // As the user signs out, use intent component and move to the very first activity
                    Intent intent = new Intent(AfterSignupActivity.this, DeviceScanActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(AfterSignupActivity.this, "Data is not uploaded yet...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // This function is called when 'mButtonUpload' button is clicked
    // this method begins to upload the user credentials (name, age, email, blood group, photo) on the cloud
    private void uploadFile() {
        if (resultUri != null || photoBitmap != null) {  // if the profile picture URI (i.e. picture being displayed in 'mImageView') is not empty then execute the following
            if(nameOfPatient_editText.getText().toString().matches("") || user.getEmail().matches("") || ageOfPatient_editText.getText().toString().matches("") || bloodGroupOfPatient_editText.getText().toString().matches("")) {  // again if any of the fields like patient's name, age, email, blood group are empty, then show the following toast notifications
                Toast.makeText(this, "One or more data fields are empty... ", Toast.LENGTH_SHORT).show();
            }
            else {  //else if none of the info, like patient's name, age, email, blood group, photo URI are empty, then proceed
                uploadIsClicked=1;  // indicates that the upload button has been clicked once
                File thumb_filePathUri = new File(resultUri.getPath());  // 'thumb_filePathUri' stores the path location of the image
                try {
                    thumb_bitmap = new Compressor(AfterSignupActivity.this)  // here we try to compress the image and store the resulting bitmap in 'thumb_bitmap'
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePathUri);

                } catch (IOException e) {  // if somehow the compression fails, show the following message in the console
                    // try-catch block prevents the application from crashing, in case the code fails to execute
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  // 'byteArrayOutputStream' stores the bitmap of the image
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);  // here the quality of the image is reduced to 50%. This is done to reduce the overall size of the Image (because database has limited space)
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();  // here we are converting the bitmap to byteArray

                final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtensions(resultUri));  // the image file will be stored in the firebase Storage, where the name of the file will be System.currentTimeMillis().mime (eg- 16347873498.png)

                // The next command says that if the image file is being uploaded to the firebase storage location, listen to the changes
                mUploadTask = fileReference.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {  // if the image file is uploaded to the firebase storage location successfully, call this method
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {  // This handler function makes sure that the enclosing code can run on the UI without bocking the UI
                            @Override
                            public void run() {
                                mProgressBar.setProgress(0);
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {  // copy the URL of the image stored in the storage reference of the firebase cloud
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl = uri;  // if copying the image URI is successful, store that in the variable 'downloadUrl'

                                        // all the credentials are passed to the 'Upload' class
                                        // The 'Upload' class makes sure to upload the Name, Age, BloodGroup, ImageUrl and Email to the firebase database
                                        final Upload upload = new Upload(nameOfPatient_editText.getText().toString().trim(),
                                                ageOfPatient_editText.getText().toString().trim(), bloodGroupOfPatient_editText.getText().toString().trim(),
                                                downloadUrl.toString().trim(), email_string.trim());

                                        // This block of code is mainly responsible for uploading data to cloud
                                        // Firstly we check if the user "email" exists in the database under any of the parent keys. If it does, 'onDataChange' is executed, else 'onCancelled' is executed
                                        mDatabaseRef_Info.orderByChild("email").equalTo(email_string.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){  // if Database exists for this user
                                                    //then since database already has some entries of user, update more entries via Upload Class
                                                    for (com.google.firebase.database.DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                                        String uid = childSnapshot.getKey();
                                                        // Listen to database changes while data is uploading
                                                        mDatabaseRef_Info.child(uid).setValue((upload), new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@NonNull DatabaseError error, DatabaseReference ref) {
                                                                System.out.println("Value was set. Error = "+error);
                                                                if (error != null) {  // if there is some error while data is being uploaded, show the following toast
                                                                    Toast.makeText(AfterSignupActivity.this, "Data could not be saved. Please try again! " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    mButtonUpload.setEnabled(true);  // if somehow data upload fails, enable the 'upload' button; so the user can again try to upload data to the cloud!
                                                                }
                                                                else {  // if data is uploaded without any error, show the following and move to next activity called 'AfterLoginActivity'
                                                                    Toast.makeText(AfterSignupActivity.this, "Data written to database successfully!", Toast.LENGTH_SHORT).show();

                                                                    // Finally after uploading the info to the cloud, delete the redundant image from the phone storage
                                                                    File redundantFile = new File(String.valueOf(myExternalFile_temporary));
                                                                    if(redundantFile.exists()) {
                                                                        redundantFile.delete();
                                                                    }

                                                                    // via intent component, move to the next class called 'AfterLoginActivity'
                                                                    Intent intent = new Intent(AfterSignupActivity.this, AfterLoginActivity.class);
                                                                    // To the new activity pass the device address and device name
                                                                    intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                                                                    intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                                                                    startActivity(intent);  // start the new activity
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    Toast.makeText(AfterSignupActivity.this, "Some Error in Uploading to database!", Toast.LENGTH_SHORT).show();
                                                    mButtonUpload.setEnabled(true);  // if somehow data upload fails, enable the 'upload' button; so the user can again try to upload data to the cloud!
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(AfterSignupActivity.this, "Data could not be saved. Please try again! " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                mButtonUpload.setEnabled(true);  // if somehow data upload fails, enable the 'upload' button; so the user can again try to upload data to the cloud!
                                            }
                                        });
                                    }
                                });
                            }
                        }, 500);  // wait for a delay of 500 ms and then execute handler function
                        Toast.makeText(AfterSignupActivity.this, "Information Uploaded!!", Toast.LENGTH_LONG).show(); // Once the info is uploaded, show the following toast notification
                    }
                }).addOnFailureListener(new OnFailureListener() {  // if somehow the upload task is not successful, listen for any failures
                    @Override
                    public void onFailure(@NonNull Exception e) {  // if the code fails, show the following toast notification
                        Toast.makeText(AfterSignupActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mButtonUpload.setEnabled(true);  // if somehow data upload fails, enable the 'upload' button; so the user can again try to upload data to the cloud!
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {  // if the upload task is going on (in progression), listen for changes


                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) { // Progress Listener for changing color of the progressBar
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mProgressBar.setProgress((int) progress);
                            }
                        });
            }
        } else { // if the URI of the image is null, probably the user has not selected any file. In that case show the following notification
            Toast.makeText(this, "No File Selected...", Toast.LENGTH_SHORT).show();
        }
    }

    // this is called when 'mButtonChooseImage' button is clicked  // it checks whether user requested to click picture or choose image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {  // it checks whether user requested to click picture or choose image from gallery
            case CAMERA_REQUEST:  // if user choose to take picture from the camera, execute the following
                if(resultCode == Activity.RESULT_OK){
                    photoBitmap = (Bitmap) data.getExtras().get("data");  // get the bitmap of the clicked image

                    // the following if-else code, makes the aspect ratio of image 1:1
                    if (photoBitmap.getWidth() >= photoBitmap.getHeight()){
                        photoBitmap = Bitmap.createBitmap(
                                photoBitmap,
                                photoBitmap.getWidth()/2 - photoBitmap.getHeight()/2,
                                0,
                                photoBitmap.getHeight(),
                                photoBitmap.getHeight()
                        );
                    }  else {
                        photoBitmap = Bitmap.createBitmap(
                                photoBitmap,
                                0,
                                photoBitmap.getHeight()/2 - photoBitmap.getWidth()/2,
                                photoBitmap.getWidth(),
                                photoBitmap.getWidth()
                        );
                    }

                    // The following code makes sure that the image is displayed correctly in the imageview
                    // the above code flips the captured image, therefore, the following code flips it agin to display it correctly
                    Matrix matrix = new Matrix();  // matrix stores the 2D bitmap
                    matrix.setScale(-1, 1);  // -1 flips the image horizontally, 1 keeps the image same along vertical direction
                    matrix.postTranslate(photoBitmap.getWidth(),0);  // this command flips the 'photoBitmap'
                    photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);
                    mImageView.setImageBitmap(photoBitmap);  // set the resulting bitmap in the image view (mImageView)

                    // The following code stores the URI of the captured image in the variable 'resultURI', that will be used to upload the image to the cloud (when 'upload' button is clicked)
                    // to get the URI of this image, first store it in the phone storage, then extract it's path; then convert that path to URI and store that URI in 'resultUri'
                    try{
                        mImageView.setDrawingCacheEnabled(true); // so that I can extract the bitmap of the image displayed in 'mImageView'
                        final Bitmap bmap = mImageView.getDrawingCache();  // this command actually extracts the bitmap of the image and stores it in the variable 'bmap'

                        runOnUiThread(new Runnable() {  //runOnUiThread command, runs the underlying code in a separate thread to avoid UI crash
                            @Override
                            public void run() {
                                if(bmap!=null) {
                                    String filename = "img.jpg";  // name of the stored image
                                    String filepath = email_string.trim();  // make a user-specific folder in the internal storage. The folder name will be the email address of the user
                                    myExternalFile_temporary = new File(getExternalFilesDir(filepath), filename);  // all the user specific credentials are also stored in the internal storage with the file name 'info.txt'

                                    if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {  // if the storage is not available in the phone, show the following toast message
                                        Toast.makeText(AfterSignupActivity.this,"Your external storage is not available", Toast.LENGTH_SHORT).show();

                                    }
                                    else {  // if the storage is available in the phone, store the image named as 'img.jpg' in the phone storage
                                        try {  // try executing the following
                                            FileOutputStream fos = new FileOutputStream(myExternalFile_temporary);  // fileOutputStream is responsible for writing/reading files
                                            bmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);  // write the bitmap file
                                            fos.close();  // close writing
                                            Toast.makeText(AfterSignupActivity.this, "Captured and Saved!" + myExternalFile_temporary, Toast.LENGTH_LONG).show();  // show this toast notification once the image is successfully stored in the phone storage
                                            resultUri = Uri.fromFile(new File(String.valueOf(myExternalFile_temporary)));  // storing the image URI in the variable 'resultUri'
                                            mImageView.destroyDrawingCache(); //just destroy cache after all necessary actions are performed for the sake of clearing space
                                            mImageView.setDrawingCacheEnabled(false);
                                        } catch (Exception e) {  // if the 'try' statement fails, execute the following
                                            Toast.makeText(AfterSignupActivity.this, "Storing image in the phone has failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            }
                        });

                    } catch (Exception e) {
                    }
                }
                break;

            case 1:  //  if user choose to take photo from gallery, execute following
                if(resultCode == RESULT_OK && data != null && data.getData() != null) {
                    CropImage.activity(data.getData())  // crop the image to make its aspect ratio as 1:1
                            .setAspectRatio(1, 1)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                }
                break;
        }

        // once cropping of image is done, store the resulting image URI in the variable 'resultUri'
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            try {
                resultUri = result.getUri();
                Picasso.with(this).load(resultUri).into(mImageView);  // display this cropped image in the imageView element 'mImageView'
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // this method is called from the 'uploadFile' function
    // this method is used to extract the file extension of the image (JPEG, PNG, etc)
    private String getFileExtensions(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();  // 'mime' stores the extension of the file
        return mime.getExtensionFromMimeType(cR.getType(uri));  // this command returns the 'mime' type of the file
    }

    //both of them for accessing file creation in phone's storage
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

    //These two functions help to avoid opening back activities while pressing back button accidentally
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {  //if user presses any phone key
        if (keyCode == KeyEvent.KEYCODE_BACK) {  // check if that key is 'Back' key. If yes,
            onBackPressed();  // call this function
        }
        return super.onKeyDown(keyCode, event);
    }

    // on pressing back button, it opens an Alert box and asks the user if they really want to leave the activity
    //if the user presses 'Yes', it leaves this activity. If the user presses 'No', it stays.
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  // constructing alert box
        builder.setTitle("Upload Or Not");  // title of Alert Box
        builder.setMessage("Do you want to leave without uploading ");  // Message of Alert Box
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {  //listen to the user Input
            public void onClick(DialogInterface dialog, int id) {  // if the user presses 'Yes'
                Intent intent = new Intent(AfterSignupActivity.this, DoctorOrPatient.class);  // go to activity 'DoctorOrPatient'
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

/**
 * Next Activity will likely to be the "AfterLoginActivity" since you are already registered!
 */