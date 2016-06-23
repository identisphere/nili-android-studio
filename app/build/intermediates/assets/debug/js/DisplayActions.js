function displayCurrentChord()
{
    setAllNeckPositionsOff(false);
    setLyrics();
    setChordText();
    setFingering(currentChord.positionList);
    setNeckPositionListOn(currentChord.positionList);
    if(currentChord.stringList.length>0)
    {
        lightStringsList(currentChord.stringList);
        setNeckPositionStringListOff(currentChord.stringList)
    }

    element_chord.style.color = 'red';
}

function setChordText()
{
    if(isChordTextExplicit(currentChord.text))
    {
        element_chord.innerHTML = "--";
        return;
    }
    element_chord.innerHTML = currentChord.text;

    if(nextChord!=null)
        element_next.innerHTML = nextChord.txt;
    else
        element_next.innerHTML = "";

    element_chord.style.fontSize = parseInt(getBestFitTextSize(element_chord))*1.2;
    element_next.style.fontSize = parseInt(getBestFitTextSize(element_next))*1.2;
}

function setLyrics()
{
    element_lyrics.innerHTML = currentChord.lyrics;
    element_lyrics.style.fontSize = getBestFitTextSize(element_lyrics);
}

function clearLyrics()
{
    element_lyrics.innerHTML = "";
}

function setFingering(chordPositionList)
{
    var size = 1.01;
    for(var i=0; i<chordPositionList.length; i++)
    {
        if(chordPositionList[i][2]==null) continue;
        var fingeringElement = document.createElement('span');
        var fret = chordPositionList[i][0];
        var string = chordPositionList[i][1];
        var positionElement = neckPositionElementArray[fret-1][6-string];
        fingeringElement.innerHTML = chordPositionList[i][2];
        fingeringElement.style.fontSize = getBestFitTextSize(positionElement, fingeringElement.innerHTML) * size;
        positionElement.appendChild(fingeringElement);
    }
}

