function stopStrummingAnimation()
{
	playStrummingAnimation = false;
	clearStrummingAnimation();
	clearDontStrummAnimation();
}

function startStrummingAnimation(interval, topString)
{
	playStrummingAnimation = true;
	for(var i=0; i<6-topString; i++)
	{
		stringElements[i].classList.add("dont_strum");
		//stringElements[i].style.animation = "";
	}
	
	if(topString==undefined)
		return;

	strummingAnimation(6-topString, interval, topString)
}

function strummingAnimation(string, interval, topString)
{
	if(!playStrummingAnimation) return;


	if(string==6)
	{
		setTimeout(function()
		{
			clearStrummingAnimation();
			strummingAnimation(6-topString, interval, topString);
		}, interval);
		return;
	}
	//stringElements[string].style.animation = "vibrate 0.1s linear 0s infinite";
	stringElements[string].classList.add("strum");
	setTimeout(function(){
		strummingAnimation(string+1, interval, topString);
	}, interval);
}

function clearStrummingAnimation()
{
	for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
	{
		stringElements[i].classList.remove("strum");
		//stringElements[i].style.animation = "";
	}
}

function clearDontStrummAnimation()
{
	for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
	{
		stringElements[i].classList.remove("dont_strum");
	}
}

