package com.nili.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.main.R;
import com.nili.globals.Commands;
import com.nili.globals.Globals;

import com.nili.operator.Operator;
import com.nili.utilities.ConnectionManager;
import com.nili.utilities.BtReadData;
import com.nili.utilities.Strumming;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity 
{
	private int uiMode;
	private boolean isPlay;

	// bt
    public ConnectionManager connectionManager;
    public BtReadData btReadData;
    
	public Strumming strumming;
	public Operator operator;
	private ListView songsListView;
	public HashMap<String, String> songsMap = new HashMap<String, String>();

	// javascript
	public  WebView			webView;
	public WebAppInterface webInterface;

	private ImageView changeModeButton;
	private ImageView forwardButton;
	private ImageView backwardButton;
	private ImageView playPauseButton;
	private ImageView reconnectButton;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// prevent locking of phone
		PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
		wakeLock.acquire();

		setContentView(R.layout.activity_main);


		Thread.currentThread().setName("Main Activity Thread");
		
		connectionManager = new ConnectionManager();
		btReadData = new BtReadData();
		operator = new Operator();
		webInterface = new WebAppInterface();
		strumming = new Strumming();
		
		
		
        songsListView = (ListView) findViewById(R.id.songsList);
        setSongsList(); 
		setWebView();
		
		changeModeButton = (ImageView) findViewById(R.id.IsAuto);
		forwardButton = (ImageView) findViewById(R.id.Forward);
		backwardButton = (ImageView) findViewById(R.id.Backward);
		playPauseButton = (ImageView) findViewById(R.id.playPause);
		reconnectButton = (ImageView) findViewById(R.id.reconnect);
		
		
		webInterface.set(this, operator);
    	webView.addJavascriptInterface(webInterface, "Android");

		connectionManager.set(this, "98:D3:31:B1:F7:92");
		connectionManager.start();

		reconnectButton.setBackgroundResource(R.drawable.reconnecting);
		synchronized(connectionManager)
		{
			try {
				connectionManager.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		reconnectButton.setBackgroundResource(R.drawable.reconnect);
		if(!Globals.isConnectedToBT)
		{
			showToast("failed connecting to blue tooth");
			reconnectButton.setVisibility(View.VISIBLE);
		}
		else
		{
			showToast("connected to blue tooth");
			reconnectButton.setVisibility(View.GONE);
		}

		waitForBtConnect();

        btReadData.set(this, connectionManager.inputStream, operator); // this thread reads the incomming data from bluetooth
		operator.set(this.connectionManager, this.webInterface, this.strumming, this);

        webInterface.start();
        btReadData.start();
		operator.start();

		// needs to wait for page to load
		uiMode = Globals.UImode.AUTO;
	}

	private void waitForBtConnect()
	{
	}

	public synchronized void setMode(int mode)
	{
		uiMode = mode;

		if(mode==Globals.UImode.AUTO)
		{
			changeModeButton.setBackgroundResource(R.drawable.auto);
			playPauseButton.setVisibility(View.GONE);
		}
		else if(mode==Globals.UImode.MANUAL)
		{
			changeModeButton.setBackgroundResource(R.drawable.manual);
			playPauseButton.setVisibility(View.GONE);
		}
		else if(mode==Globals.UImode.TIMED)
		{
			changeModeButton.setBackgroundResource(R.drawable.timed);
			playPauseButton.setVisibility(View.VISIBLE);
			isPlay = false;
		}
	}
    
	public int getMode()
	{
		return uiMode;
	}
	
    private void setSongsList() 
	{
		this.songsListView.setVisibility(View.GONE);

		String [] assetFiles = null;
    	InputStreamReader	songFileStream;
    	BufferedReader		songFileReader;
    	// create map
		try 
        {
        	// get assets files
        	assetFiles = getAssets().list("");
        	// find title in html file
            for(int i=0; i<assetFiles.length; i++)
            {
            	// for each html file find title, add name of file and title to map
            	if(assetFiles[i].lastIndexOf('.')!=-1 && assetFiles[i].substring(assetFiles[i].lastIndexOf('.')).equalsIgnoreCase(".html"))
            	{
            		songFileStream = new InputStreamReader((getAssets().open(assetFiles[i])));
            		songFileReader = new BufferedReader(songFileStream);
            		for(String line = songFileReader.readLine(); line!=null; line = songFileReader.readLine())
            		{
            			if(line.contains("title"))
            			{
            				String title = line.substring(
            						line.indexOf(">")+1, line.indexOf("</")
            						);
            				songsMap.put(title, assetFiles[i]);
            				break;
            			}
            		}
            	}
            }
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create list view
		ArrayList<String> songsList = new ArrayList<String>();
        //String[] songsList = new String[songsMap.keySet().toArray().length];
        for(int i=0; i<songsMap.keySet().toArray().length; i++)
    	{
        	songsList.add((String) songsMap.keySet().toArray()[i]);
    	}
        Collections.sort(songsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, songsList)
		{
            @Override
            public View getView(int position, View convertView,
                    ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.WHITE);

                return view;
            }
        };
        // Assign adapter to ListView
        songsListView.setAdapter(adapter); 
        // ListView Item Click Listener
        songsListView.setOnItemClickListener(new OnItemClickListener() 
        {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	               // ListView Clicked item index
	               int itemPosition = position;
	               // ListView Clicked item value
	               String  itemValue    = (String) songsListView.getItemAtPosition(position);
	               MainActivity.this.loadWebView(MainActivity.this.songsMap.get(itemValue));
	               songsListView.setVisibility(View.GONE);
			}
         });
	}

	protected void loadWebView(String url) 
	{
		this.webView.loadUrl("file:///android_asset/"+url);
	}

	private void setWebView()
	{
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

		webView = (WebView)findViewById(R.id.activity_main_webview);
		webView.setClickable(true);
    	// Enable java script
    	WebSettings webSettings = webView.getSettings();
    	webSettings.setJavaScriptEnabled(true);
    	// all pages to load from web view
    	webView.setWebViewClient(new WebViewClient());
    	webView.getSettings().setRenderPriority(RenderPriority.HIGH);
    	webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    	
    	//loadWebView(this.songsMap.get("solo"));
    	loadWebView(this.songsMap.get("Leaving On A Jet Plane - John Denver"));

    	webView.setWebViewClient(new WebViewClient() {
          @Override
          public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressDialog.hide();
          }
        });
	}
	
	public void onButtonRestart(View v) {
		Message message = new Message();
		message.arg1 = Commands.Operator.restart;
		this.operator.mHandler.sendMessage(message);

		message = new Message();
		message.arg1 = Commands.WebApp.restart;
		this.webInterface.mHandler.sendMessage(message);
	}

	public void onButtonShowSongsList(View v) {
		this.songsListView.setVisibility(View.VISIBLE);
	}

    public void onButtonToggleMode(View v)
    {
		int mode = uiMode;
    	if(mode == 2)
			mode = 0;
		else
			mode++;

		setMode(mode);
	}

    public void onButtonForward(View v)
    {
		Message message = new Message();
		message.arg1 = Commands.Operator.eventForward;
		this.operator.mHandler.sendMessage(message);
    }
    
    public void onButtonBackward(View v)
    {
		Message message = new Message();
		message.arg1 = Commands.Operator.eventBackward;
		this.operator.mHandler.sendMessage(message);
    }

	public void onButtonPlayPause(View v)
	{
		if(isPlay)
		{
			isPlay = false;
			playPauseButton.setBackgroundResource(R.drawable.pause);
		}
		else
		{
			isPlay = true;
			playPauseButton.setBackgroundResource(R.drawable.play);
		}
	}

	public void onButtonConnect(View v)
	{
		waitForBtConnect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void showToast(String toast)
	{
		Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
	}
}

