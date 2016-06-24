package com.nili.main;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.nili.globals.Commands;
import com.nili.globals.Globals;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.nili.utilities.ConnectionManager;


public class Operator extends Thread
{
	private MainActivity		mainActivity;
	private int					currentState = Globals.State.WAITING_FOR_USER_PRESS;
	private ConnectionManager	connectionManager;
	private WebAppInterface		webInterface;
	public 	Handler				mHandler;
	private Chords				chords = new Chords();

	public void run()
	{
		Thread.currentThread().setName("Operator");
		Looper.prepare();
		
		mHandler = new Handler() {
			public void handleMessage(Message message)
			{
				try
				{
					if(message.arg1== Commands.Operator.receivePress)
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
		chords.clearList();
	}

	public void receivedPressFromUser(String receivedSwitchString)
	{
		if(chords.getListSize()==0)
		{
			this.sendStringToBoth(receivedSwitchString);
			return;
		}

		String currentChordString = chords.getCurrentChord().positionString;
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
				btReturnedString = btReturnedString.substring(0,i) + Globals.BLINK_CHAR_RATE + btReturnedString.substring(i+1);
				jsReturnedString = jsReturnedString.substring(0,i) + "i" + jsReturnedString.substring(i+1);
			}
		}

		// waiting for user to lift fingers to move to next chord
		if(this.currentState == Globals.State.WAITING_FOR_USER_LIFT && mainActivity.isAutoMode())
		{
			// user  lifted fingers
			if(receivedSwitchString.equalsIgnoreCase("000000000000000000000000"))
			{
				this.sendStringToBt(currentChordString);
				sendLiftFingersToJs();
				this.currentState = Globals.State.WAITING_FOR_USER_PRESS;
			}
			return;
		}

		
		this.sendStringToBt(btReturnedString);
		this.sendStringToJs(jsReturnedString);

		// pressed correctly, perform strumming animation and wait for user to lift fingers to move to next chord
		if(pressedCorrect == chords.getPositionCoumt())
		//if(pressedCorrect>=1)
		{

			if(mainActivity.isAutoMode())
			{
				goToNextChord();
				this.currentState = Globals.State.WAITING_FOR_USER_PRESS;
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
		if(chords.getListSize()==0)
		{
			this.sendStringToBt("000000000000000000000000");
			return;
		}
		if(chords.setCurrentChord(0))
		{
			sendStringToBt(chords.getCurrentChord().positionString);
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

	private boolean goToNextChord()
	{
		if(!chords.goToNextChord()) return false;
		sendStringToBt(chords.getCurrentChord().positionString);
		return true;
	}

	private boolean goToPrevChord()
	{
		if(!chords.goToPreviousChord()) return false;
		sendStringToBt(chords.getCurrentChord().positionString);
		return true;
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
		if(goToPrevChord())
		{
			Message message = new Message();
			message.arg1 = Commands.WebApp.eventBackward;
			this.webInterface.mHandler.sendMessage(message);
		}
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
		Chords.ChordObject receivedChord = new Chords.ChordObject();
		receivedChord.positionString = receivedJson.getString("positionString");
		JSONArray stringListJson = receivedJson.getJSONArray("stringList");
		receivedChord.stringList = new ArrayList<Integer>();
		for(int i=0; i<stringListJson.length(); i++)
			receivedChord.stringList.add(Integer.parseInt(stringListJson.get(i).toString()));

		chords.addChordToList(receivedChord);
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