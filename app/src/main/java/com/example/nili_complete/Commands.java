package com.example.nili_complete;

final public class Commands 
{
	public final class WebApp 
	{
		static final int eventPressedCorrect = 1;
		static final int eventStopAnimation = 2;
		static final int liftFingers = 3;
		static final int sendStringToJs = 4;
		static final int eventUiChangeMode = 5;
		public static final int eventForward = 6;
		public static final int eventBackward = 7;
	}
	
	public final class Operator 
	{
		static final int receivePress = 1;
		static final int addChord = 2;
		public static final int finishedAddingChords = 3;
		public static final int restart = 4;
		public static final int eventForward = 5;
		public static final int eventBackward = 6;
	}
	
	public final class ConnectionManager
	{
		static final int sendToBt = 1;
	}
}
