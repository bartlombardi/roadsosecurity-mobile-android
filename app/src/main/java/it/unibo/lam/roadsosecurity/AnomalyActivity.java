package it.unibo.lam.roadsosecurity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzt.TAG;

public class AnomalyActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private List<Anomaly> anomalyList;
    private Utility utility;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private SupportMapFragment mapFragment;
    private Marker mCurrLocation;
    private Circle mCircle;
    private FloatingActionButton fab;

    private SensorManager sensorManager;
    private double mAccel, mAccelCurrent, mAccelLast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anomaly);

        anomalyList = new ArrayList<>();
        utility = new Utility();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        fab = (FloatingActionButton) findViewById(R.id.imageButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latLng != null)
                {
                    anomalyList.add(new Anomaly(latLng.latitude,latLng.longitude));
                    Snackbar.make(view, "Aggiunta una nuova anomalia.", Snackbar.LENGTH_LONG)
                            .setAction("Annulla", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    anomalyList.remove(anomalyList.size() - 1);
                                    refreshMap();
                                    drawMarkerWithCircle(latLng);
                                }
                            }).show();

                    refreshMap();
                    drawMarkerWithCircle(latLng);
                }
                else
                {
                    Snackbar.make(view, "Impossibile aggiungere una nuova anomalia," +
                            " controllare che il GPS sia attivo.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        this.getAnomalyList();

        //new GetAnomalies().execute();
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
        addAnomaliesOnMap();
    }

    public void addAnomaliesOnMap() {
        for (Anomaly anomaly: anomalyList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(anomaly.getLatitude(), anomaly.getLongitude())).title("Anomalia al " + anomaly.getTrust() + "%").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        }
    }

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

    public void getAnomalyList(){

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
                .addOnConnectionFailedListener(this)
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
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            drawMarkerWithCircle(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));

            //MarkerOptions markerOptions = new MarkerOptions();
            //markerOptions.position(latLng);
            //markerOptions.title("Current Position");
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            //mCurrLocation = mMap.addMarker(markerOptions);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        drawMarkerWithCircle(latLng);

        for (Anomaly anomaly : anomalyList) {

            float[] distance = new float[2];

            Location.distanceBetween(anomaly.getLatitude(), anomaly.getLongitude(),
                    mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);

            if (distance[0] > mCircle.getRadius()) {
                if(anomaly.getNotified()) anomaly.setNotified(false);
                //Toast.makeText(getBaseContext(), "Outside, distance from center: " + distance[0] + " radius: " + mCircle.getRadius(), Toast.LENGTH_LONG).show();
            } else {
                if(!anomaly.getNotified()) {
                    utility.playSound(getApplicationContext(),2);
                    Toast.makeText(getBaseContext(), "Attenzione, anomalia nelle vicinanze. (" + distance[0] + ")", Toast.LENGTH_LONG).show();
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

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(ax * ax + ay * ay + az * az);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            //Log.d("Test"," "+ax+" "+ay+" "+az);
            int temp = utility.compare((int) ax, (int) ay, (int) az);

            if (temp == 0) {
                //orientation x
                //Log.d("test","X orientation");
                Log.d("test","orientation x"+ (mAccelLast-mAccelCurrent));
                if ((mAccelLast - mAccelCurrent) > 5) {
                    Toast.makeText(this, "pothole x", Toast.LENGTH_SHORT).show();
                    Log.d("DARSHANROHAN", "pothole x");
                    if (latLng != null) {
                        utility.playSound(getBaseContext(),1);
                        anomalyList.add(new Anomaly(latLng.latitude,latLng.longitude));
                        refreshMap();
                        drawMarkerWithCircle(latLng);
                    }
                    else {

                    }
                }
            } else if (temp == 1) {
                //orientation y
                //Log.d("test","y orientation");
                //Log.d("test",""+(mAccelLast-mAccelCurrent));
                if ((mAccelLast - mAccelCurrent) > 5) {
                    Toast.makeText(this, "pothole y", Toast.LENGTH_SHORT).show();
                    Log.d("DARSHANROHAN", "pothole y");
                    if (latLng != null) {
                        utility.playSound(getBaseContext(),1);
                        anomalyList.add(new Anomaly(latLng.latitude,latLng.longitude));
                        refreshMap();
                        drawMarkerWithCircle(latLng);

                    }
                    else {

                    }
                }
            } else if (temp == 2) {
                //orientation z
                //Log.d("test","z orientation");
                //Log.d("test","cur:"+mAccelCurrent+"      last:"+mAccelLast);
                if ((mAccelLast - mAccelCurrent) > 5) {
                    Toast.makeText(this, "pothole z", Toast.LENGTH_SHORT).show();
                    Log.d("test",""+(mAccelLast-mAccelCurrent));
                    Log.d("DARSHANROHAN", "pothole z");
                    if (latLng != null) {
                        utility.playSound(getBaseContext(),1);
                        anomalyList.add(new Anomaly(latLng.latitude,latLng.longitude));
                        refreshMap();
                        drawMarkerWithCircle(latLng);
                    }
                    else {

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
            super.onBackPressed();
            System.exit(0);
            return;
    }

    class GetAnomalies extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://api.androidhive.info/contacts/";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray anomalies = jsonObj.getJSONArray("");

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

            return null;
        }
/*
    protected void onPostExecute(JSONObject result) {

        List<Anomaly> anomalyList = new LinkedList<Anomaly>();

        //parse JSON data
        try {
            JSONArray jArray = new JSONArray(result);
            for(int i=0; i < jArray.length(); i++) {

                JSONObject jObject = jArray.getJSONObject(i);
                anomalyList.add(new Anomaly(jObject.getDouble("name"),jObject.getDouble("name"),jObject.getDouble("name")));

            } // End Loop
            //this.progressDialog.dismiss();
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } // catch (JSONException e)
    } // protected void onPostExecute(Void v) */
    }
}