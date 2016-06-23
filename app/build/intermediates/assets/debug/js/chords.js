function getChordText(index)
{
	return chordList[index];
}

function isChordTextString(chord)
{
	if(chord.indexOf('[')!=-1) return true;
}

function chordTextToChordArray(chord)
{
	if(isChordTextString(chord))
	{
		var returnedList = new Array();
		var positionsArray = JSON.parse(chord);
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
				for(var j=0; j<=TOTAL_NUMBER_OF_FRETS; j++)
				{
					returnedList.push([j+1,string, null]);
				}
				returnedList.push(["string",string, null]);
			}
			else
				returnedList.push([fret,string, finger]);
		}
		return returnedList;
	}
}

function getPositionListOfChordText(chord)
{
	// [fret,string,finger]
	var chordArray = chordTextToChordArray(chord);
	if(chordArray!=null) return chordArray;
	// [chord name]
	else switch(chord)
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
	}
}

function getChordTopString(chord)
{
	if(chord.indexOf('A')!=-1)
		return 5;
	else if(chord.indexOf('B')!=-1)
		return 4;
	else if(chord.indexOf('C')!=-1)
		return 5;
	else if(chord.indexOf('D')!=-1)
		return 4;
	else if(chord.indexOf('E')!=-1)
		return 6;
	else if(chord.indexOf('F')!=-1)
		return 4;
	else if(chord.indexOf('G')!=-1)
		return 6;
}

