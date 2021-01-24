/** We land here from emailPasswordDoctorActivity
 * This activity gives the user two options/buttons -
 * Either to load single-sensor csv and view its graph - MakeGraph
 * Or to load multi-sensor csv and view its graph - MakeGraph_multi */


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

public class graphModeActivity extends AppCompatActivity {

    // Button declarations
    public Button read_CSV_single, read_CSV_multi;  // declaring 2 buttons of this activity - one to load a single sensor CSV from the internal memory and the other button to load multi-sensor CSV from the internal memory

    @Override
    protected void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like imt main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_mode);  // this is the XML file where you can find the layout of this activity

        // Initialising the 2 buttons
        read_CSV_single = findViewById(R.id.read_CSV_single);  // attach 'read_CSV_single' button from the xml file to the variable 'read_CSV_single'
        read_CSV_multi = findViewById(R.id.read_CSV_multi);  // attach 'read_CSV_multi' button from the xml file to the variable 'read_CSV_multi'


        // if 'read_CSV_single' button is clicked, start intent component to move to a new activity called 'MakeGraph'
        read_CSV_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(graphModeActivity.this, MakeGraph.class);
                startActivity(intent);
            }
        });

        // if 'read_CSV_multi' button is clicked, start intent component to move to a new activity called 'MakeGraph_multi'
        read_CSV_multi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(graphModeActivity.this, MakeGraph_multi.class);
                startActivity(intent);
            }
        });
    }


    // These two functions help to avoid opening back activities while pressing back button accidentally
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    // on pressing back button, it opens an Alert box and asks the user if they really want to leave the activity
    // if the user presses 'Yes', it leaves the activity. If the user presses 'No', it stays.
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want leave?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.show();
    }
}

/** Next Activity is either "MakeGraph" or "MakeGraph_multi" */
