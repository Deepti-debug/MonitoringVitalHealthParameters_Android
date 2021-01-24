/** We land here from graphModeActivity (when user clicks on single-sensor csv button)
 * This activity prompts the user to load a single-sensor csv file from the internal storage
 * Once the user does that and click on "SHOW GRAPH" Button, this activity shows the single graph corresponding to csv file
 */

//This is the package name of our project.
//The project-related files will be stored in the internal storage under the same package name
package com.a.a.remotehealthmonitoring;

//These are the various libraries imported by us
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.csvreader.CsvReader;

//This acticity is responsible for rendering graph from the CSV file (in doctor's page)
public class MakeGraph extends AppCompatActivity {
    private final static String TAG = MakeGraph.class.getSimpleName();  // TAG just stores the name of this activity. It will be used while printing logs in the console
    private static final int REQUEST_CODE = 6384;  // static integer initialisation. It will be used in the fuction 'startActivityForResult'
    private String path="";  // this string will store the path of the csv file selected from the file chooser
    private Uri uri; // This stores the URI of the CSV file selected by the user

    private Button showGraph_btn;  // declaring a button - which when clicked renders the graph corresponding to the loaded CSV file
    private TextView textLink;  // shows the location of the loaded CSV file

    private GraphView graph;  // declares the graph component
    private LineGraphSeries<DataPoint> series = null;  // declares the graph component (datapoints shown on graph)
    double lastX=0;  // This variable stores the dataPoints

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like imt main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_graph);  // this is the XML file where you can find the layout of this activity

        // initialisations
        showGraph_btn = findViewById(R.id.showGraph);  // attach 'showGraph' button from the xml file to the variable 'showGraph_btn'
        textLink = findViewById(R.id.textLink);  // attach 'textLink' text field from the xml file to the variable 'textLink'
        graph = findViewById(R.id.graphview);  // attach the 'graphview' graph component from the xml file to the variable 'graph'

        series = new LineGraphSeries<>();  // initialise the graph series component
        Viewport viewport = graph.getViewport();  // initialise the view of the graph
        // the following commands initialise the graph components:
        viewport.setXAxisBoundsManual(true);  // allows to expand the X-axis manually by pinch Zoom
        viewport.setYAxisBoundsManual(true);  // allows to expand the Y-axis manually by pinch Zoom
        viewport.setMinY(0);  // the minimum Y-axis value could be 0
//        viewport.setMaxY(700);
        viewport.setMinX(0);  // the minimum X-axis value could be 0
        viewport.setMaxX(500);  // the maximum X-axis value to be seen at once is set to 500, to see values beyond that, scroll the graph
        viewport.setScrollable(true);  // X-axis scrolling is allowed
        viewport.setScrollableY(true);  // Y-axis scrolling is allowed

        viewport.setScalable(true);   // X-axis zooming/expansion is allowed
        viewport.setScalableY(true);  // Y-axis zooming/expansion is allowed
