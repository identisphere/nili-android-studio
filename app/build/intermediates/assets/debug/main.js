var TOTAL_NUMBER_OF_FRETS = 5;
var TOTAL_NUMBER_OF_STRINGS = 6;

var originalChordElementList = $(".chord");
var originalLyricsElementList = $(".lyrics");
var originalTicksElementList = $(".tick");

var chordList = new Array();
var lyricsList = new Array();
var ticksList = new Array();


$("#title").hide();

var windowWidth = $(window).width();
var windowHeight = $(window).height();

var element_background = document.createElement('img');
var element_title = document.createElement('div');
var element_neck = document.createElement('img');
var element_bottom_stripe = document.createElement('img');
var element_chord = document.createElement('span');
var element_lyrics = document.createElement('span');
var element_logo = document.createElement('img');
var element_next = document.createElement('span');
var element_play = document.createElement('span');
var element_timer = document.createElement('span');

var isTimed = false;
var stringElements = new Array();

var currentChordIndex = 0;

var neckPositions, neckFrets;
var neckPositionElementArray  = new Array()
var neckPositionCorrectElementArray  = new Array()
var neckPositionIncorrectElementArray  = new Array()

var playStrummingAnimation = false;

var isAndroid = navigator.userAgent.toLowerCase().indexOf("android") > -1; //&& ua.indexOf("mobile");

var blinkingList;
var isBlinkOn = false;
var BLINK_INTERVAL = 200;

var isAutoMode;

getChordsLyricsAndTicks();



// 3rd         1st   controls
// A     B     C     D
// 123456123456123456123456
// 000001000001000001000000
// 100001010000000000000000
// 100001000000000000000000



var clickIndex = 0;
var demoIndex = 0;
var demoString = "000000000000000000000000";



function demoFunction()
{
	return;
	receivePositionStringFromAndroid(demoString);
	demoString = demoString.substr(0,demoIndex) + '1' + demoString.substr(demoIndex+1);
	demoIndex++;
	setTimeout(demoFunction, 300);
}

//receivePositionStringFromAndroid("030000c0000c010000000000");
clickIndex = 0;
if(!isAndroid)
{
	document.body.onclick = 
		function()
		{
			if(clickIndex%2==0)
			{
				eventPressedCorrect();
				setNeckPositionListOff([[3,1], [2,5], [3,6]], true);
				setNeckPositionListCorrect([[3,1,3], [2,5,2], [3,6,1]]);
				startStrummingAnimation(200, getChordTopString(getChordText(currentChordIndex)));
				element_chord.style.color = 'green';
				element_lyrics.style.color = 'green';
			} 
			else if (clickIndex%2==1)
			{
				eventLiftFingers();
			} 
			clickIndex++;
		};
}

var demo;
function Demo()
{

	this.startDemo = function()
	{
		setTimeout(function(){demo.action1();}, 1590);
		setTimeout(function(){demo.action2();}, 2610);
		setTimeout(function(){demo.action3();}, 3320);
		setTimeout(function(){demo.action4();}, 4410);
		setTimeout(function(){demo.action5();}, 6200);
	}
	
	this.initialize = function()
	{
		setAllNeckPositionsOff(false);
		element_title.innerHTML = "Reckoning Song -  Asaf Avidan";
		element_title.style.opacity = '1';
		setNeckPositionListOn([[3,1,3], [2,5,2], [3,6,1]]);
		setFingering([[3,1,3], [2,5,1], [3,6,2]]);
	}

	this.action1 = function()
	{
		setNeckPositionOff(3, 1, true);
		setNeckPositionListCorrect([[3,1]]);
	}
	
	this.action2 = function()
	{
		setNeckPositionOff(3, 1, true);
		setNeckPositionOn(3, 1);
	}
	
	this.action3 = function()
	{
		setNeckPositionIncorrect(3, 3);
	}
	
	this.action4 = function()
	{
		setNeckPositionOff(3, 3);
	}
	
	this.action5 = function()
	{
		setNeckPositionListOff([[3,1], [2,5], [3,6]], true);
		setNeckPositionListCorrect([[3,1,3], [2,5,2], [3,6,1]]);
		startStrummingAnimation(200, getChordTopString(getChordText(currentChordIndex)));
		element_chord.style.color = 'green';
		element_lyrics.style.color = 'green';
		
	}


	this.initialize();
}

document.body.onload = function()
{

	loadScript('./js/neckActions.js');
	loadScript('./js/chords.js');
	loadScript('./js/animation.js');
	loadScript('./js/timer.js');
	loadScript('./js/android.js');

	window.setTimeout(initialize, 300);

	// temp
	//window.setTimeout(function(){demo = new Demo();}, 400);
};

