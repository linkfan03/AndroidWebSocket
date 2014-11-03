package com.thinksouce.vw_websocket;

import com.strumsoft.websocket.phonegap.WebSocketFactory;
import com.strumsoft.websocket.phonegap.WebSocket;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        WebView wv = (WebView)findViewById(R.id.webview);				
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient() {
			   public void onProgressChanged(WebView view, int progress) {				   
			   }
			 });
		wv.setWebViewClient(
				new WebViewClient(){
					public boolean shouldOverrideUrlLoading(WebView view, String url){
						view.loadUrl(url);
						return false;
					}
		});
        wv.loadUrl("file:///android_asset/www/chat.html");//Local html file needs to be used so that we can do native -> javascript and javascript -> native
        //wv.loadUrl("http://192.168.1.17/MSWSChat/default.html");
        wv.addJavascriptInterface(new WebSocketFactory(wv, this), "WebSocketFactory");//The string name is the string used for the javascript that is going to call Android native code
        //wv.setVisibility(View.GONE);//We will use this to hide the WebView and show the app that we actually want.

//		wv.loadUrl("file:///android_asset/www/index.html");
//		wv.addJavascriptInterface(new WebSocketFactory(wv), "WebSocketFactory");
		
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
