package com.thinksouce.vw_websocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.strumsoft.websocket.phonegap.WebSocketFactory;

import java.util.Locale;

/**
 * Created by Josh on 11/4/2014.
 */
public class DriverActivity extends Activity {
    private TextToSpeech tts;
    private static int TTS_DATA_CHECK = 1;
    private boolean isTTSInitialized = false;

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
        wv.addJavascriptInterface(new WebSocketFactory(wv, this), "WebSocketFactory");//The string name is the string used for the javascript that is going to call Android native code
        //wv.setVisibility(View.GONE);
        speakUserLocale();
        confirmTTSData();
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

    private void speakUserLocale() {
        if(isTTSInitialized) {
            //Determine User's Locale
            Locale locale = this.getResources().getConfiguration().locale;

            if (tts.isLanguageAvailable(locale) >= 0)
                tts.setLanguage(locale);

            tts.setPitch(1.0f);
            tts.setSpeechRate(0.9f);

            tts.speak("ALERT!", TextToSpeech.QUEUE_ADD, null);
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