//        graph.setTitleColor(Color.rgb(255,193,7));
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel.setHorizontalAxisTitle("                       TIME");  // Here we set the X-axis Label as 'TIME'
        gridLabel.setVerticalAxisTitle("values");  // Here we set the Y-axis Label as 'Values'

        series.setTitle("Sensor Data");  // This sets the Title of the graph
        series.setColor(Color.rgb(255,102,102));  //  This sets the color of the datapoints shown on the graph
        series.setBackgroundColor(Color.argb(80,196,3,0));  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        series.setDrawBackground(true);  // The background draw is set true
        series.setDrawAsPath(true);  // The datapoints draw is set true

        //After all the initialisations that have happened via previous commands;
        //The next command is responsible to extract the URI of the CSV file selected by the user.
        Intent target = MakeGraph_FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, "Lorem ipsum");
        try {
            startActivityForResult(intent, REQUEST_CODE);  // When the file is clicked by the user, this function is executed and calls 'onActivityResult'
        } catch (ActivityNotFoundException e) {
            Toast.makeText(MakeGraph.this, "Activity Not Found!", Toast.LENGTH_SHORT).show();
        }

        // if 'showGraph_btn' button is clicked, read the CSV file and plot the graph
        showGraph_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGraph_btn.setEnabled(false);
                showGraph_btn.getBackground().setAlpha(64);
                graph.addSeries(series);
                readCSVandPlot();  // the graph is plotted using this function
            }
        });
    }

    // This activity is executed when the user clicks on a file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if(data!=null) {  // This checks if the user has actually clicked on a file i.e. the data is not null
                        uri = data.getData();  // this stores the URI of the selected file in the variable 'uri'
                        Log.i(TAG, "Uri: "+uri.toString());  // this just shows the uri of the selected file in the log console
                        try {
                            path = MakeGraph_FileUtils.getPath(this, uri);  // the 'path' variable stores the path information of the selected file
                            Toast.makeText(MakeGraph.this, "File Selected: "+path, Toast.LENGTH_SHORT).show();  // Toast message to show the file path
                            textLink.setText(path);  //set the path of the selected file in the 'textLink' text-field
                        } catch (Exception e) {
                            // if somehow the try block fails to execute properly, execute this catch block and show the toast notification given below
                            Toast.makeText(MakeGraph.this, "File not found! "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    // This function is called when the 'showGraph_btn' button is clicked. It shows the graph corresponding to the CSV file
    public void readCSVandPlot() {
        if(path!=null) {  // if the path of the selected CSV file is not empty, then execute the following
            try {
                CsvReader reader = new CsvReader(path);  // initialise CsvReader
                reader.readHeaders();  // This says that the csv involves headers

                int countHeader = reader.getHeaderCount(); // This counts the number of headers - it should be 3 for a single sensor csv (Date, Time, SensorValue)
                while (reader.readRecord())  // while each CSV file row is being read one by one, execute the following
                {
                    String date = reader.get("Date");  // 'date' variable stores the reading that appears under the header Date
                    String time = reader.get("Time");  // 'time' variable stores the reading that appears under the header Time
                    String ecg = reader.get("ECG");  // 'ecg' variable stores the reading that appears under the header ECG
                    String emg = reader.get("EMG");  // 'emg' variable stores the reading that appears under the header EMG
                    String gsr = reader.get("GSR");  // 'gsr' variable stores the reading that appears under the header GSR
                    String pulse = reader.get("Pulse");  // 'pulse' variable stores the reading that appears under the header Pulse
                    String temp = reader.get("Temp");  // 'temp' variable stores the reading that appears under the header Temp

                    if(countHeader == 3) {  // The number of columns for a single-sensor CSV file must be 3, if so, execute the enclosing statements

                        // If ecg value (row by row) is not empty then
                         if(!ecg.equals("") && !ecg.equals("!")) {
                            try {
                                // ecg value obtained from the CSV file is stored in the float variable
                                float ecg_float = Float.parseFloat(ecg);
                                // this variable value is then passed to the graph datapoint via 'appendData' method
                                series.appendData(new DataPoint(lastX++, ecg_float), true, 1000);
                            } catch (Exception e) {}

                        }

                        // If emg value (row by row) is not empty then
                        else if(!emg.equals("")) {
                            try {
                                // emg value obtained from the CSV file is stored in the float variable
                                float emg_float = Float.parseFloat(emg);
                                // this variable value is then passed to the graph datapoint via 'appendData' method
                                series.appendData(new DataPoint(lastX++, emg_float), true, 1000);
                            } catch (Exception e) {}
                        }

                        // If gsr value (row by row) is not empty then
                        else if(!gsr.equals("")) {
                            try {
                                // gsr value obtained from the CSV file is stored in the float variable
                                float gsr_float = Float.parseFloat(gsr);
                                // this variable value is then passed to the graph datapoint via 'appendData' method
                                series.appendData(new DataPoint(lastX++, gsr_float), true, 1000);
                            } catch (Exception e) {}
                        }

                        // If pulse value (row by row) is not empty then
                        else if(!pulse.equals("") && !pulse.startsWith("?")) {
                            try {
                                // pulse value obtained from the CSV file is stored in the float variable
                                float pulse_float = Float.parseFloat(pulse);
                                // this variable value is then passed to the graph datapoint via 'appendData' method
                                series.appendData(new DataPoint(lastX++, pulse_float), true, 1000);
                            } catch (Exception e) {}
                        }

                        // If temp value (row by row) is not empty then
                        else if(!temp.equals("")) {
                            try {
                                // temp value obtained from the CSV file is stored in the float variable
                                float temp_float = Float.parseFloat(temp);
                                // this variable value is then passed to the graph datapoint via 'appendData' method
                                series.appendData(new DataPoint(lastX++, temp_float), true, 1000);
                            } catch (Exception e) {}
                        }
                    }
                    else if (countHeader!=3) {  // If the number of columns are not 3, probably it is not the right CSV and in that case, execute the following toast message
                        ToastFunction();
                        break;
                    }
                }
                reader.close();  // after the whole CSV is parsed, close the CSV reader.
            } catch (Exception e) {
                ToastFunction();
            }
        }
    }

    // If the values in CSV file are empty, this function is called
    private void ToastFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);   // Alert box is shown to the user
        builder.setMessage("Choose the right CSV file");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.show();
    }
}

// This is the last activity in the doctor's page