package com.nili.globals;

import com.nili.operator.Chords;

import java.util.ArrayList;

final public class Globals
{
	public static final long MIN_TIK = 200;
	public static final int DEFAULT_TIMER = 5;

	public static boolean 		isConnectedToBT = false;

	public static final char	BLINK_CHAR_RATE = '5';

	public static final long	MIN_TIME_BETWEEN_BT_CALLS = 50;

	public static String addBtDelimeters(String string)
	{
		return "+"+string+"#";
	}

	private String removeBtDelimeters(String string)
	{
		return string.substring(1, string.length()-1);
	}

	public static String strummingPositionString(int topString)
	{
		String strummingPositions = emptyString;
		String fullFretStrumming = "111111";

		fullFretStrumming = fullFretStrumming.substring(0, topString);
		strummingPositions = strummingPositions.substring(0, emptyString.length()-6);
		strummingPositions = strummingPositions + emptyString.substring(0, 6-topString) + fullFretStrumming;

		return strummingPositions;
	}


	public static class UImode
	{
		static public int AUTO = 0;
		static public int MANUAL = 1;
		static public int TIMED = 2;
	}

	public static final String emptyString = "000000000000000000000000";

	////////////////////////////////////////
	// STRUCTURE OF BT STRING
	/////////////////////////////////////
	// 4th fret --- 1st fret
	// 6st digit in fret: top string (closest to lead strip. LED #1)
	// 1st digit in fret: bottom string (farthest from lead strip. LED #6)
	// example: "111111000000100000000001"
	//  4th fret: full lights
	//	3rd fret: no light
	//	2nd fret: top light on
	//	1st fret: bottom light on
}

