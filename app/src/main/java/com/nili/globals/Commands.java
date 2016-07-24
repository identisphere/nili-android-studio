package com.nili.globals;

final public class Commands 
{
	public final class WebApp 
	{
		public static final int eventPressedCorrect = 1;
		public static final int eventLiftFingers = 2;
		public static final int sendStringToJs = 4;
		public static final int eventForward = 5;
		public static final int eventBackward = 6;
		public static final int restart = 7;
	}
	
	public final class Operator 
	{
		public static final int receivePress = 1;
		public static final int addChord = 2;
		public static final int finishedAddingChords = 3;
		public static final int startAddingChords = 4;
		public static final int eventForward = 5;
		public static final int eventBackward = 6;
		public static final int strummedCorrect = 7;
		public static final int restart = 8;
		public static final int chordChangeTik = 9;
	}
	
	public final class ConnectionManager
	{
		public static final int sendToBt = 1;
		public static final int connectToBt = 2;
	}

	public final class Strumming
	{
		public static final int startStrumming = 1;
		public static final int stopStrumming = 2;
	}
}
