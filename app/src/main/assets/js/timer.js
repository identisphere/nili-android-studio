function setTimerCorrect()
{
	element_timer.style.color = "green";
}

function setTimerIncorrect()
{
	element_timer.style.color = "red";
}

function setTimerStart()
{
	element_timer.innerHTML = ticksList[currentChordIndex];	
}

function setTimer(value)
{
	element_timer.innerHTML = value;	
}

function setTimerVisible(tick)
{
	element_timer.style.visibility = '';
	setTimer(tick)
}

function setTimerHidden()
{
	element_timer.style.visibility = 'hidden';
}

