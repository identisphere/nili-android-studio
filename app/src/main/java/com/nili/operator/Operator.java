package com.nili.operator;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.nili.globals.Commands;
import com.nili.globals.Globals;

import com.nili.main.MainActivity;
import com.nili.main.WebAppInterface;
import com.nili.utilities.ConnectionManager;
import com.nili.utilities.Listener;
import com.nili.utilities.Strumming;


public class Operator extends Thread
{
	private MainActivity mainActivity;
	private int					currentState = State.WAITING_FOR_CORRECT_PRESS;
	private ConnectionManager	connectionManager;
	private WebAppInterface 	webInterface;
	public 	Handler				mHandler;
	private Chords				chords = new Chords();
	private Listener			listener = new Listener();
	private Strumming 			strumming;

	private class UserPress
	{
		public String btPositions;
		public String jsPositions;
		public int pressedCorrect = 0;
	}

	static public class State
	{
		static public int WAITING_FOR_CORRECT_PRESS = 0;
		static public int WAITING_FOR_USER_LIFT = 1;
		static public int WAITING_FOR_CORRECT_STRUMM = 2;
		static public int NEW_CHORD = 3;
		static public int STRUMMED_CORRECT = 4;
		public static int PRESSED_CORRECT = 5;
		public static int USER_LIFT_FINGERS = 6;
		public static int FINISHED_SONG = 7;
	}


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
						finishedAddingChords();
					else if(message.arg1==Commands.Operator.startAddingChords)
						startAddingChords();
					else if(message.arg1==Commands.Operator.eventForward)
						eventForward();
					else if(message.arg1==Commands.Operator.eventBackward)
						eventBackward();
					else if(message.arg1==Commands.Operator.strummedCorrect)
						eventStrummedCorrect();
					else if(message.arg1==Commands.Operator.restart)
						eventRestart();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};

		Looper.loop();
	}

	private void eventStrummedCorrect()
	{
		handleStateChange(State.STRUMMED_CORRECT);
	}

	private void eventRestart()
	{
		chords.setToFirstChord();
		handleStateChange(State.NEW_CHORD);

		// temp
		createFakePress();
	}

	private void eventForward()
	{
		if(!goToNextChord()) return;

		Message message = new Message();
		message.arg1 = Commands.WebApp.eventForward;
		this.webInterface.mHandler.sendMessage(message);

		handleStateChange(State.NEW_CHORD);
	}

	private void eventBackward()
	{
		if(!chords.goToPreviousChord()) return;

		Message message = new Message();
		message.arg1 = Commands.WebApp.eventBackward;
		this.webInterface.mHandler.sendMessage(message);

		handleStateChange(State.NEW_CHORD);
	}

	private void startAddingChords()
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


		UserPress userPress = processUserPress(receivedSwitchString);

		// waiting for user lift
		if(currentState == State.WAITING_FOR_USER_LIFT)
			//auto mode, user lifted fingers
			if(mainActivity.isAutoMode()
			&&
			receivedSwitchString.equalsIgnoreCase("000000000000000000000000"))
			{
				handleStateChange(State.USER_LIFT_FINGERS);
			}
			else
				return;
		// waiting for user to press full chord correct
		else if(currentState==State.WAITING_FOR_CORRECT_PRESS
			&&
			userPress.pressedCorrect == chords.current().positionCount)
		{
			handleStateChange(State.PRESSED_CORRECT);
		}
		// send processed string to both
		else
		{
			this.sendStringToBt(userPress.btPositions);
			this.sendStringToJs(userPress.jsPositions);
		}
	}

	public void handleStateChange(int eventType)
	{
		// new chord
		if(eventType == State.NEW_CHORD)
		{
			stopStrumming();
			sendStringToBt(chords.current().positionString);
			// set open string or not
			if(chords.isChordEmptyString(chords.current()))
			{
				currentState = State.WAITING_FOR_CORRECT_STRUMM;
				listener.setCurrentString(chords.current().emptyStringList.get(0));
			}
			else
				currentState = State.WAITING_FOR_CORRECT_PRESS;
		}
		// finished song
		else if(eventType == State.FINISHED_SONG)
		{
			eventRestart();

			Message message = new Message();
			message.arg1 = Commands.WebApp.restart;
			this.webInterface.mHandler.sendMessage(message);
		}
		// was waiting for user to press correct, and user pressed correct
		else if(currentState == State.WAITING_FOR_CORRECT_PRESS
				&&
				eventType == State.PRESSED_CORRECT)
		{
			sendPressedCorrectToJs();
			if(chords.current().positionCount==1)
				blinkNeck(100, chords.next().positionString);
			else
				startStrumming(chords.current());
			currentState = State.WAITING_FOR_USER_LIFT;
		}
		// was waiting for user to lift fingers, and user lifted fingers
		else if(currentState == State.WAITING_FOR_USER_LIFT
				&&
				eventType == State.USER_LIFT_FINGERS)
		{
			if(mainActivity.isAutoMode())
			{
				eventForward();
			}
		}
		// was waiting for use to strum correct, and user strummed correct
		else if(currentState == State.WAITING_FOR_CORRECT_STRUMM
				&&
				eventType == State.STRUMMED_CORRECT)
		{
			eventForward();
		}
	}

	public UserPress processUserPress(String receivedSwitchString)
	{
		String currentChordString = chords.current().positionString;
		UserPress userPress = new UserPress();
		userPress.btPositions = currentChordString;
		userPress.jsPositions = currentChordString;

		for(int i=0; i<receivedSwitchString.length(); i++)
		{
			// pressed right. set char to 0
			if(receivedSwitchString.charAt(i)=='1' && currentChordString.charAt(i)=='1')
			{
				userPress.btPositions = userPress.btPositions.substring(0,i) + "0" + userPress.btPositions.substring(i+1);
				userPress.jsPositions = userPress.jsPositions.substring(0,i) + "c" + userPress.jsPositions.substring(i+1);
				userPress.pressedCorrect++;
			}

			// pressed wrong. set char to blinkRate
			if(receivedSwitchString.charAt(i)=='1' && currentChordString.charAt(i)=='0')
			{
				userPress.btPositions = userPress.btPositions.substring(0,i) + Globals.BLINK_CHAR_RATE + userPress.btPositions.substring(i+1);
				userPress.jsPositions = userPress.jsPositions.substring(0,i) + "i" + userPress.jsPositions.substring(i+1);
			}
		}
		return userPress;
	}

	public void finishedAddingChords()
	{
		if(chords.getListSize()==0)
		{
			this.sendStringToBt("000000000000000000000000");
			return;
		}
		eventRestart();
	}

	private boolean goToNextChord()
	{
		if(!chords.goToNextChord())
		{
			handleStateChange(State.FINISHED_SONG);
			return false;
		}
		else return true;
	}

	private void blinkNeck(long delay, String postBlinkPositions)
	{
		this.sendStringToBt("111111111111111111111111");
		try { Thread.sleep(delay); } catch (Exception e) {e.printStackTrace();}
		this.sendStringToBt(postBlinkPositions);
	}

	public void createFakePress()
	{
		if(1==1) return;
    	try {
			// G chord: 000000100001010000000000
			receivedPressFromUser("000000100001010000000000");
			Thread.sleep(50);
			receivedPressFromUser("000000000000000000000000");
			Log.d("a", "a");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendPressedCorrectToJs()
	{
		Message message = new Message();
		message.arg1 = Commands.WebApp.eventPressedCorrect;
		this.webInterface.mHandler.sendMessage(message);
	}

	// run by javascript
	public void addChordToChordList(String chordJsonString) throws Exception
	{
		chords.addChordToList(chordJsonString);
	}
	
	synchronized void sendStringToBt(String data)
	{
		data = Globals.addBtDelimeters(data);

		Message message = new Message();
		message.arg1 = Commands.ConnectionManager.sendToBt;
		message.obj = Globals.addBtDelimeters(data);
		this.connectionManager.mHandler.sendMessage(message);
	}
	
	private void sendStringToJs(String positionString)
	{
		Message message = new Message();
		message.arg1 = Commands.WebApp.sendStringToJs;
		message.obj = positionString;
		this.webInterface.mHandler.sendMessage(message);
	}
	
	private void startStrumming(Chords.ChordObject chord)
	{
		Message message = new Message();
		message.arg1 = Commands.Strumming.startStrumming;
		message.arg2 = chord.topString;
		this.strumming.mHandler.sendMessage(message);
	}

	private void stopStrumming()
	{
		this.strumming.isActive = false;
		/*
		Message message = new Message();
		message.arg1 = Commands.Strumming.stopStrumming;
		this.strumming.mHandler.sendMessage(message);
		*/
	}

	public void sendStringToBoth(String positionString)
	{
		sendStringToBt(positionString);
		sendStringToJs(positionString);
	}
	
	public Operator()
	{
	}
	
	public void set(ConnectionManager connectionManager, WebAppInterface webInterface, Strumming strumming, MainActivity mainActivity)
	{
		this.connectionManager = connectionManager;
		this.webInterface = webInterface;
		this.mainActivity = mainActivity;
		this.strumming = strumming;
		listener.set(this);
	}
}