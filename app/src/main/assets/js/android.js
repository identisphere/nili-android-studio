function eventForward()
{
	console.log("eventForward: ");
	stopStrummingAnimation();
	moveToNextChord();
	displayCurrentChord();
}

function eventBackward()
{
	console.log("eventBackward: ");
	stopStrummingAnimation();
	moveToPreviousChord();
	displayCurrentChord()
}

function eventStop()
{
	console.log("eventStop();");
	stopStrummingAnimation();
	setCurrentChord(0);

	setChordText();
	setLyrics();

	setAllNeckPositionsOff(false);
	setFingering(currentChord.positionList);
	setNeckPositionListOn(currentChord.positionList);
}

function eventPressedCorrect()
{
	if(!checkEventIsReal()) return;
	
	console.log("eventPressedCorrect");

	setAllNeckPositionsOff(true);
	setNeckPositionCorrectList(currentChord.positionList);

	element_chord.style.color = 'green';

	if(isChordTextExplicit(currentChord.text))
		setStringOn(currentChord.positionList[0][1]);
	else
		startStrummingAnimation(100, currentChord.topString);
}

function eventLiftFingers()
{
	stopStrummingAnimation();
	displayCurrentChord();
}

function sendMessageToAndroid(message)
{
	////// ZVI /////
	// this will execute messageFromJs in WebAppInterface object.
	console.log("send message to Android: " + message);
	if(isAndroid)
	{
		//noinspection JSUnresolvedVariable,JSUnresolvedFunction
		Android.messageFromJs(message);
	}
}

function receivePositionStringFromAndroid(_positionString)
{
	console.log("receivePositionStringFromAndroid: " + _positionString);
	var positionOnList = [];
	var positionBlinkList = [];
	var positionCorrectList = [];
	var positionIncorrectList = [];
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

		}
		else if(positionString.charAt(i)=='c')
		{
			positionCorrectList.push(position);

		}
		else if(positionString.charAt(i)!='1' && positionString.charAt(i)!='0')
		{
			//positionBlinkList.push(position);
			positionIncorrectList.push(position);

		}
	}

	setAllNeckPositionsOff(true);
	setNeckPositionListOn(positionOnList);
	setNeckPositionListCorrect(positionCorrectList);
	setNeckPositionListIncorrect(positionIncorrectList);
	//updateBlinkingList(positionBlinkList);
}

function sendChordToAndroid(chord)
{
	var jsonChord = JSON.stringify(chord);
	sendMessageToAndroid("addChord_"+jsonChord.toString());
}