function getChordsLyricsAndTicks()
{
	for(var i=0; i<originalChordElementList.length; i++)
	{
		chordList[i] = originalChordElementList[i].innerHTML;
		$(originalChordElementList[i].remove());
	}
	for(var i=0; i<originalLyricsElementList.length; i++)
	{
		lyricsList[i] = originalLyricsElementList[i].innerHTML;
		$(originalLyricsElementList[i].remove());
	}
	for(var i=0; i<originalTicksElementList.length; i++)
	{
		ticksList[i] = originalTicksElementList[i].innerHTML;
		$(originalTicksElementList[i].remove());
	}
}

function initialize()
{
	createElements();
	createTable();
	createStrings()
	createCorrectIncorrectTable();
	blink();


	sendMessageToAndroid("start_chords");
	for(var i=0; i<chordList.length; i++)
	{
		sendChordToAndroid(currentChordIndex);
		currentChordIndex++;
	}
	sendMessageToAndroid("end_chords");

	/*
	sendMessageToAndroid("start_ticks");
	for(var i=0; i<ticksList.length; i++)
	{
		sendMessageToAndroid("addTick_"+ticksList[i]);
		currentChordIndex++;
	}

	sendMessageToAndroid("end_ticks");
	 */
	currentChordIndex = 0;

	setTimerIncorrect();
	setTimerHidden();
	
	if(chordList.length!=0)
	{
		displayCurrentChord();
	}
}

var lastEventTime = new Date().getTime();
function checkEventIsReal()
{
	var newEventTime = new Date().getTime();  
	if((newEventTime - lastEventTime) < 250)
	{
		lastEventTime = newEventTime;
		return false;;
	}
	lastEventTime = newEventTime;
	return true;
}

function sendChordToAndroid(index)
{
	var currentChordToAndroid = getPositionListOfChordText(chordList[index]);
	if(currentChordToAndroid == null) return;
	currentChordToAndroid = convertPositionListToString(currentChordToAndroid);

	sendMessageToAndroid("addChord_"+currentChordToAndroid.toString());
}

function convertPositionListToString(positionList)
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

function updateBlinkingList(newBlinkingList)
{
	if(blinkingList==null)
	{
		blinkingList = newBlinkingList;
		return;
	}
	blinkingList = newBlinkingList;
}

function blink()
{
	if(blinkingList==null)
	{
		setTimeout(function(){blink();}, BLINK_INTERVAL);
		return;
	}
	if(isBlinkOn)
	{
		setNeckPositionListOff(blinkingList);
		isBlinkOn = false;
	}
	else
	{
		setNeckPositionListOn(blinkingList);
		isBlinkOn = true;
	}
	
	setTimeout(function(){blink();}, BLINK_INTERVAL)
}

function moveToNextChord()
{
	if(currentChordIndex+1>=chordList.length) return;

	currentChordIndex++;
}

function moveToPreviousChord()
{
	if(currentChordIndex<=0) return;

	currentChordIndex--;
}

function displayCurrentChord()
{
	setAllNeckPositionsOff(false);
	setLyrics();
	setChordText();
	setFingering(getPositionListOfChordText(getChordText(currentChordIndex)));
	setNeckPositionListOn(getPositionListOfChordText(getChordText(currentChordIndex)));
	
	element_chord.style.color = 'red';
}

function setChordText()
{
	if(getChordText(currentChordIndex).indexOf('[')!=-1)
	{
		element_chord.innerHTML = "--";
		return;
	}
	element_chord.innerHTML = getChordText(currentChordIndex);
	
	if(currentChordIndex+1<chordList.length)
		element_next.innerHTML = getChordText(currentChordIndex+1);
	else
		element_next.innerHTML = "";

	element_chord.style.fontSize = parseInt(getBestFitTextSize(element_chord))*1.2;
	element_next.style.fontSize = parseInt(getBestFitTextSize(element_next))*1.2;
}

