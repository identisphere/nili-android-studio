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
import com.nili.utilities.Timer;

import java.util.Date;


public class Operator extends Thread
{
	private MainActivity mainActivity;
	private int userState = State.WAITING_FOR_CORRECT_PRESS;
	private WebAppInterface 	webInterface;
	public 	Handler				mHandler;

	private Chords				chords = new Chords();
	private Listener			listener = new Listener();

	private BtOperations btOperations;
	private Timer timer;

	public boolean tiksAvailable() {
		return chords.isTiksAvailable();
	}

	private class UserProcessedPress
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
		public static int CHORD_END_TIK = 8;
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
						receivedPressFromUser((String) message.obj);
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
					else if(message.arg1==Commands.Operator.chordChangeTik)
						eventForward();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};

		Looper.loop();
	}

	public void eventChordTik() { handleStateChange(State.CHORD_END_TIK); }

	private void eventStrummedCorrect()
	{
		handleStateChange(State.STRUMMED_CORRECT);
	}

	private void eventRestart()
	{
		chords.setToFirstChord();
		handleStateChange(State.NEW_CHORD);
		mainActivity.setUiModeAndPause(mainActivity.getUiMode());

		// temp
		createFakePress();
	}

	private void eventForward()
	{
		if(!chords.goToNextChord())
		{
			handleStateChange(State.FINISHED_SONG);
			return;
		}

		Message message = new Message();
		message.arg1 = Commands.WebApp.eventForward;
		this.webInterface.mHandler.sendMessage(message);

		handleStateChange(State.NEW_CHORD);
		Log.d("operator:eventForward: ", String.valueOf(new Date().getTime()));

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
		chords.reset();
		timer.setActive(true);
	}

	public void finishedAddingChords()
	{
		if(chords.getListSize()==0)
		{
			btOperations.sendStringToBt("000000000000000000000000");
			return;
		}
		eventRestart();
	}

	// run by javascript
	public void addChordToChordList(String chordJsonString) throws Exception
	{
		chords.addChordToList(chordJsonString);
	}

	public void receivedPressFromUser(String receivedSwitchString)
	{
		if(chords.getListSize()==0)
		{
			this.sendStringToBoth(receivedSwitchString);
			return;
		}

		UserProcessedPress userProcessedPress;
		// Timed mode, don`t show pressed wrong
		if(mainActivity.getUiMode()==Globals.UImode.TIMED)
			userProcessedPress = processUserPress(receivedSwitchString, false);
		else
			userProcessedPress = processUserPress(receivedSwitchString, true);


		// waiting for user to lift fingers, user lifted fingers, auto mode
		if(userState == State.WAITING_FOR_USER_LIFT)
		{
			// timed mode
			if(mainActivity.getUiMode() == Globals.UImode.TIMED)
				return;
			// auto/manual mode
			if(receivedSwitchString.equalsIgnoreCase(Globals.emptyString))
				handleStateChange(State.USER_LIFT_FINGERS);
		}
		// waiting for user to press full chord correct
		else if(userState ==State.WAITING_FOR_CORRECT_PRESS
			&&
			userProcessedPress.pressedCorrect == chords.current().positionCount)
		{
			handleStateChange(State.PRESSED_CORRECT);
		}
		// send processed string to both
		else
		{
			btOperations.sendStringToBt(userProcessedPress.btPositions);
			this.sendStringToJs(userProcessedPress.jsPositions);
		}
	}

	public void handleStateChange(int eventType)
	{
		// new chord
		if (eventType == State.NEW_CHORD)
		{
			btOperations.stopStrumming();
			btOperations.sendStringToBt(chords.current().positionString);
			timer.setCounter(chords.getCounter());

			// set open string or not
			if(chords.isChordEmptyString(chords.current()))
			{
				userState = State.WAITING_FOR_CORRECT_STRUMM;
				listener.setCurrentString(chords.current().emptyStringList.get(0));
			}
			else
				userState = State.WAITING_FOR_CORRECT_PRESS;
		}
		// finished song
		else if(eventType == State.FINISHED_SONG)
		{
			/*
			eventRestart();

			Message message = new Message();
			message.arg1 = Commands.WebApp.restart;
			this.webInterface.mHandler.sendMessage(message);
			*/
		}
		// was waiting for user to press correct, and user pressed correct
		else if(userState == State.WAITING_FOR_CORRECT_PRESS
				&&
				eventType == State.PRESSED_CORRECT)
		{
			sendPressedCorrectToJs();
			if(mainActivity.getUiMode()==Globals.UImode.TIMED)
				btOperations.sendStringToBt(Globals.emptyString);
			else if(mainActivity.getUiMode()==Globals.UImode.AUTO)
				if(chords.current().positionCount==1)
					btOperations.blinkNeck(100, chords.next().positionString);
				else
					//btOperations.startStrumming(chords.current());
					btOperations.sendStringToBt(Globals.strummingPositionString(chords.current().topString));
			else if(mainActivity.getUiMode()==Globals.UImode.MANUAL)
				if(chords.current().positionCount==1)
					btOperations.blinkNeck(100, Globals.emptyString);
				else
					btOperations.startStrumming(chords.current());

			userState = State.WAITING_FOR_USER_LIFT;
		}
		// waiting for strummed correct, and strummed correct
		else if(userState == State.WAITING_FOR_CORRECT_STRUMM && eventType == State.STRUMMED_CORRECT)
		{
			if(mainActivity.getUiMode()==Globals.UImode.TIMED)
				btOperations.sendStringToBt(Globals.emptyString);
			else if(mainActivity.getUiMode()==Globals.UImode.AUTO)
				eventForward();
			else if(mainActivity.getUiMode()==Globals.UImode.MANUAL)
				btOperations.blinkNeck(100, Globals.emptyString);
		}
		// waiting for user to lift fingers, and user lifted fingers
		else if(userState == State.WAITING_FOR_USER_LIFT && eventType == State.USER_LIFT_FINGERS)
		{
			if(mainActivity.getUiMode()==Globals.UImode.AUTO)
				eventForward();
			else if(mainActivity.getUiMode()==Globals.UImode.MANUAL || mainActivity.getUiMode()==Globals.UImode.TIMED)
			{
				handleStateChange(State.NEW_CHORD);
				sendEventLiftFingersToJs();
			}
		}
	}

	public UserProcessedPress processUserPress(String receivedSwitchString, boolean showWrong)
	{
		String currentChordString = chords.current().positionString;
		UserProcessedPress userPress = new UserProcessedPress();
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
			if(showWrong && receivedSwitchString.charAt(i)=='1' && currentChordString.charAt(i)=='0')
			{
				userPress.btPositions = userPress.btPositions.substring(0,i) + Globals.BLINK_CHAR_RATE + userPress.btPositions.substring(i+1);
				userPress.jsPositions = userPress.jsPositions.substring(0,i) + "i" + userPress.jsPositions.substring(i+1);
			}
		}
		return userPress;
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

	private void sendEventLiftFingersToJs()
	{
		Message message = new Message();
		message.arg1 = Commands.WebApp.eventLiftFingers;
		this.webInterface.mHandler.sendMessage(message);
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
		btOperations.sendStringToBt(positionString);
		sendStringToJs(positionString);
	}
	
	public Operator()
	{
	}
	
	public void set(ConnectionManager connectionManager, WebAppInterface webInterface, Strumming strumming, Timer timer, MainActivity mainActivity)
	{
		this.webInterface = webInterface;
		this.mainActivity = mainActivity;
		this.timer = timer;
		this.btOperations = new BtOperations(strumming, connectionManager);
		listener.set(this);
	}
}