package com.example.nili_complete;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;


public class Operator extends Thread
{
	private MainActivity		mainActivity;
	private String 				currentChordString;
	private final char 			blinkRateChar = '5';
	private int 				positionCount;
	private boolean 			waitingForNoPress = false;
	private ConnectionManager	connectionManager;
	private WebAppInterface		webInterface;
	public 	Handler				mHandler;
	public	ArrayList<ChordObject> chordList = new ArrayList<ChordObject>();
	private int currentChordIndex; 
	private Object waitingForBlink = new Object();

	public void run()
	{
		Thread.currentThread().setName("Operator");
		Looper.prepare();
		
		mHandler = new Handler() {
			public void handleMessage(Message message)
			{
				try
				{
					if(message.arg1==Commands.Operator.receivePress)
						receivedPressFromUser((String)message.obj);
					else if(message.arg1==Commands.Operator.addChord)
						addChordToChordList((String)message.obj);
					else if(message.arg1==Commands.Operator.finishedAddingChords)
						initialize();
					else if(message.arg1==Commands.Operator.restart)
						restart();
					else if(message.arg1==Commands.Operator.eventForward)
						eventForward();
					else if(message.arg1==Commands.Operator.eventBackward)
						eventBackward();
				}
				catch (Exception ex)
				{

				}
			}
		};
		
		Looper.loop();
	}
	
	protected void restart() 
	{
		this.chordList.clear();
	}

	public void receivedPressFromUser(String receivedSwitchString)
	{
		if(this.chordList.size()==0)
		{
			this.sendStringToBoth(receivedSwitchString);
			return;
		}

		String btReturnedString = currentChordString;
		String jsReturnedString = currentChordString;

		int pressedCorrect = 0;
		for(int i=0; i<receivedSwitchString.length(); i++)
		{
			// pressed right. set char to 0
			if(receivedSwitchString.charAt(i)=='1' && currentChordString.charAt(i)=='1')
			{
				btReturnedString = btReturnedString.substring(0,i) + "0" + btReturnedString.substring(i+1);
				jsReturnedString = jsReturnedString.substring(0,i) + "c" + jsReturnedString.substring(i+1);
				pressedCorrect++;
			}
				
			// pressed wrong. set char to blinkRate
			if(receivedSwitchString.charAt(i)=='1' && currentChordString.charAt(i)=='0')
			{
				btReturnedString = btReturnedString.substring(0,i) + blinkRateChar + btReturnedString.substring(i+1);
				jsReturnedString = jsReturnedString.substring(0,i) + "i" + jsReturnedString.substring(i+1);
			}
		}

		// waiting for user to lift fingers to move to next chord
		if(this.waitingForNoPress && mainActivity.isAutoMode())
		{
			if(receivedSwitchString.equalsIgnoreCase("000000000000000000000000"))
			{
				this.sendStringToBt(this.currentChordString);
				sendLiftFingersToJs();
				this.waitingForNoPress = false;
			}
			return;
		}

		
		this.sendStringToBt(btReturnedString);
		this.sendStringToJs(jsReturnedString);

		// pressed correctly, perform strumming animation and wait for user to lift fingers to move to next chord
		if(pressedCorrect==this.positionCount)
		//if(pressedCorrect>=1)
		{

			if(mainActivity.isAutoMode())
			{
				goToNextChord();
				this.waitingForNoPress = true;
			}

			this.sendStringToBt("111111111111111111111111");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			sendStringToBt(currentChordString);
			sendPressedCorrectToJs(jsReturnedString);
		}
		
	}

	
	public void initialize()
	{
		if(chordList.size()==0)
		{
			this.sendStringToBt("000000000000000000000000");
			return;
		}
		this.currentChordIndex = 0;
		if(setCurrentChord(this.currentChordIndex))
		{
			sendStringToBt(chordList.get(currentChordIndex).positionString);
		}
		
		// temp
    	//createFakePress();
	}
	
