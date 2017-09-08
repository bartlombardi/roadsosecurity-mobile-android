package it.unibo.lam.roadsosecurity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AnomalyActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,LocationListener, OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private SupportMapFragment mapFragment;
    private Marker mCurrLocation;

    private Circle mCircle;

    private ProgressDialog pDialog;
    private FloatingActionButton fab;

    private List<Anomaly> anomalyList;
    private List<Anomaly> anomalyDetectedList;
    private Utility utility;

    private SensorManager sensorManager;
    private double mAccel, mAccelCurrent, mAccelLast;

    private TextView footerSpeed;

    private TextView footerDistance;
    private Location lastKnownLocation;
    private double totalDistance;

    private TextView footerAnomaly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anomaly);

        lastKnownLocation = null;
        totalDistance = 0;

        anomalyList = new ArrayList<>();
        anomalyDetectedList = new ArrayList<>();
        utility = new Utility();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        footerSpeed = (TextView) findViewById(R.id.speed);
        footerDistance = (TextView) findViewById(R.id.distance);
        footerAnomaly = (TextView) findViewById(R.id.anomaly);

        fab = (FloatingActionButton) findViewById(R.id.imageButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latLng != null)
                {
                    anomalyDetectedList.add(new Anomaly(latLng.latitude,latLng.longitude));
                    Snackbar.make(view, "New anomaly added.", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    anomalyDetectedList.remove(anomalyDetectedList.size() - 1);
                                    refreshMap();
                                    drawMarkerWithCircle(latLng);
                                }
                            }).show();

                    refreshMap();
                    drawMarkerWithCircle(latLng);
                }
                else
                {
                    Snackbar.make(view, "Failed to add a new anomaly," +
                            " check GPS is active.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        //DEBUG JSON FROM FILE
        //this.getAnomalyList();

        new AsyncTaskParseJson().execute();
    }

    public void refreshMap() {

        mMap.clear();
        addAnomaliesOnMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        //DEBUG JSON FROM FILE
        //addAnomaliesOnMap();
    }

    public void addAnomaliesOnMap() {
        for (Anomaly anomaly: anomalyList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(anomaly.getLatitude(), anomaly.getLongitude())).title(new DecimalFormat("##.##").format(anomaly.getTrust()) + "% chance of an anomaly here").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        }

        for (Anomaly anomaly: anomalyDetectedList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(anomaly.getLatitude(), anomaly.getLongitude())).title(new DecimalFormat("##.##").format(anomaly.getTrust()) + "% chance of an anomaly here").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        }
    }

    /*
    DEBUG - JSON FILE
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("file.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void getAnomalyList() {

        String jsonStr = loadJSONFromAsset();

        if (jsonStr != null) {
            try {
                // Getting JSON Array node
                JSONArray anomalies = new JSONArray(jsonStr);

                // looping through All anomalies
                for (int i = 0; i < anomalies.length(); i++) {
                    JSONObject c = anomalies.getJSONObject(i);
                    double latitude = c.getDouble("Latitude");
                    double longitude = c.getDouble("Longitude");
                    double trust = c.getDouble("Trust");

                    // adding contact to contact list
                    anomalyList.add(new Anomaly(latitude,longitude,trust));
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }

        } else {
            Log.e(TAG, "Couldn't get json from server.");
        }
    }
    */

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void drawMarkerWithCircle(LatLng position) {

        if(mCircle != null)  mCircle.remove();

        double radiusInMeters = 50.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8).visible(false);
        mCircle = mMap.addCircle(circleOptions);

        //MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.prova));
        //mCurrLocation = mMap.addMarker(markerOptions);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            drawMarkerWithCircle(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0.1F);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(lastKnownLocation == null)
        {
            lastKnownLocation = location;
        }else {

            totalDistance += utility.distance(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),location.getLatitude(),location.getLongitude());

            //Location.distanceBetween(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),location.getLatitude(),location.getLongitude(),distance);
            //distance = lastKnownLocation.distanceTo(location);
            footerDistance.setText(new DecimalFormat("####.##").format(totalDistance).toString() + " Km" );
        }

        footerSpeed.setText(Integer.toString((int) ((location.getSpeed() * 3600) / 1000)) + " Km/h");

        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        drawMarkerWithCircle(latLng);

        for (Anomaly anomaly : anomalyList) {

            float[] distance = new float[2];

            Location.distanceBetween(anomaly.getLatitude(), anomaly.getLongitude(),
                    mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);

            if (distance[0] > mCircle.getRadius()) {
                if(anomaly.getNotified()) anomaly.setNotified(false);
            } else {
                if(!anomaly.getNotified()) {
                    utility.playSound(getApplicationContext(),2);
                    Toast.makeText(getBaseContext(), "Stay attention, anomaly nearby. (" + distance[0] + ")", Toast.LENGTH_LONG).show();
                    anomaly.setNotified(true);
                }
            }
        }

        if(mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        }

        if (mCurrLocation != null) {
            mCurrLocation.remove();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double ax, ay, az;

        footerAnomaly.setText(Integer.toString(anomalyDetectedList.size()));

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(ax * ax + ay * ay + az * az);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            int temp = utility.compare((int) ax, (int) ay, (int) az);

            //orientation y
            if (temp == 1) {
                Log.d("test","y orientation");
                if ((mAccelLast - mAccelCurrent) > 5) {
                    if (latLng != null) {
                        utility.playSound(getBaseContext(),1);
                        anomalyDetectedList.add(new Anomaly(latLng.latitude,latLng.longitude));
                        refreshMap();
                        drawMarkerWithCircle(latLng);
                    }
                }
            } else if (temp == 2) {
                //orientation z
                Log.d("test","z orientation");
                if ((mAccelLast - mAccelCurrent) > 5) {
                    if (latLng != null) {

                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("Accident detected")
                                .setMessage("Do you really want to send emergency alert message?")
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(android.R.string.yes, null)
                                .create();

                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            private static final int AUTO_DISMISS_MILLIS = 10000;
                            @Override
                            public void onShow(final DialogInterface dialog) {
                                final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                final CharSequence positiveButtonText = defaultButton.getText();
                                new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        defaultButton.setText(String.format(
                                                Locale.getDefault(), "%s (%d)",
                                                positiveButtonText,
                                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                                        ));
                                    }
                                    @Override
                                    public void onFinish() {
                                        if (((AlertDialog) dialog).isShowing()) {
                                            utility.sendSMS(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("number", ""),
                                                    "Accident occurred. You are requested to help. Accident location lat: " + latLng.latitude + " N lon: " + latLng.longitude + " .");
                                            dialog.dismiss();
                                        }
                                    }
                                }.start();
                            }
                        });
                        dialog.show();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public void onBackPressed() {

            new AsyncTaskSendJson().execute();
    }

    private class AsyncTaskParseJson extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AnomalyActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();

            String url = "http://bartlombardi-001-site1.dtempurl.com/Api/Anomalies";
            String jsonStr = sh.makeServiceCall(url);

            Log.e("ASYNC", "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONArray anomalies = new JSONArray(jsonStr);

                    // looping through All anomalies
                    for (int i = 0; i < anomalies.length(); i++) {
                        JSONObject c = anomalies.getJSONObject(i);
                        double latitude = c.getDouble("Latitude");
                        double longitude = c.getDouble("Longitude");
                        double trust = c.getDouble("Trust");

                        anomalyList.add(new Anomaly(latitude,longitude,trust));
                    }
                } catch (final JSONException e) {
                    Log.e("ASYNC", "Json parsing error: " + e.getMessage());
                }

            } else {
                Log.e("ASYNC", "Couldn't get json from server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            addAnomaliesOnMap();

            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    private class AsyncTaskSendJson extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(AnomalyActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();

            for(Anomaly anomaly : anomalyDetectedList) {

                String url = "http://bartlombardi-001-site1.dtempurl.com/Api/Anomalies";
                url += "?latitude="+anomaly.getLatitude()+"&longitude="+anomaly.getLongitude();
                String jsonStr = sh.makeServiceCall(url);
                Log.e("ASYNC", "Response from url: " + jsonStr);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();


            System.exit(0);
        }
    }

}