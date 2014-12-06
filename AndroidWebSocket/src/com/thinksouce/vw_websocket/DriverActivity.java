package com.thinksouce.vw_websocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import com.strumsoft.websocket.phonegap.WebSocketFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Josh on 11/4/2014.
 */
public class DriverActivity extends Activity implements LocationListener {
    private TextToSpeech tts;
    private static int TTS_DATA_CHECK = 1;
    private boolean isTTSInitialized = false;
    private String provider;
    private Location driverLocation;
    private boolean done = false;
    private LocationManager locationManager;
    private WebSocketFactory webSocketFactory;
    private List<Long> lastWarnedTime = new ArrayList<Long>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        WebView wv = (WebView) findViewById(R.id.webview);
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
        webSocketFactory = new WebSocketFactory(wv, this);
        wv.addJavascriptInterface(webSocketFactory, "WebSocketFactory");//The string name is the string used for the javascript that is going to call Android native code
        //wv.setVisibility(View.GONE);

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
            driverLocation = location;
            Location bicycleRider = createNewLocationDistanceAway(10, driverLocation);
            Location roadWorker = createNewLocationDistanceAway(50, driverLocation);
            Location student = createNewLocationDistanceAway(30, driverLocation);
            // get rid of bottom line to fix
            //Toast.makeText(context, "Current Speed:" + location.getSpeed(), Toast.LENGTH_SHORT).show();
        }
        confirmTTSData();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CheckNewHazards();
                    }
                }, 0, 2000);
            }
        });
        thread.start();
    }

    // Request updates at startup
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);

    }

    public Location createNewLocationDistanceAway(float distance, Location originalLocation){
        Location newLocation = new Location(originalLocation);
        while(newLocation.distanceTo(originalLocation) < distance){
            newLocation.setLatitude(newLocation.getLatitude() + (1/110500));
        }
        return newLocation;
    }

    public void CheckNewHazards(){
        List<HazardSignal> hazardSignalList = webSocketFactory.getHazardSignals();
        for(int i = 0; i < hazardSignalList.size(); i++){
            CheckAlert(hazardSignalList.get(i));
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void CheckAlert(HazardSignal hazardSignal){
        Location hazardLocation = createLocationFromHazard(hazardSignal);
        if(driverLocation.distanceTo(hazardLocation) < 1400){
            //driver going 75 miles per hour bicyclist going 30 in opposite direction so effectively 100mph * 20 seconds
            //Gives us 1400 meters
            if(isOnCollisionCourse(driverLocation.getBearing(), hazardSignal.bearing)){
                float distanceCoveredIn20Seconds = (driverLocation.getSpeed() + hazardSignal.current_speed) * 20;
                float distanceToWarn = distanceCoveredIn20Seconds + getBrakingDistance(driverLocation.getSpeed());
                if(distanceToWarn < 10){
                    distanceToWarn = 10;
                }
                if(driverLocation.distanceTo(hazardLocation) < distanceToWarn && System.currentTimeMillis() - hazardSignal.lastWarnedTime > 4000){
                    //within distance to warn and hasn't been warned in the past 4 seconds
                    speakUserLocale(hazardSignal.signalType);
                    hazardSignal.lastWarnedTime = System.currentTimeMillis();
                }
            }
        }
    }

    //determine the distance needed to go to a complete stop based on the speed of the driver
    public float getBrakingDistance (float speed){
        float brakingDistance = (float)(0.278*speed*2.5) + (float)((0.039 * Math.pow(speed,2))/3.4);
        return brakingDistance;
    }

    public boolean isOnCollisionCourse(float driverBearing, float hazardBearing){
        float differenceInBearing = driverBearing - hazardBearing;
        differenceInBearing = Math.abs(differenceInBearing);
        if(differenceInBearing < 90 || differenceInBearing > 315){
            return true;
        }
        return false;
    }

    public boolean isOppositeDirection(Location driverLocation, HazardSignal hazardSignal){
        float driverBearing = driverLocation.getBearing();
        float hazardBearing = hazardSignal.bearing;
        float differenceInBearing = driverBearing - hazardBearing;
        if(differenceInBearing < 0){
            differenceInBearing = differenceInBearing * -1;
        }
        if(differenceInBearing < 210 && differenceInBearing > 150){
            return true;
        }
        return false;
    }

    public Location createLocationFromHazard(HazardSignal hazardSignal){
        Location location = new Location(driverLocation);
        location.setLatitude(hazardSignal.latitude);
        location.setLongitude(hazardSignal.longitude);
        location.setTime(hazardSignal.dateTime);
        location.setBearing(hazardSignal.bearing);
        location.setSpeed(hazardSignal.current_speed);
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        //driverLocation = location;
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

    //start voice alert notification
    private void confirmTTSData()  {
        Intent intent = new Intent(Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_DATA_CHECK);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == Engine.CHECK_VOICE_DATA_PASS) {
                //Voice data exists
                initializeTTS();
            }
            else {
                Intent installIntent = new Intent(Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, new OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    isTTSInitialized = true;
                }
                else {
                    //Handle initialization error here
                    isTTSInitialized = false;
                }
            }
        });
    }

    private void speakUserLocale(SignalType signalType) {
        if(isTTSInitialized) {
            //Determine User's Locale
            Locale locale = this.getResources().getConfiguration().locale;

            if (tts.isLanguageAvailable(locale) >= 0)
                tts.setLanguage(locale);

            tts.setPitch(1.0f);
            tts.setSpeechRate(0.9f);

            tts.speak("ALERT THERE IS A " + signalType.toString() + " NEARBY!", TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}