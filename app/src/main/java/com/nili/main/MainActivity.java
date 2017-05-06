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
import com.nili.utilities.Timer;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity 
{
	private int uiMode;
	private boolean isLightsActive = true;

	// bt
    public ConnectionManager connectionManager;
    public BtReadData btReadData;
    
	public Strumming strumming;
	public Operator operator;
	private ListView songsListView;
	public HashMap<String, String> songsMap = new HashMap<String, String>();
	private Timer timer;


	// javascript
	public WebView			webView;
	public WebAppInterface webInterface;

	private ImageView changeModeButton;
	private ImageView forwardButton;
	private ImageView backwardButton;
	private ImageView playPauseButton;
	private ImageView reconnectButton;
	private TextView counterText;
	private TextView timerPlus;
	private TextView timerMinus;
	private TextView timerText;
	private TextView toggleLightsText;
	private ProgressDialog loadingSpinner;
	private int counter;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);


		// showing a "loading" message until loaded

		Thread.currentThread().setName("Main Activity Thread");
		
		connectionManager = new ConnectionManager();
		btReadData = new BtReadData();
		operator = new Operator();
		webInterface = new WebAppInterface();
		strumming = new Strumming();
		timer = new Timer();

        songsListView = (ListView) findViewById(R.id.songsList);
		changeModeButton = (ImageView) findViewById(R.id.IsAuto);
		forwardButton = (ImageView) findViewById(R.id.Forward);
		backwardButton = (ImageView) findViewById(R.id.Backward);
		playPauseButton = (ImageView) findViewById(R.id.playPause);
		reconnectButton = (ImageView) findViewById(R.id.reconnect);
		counterText = (TextView) findViewById(R.id.counterText);
		timerPlus = (TextView) findViewById(R.id.timerPlus);
		timerMinus = (TextView) findViewById(R.id.timerMinus);
		timerText = (TextView) findViewById(R.id.timerText);
		toggleLightsText = (TextView) findViewById(R.id.toggleLightsText);

		loadingSpinner = new ProgressDialog(MainActivity.this);
		loadingSpinner.setMessage("Loading ...");
		loadingSpinner.setCancelable(false);

		setSongsList();
		setWebView();



		webInterface.set(this, operator);
		///// ZVI ////
		// this sets webInteface to communicate with the web view JS
		// For JS to run some_function in webInterface, you need to write 'Android.some_function" in the JS
    	webView.addJavascriptInterface(webInterface, "Android");

		try
		{
			connectionManager.set(this, "98:D3:31:B1:F7:92");
			connectionManager.start();
			while(connectionManager.mHandler == null)
				Thread.sleep(200);

			waitForBtConnect();

			strumming.set(this.connectionManager);
			timer.set(operator, this);
			btReadData.set(this, connectionManager.inputStream, operator); // this thread reads the incomming data from bluetooth
			operator.set(this.connectionManager, this.webInterface, this.strumming, this.timer, this);

			webInterface.start();
			while(webInterface.mHandler == null)
				Thread.sleep(200);
			strumming.start();
			while(strumming.mHandler == null)
				Thread.sleep(200);
			operator.start();
			while(operator.mHandler == null)
				Thread.sleep(200);

			timer.start();
			btReadData.start();

			setLightsStatus(true);
		}
		catch(Exception ex)
		{

		}
	}

	private void setLightsStatus(boolean isActive)
	{
		Message message = new Message();
		if(isActive)
		{
			message.arg1 = Commands.ConnectionManager.lightsOn;
			toggleLightsText.setText("ON");
		}
		else
		{
			message.arg1 = Commands.ConnectionManager.lightsOff;
			toggleLightsText.setText("OFF");
		}
		this.connectionManager.mHandler.sendMessage(message);
	}


	private void waitForBtConnect()
	{

		Message message = new Message();
		message.arg1 = Commands.ConnectionManager.connectToBt;
		this.connectionManager.mHandler.sendMessage(message);

		reconnectButton.setBackgroundResource(R.drawable.reconnecting);
		synchronized(connectionManager)
		{
			try {
				connectionManager.wait(6000);
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
	}

	public void setUiModeAndPause(int mode)
	{
		uiMode = mode;

		setIsPaused(true);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (uiMode == Globals.UImode.AUTO) {
					changeModeButton.setBackgroundResource(R.drawable.auto);
					showTimedControls(false);
				} else if (uiMode == Globals.UImode.MANUAL) {
					changeModeButton.setBackgroundResource(R.drawable.manual);
					showTimedControls(false);
				} else if (uiMode == Globals.UImode.TIMED) {
					changeModeButton.setBackgroundResource(R.drawable.timed);
					showTimedControls(true);
					if (!operator.tiksAvailable()) {
						setUiModeAndPause(0);
					}
				}
			}
		});
	}

	private void showTimedControls(boolean isTimed)
	{
		if(isTimed)
		{
			playPauseButton.setVisibility(View.VISIBLE);
			counterText.setVisibility(View.VISIBLE);
			timerPlus.setVisibility(View.VISIBLE);
			timerMinus.setVisibility(View.VISIBLE);
			timerText.setVisibility(View.VISIBLE);
		}
		else
		{
			playPauseButton.setVisibility(View.GONE);
			counterText.setVisibility(View.GONE);
			timerPlus.setVisibility(View.GONE);
			timerMinus.setVisibility(View.GONE);
			timerText.setVisibility(View.GONE);
		}
	}

	public int getUiMode()
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

	// ZVI
	// load the HTML in web view.
	// url is relevant to the 'assets' folder
	protected void loadWebView(String url) 
	{
		this.webView.loadUrl("file:///android_asset/"+url);
	}

	private void setWebView()
	{

		///////// ZVI //////////////
		// connect webView object to display
		webView = (WebView)findViewById(R.id.activity_main_webview);
		// set clickable
		webView.setClickable(true);
    	// Enable java script
    	WebSettings webSettings = webView.getSettings();
    	webSettings.setJavaScriptEnabled(true);
    	// all pages to load from web view, instead of opening a new window
    	webView.setWebViewClient(new WebViewClient());
		// performance things
    	webView.getSettings().setRenderPriority(RenderPriority.HIGH);
    	webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    	
    	//loading the html:
		// webView.loadUrl("file:///android_asset/"+url);
		// url will be relative to the 'assets' folder
		loadWebView(this.songsMap.get("solo"));

		// call super function
    	webView.setWebViewClient(new WebViewClient() {
          @Override
          public void onPageFinished(WebView view, String url) {
			  super.onPageFinished(view, url);
          }
        });
		///////// ZVI //////////////
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

		setUiModeAndPause(mode);
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
		togglePlayMode();
	}

	public void onToggleLightsActive(View v)
	{
		isLightsActive = !isLightsActive;
		setLightsStatus(isLightsActive);
	}

	private void togglePlayMode()
	{
		if(timer.getIsPaused())
			setIsPaused(false);
		else
			setIsPaused(true);
	}

	private void setIsPaused(boolean isPaused)
	{
		if(isPaused)
		{
			playPauseButton.setBackgroundResource(R.drawable.play);
			timer.setIsPaused(true);
		}
		else
		{
			playPauseButton.setBackgroundResource(R.drawable.pause);
			timer.setIsPaused(false);
		}
	}

	public void onButtonConnect(View v)
	{
		Message message = new Message();
		message.arg1 = Commands.ConnectionManager.connectToBt;
		this.connectionManager.mHandler.sendMessage(message);

		waitForBtConnect();
	}

	public void onButtonTimerPlus(View v)
	{
		timer.changeRatio(1);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				timerText.setText(String.valueOf(timer.getRatio()));
			}
		});
	}

	public void onButtonTimerMinus(View v)
	{
		timer.changeRatio(-1);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				timerText.setText(String.valueOf(timer.getRatio()));
			}
		});
	}

	public void setCounter(int counter)
	{
		this.counter = counter;
		counterText.setText(String.valueOf(counter));
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

	public void setLoadingStarted() {
		webView.setVisibility(View.GONE);
		loadingSpinner.show();
	}

	public void setLoadingFinished() {
		setUiModeAndPause(uiMode);
		loadingSpinner.hide();
		webView.setVisibility(View.VISIBLE);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				timerText.setText(String.valueOf(timer.getRatio()));
			}
		});
	}
}

