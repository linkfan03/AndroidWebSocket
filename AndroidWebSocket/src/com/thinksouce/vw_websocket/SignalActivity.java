package com.thinksouce.vw_websocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.strumsoft.websocket.phonegap.WebSocketFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Josh on 11/4/2014.
 */
public class SignalActivity extends Activity implements LocationListener {
    private TextView latitudeField;
    private TextView longitudeField;
    private TextView directionField;
    private LocationManager locationManager;
    private String provider;
    private WebView wv;
    private Location lastBroadcastedLocation;
    private SignalType signalType;
    private UUID deviceID = UUID.randomUUID();
    private long lastBroadcastedTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        signalType = SignalType.values()[intent.getIntExtra("signalType", 0)];


        setContentView(R.layout.activity_signal);
        wv = (WebView) findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            }
        });
        wv.setWebViewClient(
                new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
        wv.loadUrl("file:///android_asset/www/chat.html");
        wv.addJavascriptInterface(new WebSocketFactory(wv, this), "WebSocketFactory");//The string name is the string used for the javascript that is going to call Android native code
        wv.setVisibility(View.GONE);
        latitudeField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        directionField = (TextView) findViewById(R.id.TextView06);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
            // get rid of bottom line to fix
            //Toast.makeText(context, "Current Speed:" + location.getSpeed(), Toast.LENGTH_SHORT).show();
        } else {
            latitudeField.setText("Location not available");
            longitudeField.setText("Location not available");
        }
        wv.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendLocation(createNewLocation(lastBroadcastedLocation));
            }
        }, 10000);
    }

    // Request updates at startup
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }


    public Location createNewLocation (Location location){
        //Create new location with the current time using the last broadcasted location
        //This will be used to determine if it is time to broadcast the location again
        Location newLocation = new Location(location);
        newLocation.setTime(System.currentTimeMillis());
        return newLocation;
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        sendLocation(location);
    }

    public void sendLocation(Location location){
        if(location != null && (lastBroadcastedLocation == null || location.getTime() - lastBroadcastedLocation.getTime() >= 60000 || location.distanceTo(lastBroadcastedLocation) >= 10.00)) {
            //only broadcast new location if no location has been sent before
            //or it has been 1 minute since last broadcast
            //or the signal phone has moved 10 meters since last broadcast
            double lat = (double) (location.getLatitude());
            double lng = (double) (location.getLongitude());
            double brng = (double) (location.getBearing());
            latitudeField.setText(String.valueOf(lat));
            longitudeField.setText(String.valueOf(lng));
            directionField.setText(String.valueOf(brng));
            double spd = (double) (location.getSpeed());
            braking(spd);
            Toast.makeText(this, "Current speed:" + location.getSpeed(), Toast.LENGTH_SHORT).show();
            HashMap<String, String> locationMap = new HashMap<String, String>();
            locationMap.put("latitude", String.valueOf(location.getLatitude()));
            locationMap.put("longitude", String.valueOf(location.getLongitude()));
            locationMap.put("current_speed", String.valueOf(location.getSpeed()));
            locationMap.put("bearing", String.valueOf(location.getBearing()));
            locationMap.put("signalType", String.valueOf(signalType.toInt()));
            locationMap.put("deviceID", String.valueOf(deviceID));
            locationMap.put("dateTime", String.valueOf(System.currentTimeMillis()));
            JSONObject locationJsonObject = new JSONObject(locationMap);
            String locationJson = locationJsonObject.toString();
            wv.loadUrl("javascript:doSend('" + locationJson + "')");
            lastBroadcastedLocation = location;
        }
    }

    //determine the distance needed to go to a complete stop based on the speed of the driver
    public void braking (double speed){
        double brakingDistance = (1.47*speed*2.5) + ((1.075 * Math.pow(speed,2))/11.2);
        Toast.makeText(this, "Braking distance " + brakingDistance +" feet",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}