	public void createFakePress()
	{
    	try {

    		Thread.sleep(1000);
    		//webInterface.sendMessageToJs("eventPressedCorrect()");
    		//webInterface.sendMessageToJs("eventPressedCorrect(000000100001010000000000)");
	    	receivedPressFromUser("000000100001010000000000");
	    	receivedPressFromUser("000000000000000000000000");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void eventForward()
	{
		if(goToNextChord())
		{
			Message message = new Message();
			message.arg1 = Commands.WebApp.eventForward;
			this.webInterface.mHandler.sendMessage(message);
		}
	}
	
	private void eventBackward()
	{
		if(goToPreviousChord())
		{
			Message message = new Message();
			message.arg1 = Commands.WebApp.eventBackward;
			this.webInterface.mHandler.sendMessage(message);
		}
	}

	private boolean goToNextChord()
	{
		if(this.currentChordIndex == chordList.size()-1) return false;
		this.currentChordIndex++;
		setCurrentChord(this.currentChordIndex);
		return true;
	}
	
	private boolean goToPreviousChord()
	{
		if(this.currentChordIndex == 0) return false;
		this.currentChordIndex--;
		setCurrentChord(this.currentChordIndex);
		return true;
	}
	
	private void sendPressedCorrectToJs(String positionString) 
	{
		Message message = new Message();
		message.arg1 = Commands.WebApp.eventPressedCorrect;
		message.obj = positionString;
		this.webInterface.mHandler.sendMessage(message);
	}

	private void sendLiftFingersToJs() 
	{

		Message message = new Message();
		message.arg1 = Commands.WebApp.liftFingers;
		this.webInterface.mHandler.sendMessage(message);
	}

	// run by javascript
	public void addChordToChordList(String chordJsonString) throws Exception
	{
		JSONObject receivedJson = new JSONObject(chordJsonString);
		ChordObject receivedChord = new ChordObject();
		receivedChord.positionString = receivedJson.getString("positionString");
		JSONArray stringListJson = receivedJson.getJSONArray("stringList");
		receivedChord.stringList = new ArrayList<Integer>();
		for(int i=0; i<stringListJson.length(); i++)
			receivedChord.stringList.add(Integer.parseInt(stringListJson.get(i).toString()));

		this.chordList.add(receivedChord);
	}
	
	public boolean setCurrentChord(int index)
	{
		if(this.chordList.size()==0)
		{
			return false;
		}
		if(index>this.chordList.size())
		{
			this.sendStringToBt("000000000000000000000000");
			return false;
		}
		this.currentChordString = this.chordList.get(index).positionString;
		this.positionCount = 0;
		for(int i=0; i<this.currentChordString.length(); i++)
		{
			if(this.currentChordString.charAt(i)=='1')
				this.positionCount++;
		}
		
		this.sendStringToBt(this.currentChordString);
		return true;
	}
	
	void sendStringToBt(String data)
	{
		data = this.addBtDelimeters(data);

		Message message = new Message();
		message.arg1 = Commands.ConnectionManager.sendToBt;
		message.obj = addBtDelimeters(data);
		this.connectionManager.mHandler.sendMessage(message);
	}
	
	private void sendStringToJs(String positionString)
	{
		Message message = new Message();
		message.arg1 = Commands.WebApp.sendStringToJs;
		message.obj = positionString;
		this.webInterface.mHandler.sendMessage(message);
	}
	
	public void sendStringToBoth(String positionString)
	{
		sendStringToBt(positionString);
		sendStringToJs(positionString);
	}
	
	public Operator()
	{
	}
	
	private String addBtDelimeters(String string)
	{
		return "+"+string+"#";
	}
	
	private String removeBtDelimeters(String string)
	{
		return string.substring(1, string.length()-1);
	}
	
	public void set(ConnectionManager connectionManager, WebAppInterface webInterface, MainActivity mainActivity) 
	{
		this.connectionManager = connectionManager;
		this.webInterface = webInterface;
		this.mainActivity = mainActivity;
		Thread.currentThread().setName("Operator Thread");
	}
}