function setStringListOn(stringList)
{
	for(var i=0; i < stringList.length; i++)
		setStringOn(stringList[i]);
}

function setStringOn(string)
{
	//stringElements[string].style.animation = "vibrate 0.1s linear 0s infinite";
	stringElements[TOTAL_NUMBER_OF_STRINGS-string].classList.add("strum");
}

function setStringOff(string)
{
	stringElements[TOTAL_NUMBER_OF_STRINGS-string-1].classList.remove("strum");
	//stringElements[i].style.animation = "";
}

function setNeckPositionStringOff(string)
{
	for(var fret=1; fret<=TOTAL_NUMBER_OF_FRETS; fret++)
		setNeckPositionOff(fret, string, false)
}

function setNeckPositionStringListOff(stringList)
{
	for(var i=0; i<stringList.length; i++)
		setNeckPositionStringOff(stringList[i]);
}

function setNeckPositionOn(fret, string, leaveFingering)
{
	if(neckFrets==null) neckFrets = document.getElementsByClassName("fret_table");
	var position = neckPositionElementArray[fret-1][6-string]; 

	position.style.opacity = "1";
	//position.style.zIndex = 4;
	if(leaveFingering==false) position.innerHTML = "";
}

function setNeckPositionOff(fret, string, leaveFingering)
{
	if(neckFrets==null) neckFrets = document.getElementsByClassName("fret_table");
	var position = neckPositionElementArray[fret-1][6-string];
	position.style.opacity = "0.2";
	//position.style.zIndex = 1;
	if(leaveFingering==false) position.innerHTML = "";

	neckPositionIncorrectElementArray[fret-1][6-string].style.opacity = '0';
	neckPositionCorrectElementArray[fret-1][6-string].style.opacity = '0';
	
}

function setNeckPositionCorrectList(positionList)
{
	for(var i=0; i<positionList.length; i++)
		setNeckPositionCorrect(positionList[i][0], positionList[i][1]);
}

function setNeckPositionCorrect(fret, string)
{
	var position = neckPositionCorrectElementArray[fret-1][6-string]; 
	position.style.opacity = '1';
}

function setNeckPositionIncorrect(fret, string)
{
	var position = neckPositionIncorrectElementArray[fret-1][6-string]; 
	position.style.opacity = '1';
}

function setNeckPositionBlinkOn(fret, string, leaveFingering)
{
	if(neckFrets==null) neckFrets = document.getElementsByClassName("fret_table");
	var position = neckPositionElementArray[fret-1][6-string]; 

	position.style.animation = "blink 0.1s linear 0s infinite";
	//position.style.zIndex = 1;
	if(leaveFingering==false) position.innerHTML = "";
	
}

function setNeckPositionBlinkOff(fret, string, leaveFingering)
{
	if(neckFrets==null) neckFrets = document.getElementsByClassName("fret_table");
	var position = neckPositionElementArray[fret-1][6-string]; 

	position.style.animation = "";
	//position.style.zIndex = 1;
	if(leaveFingering==false) position.innerHTML = "";
	
}

function setAllNeckPositionsOff(leaveFingering)
{
	if(neckPositions==null) neckPositions = document.getElementsByClassName("position_td");
	for(var fret=0; fret<=TOTAL_NUMBER_OF_FRETS; fret++)
	{
		for(var string=0; string<TOTAL_NUMBER_OF_STRINGS; string++)
		{
			//neckPositionCorrectElementArray[fret][string].style.opacity = '0';
			setNeckPositionOff(fret+1, string+1, leaveFingering);
		}
	}
}

function setAllNeckPositionsOn(leaveFingering)
{
	if(neckPositions==null) neckPositions = document.getElementsByClassName("position_td");
	for(var fret=0; fret<=TOTAL_NUMBER_OF_FRETS; fret++)
	{
		for(var string=0; string<TOTAL_NUMBER_OF_STRINGS; string++)
		{
			setNeckPositionOn(fret+1, string+1, leaveFingering);
		}
	}
}

function setNeckPositionListOff(positionList)
{
	for(var i=0; i<positionList.length; i++)
	{
		setNeckPositionOff(positionList[i][0], positionList[i][1])
	}
}

function setNeckPositionListOn(positionList)
{
	for(var i=0; i<positionList.length; i++)
	{
		if(positionList[i][0]=="string")
			stringElements[
				TOTAL_NUMBER_OF_STRINGS - positionList[i][1]
			].classList.add("strum");
		else
			setNeckPositionOn(positionList[i][0], positionList[i][1])
	}
}

function setNeckPositionListCorrect(positionList)
{
	for(var i=0; i<positionList.length; i++)
	{
		setNeckPositionCorrect(positionList[i][0], positionList[i][1])
	}
}

function setNeckPositionListIncorrect(positionList)
{
	for(var i=0; i<positionList.length; i++)
	{
		setNeckPositionIncorrect(positionList[i][0], positionList[i][1])
	}
}

