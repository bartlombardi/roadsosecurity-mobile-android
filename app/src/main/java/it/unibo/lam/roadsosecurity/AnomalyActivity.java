package it.unibo.lam.roadsosecurity;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.google.android.gms.internal.zzt.TAG;

public class AnomalyActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<Anomaly> anomalyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anomaly);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        anomalyList = new ArrayList<>();
        this.getAnomalyList();

        //new GetAnomalies().execute();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        List<LatLng>a = new LinkedList<>();

        LatLng sydney = new LatLng(-34, 151);

        for (Anomaly anomaly: anomalyList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(anomaly.getLatitude(),anomaly.getLongitude())).title(" Probabilità del" + anomaly.getTrust()));
        }
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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



    class GetAnomalies extends AsyncTask<Void, Void, Void>{

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

