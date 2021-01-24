/** We land here from graphModeActivity (when user clicks on multi-sensor csv button)
 * This activity prompts the user to load a multi-sensor csv file from the internal storage
 * Once the user does that and click on "SHOW GRAPH" Button, this activity shows the multiple graphs corresponding to csv file
 * In this case, it shows four graphs - ecg, emg, pulse, gsr
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
import com.csvreader.CsvReader;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MakeGraph_multi extends AppCompatActivity {
    private final static String TAG = MakeGraph.class.getSimpleName();  // TAG just stores the name of this activity. It will be used while printing logs in the console

    private static final int REQUEST_CODE = 6384;  // static integer initialisation. It will be used in the fuction 'startActivityForResult'
    private String path="";  // this string will store the path of the csv file selected from the file chooser
    private Uri uri;  // This stores the URI of the CSV file selected by the user

    private Button showGraph_btn;  // declaring a button - which when clicked renders the graph corresponding to the loaded CSV file
    private TextView textLink;  // shows the location of the loaded CSV file
    double lastX_ecg=0, lastX_emg=0, lastX_gsr=0, lastX_pulse=0;  // This variable stores the dataPoint count for each of the 4 graphs

    private GraphView graphECG, graphEMG, graphGSR, graphPulse;  // declares 4 graph components for each of the four sensors
    private LineGraphSeries<DataPoint> seriesECG = null;  // declares the ecg graph component (to show the datapoints on graph)
    private LineGraphSeries<DataPoint> seriesEMG = null;  // declares the emg graph component (to show the datapoints on graph)
    private LineGraphSeries<DataPoint> seriesGSR = null;  // declares the gsr graph component (to show the datapoints on graph)
    private LineGraphSeries<DataPoint> seriesPulse = null;  // declares the pulse graph component (to show the datapoints on graph)

    @Override
    public void onCreate(Bundle savedInstanceState) {  // onCreate is the main function where everything runs - it is like imt main() of C language.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_graph_multi);  // this is the XML file where you can find the layout of this activity

        // initialisations
        showGraph_btn = findViewById(R.id.showGraph); // attach 'showGraph' button from the xml file to the variable 'showGraph_btn'
        textLink = findViewById(R.id.textLink);  // attach 'textLink' text field from the xml file to the variable 'textLink'

        graphECG = findViewById(R.id.graphviewECG);  // attach the 'graphviewECG' graph component from the xml file to the variable 'graphECG'
        seriesECG = new LineGraphSeries<>();  // initialised the seriesECG graph component (declared earlier)
        Viewport viewport1 = graphECG.getViewport();  // initialise the view of the ecg graph
        // the following commands initialise the graph components:
        viewport1.setXAxisBoundsManual(true);  // allows to expand the X-axis manually by pinch Zoom
        viewport1.setYAxisBoundsManual(true);  // allows to expand the Y-axis manually by pinch Zoom
        viewport1.setMinY(0);  // the minimum Y-axis value could be 0
        viewport1.setMaxY(700);  // the maximum Y-axis value could be 700
        viewport1.setMinX(0);  // the minimum X-axis value could be 0
        viewport1.setMaxX(500);  // the maximum X-axis value could be 500
        viewport1.setScrollable(true);  // graph scrolling is allowed
        graphECG.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graphECG.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel = graphECG.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel.setHorizontalAxisTitle("                       TIME");  // Here we set the X-axis Label as 'TIME'
        gridLabel.setVerticalAxisTitle("ecg values");  // Here we set the Y-axis Label as 'ecg values'
        graphECG.getViewport().setScalable(true);   // graph zooming/expansion is allowed
        graphECG.getViewport().setScalableY(true);   // Y-axis zooming/expansion is allowed
        seriesECG.setTitle("ECG Data");  // This sets the Title of the graph
        seriesECG.setColor(Color.rgb(255,102,102));  //  This sets the color of the datapoints shown on the graph
        seriesECG.setBackgroundColor(Color.argb(80,196,3,0));  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        seriesECG.setDrawBackground(true);  // The background draw is set true
        seriesECG.setDrawAsPath(true);  // The datapoints draw is set true
        viewport1.setScalable(true);   //enable zooming and scrolling
        viewport1.setScalableY(true);  //enable Y-axis zooming and scrolling
//        graphECG.setTitleColor(Color.rgb(255,193,7));

        graphEMG = findViewById(R.id.graphviewEMG);  // attach the 'graphviewEMG' graph component from the xml file to the variable 'graphEMG'
        seriesEMG = new LineGraphSeries<>();  // initialised the seriesEMG graph component (declared earlier)
        Viewport viewport2 = graphEMG.getViewport();  // initialise the view of the ecg graph
        // the following commands initialise the graph components:
        viewport2.setXAxisBoundsManual(true);  // allows to expand the X-axis manually by pinch Zoom
        viewport2.setYAxisBoundsManual(true);  // allows to expand the Y-axis manually by pinch Zoom
        viewport2.setMinY(0); // the minimum Y-axis value could be 0
//        viewport1.setMaxY(700);
        viewport2.setMinX(0); // the minimum X-axis value could be 0
        viewport2.setMaxX(500); // the maximum X-axis value could be 500
        viewport2.setScrollable(true);  // graph scrolling is allowed
        graphEMG.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graphEMG.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel2 = graphEMG.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel2.setHorizontalAxisTitle("                       TIME");  // Here we set the X-axis Label as 'TIME'
        gridLabel2.setVerticalAxisTitle("emg values");  // Here we set the Y-axis Label as 'emg values'
        graphEMG.getViewport().setScalable(true);  // graph zooming/expansion is allowed
        graphEMG.getViewport().setScalableY(true);  // Y-axis zooming/expansion is allowed
        seriesEMG.setTitle("EMG Data");  // This sets the Title of the graph
        seriesEMG.setColor(Color.rgb(255,102,102));  //  This sets the color of the datapoints shown on the graph
        seriesEMG.setBackgroundColor(Color.argb(80,196,3,0));  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        seriesEMG.setDrawBackground(true);  // The background draw is set true
        seriesEMG.setDrawAsPath(true);  // The datapoints draw is set true
        viewport2.setScalable(true);   //enable zooming and scrolling
        viewport2.setScalableY(true);  //enable Y-axis zooming and scrolling
//        graphEMG.setTitleColor(Color.rgb(255,193,7));

        graphGSR = findViewById(R.id.graphviewGSR);  // attach the 'graphviewGSR' graph component from the xml file to the variable 'graphGSR'
        seriesGSR = new LineGraphSeries<>();  // initialised the seriesGSR graph component (declared earlier)
        Viewport viewport3 = graphGSR.getViewport();  // initialise the view of the ecg graph
        // the following commands initialise the graph components:
        viewport3.setXAxisBoundsManual(true);  // allows to expand the X-axis manually by pinch Zoom
        viewport3.setYAxisBoundsManual(true);  // allows to expand the Y-axis manually by pinch Zoom
        viewport3.setMinX(0); // the minimum X-axis value could be 0
        viewport3.setMaxX(500); // the maximum X-axis value could be 500
        viewport3.setScrollable(true);  // graph scrolling is allowed
        graphGSR.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graphGSR.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel3 = graphGSR.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel3.setHorizontalAxisTitle("                       TIME");  // Here we set the X-axis Label as 'TIME'
        gridLabel3.setVerticalAxisTitle("gsr values");  // Here we set the Y-axis Label as 'gsr values'
        graphGSR.getViewport().setScalable(true);  // graph zooming/expansion is allowed
        graphGSR.getViewport().setScalableY(true);  // Y-axis zooming/expansion is allowed
        seriesGSR.setTitle("GSR Data");  // This sets the Title of the graph
        seriesGSR.setColor(Color.rgb(255,102,102));  //  This sets the color of the datapoints shown on the graph
        seriesGSR.setBackgroundColor(Color.argb(80,196,3,0));  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        seriesGSR.setDrawBackground(true);  // The background draw is set true
        seriesGSR.setDrawAsPath(true);  // The datapoints draw is set true
        viewport3.setScalable(true);   //enable zooming and scrolling
        viewport3.setScalableY(true);  //enable Y-axis zooming and scrolling
//        graphGSR.setTitleColor(Color.rgb(255,193,7));

        graphPulse = findViewById(R.id.graphviewPULSE);  // attach the 'graphviewPULSE' graph component from the xml file to the variable 'graphPulse'
        seriesPulse = new LineGraphSeries<DataPoint>();  // initialised the seriesPulse graph component (declared earlier)
        Viewport viewport4 = graphPulse.getViewport();  // initialise the view of the ecg graph
        viewport4.setXAxisBoundsManual(true);  // allows to expand the X-axis manually by pinch Zoom
        viewport4.setYAxisBoundsManual(true);  // allows to expand the Y-axis manually by pinch Zoom
        viewport4.setMinX(0);  // the minimum X-axis value could be 0
        viewport4.setMaxX(500);  // the maximum X-axis value could be 500
        viewport4.setMinY(0);  // the minimum Y-axis value could be 0
//        viewport4.setMaxY(1000);
        viewport4.setScrollable(true);  // graph scrolling is allowed
        graphPulse.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#800000"));  // This shows horizontal grids of gray color in the graph
        graphPulse.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#800000"));  // This shows vertical grids of gray color in the graph
        GridLabelRenderer gridLabel4 = graphPulse.getGridLabelRenderer();  // This initialises the 'gridLabel' (X-axis label and Y-axis label)
        gridLabel4.setHorizontalAxisTitle("                       TIME(sec)");  // Here we set the X-axis Label as 'TIME'
        gridLabel4.setVerticalAxisTitle("pulse values");  // Here we set the Y-axis Label as 'pulse values'
        graphPulse.getViewport().setScalable(true);  // graph zooming/expansion is allowed
        graphPulse.getViewport().setScalableY(true);  // Y-axis zooming/expansion is allowed
        seriesPulse.setColor(Color.rgb(255,102,102));  //  This sets the color of the datapoints shown on the graph
        seriesPulse.setBackgroundColor(Color.argb(80,196,3,0));  //  This sets the color of the background shown on the graph (i.e. area under the graph)
        seriesPulse.setDrawBackground(true);  // The background draw is set true
        seriesPulse.setDrawAsPath(true);  // The datapoints draw is set true
        seriesPulse.setTitle("Pulse Data");
        viewport4.setScalable(true);   //enable zooming and scrolling
        viewport4.setScalableY(true);  //enable Y-axis zooming and scrolling

        //After all the initialisations that have happened via previous commands;
        //The next command is responsible to extract the URI of the CSV file selected by the user.
        Intent target = MakeGraph_FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, "Lorem ipsum");
        try {
            startActivityForResult(intent, REQUEST_CODE);  // When the file is clicked by the user, this function is executed and calls 'onActivityResult'
        } catch (ActivityNotFoundException e) {
            Toast.makeText(MakeGraph_multi.this, "Activity Not Found!", Toast.LENGTH_SHORT).show();
        }

        // if 'showGraph_btn' button is clicked, read the CSV file and plot the graph
        showGraph_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGraph_btn.setEnabled(false);
                showGraph_btn.getBackground().setAlpha(64);
                graphECG.addSeries(seriesECG);
                graphEMG.addSeries(seriesEMG);
                graphGSR.addSeries(seriesGSR);
                graphPulse.addSeries(seriesPulse);
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
                            Toast.makeText(MakeGraph_multi.this, "File Selected: "+path, Toast.LENGTH_SHORT).show();  // Toast message to show the file path
                            textLink.setText(path);  //set the path of the selected file in the 'textLink' text-field
                        } catch (Exception e) {
                            // if somehow the try block fails to execute properly, execute this catch block and show the toast notification given below
                            Toast.makeText(MakeGraph_multi.this, "File not found! "+e, Toast.LENGTH_SHORT).show();
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

                    // Toast.makeText(MakeGraph.this, "data: "+emg, Toast.LENGTH_SHORT).show();
                    // perform program logic here
                                        Log.d(TAG, "val1: "+ ecg);

                                        Log.d(TAG, "val2: "+ emg);

                                        Log.d(TAG, "val3: "+ gsr);

                                        Log.d(TAG, "val4: "+ pulse);

                    if(countHeader == 6) {  // if the number of columns are 6 (date, time, ecg, emg, gsr, pulse) only then execute the following
                        if (!ecg.equals("") && !emg.equals("") && !gsr.equals("") && !pulse.equals("")) {  // here we check that none of these values must be empty

                            if (!ecg.equals("!")) {  // If ecg value (row by row) is not empty then
                                try {  // ecg value obtained from the CSV file is stored in the float variable
                                    float ecg_float = Float.parseFloat(ecg);
                                    seriesECG.appendData(new DataPoint(lastX_ecg++, ecg_float), true, 1000);
                                } catch (Exception e) {}
                            }

                            try {  // If emg value (row by row) is not empty then
                                float emg_float = Float.parseFloat(emg);  // emg value obtained from the CSV file is stored in the float variable
                                seriesEMG.appendData(new DataPoint(lastX_emg++, emg_float), true, 1000);
                            } catch (Exception e) {}
//
                            try {  // If gsr value (row by row) is not empty then
                                float gsr_float = Float.parseFloat(gsr);  // gsr value obtained from the CSV file is stored in the float variable
                                seriesGSR.appendData(new DataPoint(lastX_gsr++, gsr_float), true, 1000);
                            } catch (Exception e) {}

                            if (!pulse.startsWith("?")) {
                                try {  // If pulse value (row by row) is not empty then
                                    float pulse_float = Float.parseFloat(pulse);  // pulse value obtained from the CSV file is stored in the float variable
                                    seriesPulse.appendData(new DataPoint(lastX_pulse++, pulse_float), true, 1000);
                                }  catch (Exception e) {}
                            }
                        }

                    } else if(countHeader != 6) { // if the number of columns are not equal to 6, then it is probably the wrong CSV file
                        ToastFunction();  // in that case show this notification to the user
                        break;
                    }
                }
                reader.close();  // after the whole single-sensor CSV file is parsed, close the CSV reader.
            } catch (Exception e) {
                Toast.makeText(MakeGraph_multi.this, "Choose the right CSV file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // this is called when the loaded CSV is not the right one!
    private void ToastFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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