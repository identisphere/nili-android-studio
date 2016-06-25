function displayCurrentChord()
{
    setAllNeckPositionsOff(false);
    setLyrics();
    setChordText();
    setFingering(currentChord.positionList);
    setNeckPositionListOn(currentChord.positionList);
    if(currentChord.emptyStringList.length>0)
    {
        setStringListOn(currentChord.emptyStringList);
        setNeckPositionStringListOff(currentChord.emptyStringList)
    }

    element_chord.style.color = 'red';
}

function setChordText()
{
    setChordElement(currentChord, element_chord);
    if(nextChord!=null)
        setChordElement(nextChord, element_next);
}

function setChordElement(chord, element)
{
    if(isChordTextExplicit(chord.text))
        element.style.opacity = "0";
    else
    {
        element.style.opacity = "1";
        element.innerHTML = chord.text;
    }

    element.style.fontSize = parseInt(getBestFitTextSize(element))*1.2;
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

