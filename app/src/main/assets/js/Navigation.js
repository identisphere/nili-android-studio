function moveToNextChord()
{
    if(currentChordIndex>chordList.length) return;
    currentChordIndex++;
    setCurrentChord(currentChordIndex);
}

function moveToPreviousChord()
{
    if(currentChordIndex<=0) return;
    currentChordIndex--;
    setCurrentChord(currentChordIndex);
}

function setCurrentChord(index)
{
    currentChordIndex = index;
    if(currentChordIndex <= chordList.length)
        currentChord = chordList[currentChordIndex];

    if(currentChordIndex+1 < chordList.length)
        nextChord = chordList[currentChordIndex+1];
    else
        nextChord = null;

    if(currentChordIndex>0)
        prevChord = chordList[currentChordIndex-1];
    else
        prevChord = null;
}

