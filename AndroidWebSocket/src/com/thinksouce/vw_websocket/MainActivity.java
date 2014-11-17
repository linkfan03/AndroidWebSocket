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
    Button studentButton;
    Button bicycleButton;
    Button roadWorkerButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        driverButton = (Button)findViewById(R.id.driverButton);
        studentButton = (Button)findViewById(R.id.studentButton);
        bicycleButton = (Button)findViewById(R.id.bicycleButton);
        roadWorkerButton = (Button)findViewById(R.id.roadWorkerButton);
        final Intent driverIntent = new Intent(this, DriverActivity.class);
        final Intent signalIntent = new Intent(this, SignalActivity.class);
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(driverIntent);
            }
        });
        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signalIntent.putExtra("signalType", SignalType.Student.toInt());
                startActivity(signalIntent);
            }
        });
        bicycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signalIntent.putExtra("signalType", SignalType.BicycleRider.toInt());
                startActivity(signalIntent);
            }
        });
        roadWorkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signalIntent.putExtra("signalType", SignalType.RoadWorker.toInt());
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
