package com.thinksouce.vw_websocket;

import com.strumsoft.websocket.phonegap.WebSocketFactory;
import com.strumsoft.websocket.phonegap.WebSocket;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainActivity extends Activity {
    Button driverButton;
    Button signalButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        driverButton = (Button)findViewById(R.id.driverButton);
        signalButton = (Button)findViewById(R.id.signalButton);
        final Intent driverIntent = new Intent(this, DriverActivity.class);
        final Intent signalIntent = new Intent(this, SignalActivity.class);
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(driverIntent);
            }
        });
        signalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(signalIntent);
            }
        });
		
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
