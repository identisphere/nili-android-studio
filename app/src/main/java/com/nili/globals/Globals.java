package com.nili.globals;

import java.util.ArrayList;

final public class Globals
{
	public static boolean 		isConnectedToBT = false;

	public static final char	BLINK_CHAR_RATE = '5';

	static public class State
	{
		static public int WAITING_FOR_USER_PRESS = 0;
		static public int WAITING_FOR_USER_LIFT = 1;
	}

	////////////////////////////////////////
	// STRUCTURE OF BT STRING
	/////////////////////////////////////
	// 4th fret --- 1st fret
	// 1st digit in fret: top string (closest to lead strip. LED #1)
	// 6st digit in fret: bottom string (farthest from lead strip. LED #6)
	// example: "111111000000100000000001"
	//  4th fret: full lights
	//	3rd fret: no light
	//	2nd fret: top light on
	//	1st fret: bottom light on
}

