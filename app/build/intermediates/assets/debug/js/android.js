function eventSetTimer(tick)
{
	setTimer(tick);
}

function eventLiftFingers()
{
	if(!checkEventIsReal()) return;

	console.log("eventLiftFingers");

	eventStopStrummingAnimation();
	displayCurrentChord()
}

function eventForward()
{
	console.log("eventForward: ");
	eventStopStrummingAnimation();
	moveToNextChord();
	displayCurrentChord();
}

function eventBackward()
{
	console.log("eventBackward: ");
	eventStopStrummingAnimation();
	moveToPreviousChord();
	displayCurrentChord()
}


function eventPressedCorrect(positionString)
{
	if(!checkEventIsReal()) return;
	
	console.log("eventPressedCorrect: " + positionString);

	receivePositionStringFromAndroid(positionString);

	eventStartStrummingAnimation();
	
	if(isAutoMode)
	{
		moveToNextChord()
		setNeckPositionListOn(getPositionListOfChordText(getChordText(currentChordIndex)));
	}

	element_chord.style.color = 'green';
}

function eventStartStrummingAnimation()
{
	startStrummingAnimation(200, getChordTopString(getChordText(currentChordIndex)));
}


function eventStopStrummingAnimation()
{
	stopStrummingAnimation();
}

function setMode(isAuto)
{
	console.log("eventSetMode: " + isAuto);
	isAutoMode = isAuto;
}

function eventSetTimed(tick)
{
	setTimerVisible(tick)
}

function eventSetManual()
{
	setTimerHidden();
}


function sendMessageToAndroid(message)
{
	console.log("send message to Android: " + message)
	if(isAndroid)
	{
		Android.messageFromJs(message);
	}
}

function receivePositionStringFromAndroid(_positionString)
{
	console.log("receivePositionStringFromAndroid: " + _positionString);
	var positionOnList = new Array();
	var positionBlinkList = new Array();
	var positionCorrectList = new Array();
	var positionIncorrectList = new Array();
	var fretIndex, stringIndex;
	var positionString = _positionString;

	for(var i=0; i<positionString.length; i++)
	{
		var fretIndex =  5 - (1 + Math.floor(i/6));
		var stringIndex = (6 - i%6);
		var position = [fretIndex, stringIndex]; 
		if(positionString.charAt(i)=='1')
		{
			positionOnList.push(position);
			continue;
		}
		else if(positionString.charAt(i)=='c')
		{
			positionCorrectList.push(position);
			continue;
		}
		else if(positionString.charAt(i)!='1' && positionString.charAt(i)!='0')
		{
			//positionBlinkList.push(position);
			positionIncorrectList.push(position);
			continue;
		}
	}

	setAllNeckPositionsOff(true);
	setNeckPositionListOn(positionOnList);
	setNeckPositionListCorrect(positionCorrectList);
	setNeckPositionListIncorrect(positionIncorrectList);
	//updateBlinkingList(positionBlinkList);
}

function eventStop()
{
	stopStrummingAnimation();
	currentChordIndex = 0;
	
	setChordText();
	setLyrics();

	setAllNeckPositionsOff(false);
	setFingering(getPositionListOfChordText(getChordText(currentChordIndex)));
	setNeckPositionListOn(getPositionListOfChordText(getChordText(currentChordIndex)));
}

