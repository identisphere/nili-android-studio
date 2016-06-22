function getChordText(index)
{
	return chordList[index];
}

function getPositionListOfChordText(chord)
{
	// [fret,string,finger]
	if(chord.indexOf('[')!=-1)
	{
		return JSON.parse(chord);
		return;
	}
	switch(chord)
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

