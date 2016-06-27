function getChordText(index)
{
	return chordList[index];
}

function isChordTextExplicit(chordText)
{
	if(chordText.indexOf('[')!=-1)
		return true
	return false
}

function chordTextToStringList(chordText)
{
	if(!isChordTextExplicit(chordText)) return new Array();

	var returnedList = new Array();
	var positionsArray = JSON.parse(chordText);
	for(var i=0; i<positionsArray.length; i++)
	{
		var fret = positionsArray[i][0];
		var string = positionsArray[i][1];
		var finger = null;
		if(positionsArray[i][2]!=null)
			finger = positionsArray[i][2]
		// is string
		if(fret == '0')
		{
			returnedList.push(string);
		}
	}
	return returnedList;
}

function explicitChordTextToChordList(chordText)
{
	if(isChordTextExplicit(chordText))
	{
		var returnedList = new Array();
		var positionsArray = JSON.parse(chordText);
		for(var i=0; i<positionsArray.length; i++)
		{
			var fret = positionsArray[i][0];
			var string = positionsArray[i][1];
			var finger = null;
			if(positionsArray[i][2]!=null)
				finger = positionsArray[i][2]
			// if string: fill all frets in string
			if(fret == '0')
				for(var j=1; j<=TOTAL_NUMBER_OF_FRETS; j++)
					returnedList.push([j,string, null]);
			else
				returnedList.push([fret,string, finger]);
		}
		return returnedList;
	}
}

function chordTextToPositionList(chordText)
{
	// [fret,string,finger]
	var chordArray = explicitChordTextToChordList(chordText);
	if(chordArray!=null) return chordArray;
	// [chord name]
	else switch(chordText)
	{
		case 'A':
			return [[2,2,1],[2,3,1],[2,4,1]];
		case 'Am':
			return [[1,2,1],[2,3,3],[2,4,3]];
		case 'Cadd9':
			return [[3,5,3],[2,4,2],[3,2,1]];
		case 'C':
			return [[3,5,3],[2,4,2],[1,2,1]];
		case 'Bb':
			return [[1,1,1],[3,2,2],[3,3,3],[3,4,4]];
		case 'Bm':
			return [[2,1,1],[2,2,1],[3,3,2],[4,4,3]];
		case 'D':
			return [[2,3,2],[3,2,4],[2,1,3]];
		case 'Dmaj7':
			return [[2,3,1],[2,2,2],[2,1,3]];
		case 'Em':
			return [[2,5,1],[2,4,2]];
		case 'E':
			return [[2,5,1],[2,4,2],[1,3,1]];
		case 'F':
			return [[1,1,1],[1,2,1],[2,3,2],[3,4,3]];
		case 'F#m':
			return [[2,1,1],[2,2,1],[4,4,3]];
		case 'G':
			return [[3,6,3],[2,5,2],[3,1,4]];
		case 'test0':
			return[[4,6],[4,5],[4,4]];
		case 'test1':
			return[[4,3],[4,2],[4,1]];
		case 'test2':
			return[[3,6],[3,5],[3,4]];
		case 'test3':
			return[[3,3],[3,2],[3,1]];
	}
}

function chordTextTopString(chordText)
{
	if(isChordTextExplicit(chordText))
	{
		return null;
	}

	else if(chordText.indexOf('A')!=-1)
		return 5;
	else if(chordText.indexOf('B')!=-1)
		return 4;
	else if(chordText.indexOf('C')!=-1)
		return 5;
	else if(chordText.indexOf('D')!=-1)
		return 4;
	else if(chordText.indexOf('E')!=-1)
		return 6;
	else if(chordText.indexOf('F')!=-1)
		return 4;
	else if(chordText.indexOf('G')!=-1)
		return 6;
}

function positionListToString(positionList)
{
	var returnedString = "000000000000000000000000";

	for(var i=0; i<positionList.length; i++)
	{
		var fret = positionList[i][0];
		var string = positionList[i][1];
		var replaceIndex = 30 - (fret*6 + string);
		returnedString = returnedString.substr(0,replaceIndex) + '1' + returnedString.substr(replaceIndex+1);
	}

	return returnedString;
}

