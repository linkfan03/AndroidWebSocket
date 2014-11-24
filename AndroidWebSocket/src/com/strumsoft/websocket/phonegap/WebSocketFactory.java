/*
 * Copyright (c) 2010 Animesh Kumar  (https://github.com/anismiles)
 * Copyright (c) 2010 Strumsoft  (https://strumsoft.com)
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *  
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *  
 */
package com.strumsoft.websocket.phonegap;

import java.net.URI;
import java.util.Random;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * The <tt>WebSocketFactory</tt> is like a helper class to instantiate new
 * WebSocket instaces especially from Javascript side. It expects a valid
 * "ws://" URI.
 * 
 * @author Animesh Kumar
 */
public class WebSocketFactory {

	/** The app view. */
	WebView appView;
    Context mContext;
    boolean first = true;
    private String toast;

    /**
	 * Instantiates a new web socket factory.
	 * 
	 * @param appView
	 *            the app view
	 */
	public WebSocketFactory(WebView appView, Context c) {
		this.appView = appView;
        mContext = c;
	}

	public WebSocket getInstance(String url) {
		// use Draft75 by default
		return getInstance(url, WebSocket.Draft.DRAFT75);
	}

	public WebSocket getInstance(String url, WebSocket.Draft draft) {
		WebSocket socket = null;
		Thread th = null;
		try {
			socket = new WebSocket(appView, new URI(url), draft, getRandonUniqueId());
			th = socket.connect();
			return socket;
		} catch (Exception e) {
			//Log.v("websocket", e.toString());
			if(th != null) {
				th.interrupt();
			}
		} 
		return null;
	}

	/**
	 * Generates random unique ids for WebSocket instances
	 * 
	 * @return String
	 */
	private String getRandonUniqueId() {
		return "WEBSOCKET." + new Random().nextInt(100);
	}

    /** Show a toast from the web page */
    @JavascriptInterface//You have to have this for the javascript to be able to call the method
    public void showToast(String toast) {
        this.toast = toast;
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        if(first){
            first = false;
//            appView.loadUrl("javascript:doSend('1')");//How to call javascript code from Android
//            appView.loadUrl("javascript:doSend('2')");
//            appView.loadUrl("javascript:doSend('3')");

        }
    }

}