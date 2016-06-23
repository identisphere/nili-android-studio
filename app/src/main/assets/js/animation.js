function stopStrummingAnimation()
{
	playStrummingAnimation = false;
	clearStrummingAnimation();
	clearDontStrummAnimation();
}

function startStrummingAnimation(interval, topString)
{
	playStrummingAnimation = true;
	clearStrummingAnimation();
	clearDontStrummAnimation();
	
	if(topString==undefined)
		return;

	strummingAnimation(topString, interval, topString)
}

function strummingAnimation(string, interval, topString)
{
	if(!playStrummingAnimation) return;


	if(string==0)
	{
		clearStrummingAnimation();
		setTimeout(function()
		{
			strummingAnimation(topString, interval, topString);
		}, interval);
		return;
	}
	setStringOn(string);
	setTimeout(function(){
		strummingAnimation(string-1, interval, topString);
	}, interval);
}

function clearStrummingAnimation()
{
	for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
	{
		setStringOff(i)
	}
}

function clearDontStrummAnimation()
{
	for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
	{
		setStringOff(i);
	}
}