function setLyrics()
{
	element_lyrics.innerHTML = lyricsList[currentChordIndex];
	element_lyrics.style.fontSize = getBestFitTextSize(element_lyrics);

	element_title.innerHTML = lyricsList[currentChordIndex+1];
	element_title.style.fontSize = getBestFitTextSize(element_title);

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

function createTable()
{
	neckPositionElementArray = new Array(TOTAL_NUMBER_OF_FRETS);

	for(var i=0; i<TOTAL_NUMBER_OF_FRETS; i++)
	{
		neckPositionElementArray[i] = new Array();
	}
	
	for(var j=0; j<TOTAL_NUMBER_OF_FRETS; j++)
	{
		var element_neck_fret = document.createElement('table');
		element_neck_fret.className = "fret_table";
		for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
		{
			element_neck_fret.appendChild(document.createElement('tr'));
			element_neck_fret.rows[i].appendChild(document.createElement('td'));
			element_neck_fret.rows[i].cells[0].style.width = windowWidth*0.1;
			element_neck_fret.rows[i].cells[0].style.height = windowHeight*0.0515;
			element_neck_fret.rows[i].cells[0].className = "position_td";

			var backgroundColor = 'white';
			// backgroundColor = "#fffe79";
			// backgroundColor = "#ff7979";

			element_neck_fret.rows[i].cells[0].style.backgroundColor = backgroundColor;
			element_neck_fret.rows[i].cells[0].style.boxShadow = "0px 0px 10px 3px "+ backgroundColor;
			
			element_neck_fret.rows[i].cells[0].style.fontSize = $(element_neck_fret.rows[i].cells[0]).height()*2 + "px";
			element_neck_fret.rows[i].cells[0].style.lineHeight = $(element_neck_fret.rows[i].cells[0]).height()*0.01+ "px";
			
			neckPositionElementArray[j][i] = element_neck_fret.rows[i].cells[0];
			
			//element_neck_fret.rows[i].cells[0].innerHTML = j;
			
			/*element_neck_fret.rows[i].cells[0].style.backgroundColor = randomColor();
			element_neck_fret.rows[i].cells[0].innerHTML = j+","+i;*/
		}
	
		document.body.appendChild(element_neck_fret);
		element_neck_fret.style.zIndex = 1;
		element_neck_fret.style.borderSpacing = windowHeight*0.018;
	}

	var neckTop = $(element_neck).position().top;
	var neckHeight = $(element_neck).height();
	var neckWidth = $(element_neck).width();
	for(var i=0; i<TOTAL_NUMBER_OF_FRETS; i++)
	{
		$(".fret_table")[i].style.left = ($($(".fret_table")[0]).width()/3)*(i+1)+(neckWidth/6)*i;
		$(".fret_table")[i].style.top = neckTop;
	}
}

function createCorrectIncorrectTable()
{
	neckPositionCorrectElementArray = new Array();
	for(var i=0; i<TOTAL_NUMBER_OF_FRETS; i++)
	{
		neckPositionCorrectElementArray[i] = new Array()
		for(var j=0; j<TOTAL_NUMBER_OF_STRINGS; j++)
		{
			var correctImage = document.createElement('img');
			correctImage.src = './pics/v.png';
			correctImage.style.height = neckPositionElementArray[i][j].style.height;
			correctImage.style.width = neckPositionElementArray[i][j].style.width;
			correctImage.style.position = 'absolute';
			correctImage.style.left = $(neckPositionElementArray[i][j]).offset().left;
			correctImage.style.top = $(neckPositionElementArray[i][j]).offset().top;
			neckPositionCorrectElementArray[i][j] = correctImage; 
			document.body.appendChild(correctImage);
			neckPositionCorrectElementArray[i][j].style.opacity = '0';
		}
	}

	neckPositionIncorrectElementArray = new Array();
	for(var i=0; i<TOTAL_NUMBER_OF_FRETS; i++)
	{
		neckPositionIncorrectElementArray[i] = new Array()
		for(var j=0; j<TOTAL_NUMBER_OF_STRINGS; j++)
		{
			var correctImage = document.createElement('img');
			correctImage.src = './pics/x.png';
			correctImage.style.height = neckPositionElementArray[i][j].style.height;
			correctImage.style.width = neckPositionElementArray[i][j].style.width;
			correctImage.style.position = 'absolute';
			correctImage.style.left = $(neckPositionElementArray[i][j]).offset().left;
			correctImage.style.top = $(neckPositionElementArray[i][j]).offset().top;
			neckPositionIncorrectElementArray[i][j] = correctImage; 
			document.body.appendChild(correctImage);
			neckPositionIncorrectElementArray[i][j].style.opacity = '0';
		}
	}
}

function createStrings()
{
	var fretTable = $(".fret_table")[0];
	var stringsContainerElement = document.createElement('div');
	stringsContainerElement.style.position = 'absolute';
	stringsContainerElement.style.top = $(fretTable.rows[0]).offset().top;
	stringsContainerElement.style.left = 0;
	stringsContainerElement.style.width = $(window).width();
	
	document.body.appendChild(stringsContainerElement);
	for(var i=0; i<TOTAL_NUMBER_OF_STRINGS; i++)
	{
		stringElements[i] = document.createElement('div');
		stringElements[i].style.position = 'relative';

		stringElements[i].style.width = $(window).width();
		stringElements[i].style.height = $(fretTable.rows[0]).height()/9;
		if(i==0)
			$(stringElements[i]).css("margin-top", $(fretTable).height()/20);
		else
			$(stringElements[i]).css("margin-top", $(fretTable).height()/7);
		stringElements[i].style.zIndex = 1;
		stringElements[i].className = "string";
		stringElements[i].style.boxShadow = "0px 5px 10px 3px black";
		stringsContainerElement.appendChild(stringElements[i]);
	}
	
}

function randomColor()
{
	return Math.floor(Math.random()*16777215).toString(16);
}


function createElements()
{
	element_background.src = "./pics/back.png";
	document.body.appendChild(element_background);
	setPosition(element_background, 0, 0, 1, 1);

	element_title.innerHTML = document.getElementById('title').innerHTML;
	document.body.appendChild(element_title);
	setPosition(element_title, 0.25, 0.047, 0.48, 0.16);
	element_title.className = "text next";

	element_neck.src = "./pics/neck.png";
	document.body.appendChild(element_neck);
	setPosition(element_neck, 0, 0.22, 1, 0.42);
	element_neck.style.boxShadow = "0px 5px 3px black";

	element_bottom_stripe.src = "./pics/bottom_stripe.png";
	document.body.appendChild(element_bottom_stripe);
	setPosition(element_bottom_stripe, 0, 0.7, 1, 0.22);

	document.body.appendChild(element_chord);
	//element_chord.innerHTML = $(originalChordElementList)[currentChordIndex].innerHTML;
	setPosition(element_chord, 0.04, 0.64, 0.16, 0.28, 1.3);
	element_chord.className = "chord";

	document.body.appendChild(element_lyrics);
	//element_lyrics.innerHTML = $(originalLyricsElementList)[currentChordIndex].innerHTML;
	setPosition(element_lyrics, 0.2, 0.67, 0.79, 0.2);
	element_lyrics.className = "text";

	element_logo.src = "./pics/logo.png";
	document.body.appendChild(element_logo);
	setPosition(element_logo, 0.74, 0.047, 0.2, 0.15);

	document.body.appendChild(element_next);
	//element_next.innerHTML = $(originalChordElementList)[currentChordIndex+1].innerHTML;
	setPosition(element_next, 0.1, 0.03, 0.1, 0.15, 1.3);
	element_next.className = "next chord";
	
	element_timer.className = 'timer';
	document.body.appendChild(element_timer);
	setPosition(element_timer, 0.5, 0.1, 0.4, 0.4)
	element_timer.style.left = windowWidth/2 - $(element_timer).width()/2;
	element_timer.style.visibility = 'hidden';

	element_timer.style.visibility = '';
	element_timer.innerHTML = '1';

	/*
	 * PLAY 
	document.body.appendChild(element_play);
	element_play.innerHTML = "PLAY";
	setPosition(element_play, 0.5, 0.5, 0.7, 0.7);
	element_play.style.left = parseInt(element_play.style.left) - $(element_play).width()/2;
	element_play.style.top = parseInt(element_play.style.top) - $(element_play).height()/2;
	element_play.className = "play";
	*/
}

function getBestFitTextSize(element, optional_text)
{
	var returnedFontSize, currentFontSize = 5;
	var tryElement = document.createElement('span');
	if(optional_text==null)	tryElement.innerHTML = element.innerHTML;
	else					tryElement.innerHTML = optional_text;
	
	tryElement.style.fontSize = currentFontSize + "px";
	document.body.appendChild(tryElement);
	
	var error = 0;
	while($(tryElement).width()<$(element).width() && $(tryElement).height()<$(element).height())
	{
		if(error>100) break;
		error++;
		currentFontSize = currentFontSize + 5; 
		tryElement.style.fontSize = currentFontSize + "px";
	}
	
	returnedFontSize = tryElement.style.fontSize; 
	document.body.removeChild(tryElement);
	return returnedFontSize;
}

function setPosition(element, x, y, width, height)
{
	if(y!=null)
		element.style.left = x*windowWidth;
	if(x!=null)
		element.style.top = y*windowHeight;
	if(width!=null)
		element.style.width = (width*windowWidth);
	if(height!=null)
		element.style.height = (height*windowHeight);

	element.style.fontSize = getBestFitTextSize(element);
	
	//element.style.backgroundColor = randomColor();
	//element.style.border = "1px solid";
}

function loadScript(url, callback)
{
    // Adding the script tag to the head as suggested before
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    // Then bind the event to the callback function.
    // There are several events for cross browser compatibility.
    script.onreadystatechange = callback;
    script.onload = callback;

    // Fire the loading
    head.appendChild(script);
}