package com.example.nili_complete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity 
{
	// bt
    public	ConnectionManager	connectionManager;
    public	ReadData			readData;
    
	public	TextView			dataReceived;
	public 	TextView			dataSent;
	
	public	Operator	operator;
	private ListView songsListView;
	public HashMap<String, String> songsMap = new HashMap<String, String>();

	// javascript
	public  WebView			webView;
	public	WebAppInterface webInterface;
	public	String jsCommand;
	
	public Object waitConnection = new ReentrantLock();
	
	private boolean isAutoMode;
	private ImageView changeModeButton;
	private ImageView forwardButton;
	private ImageView backwardButton;
	
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
		readData = new ReadData();
		operator = new Operator();
		webInterface = new WebAppInterface();
		
		
		
        songsListView = (ListView) findViewById(R.id.songsList);
        setSongsList(); 
		setWebView();
		
		changeModeButton = (ImageView) findViewById(R.id.IsAuto);
		forwardButton = (ImageView) findViewById(R.id.Forward);
		backwardButton = (ImageView) findViewById(R.id.Backward);
		
		
		webInterface.set(this, operator);
    	webView.addJavascriptInterface(webInterface, "Android");

		connectionManager.set(this, "98:D3:31:B1:F7:92");
		connectionManager.start();

		synchronized(connectionManager)
		{
			try {
				connectionManager.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(!Globals.isConnectedToBT)
		{
			showToast("failed connecting to blue tooth");
			//return;
		}
		else
		{
			showToast("connected to blue tooth");
		}
		
        // connect java script to android

        readData.set(this, connectionManager.inputStream, operator); // this thread reads the incomming data from bluetooth
		operator.set(this.connectionManager, this.webInterface, this);

        webInterface.start();
        readData.start();
		operator.start();

		//setUiAutoMode(false);
		//setMode(false);
	}

    private void setUiAutoMode(boolean isAuto)
    {
		this.forwardButton.setVisibility(View.GONE);
		this.backwardButton.setVisibility(View.GONE);

		
		if(isAuto)
		{
			changeModeButton.setBackgroundResource(R.drawable.auto);
			this.forwardButton.setVisibility(View.GONE);
			this.backwardButton.setVisibility(View.GONE);
		}
		else
		{
			changeModeButton.setBackgroundResource(R.drawable.manual);
			this.forwardButton.setVisibility(View.VISIBLE);
			this.backwardButton.setVisibility(View.VISIBLE);
		}
    }
    
	private void setMode(boolean isAuto)
	{
		isAutoMode = isAuto;
		
		Message message = new Message();
		message.arg1 = Commands.WebApp.eventUiChangeMode;
		message.obj = Boolean.toString(isAutoMode);
		this.webInterface.mHandler.sendMessage(message);
	}
    
	public boolean isAutoMode()
	{
		return isAutoMode;
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
    	
    	loadWebView(this.songsMap.get("solo"));
    	//loadWebView(this.songsMap.get("Leaving On A Jet Plane - John Denver"));

    	webView.setWebViewClient(new WebViewClient() {
          @Override
          public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressDialog.hide();
          }
        });
	}
	
	public void onButtonRestart(View v)
	{
		this.operator.initialize();
		this.webInterface.performStop();
	}
	
	public void onButtonShowSongsList(View v)
	{
		this.songsListView.setVisibility(View.VISIBLE);
	}

    public void eventUiToggleMode(View v)
    {
    	isAutoMode = !isAutoMode;
		
    	setMode(isAutoMode);
		setUiAutoMode(isAutoMode);
    }

    public void eventUiForward(View v)
    {
		Message message = new Message();
		message.arg1 = Commands.Operator.eventForward;
		this.operator.mHandler.sendMessage(message);
    }
    
    public void eventUiBackward(View v)
    {
		Message message = new Message();
		message.arg1 = Commands.Operator.eventBackward;
		this.operator.mHandler.sendMessage(message);
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

