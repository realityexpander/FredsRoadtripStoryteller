package data.loadMarkers.sampleData

// This is a sample of a full page of HTML from the HMDB website.
// This page represents the first page of a multiple page search result for markers near a specific location.
fun sunnyvaleMarkersPage1Html() = """
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="windows-1252">
        <meta name=viewport content="width=device-width, initial-scale=1">
        <link rel=stylesheet type="text/css" href="styles13.css">
        <link rel="P3Pv1" href="https://www.hmdb.org/w3c/p3p.xml">
        <link rel="alternate" type="application/rss+xml" title="HMDB New Markers" href="https://www.hmdb.org/rss/">
        <meta http-equiv="content-language” content=”en-US”>
<meta name=" msapplication-config " content=" none "/>

  <META NAME=" keywords " CONTENT=" public history, local history, historical markers, roadside historical markers, historical signs, monopole markers, commemorative plaques, mapped history, historical location ">
<meta name=" twitter:card " content=" summary ">
<meta name=" twitter:site " content=" @HMdbEditors ">
 
<link rel=" canonical " href=" https://www.hmdb.org/results.asp?Search=Coord&Latitude= 37.422160&Longitude=-122.084270&Miles= 10&MilesType=1&HistMark= Y&WarMem=Y&FilterNOT= &FilterTown=&FilterCounty= &FilterState=&FilterCountry=">
<meta property=" og:url " content=" https://www.hmdb.org/results.asp?Search=Coord&Latitude= 37.422160&Longitude=-122.084270&Miles= 10&MilesType=1&HistMark= Y&WarMem=Y&FilterNOT= &FilterTown=&FilterCounty= &FilterState=&FilterCountry=" />
<meta name=" twitter:url " content=" https://www.hmdb.org/results.asp?Search=Coord&Latitude= 37.422160&Longitude=-122.084270&Miles= 10&MilesType=1&HistMark= Y&WarMem=Y&FilterNOT= &FilterTown=&FilterCounty= &FilterState=&FilterCountry=">
  
<title>Near 37.422160 -122.084270</title>
<meta property=" og:title " content=" Near 37.422160 -122.084270 " />
<meta name=" twitter:title " content=" Near 37.422160 -122.084270 ">


<meta name=" description " content=" A list of Historical Markers and War Memorials about .">
<meta property=" og:type " content=" article " />
<meta property=" og:description " content=" A list of Historical Markers and War Memorials about ." />
<meta name=" twitter:description " content=" A list of Historical Markers and War Memorials about .">

<meta name=" twitter:image " content=" https://www.hmdb.org/WashingtonSleptHere.jpg ">


<!-- Global site tag (gtag.js) - Google Analytics -->
<script defer src=" https://www.googletagmanager.com/gtag/js?id=G-CHJ9B6H73H "></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', 'G-CHJ9B6H73H');
</script>

<!-- Google Tag Manager -->
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-PFFLKZJ');</script>
<!-- End Google Tag Manager -->




</head>

<body onbeforeprint=" beforePrint()" onload=''>

<!-- Google Tag Manager (noscript) -->
<noscript><iframe src=" https://www.googletagmanager.com/ns.html?id=GTM-PFFLKZJ "
height=" 0 " width=" 0 " style=" display:none;visibility:hidden "></iframe></noscript>
<!-- End Google Tag Manager (noscript) -->

<div class=entirebody style='font-size:116%;'>
<header>
  <div class=masthead>
    <table><tr>
      <td class=logo1>
        <a href='../'><img class=logoimg alt='Marker Logo' src='Marker.png' height=35></a>
      </td><td class=logo2>
        <a href='../'>HMdb.org</a>
      </td><td class=logo3>
        <b>THE HISTORICAL<br>MARKER DATABASE</b>
      </td><td class=logo4a>
        &#8220;Bite-Size Bits of Local, National, and Global History&#8221;
      </td>
    </tr></table>
    <div class=logo4b>&#8220;Bite-Size Bits of Local, National, and Global History&#8221;</div>
  </div> <!-- masthead -->

	<div class=menu>
	 <div id=menunorm1 class=menunorm>
	  <div class=menu2> <!-- search form and button -->
      <form method=get action=results.asp onsubmit='searchButton.disabled=true;searchButton.value=" &nbsp;&nbsp;Wait&nbsp;&nbsp;"; document.body.style.cursor = " progress ";'>
      <input type=hidden name=Search value=KeywordA>
      <input type=text style='font-family:sans-serif; width:110px;' Name=SearchFor placeholder='word or phrase' value=''>
      <input style='font-family:sans-serif;' id=searchButton type=submit Value=Search title='Keyword search; use SEARCHES for other kinds'>
      </form>
	  </div> <!-- menu2 -->
	  <div class=menu1> <!-- right half -->
		<span class=button title='Home page'><a href='../'><big><big>&#8962;</big></big></a></span>
		<a href='mymarkers.asp'><span class=button title='Your lists'>My&nbsp;Markers</span></a>
		<a href='markeraddfirst.asp'><span class=button title='Upload photos and fill out a form'>Add&nbsp;A&nbsp;Marker</span></a> 
		<a href=forum.asp><span class=button title='Discussions on historical markers'>Forum</span></a> 
		<a href='faq.asp'><span class=button title='Frequently Asked Questions'>FAQ</span></a> 
		<a href=about.asp><span class=button title='Our mastshead and notes from the publisher'>About&nbsp;Us</span></a>
		<a href='https://www.bonfire.com/store/hmdb-org/' target=_blank><span class=button title='Merchandise Store'>Merch</span></a>
	  </div> <!-- menu1 -->
	 </div> <!-- menunorm -->
	 <div id=menunorm2 class=menunorm>
	  <div class=menu3>  <!-- left half -->
		<span id=BulletsButtonTop><a href='m.asp?m=random'><span class=button title=" Show a random entry "><big>&starf;</big></span></a></span>
		<span id=BulletsButtonBottom class=BulletsButtonBottom><a href='m.asp?m=random'><span class=button title=" Show a random entry "><big>&starf;</big></span></a></span>
		<a href='nearby.asp' title='Entries near your present location'><span class=button style=" background-color:var(--hmdb-purple-darkest);">Near&nbsp;You</span></a>
		<a href='wantedmarkers.asp'><span class=button title='Markers we do not have but know where they are'>Want&nbsp;Lists</span></a>
		<a href='series.asp'><span class=button title='Lists of marker series'>Series</span></a> 
		<a href='categories.asp'><span class=button title='Entries categorized by topic'>Topics</span></a>
		<a href='geolists.asp'><span class=button title='A menu of countries, states, counties, cities and towns'>Locations</span></a>
		<a href=search.asp><span class=button title='Other ways to search'>Searches</span></a>
	  </div> <!-- menu3 -->
	 </div> <!-- menunorm -->

	 <div id=menualt class=menualt>
	  <div class=menu2> <!-- search form and button -->
      <form method=get action=results.asp onsubmit='searchButton2.disabled=true;searchButton2.value=" &nbsp;&nbsp;Wait&nbsp;&nbsp;";'>
      <input type=hidden name=Search value=KeywordA>
		<input type=text style='font-family:sans-serif;  width:80px;' Name=SearchFor value=''>
		<input style='font-family:sans-serif;' id=searchButton2 type=submit Value=Search title='Keyword search'>
		</form>
	  </div> <!-- menu2 -->
	  <div class=menu1> <!-- right half -->
		<a onclick=" menunorm1.style.display='block' ; menunorm2.style.display='block' ; menualt.style.display='none' ; homebutton.style.display='none' ;"><span class=button style='color:black;background-color:white;' title='Menu'>&#9776;</span></a>

		<a href='nearby.asp' title='Entries near your present location'><span class=button style=" background-color:blue;">Near&nbsp;You</span></a>

	  </div> <!-- menu1 -->
	 </div> <!-- menunormal -->
	 
	</div> <!-- menu -->

</header>

<script>
   let isPlaying = false;
function speak (message,button,lang,iteration) {
   isPlaying= true; 
   window.speechSynthesis.cancel();
   if (String(lang)==='en') {lang='en-US'};
   
         var voices = speechSynthesis.getVoices();
         if (voices.length===0 && iteration<5) {setTimeout(() => {  speak(message,lang,iteration+1); }, 100); return;}

         var voice=null;
         for (let i=0; i < voices.length; i++) {
              if (voices[i].lang==String(lang)) {var voice=voices[i]; break;};
         };

         if (voice===null) {for (let i=0; i < voices.length; i++) {
              if (voices[i].lang.substr(0,2)==String(lang).substr(0,2)) {var voice=voices[i];};
         }};
         if (voice===null) {alert('At this moment we can’t find an english voice in your system. Wait a few seconds and try again.'); return;};
         console.log(" Voice=" + voice.name + " voiceLang=" + voice.lang + " iteration=" + iteration);
        
// chop up into sentences because speechSynthesis can't handle long text.
    message=message+' [|].';
    m = message.match(/([^\.!\?]+[\.!\?]+)|([^\.!\?]+$)/g);
    for (let chunk of m) {
         var msg = new SpeechSynthesisUtterance(chunk.replace(" [|].","")); 
         msg.voice=voice;
         msg.lang=lang ;
         msg.rate=1;
         window.speechSynthesis.speak(msg);  
    }      

// chrome (desktop only) stops after 15 seconds so this is workaround
var isChromium = window.chrome;
var winNav = window.navigator;
var vendorName = winNav.vendor;
var isOpera = typeof window.opr !== " undefined ";
var isIEedge = winNav.userAgent.indexOf(" Edg ") > -1;
var isIOSChrome = winNav.userAgent.match(" CriOS ");
var ua = navigator.userAgent.toLowerCase();
var isAndroid = ua.indexOf(" android ") > -1; //&& ua.indexOf(" mobile "); 

if (isIOSChrome) {
   // is Google Chrome on IOS
} else if(
  isChromium !== null &&
  typeof isChromium !== " undefined " &&
  vendorName === " Google Inc." &&
  isOpera === false &&
  isIEedge === false &&
  isAndroid === false
  
) {
   // it is Google Chrome
       var synthesisInterval = setInterval(() => {
            if (!isPlaying) { 
                clearInterval(synthesisInterval);
            } else {
                window.speechSynthesis.pause();
                window.speechSynthesis.resume();
            } 
        }, 13000);  
      };
  document.getElementById('speakbutton'+button).setAttribute('onClick', 'stopSpeaking('+button + ',"'+ lang + '")');
  document.getElementById('speakbutton'+button).innerHTML='<img src=SpeakStopIcon.png title=\'Click to stop speech.\'>';
}  

function stopSpeaking(button,lang) {
  window.speechSynthesis.cancel();
  isPlaying=false;
  document.getElementById('speakbutton'+button).setAttribute('onClick', " speak(document.getElementById('inscription"+button+"' ).innerText," + button + " ,'" + lang + "' ,0);");
  document.getElementById('speakbutton'+button).innerHTML='<img src=SpeakIcon.png title=\'Click to hear the inscription.\'>';  
}
</script>


<div id=mainblock class=mainblock style='min-height:750px;'> <!-- Main division-->
<div id=toplinks><div class=toplinks style='text-align:left;'><div id=firstArrow class=arrownext><a href='m.asp?m=113017'><span class=arrowtext>FIRST</span>&nbsp;&#9658;</a></div><div class=articletoplinks style='float:right;'><i>FIRST browses through these results.</i></div>
<div class=articletoplinks style='text-align:left;margin-left:10px; font-size:80%'><div id=FurtherFilter class=framedblock style='visibility:hidden; position:absolute; text-align:left; max-width:350px; margin-top: -17px; margin=bottom:-4px; margin-left:22px; margin-right:25px; background-color:white; border: 3px solid;padding:8px; font-size:106%'><input type=submit onclick='FurtherFilter.style.visibility=" hidden "; return false;' value='&#10006;' style='font-family:sans-serif; float:right;'>
<div><b><i>Apply These Filters</i></b></div>
<div class=shim6pt>&nbsp;</div>
<div class=instructions>These filters will replace previously applied filters.</div>
<div class=shim6pt>&nbsp;</div>

<form method=get action=results.asp onsubmit='var searchbuttons = document.querySelectorAll(" input[type=\"submit\" ]"); for (var i = 0; i < searchbuttons.length; i++) {searchbuttons[i].disabled=true};'>

<fieldset style='border-color:blue;border-width:1pt;'>
<legend style='color:blue;text-align:left;'>Filters</legend>
<div style=" text-align:left;">
<table><tr><td>Include</td><td><input style='font-family:sans-serif;'  type=Checkbox id=Filter1h Name=HistMark value='Y' 
 CHECKED 
>Historical Markers 
</td></tr><tr><td>Include</td><td><input style='font-family:sans-serif;'  type=Checkbox id=Filter1h Name=WarMem value='Y' 
 CHECKED 
>War Memorials </td></td></table>
<hr style='background-color:blue; height:1px; border:0; margin-left:-6px; margin-right:-6px;'>
<table><tr><td>Include</td><td><input style='font-family:sans-serif;'  type=radio id=Filter1g Name=FilterNOT value='' 
 CHECKED 
>only matches for ...
</td></tr><tr><td></td><td><input style='font-family:sans-serif;'  type=radio id=Filter1g Name=FilterNOT value='NOT' 

>all except for ...</td></td></table>


<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1a Name=FilterTown placeholder='[no city, town or place filter]' value='' Size=24></div>
<div class=instructions>&nbsp;</div>
<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1b Name=FilterCounty placeholder='[no county or parish filter]' value='' Size=24></div>
<div class=instructions>&#9650;You may omit the word " County " but not " Parish "</div>

<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1c Name=FilterState placeholder='[no state or province filter]' value='' Size=24></div>
<div class=instructions>&#9650;You may use the postal abbreviation</div>

<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1d Name=FilterZip placeholder='[no zip or postal code filter]' value='' Size=24></div>
<div class=instructions>&nbsp;</div>
<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1e Name=FilterCountry placeholder='[no country filter]' value='' Size=24></div>
<div class=instructions>&nbsp;</div>
<div><select id=Filter1f name=FilterCategory>
<Option Value=0>
[no second topic filter]<Option Value='78'>9/11 Attacks<Option Value='44'>Abolition & Underground RR<Option Value='63'>African Americans<Option Value='1'>Agriculture<Option Value='51'>Air & Space<Option Value='-1'>All African American Topics<Option Value='-2'>All Transportation Topics<Option Value='52'>Animals<Option Value='62'>Anthropology & Archaeology<Option Value='73'>Architecture<Option Value='28'>Arts, Letters, Music<Option Value='70'>Asian Americans<Option Value='50'>Bridges & Viaducts<Option Value='20'>Cemeteries & Burial Sites<Option Value='57'>Charity & Public Work<Option Value='3'>Churches & Religion<Option Value='29'>Civil Rights<Option Value='4'>Colonial Era<Option Value='13'>Communications<Option Value='61'>Disasters<Option Value='7'>Education<Option Value='31'>Entertainment<Option Value='53'>Environment<Option Value='56'>Exploration<Option Value='6'>Forts and Castles<Option Value='37'>Fraternal or Sororal Organizations<Option Value='67'>Government & Politics<Option Value='55'>Heroes<Option Value='69'>Hispanic Americans<Option Value='36'>Horticulture & Forestry<Option Value='79'>Immigration<Option Value='8'>Industry & Commerce<Option Value='45'>Labor Unions<Option Value='34'>Landmarks<Option Value='76'>Law Enforcement<Option Value='40'>Man-Made Features<Option Value='32'>Military<Option Value='-3'>Military & All War Topics<Option Value='2'>Native Americans<Option Value='9'>Natural Features<Option Value='39'>Natural Resources<Option Value='5'>Notable Buildings<Option Value='10'>Notable Events<Option Value='66'>Notable Places<Option Value='72'>Paleontology<Option Value='75'>Parks & Recreational Areas<Option Value='43'>Patriots & Patriotism<Option Value='38'>Peace<Option Value='22'>Political Subdivisions<Option Value='26'>Railroads & Streetcars<Option Value='-10'>Reserved10<Option Value='-4'>Reserved4<Option Value='-5'>Reserved5<Option Value='-6'>Reserved6<Option Value='-7'>Reserved7<Option Value='-8'>Reserved8<Option Value='-9'>Reserved9<Option Value='48'>Roads & Vehicles<Option Value='41'>Science & Medicine<Option Value='12'>Settlements & Settlers<Option Value='46'>Sports<Option Value='14'>War of 1812<Option Value='68'>War, 1st Iraq & Desert Storm<Option Value='58'>War, 2nd Iraq<Option Value='59'>War, Afghanistan<Option Value='19'>War, Cold<Option Value='23'>War, French and Indian<Option Value='27'>War, Korean<Option Value='47'>War, Mexican-American<Option Value='30'>War, Spanish-American<Option Value='64'>War, Texas Independence<Option Value='15'>War, US Civil<Option Value='16'>War, US Revolutionary<Option Value='35'>War, Vietnam<Option Value='17'>War, World I<Option Value='18'>War, World II<Option Value='54'>Wars, Non-US<Option Value='60'>Wars, US Indian<Option Value='49'>Waterways & Vessels<Option Value='74'>Women
</select></div>
<div class=instructions>&nbsp;</div>
<div><input style='font-family:sans-serif;font-weight:bold;'  type=text id=Filter1g Name=FilterString placeholder='[no inscription filter]' value='' Size=24 ></div>
<div class=instructions>&#9650;A string of characters in the inscription</div>
</div >
</fieldset>



<center>
<input style='font-family:sans-serif;'  id=TheButton1 type=submit Value=Apply>
</center>
<input type='hidden' name='Search' value=" Coord "><input type='hidden' name='Longitude' value=" -122.084270 "><input type='hidden' name='Latitude' value=" 37.422160 "><input type='hidden' name='Miles' value=" 10 "><input type='hidden' name='MilesType' value=" 1 "></form></div><span style='white-space: nowrap;'><a href=# onclick='FurtherFilter.style.visibility=" visible "; return false;'><b>Adjust&nbsp;Filters</b></a>  &#8212 <a href=/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&DP=.A>Show&nbsp;Directions</a> &#8212 <a href=/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&DP=.O>Omit&nbsp;Inscription</a> </span></div> <!-- articletoplinks -->
</div> <!-- toplinks -->
</div> <!-- toplinks -->
<article class=bodysansserif><div class=noprint id=ClickToMap style='float:left; width:55px; margin-right:15px;margin-top:-3px;margin-left:-6px; text-align:center;'> &nbsp;<br>&nbsp;<br>&nbsp;</div><div class=noprint id=ClickToDownload style='float:right; width:55px; margin-left:15px;margin-top:-15px;text-align:center;'> &nbsp;<br>&nbsp;<br>&nbsp;</div><div style='text-align:center;'><i>239 entries match your criteria. The first 100 are listed. <span class=noprint>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  <a href='/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&Page=2' style='background-color:yellow;'>Next&nbsp;100&nbsp;</i>&#8883;<i></a></span></i></div><div class=onlyprint><br>&nbsp;</div><div class=shim6pt>&nbsp;</div><h2 class=gray style='margin-left:65px;margin-right:65px;'>Near 37.422160 -122.084270</h2><center> Entries within approximately 10 square miles roughly centered on these coordinates.</center><div class=shim12pt>&nbsp;</div><div id=TheListItself>

<div class=photoright><div style='max-width:400px; margin-left: auto; margin-right: auto;'><a href='m.asp?m=113017'><img class=photoimage src=Photos4/413/Photo413301.jpg alt='Early People of the Creek Marker image, Touch for more information'></a><div class=imagecredit>By Douglass Halvorsen,  January 1, 2015</div> <!-- imagecredit --> <div class=imagecaption>Early People of the Creek Marker</div></div></div>

 <div style='cursor:pointer'><select id=theSort onchange='window.location.assign(" results.asp?Search=Coord&Latitude= 37.422160&Longitude=-122.084270&Miles= 10&MilesType=1&HistMark= Y&WarMem=Y&FilterNOT= &FilterTown=&FilterCounty= &FilterState=&FilterCountry= &FilterCategory=0&Sort="+theSort.options[theSort.selectedIndex].value);' style='color:darkorange; line-height:90%; font-weight: bold; font-style: italic; border:none; margin-bottom:-9pt'><option value=geo>GEOGRAPHIC SORT</option><option value='title'>Title Sort</option><option value='num'>Marker Number Sort</option><option value='numN'>Marker Number Sort N</option><option value='numT'>Marker Number Sort T</option><option value='pub'>Publication Order Sort </option><option value='pubA'>Publication Order Sort A</option><option value='erect'>Erected Year Sort</option><option value='chg'>Most Recently Changed Sort</option></select></div>
<table id=M113017><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>1</small></i> </font>&#9658; <a Name=113017>California, San Mateo County, Menlo Park &#8212;  <a href='../m.asp?m=113017'>Early People of the Creek</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.44765,-122.170317 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>As the largest local watercourse, San Francisquito Creek played a major role in the lives of native Americans, Spanish explorers and early Anglo settlers of the area.  

For perhaps 7,000 years, the native peoples -- called <i>Costanos</i> by  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=113017 " target=_blank>Map</a><small> (db&nbsp;m113017)</small> HM</span></td></tr></table>
<table id=M2483><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>2</small></i> </font>&#9658; <a Name=2483>California, San Mateo County, Menlo Park &#8212; 955 &#8212;  <a href='../m.asp?m=2483'>Menlo Park Railroad Station</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.4549,-122.1826 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This building, constructed in 1867 by the San Francisco and San Jose Railroad Company, is the oldest railroad passenger station in California. The Victorian ornamentation was added in the 1890s when the station was remodeled to serve the  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=2483 " target=_blank>Map</a><small> (db&nbsp;m2483)</small> HM</span></td></tr></table>
<table id=M25054><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>3</small></i> </font>&#9658; <a Name=25054>California, San Mateo County, Menlo Park &#8212; 2 &#8212;  <a href='../m.asp?m=25054'>Portola Journey's End</a> — <i>November 6-10, 1769</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.44785973,-122.17095539 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Near " El Palo Alto ", the tall tree, the Portola Expedition of 63 men and 200 horses and mules camped. They had traveled from San Diego in search of Monterey but discovered instead the Bay of San Francisco. Finding the bay too large to go around and  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=25054 " target=_blank>Map</a><small> (db&nbsp;m25054)</small> HM</span></td></tr></table>
<table id=M41337><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>4</small></i> </font>&#9658; <a Name=41337>California, San Mateo County, Menlo Park &#8212;  <a href='../m.asp?m=41337'>San Francisquito Creek Watershed</a> — <i>1891 to World War II</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.44730394,-122.17005014 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>After Stanford University opened in 1891, business increased in nearby Mayfield and Menlo Park, towns dating from the 1850s. Mayfield was a farming center, and Menlo Park a summer home to San Franciscans. Residents of both places rejected Senator  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=41337 " target=_blank>Map</a><small> (db&nbsp;m41337)</small> HM</span></td></tr></table>
<table id=M41353><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>5</small></i> </font>&#9658; <a Name=41353>California, San Mateo County, Menlo Park &#8212;  <a href='../m.asp?m=41353'>San Francisquito Creek Watershed</a> — <i>World War II to Century 21</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.44721451,-122.17001259 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>By the time America entered World War II in 1941, the creek had been straightened at its outlet to the Bay near the Palo Alto Municipal Airport. The Army built Dipple General Hospital on land near the creek in Menlo Park. Later, Stanford Research  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=41353 " target=_blank>Map</a><small> (db&nbsp;m41353)</small> HM</span></td></tr></table>
<table id=M11992><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>6</small></i> </font>&#9658; <a Name=11992>California, San Mateo County, Portola Valley &#8212; 825 &#8212;  <a href='../m.asp?m=11992'>Casa de Tableta</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.38222,-122.19278 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This structure, built by Felix Buelna in the 1850s, served as a gambling retreat and meeting place for Mexican-Californios. It was strategically located on the earliest trail used both by rancheros and American settlers crossing the peninsula to the  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=11992 " target=_blank>Map</a><small> (db&nbsp;m11992)</small> HM</span></td></tr></table>
<table id=M18467><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>7</small></i> </font>&#9658; <a Name=18467>California, San Mateo County, Portola Valley &#8212;  <a href='../m.asp?m=18467'>Hallidie Tramway</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.38277,-122.23131 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> In 1894 Andrew Hallidie, inventor of San Francisco’s cable cars, built an aerial tramway on his hillside property, Eagle Home Farm. It served as a model for prospective customers. The tramway stretched 7,341 ft from this vicinity to a station near  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=18467 " target=_blank>Map</a><small> (db&nbsp;m18467)</small> HM</span></td></tr></table>
<table id=M18463><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>8</small></i> </font>&#9658; <a Name=18463>California, San Mateo County, Portola Valley &#8212; 909 &#8212;  <a href='../m.asp?m=18463'>Our Lady of the Wayside</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.3838,-122.23403333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Built in 1912 this country church was the first executed design of noted architect, Timothy L. Pflueger, who had just begun work for James Miller. An awareness of the Spanish California Missions inspired the style, which contrasts with the large  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=18463 " target=_blank>Map</a><small> (db&nbsp;m18463)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 8 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M25052><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>9</small></i> </font>&#9658; <a Name=25052>California, San Mateo County, Portola Valley &#8212; SMA-025 &#8212;  <a href='../m.asp?m=25052'>Portola Primary School</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.38202732,-122.22873688 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Portola Primary School was built in 1909. The bell was moved in 1893 from the abandoned Searsville School. The building served as the First town hall when the town of Portola Valley was incorporated in 1964. It was acquired by the town in 1976.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=25052 " target=_blank>Map</a><small> (db&nbsp;m25052)</small> HM</span></td></tr></table>
<table id=M32621><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>10</small></i> </font>&#9658; <a Name=32621>California, San Mateo County, Redwood City &#8212; 2 &#8212;  <a href='../m.asp?m=32621'>" Old " San Mateo County Courthouse</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48697767,-122.22984195 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>In 1858, Simon M. Mezes donated land to the county so that a courthouse could be built. This is the third courthouse built on this exact site and the forth built in the property. In 1903, the architectural firm of Dodge and Dolliver designed a domed  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=32621 " target=_blank>Map</a><small> (db&nbsp;m32621)</small> HM</span></td></tr></table>
<table id=M206589><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>11</small></i> </font>&#9658; <a Name=206589>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206589'>Airfield</a> — <i>1916-1939</i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48496667,-122.2032 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Redwood City was an important center for early aviation and Stanford Redwood City is located on what was once an airfield.</b> Silas Christofferson, a visionary young pilot and engineer, bought Michael Lynch’s flower field in early 1916 and built  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206589 " target=_blank>Map</a><small> (db&nbsp;m206589)</small> HM</span></td></tr></table>
<table id=M62625><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>12</small></i> </font>&#9658; <a Name=62625>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62625'>Alhambra Theater</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.485986,-122.22612 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The finest playhouse between San Francisco and San Jose opened here January 20, 1896. In 1921, the building was purchased by Redwood City Masonic Lodge which was instituted August 28, 1863.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62625 " target=_blank>Map</a><small> (db&nbsp;m62625)</small> HM</span></td></tr></table>
<table id=M206740><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>13</small></i> </font>&#9658; <a Name=206740>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206740'>Alta California</a> — <i>1795-1848</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48498333,-122.2033 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Spain claimed Alta California after its conquest of Mexico (1519-1521)</b> When reports of British and Russian encroachment in Northern California began to circulate in the 1760s, Spain expanded its colonial settlements to defend the California  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206740 " target=_blank>Map</a><small> (db&nbsp;m206740)</small> HM</span></td></tr></table>
<table id=M206566><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>14</small></i> </font>&#9658; <a Name=206566>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206566'>Ampex Years</a> — <i>1954-2005</i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48506667,-122.20318333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> 
Alexander M. Poniatoff (1892-1980), a Russian immigrant to the United States, and his colleagues saw enormous commercial potential in the German recording technology they encountered during World War II. In 1944, he founded Ampex – Poniatoff’s  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206566 " target=_blank>Map</a><small> (db&nbsp;m206566)</small> HM</span></td></tr></table>
<table id=M62577><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>15</small></i> </font>&#9658; <a Name=62577>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62577'>California Square</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.487612,-122.23041 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This was a public plaza in the Mezesville townsite, and was a park until 1959 when it was given to San Mateo County for a Hall of Justice and Records.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62577 " target=_blank>Map</a><small> (db&nbsp;m62577)</small> HM</span></td></tr></table>
<table id=M202534><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>16</small></i> </font>&#9658; <a Name=202534>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202534'>California Square</a> — <i>The Path of History</i> — <small><i> <font color=red> Reported unreadable</font></i></small></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>California Square was a parcel of land located north of Marshall Street, between Hamilton and Winslow, diagonally located across the street from the Courthouse. The site was originally designated as a public park plaza in the Town of Mezesville  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202534 " target=_blank>Map</a><small> (db&nbsp;m202534)</small> HM</span></td></tr></table>
<table id=M202732><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>17</small></i> </font>&#9658; <a Name=202732>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202732'>Diller-Chamberlain Gen. Store/American Hotel-American House/Sequoia Hotel/Bank of San Mateo County</a> — <i>Redwood City - Path of History</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.486744,-122.22618 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> <i>(Four markers are mounted on this pedestal.)</i> 
<i>Original Marker:</i>
Diller-Chamberlain General Store
(Quong Lee Laundry) </b>
726 Main Street
J.V. Diller had this one-story brick building constructed in 1859 to house his general  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202732 " target=_blank>Map</a><small> (db&nbsp;m202732)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 17 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M60491><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>18</small></i> </font>&#9658; <a Name=60491>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=60491'>Diller's Island</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.484036,-122.227215 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Children and teachers 
crossed footbridges to reach 
the " Island " public school here from 1864 to 1895<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=60491 " target=_blank>Map</a><small> (db&nbsp;m60491)</small> HM</span></td></tr></table>
<table id=M62622><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>19</small></i> </font>&#9658; <a Name=62622>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62622'>Embarcadero Turning Basin Site</a> — <i>This is the site of the beginning of Redwood City!</i> — The Path of History &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48648,-122.227451 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>You are standing over what was once Redwood City’s original waterfront, made up of creeks, tidal basins, and a fresh-water slough, providing the start of lumber, shipping and shipbuilding trades for the area. The tidal basins south of Bradford  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62622 " target=_blank>Map</a><small> (db&nbsp;m62622)</small> HM</span></td></tr></table>
<table id=M25569><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>20</small></i> </font>&#9658; <a Name=25569>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=25569'>Eureka Corner</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48646368,-122.22647309 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> A hotel on this site, owned by Harry N. Morse and Daniel W. Balch, was the site of the first town meeting in 1854. Residents rejected a Mezesville government.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=25569 " target=_blank>Map</a><small> (db&nbsp;m25569)</small> HM</span></td></tr></table>
<table id=M202543><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>21</small></i> </font>&#9658; <a Name=202543>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202543'>Fire Station No. 1</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48401667,-122.22703333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> The Redwood City Volunteer Fire Department started in 1861, and was the first non-profit organization in San Mateo County. It wasn’t until 1921 when this building at 1044 Middlefield Road was built that the firefighters finally received a salary.   . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202543 " target=_blank>Map</a><small> (db&nbsp;m202543)</small> HM</span></td></tr></table>
<table id=M202544><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>22</small></i> </font>&#9658; <a Name=202544>California, San Mateo County, Redwood City &#8212; GYP 136-07 &#8212;  <a href='../m.asp?m=202544'>Fire Station No. 1</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48413333,-122.2274 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The forerunner to today’s Redwood City Fire Department began in 1861 when citizens met to establish a volunteer fire company. Known as Redwood City Fire Company No. 1, it was the first non-profit organization in San Mateo County. The first fire  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202544 " target=_blank>Map</a><small> (db&nbsp;m202544)</small> HM</span></td></tr></table>
<table id=M202494><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>23</small></i> </font>&#9658; <a Name=202494>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202494'>First Fire House</a> — <i>1862 - 1869</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48781667,-122.22628333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Built for the county’s first fire engine, a building on this site, also housed the first municipal offices in 1867.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202494 " target=_blank>Map</a><small> (db&nbsp;m202494)</small> HM</span></td></tr></table>
<table id=M62621><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>24</small></i> </font>&#9658; <a Name=62621>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62621'>Former Site of Sequoia High School</a> — <i>The Path of History</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48642,-122.228964 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Sequoia High School started September 16, 1895 with 53 students attending classes held on the third floor of the Redwood City Grammar School (replaced by the building now housing the Fox Theatre one block down Broadway). At that time, the building  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62621 " target=_blank>Map</a><small> (db&nbsp;m62621)</small> HM</span></td></tr></table>
<table id=M41621><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>25</small></i> </font>&#9658; <a Name=41621>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=41621'>Fox Theater</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.4863562,-122.22960055 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This property has been 
Placed on the 
National Register 
of Historic Places</b> 
By the United States 
Department of the Interior 
1928</b><!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=41621 " target=_blank>Map</a><small> (db&nbsp;m41621)</small> HM</span></td></tr></table>
<table id=M202545><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>26</small></i> </font>&#9658; <a Name=202545>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202545'>Grand Army of the Republic</a> — <i>The Historic Union Cemetery</i> — Redwood City, CA &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.47463333,-122.22268333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The GRAND ARMY of the REPUBLIC was a fraternal organization composed of honorably discharged veterans of the Union Army, Navy, Marine Corps and Revenue Cutter Service who served in the American Civil War. Founded in 1866, it grew to include about  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202545 " target=_blank>Map</a><small> (db&nbsp;m202545)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 26 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M62672><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>27</small></i> </font>&#9658; <a Name=62672>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62672'>Grand Army of the Republic Memorial</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.474521,-122.22277 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller><i>(front):</i> 
To the memory of California's Patriotic dead who served during the war for the Union
Mustered out  
<i>(back):</i> 
Erected by the grateful people of San Mateo
All honor to the nations defenders<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62672 " target=_blank>Map</a><small> (db&nbsp;m62672)</small> WM</span></td></tr></table>
<table id=M206511><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>28</small></i> </font>&#9658; <a Name=206511>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206511'>Historic Connection</a> — <i><i>1905 and on</i></i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48515,-122.20313333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> The connection between Stanford University and the City of Redwood City goes back a long time.</b> While the first students were admitted to the University on October 1, 1891 – with the mission to “qualify its students for personal success, and  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206511 " target=_blank>Map</a><small> (db&nbsp;m206511)</small> HM</span></td></tr></table>
<table id=M206591><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>29</small></i> </font>&#9658; <a Name=206591>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206591'>Horticultural Heritage</a> — <i>1898-1916</i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48488333,-122.20323333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Nurseryman Michael Lynch bought the property that is now the Stanford Redwood City campus in 1898.</b> Lynch was best known for growing violets and sweet peas for sale as cut flowers and in seed packets. In 1901, he was hired by Jane Stanford to  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206591 " target=_blank>Map</a><small> (db&nbsp;m206591)</small> HM</span></td></tr></table>
<table id=M202538><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>30</small></i> </font>&#9658; <a Name=202538>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202538'>In Memoriam George Edgar Filkins</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.47441667,-122.22263333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>1st Lieut. 15th U.S. Infty. 
Born at Aurora, N.Y. 
August 1, 1842 
Died at San Francisco 
May 15, 1887 

At the age of 19 he enlisted in the 1st. Regt. Wis. Volunteer Infty. He fought in the Battles of Falling Water, Chattanoogo (sic),  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202538 " target=_blank>Map</a><small> (db&nbsp;m202538)</small> HM WM</span></td></tr></table>
<table id=M202539><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>31</small></i> </font>&#9658; <a Name=202539>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202539'>Lathrop House</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48731667,-122.23005 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> A classic example of early " Steamboat Gothic " architecture erected in 1863 as the residence of San Mateo County's first Clerk, Recorder and Assessor, Benjamin G. Lathrop. Later the residence of General Patrick Edward Connor and Sheriff Joel  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202539 " target=_blank>Map</a><small> (db&nbsp;m202539)</small> HM</span></td></tr></table>
<table id=M202540><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>32</small></i> </font>&#9658; <a Name=202540>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202540'>Lathrop-Connor-Mansfield House</a> — <i>The Path of History</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.4874,-122.22988333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This home housed three families of historical significance. B.G. Lathrop who had the house built in 1863 was San Mateo County’s first clerk-recorder and assessor, serving until 1864 when he became Chairman of the Board of Supervisors for San Mateo  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202540 " target=_blank>Map</a><small> (db&nbsp;m202540)</small> HM</span></td></tr></table>
<table id=M206644><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>33</small></i> </font>&#9658; <a Name=206644>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206644'>Mezesville</a> — <i>1850-1900</i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.4849,-122.20333333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> The discovery of gold at Sutter’s Fort in 1848 ushered in a period of rapid change in California as thousands of immigrants flooded into the state and the non-native population grew from 20,000 to 100,000 in one year.</b> Mexico ceded California to  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206644 " target=_blank>Map</a><small> (db&nbsp;m206644)</small> HM</span></td></tr></table>
<table id=M206786><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>34</small></i> </font>&#9658; <a Name=206786>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206786'>Native Americans</a> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48508333,-122.20328333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> The San Francisco Bay Area was settled by Native Americans before the end of the last Ice Age flooded the river valley (<i>rúmmey waayi</i>) that became San Francisco Bay (about 5,000 years ago).</b> The ancestors <i>(muwékmakuš)</i> of the Muwekma  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206786 " target=_blank>Map</a><small> (db&nbsp;m206786)</small> HM</span></td></tr></table>
<table id=M62580><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>35</small></i> </font>&#9658; <a Name=62580>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62580'>New Sequoia/Fox Theatre</a> — <i>Former site of the Central Grammar School</i> — The Path of History &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.486403,-122.22947 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This block of Broadway underwent a number of major changes during the first half of the twentieth century.
It started out as the Central Grammar School in 1895 (legally named “Redwood City Public School”) shown at the left above. Part of a third  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62580 " target=_blank>Map</a><small> (db&nbsp;m62580)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 35 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M62598><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>36</small></i> </font>&#9658; <a Name=62598>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62598'>Old San Mateo County Courthouse</a> — <i>The Path of History</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.486386,-122.22976 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Simon M. Mezes, owner of the land that now includes most of Downtown Redwood City, donated a block to the newly-formed San Mateo County in 1858 so that a courthouse could be built. There were eventually four courthouses built on this property.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62598 " target=_blank>Map</a><small> (db&nbsp;m62598)</small> HM</span></td></tr></table>
<table id=M206827><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>37</small></i> </font>&#9658; <a Name=206827>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=206827'>Pleistocene Period</a> — <i>2.6 Million - 11,700 Years Ago</i> — Stanford | Redwood City &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48515,-122.20325 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Fossilized bones were discovered on this site in 2017 during earthwork grading operations for the Stanford Redwood City campus. </b>The bones were found at substantial depth below the ground surface. They were determined to be from the Pleistocene  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=206827 " target=_blank>Map</a><small> (db&nbsp;m206827)</small> HM</span></td></tr></table>
<table id=M41623><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>38</small></i> </font>&#9658; <a Name=41623>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=41623'>Redwood City Arch</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48626681,-122.23259926 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Redwood City 
Climate Best by Government Test</b> 

This archway sign and slogan is based on the design of two earlier archway signs that once spanned the El Camino Real, designating the northern and southern entryways into Redwood City. A  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=41623 " target=_blank>Map</a><small> (db&nbsp;m41623)</small> HM</span></td></tr></table>
<table id=M202493><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>39</small></i> </font>&#9658; <a Name=202493>California, San Mateo County, Redwood City &#8212; GPY 135-4 &#8212;  <a href='../m.asp?m=202493'>Redwood City Fire Department</a> — <i>150th Anniversary</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48776667,-122.22718333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>On the evening of September 11, 1861, a group of concerned citizens met at the San Mateo County Courthouse. Their mission was to organize the Redwood Hose Company for the purpose of providing fire protection to the small settlement of Mezesville and  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202493 " target=_blank>Map</a><small> (db&nbsp;m202493)</small> HM</span></td></tr></table>
<table id=M62846><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>40</small></i> </font>&#9658; <a Name=62846>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62846'>Redwood City War Memorial</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.484841,-122.22769 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>World War 11</b> 
Corporal James Lindsay Wilson 
V.F.W. Post No. 2310 

In memoriam 
to the following Redwood City men 
who gave their lives in World War II 
1941 &#8211; 1945
Raymond J. Barra • Russell V. Braca • Everett F. Bottena • Robert  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62846 " target=_blank>Map</a><small> (db&nbsp;m62846)</small> WM</span></td></tr></table>
<table id=M25526><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>41</small></i> </font>&#9658; <a Name=25526>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=25526'>Sequoia Union High School</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48634024,-122.22900242 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=25526 " target=_blank>Map</a><small> (db&nbsp;m25526)</small> HM</span></td></tr></table>
<table id=M62671><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>42</small></i> </font>&#9658; <a Name=62671>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62671'>Solari Family Windmill</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.474756,-122.22252 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This windmill was built in the 1880’s on the Solari farm, located at Whipple Avenue and Old County Road in Redwood City.  

It was move in the 1930’s to the new family farm located on Manzanita Street near Middlefield Road and the railroad tracks,  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62671 " target=_blank>Map</a><small> (db&nbsp;m62671)</small> HM</span></td></tr></table>
<table id=M93230><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>43</small></i> </font>&#9658; <a Name=93230>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=93230'>Solari Windmill</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.47479742,-122.22251281 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This reconstructed windmill was originally built in the 1890's on the George Solari family farm which was located near Whipple Avenue and Old County Road in Redwood City. When the area was subdivided in the 1930's, the windmill was moved to the new  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=93230 " target=_blank>Map</a><small> (db&nbsp;m93230)</small> HM</span></td></tr></table>
<table id=M62575><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>44</small></i> </font>&#9658; <a Name=62575>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=62575'>Soledad O. de Arguello</a> — <i>1897 - 1874</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.486361,-122.23249 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> <i>Left Side - English</i> 
She donated 59,000 acres of land for the benefit of all people.  
<i>Right Side - Spanish</i> 
Donadora de 59,000 acres par beneficio de la comunidad<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=62575 " target=_blank>Map</a><small> (db&nbsp;m62575)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 44 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M202495><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>45</small></i> </font>&#9658; <a Name=202495>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202495'>Stage Station</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48331667,-122.2259 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>In 1865, Simon L. Knights began service to Searsville, Woodside, and later the coastside. In 1906, the line converted to automobiles.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202495 " target=_blank>Map</a><small> (db&nbsp;m202495)</small> HM</span></td></tr></table>
<table id=M202536><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>46</small></i> </font>&#9658; <a Name=202536>California, San Mateo County, Redwood City &#8212;  <a href='../m.asp?m=202536'>The Path of History</a> — <i>Redwood City Walking Tour</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48676667,-122.22605 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> <i>South Panel - 1 of 4</i> 
Welcome to historic Downtown Redwood City!</b>
Step back in time, over 150 years, when much of Downtown Redwood City was made up of creeks and wharves. Discover that the town got its name, not from indigenous redwood  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202536 " target=_blank>Map</a><small> (db&nbsp;m202536)</small> HM</span></td></tr></table>
<table id=M25528><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>47</small></i> </font>&#9658; <a Name=25528>California, San Mateo County, Redwood City &#8212; 3 &#8212;  <a href='../m.asp?m=25528'>The Pioneer Store</a> — <i>Diller-Chamberlain Store</i> — Historic Trail Site #3 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.48709579,-122.22656026 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> 
Redwood City Historic Landmark No.2 
National Register Historic District
 


This Brick Building was constructed in 1859 as a general store for J.V. Diller, who became Redwood City's first mayor in 1867. From 1875 until 1911, P.P.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=25528 " target=_blank>Map</a><small> (db&nbsp;m25528)</small> HM</span></td></tr></table>
<table id=M202537><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>48</small></i> </font>&#9658; <a Name=202537>California, San Mateo County, Redwood City &#8212; 816 &#8212;  <a href='../m.asp?m=202537'>Union Cemetery</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.47268333,-122.22298333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Union Cemetery's name reflects the controversy that erupted in the Civil War, three years after the cemetery's beginnings in 1859. Pro- and anti- slavery feelings ran high in California, and the founders of the cemetery strongly opposed the  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=202537 " target=_blank>Map</a><small> (db&nbsp;m202537)</small> HM</span></td></tr></table>
<table id=M72481><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>49</small></i> </font>&#9658; <a Name=72481>California, San Mateo County, San Mateo &#8212;  <a href='../m.asp?m=72481'>St. Denis Church and Cemetery</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.42049,-122.218108 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>One half mile south of this site stood the first church in San Mateo County. Dedicated in 1853 by Catholic Archbishop Joseph S. Alemany. He named it after St. Denis to honor the founder, Dennis Martin, pioneer lumberman and farmer, who also  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=72481 " target=_blank>Map</a><small> (db&nbsp;m72481)</small> HM</span></td></tr></table>
<table id=M24342><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>50</small></i> </font>&#9658; <a Name=24342>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24342'>Alviso</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.426799,-121.979039 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> 

&#8729;&#8729;&#8729;Known as the El Embarcadero de Santa Clara in pre-American days. &#8729;&#8729;&#8729;Was the port of entry for San Jose prior to the coming of the railroad. &#8729;&#8729;&#8729;Surveyed and platted by C.S. Lyman in  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24342 " target=_blank>Map</a><small> (db&nbsp;m24342)</small> HM</span></td></tr></table>
<table id=M24364><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>51</small></i> </font>&#9658; <a Name=24364>California, Santa Clara County, Alviso &#8212; 69 &#8212;  <a href='../m.asp?m=24364'>Bayside Cannery</a> — <i>ca. 1906</i> — City of San Jose – Capital of the Silicon Valley &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.428247,-121.979195 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Established ca.1906, Bayside Canning Company expanded to become the third largest cannery in the United States by 1931. Under the leadership of Thomas Foon Chew, Bayside was the first cannery in the world to can green asparagus. The main building  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24364 " target=_blank>Map</a><small> (db&nbsp;m24364)</small> HM</span></td></tr></table>
<table id=M24367><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>52</small></i> </font>&#9658; <a Name=24367>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24367'>China Camp</a> — <i>Ca. 1895</i> — City of San Jose – Capital of Silicon Valley &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.428328,-121.978916 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This building served as lodging for many Bayside Cannery workers who normally lived in San Francisco or other distant cities. In addition to dorm rooms, the building also contained a kitchen and dining hall. During the cannery’s operation, this  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24367 " target=_blank>Map</a><small> (db&nbsp;m24367)</small> HM</span></td></tr></table>
<table id=M24408><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>53</small></i> </font>&#9658; <a Name=24408>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24408'>Discover Alviso’s Rich History</a> — <i>Alviso Marina County Park</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.429931,-121.979381 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Alviso’s marina today starkly contrasts with its past as a bustling seaport. In the mid-19th century, Alviso was a transportation hub through which crops, goods and people circulated, fueling the economic growth of the South Bay. Port activity in  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24408 " target=_blank>Map</a><small> (db&nbsp;m24408)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 53 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M24452><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>54</small></i> </font>&#9658; <a Name=24452>California, Santa Clara County, Alviso &#8212; 67 &#8212;  <a href='../m.asp?m=24452'>H.G. Wade’s Warehouse</a> — <i>ca. 1860</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.42463,-121.977205 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Harry George Wade’s Warehouse was originally used to store hay and grain bound for San Francisco. It was later used to store stagecoaches for the Alviso to Monterey stage lines. In addition to being used by Wells Fargo and Company for stagecoach  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24452 " target=_blank>Map</a><small> (db&nbsp;m24452)</small> HM</span></td></tr></table>
<table id=M24450><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>55</small></i> </font>&#9658; <a Name=24450>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24450'>La Montagne Boarding House</a> — <i>ca. 1890</i> — City of San Jose Capital of Silicon Valley &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.42728,-121.976868 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Originally built as a private home, the house was remodeled in 1904 to serve as a boarding house for PG&E employees. It was later purchased by William Clampett and Jane Huxham in the 1920s. During the 1940s, boat restoration work for PG&E was  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24450 " target=_blank>Map</a><small> (db&nbsp;m24450)</small> HM</span></td></tr></table>
<table id=M24429><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>56</small></i> </font>&#9658; <a Name=24429>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24429'>Location, Location, Location</a> — <i>Once a Hub for the South Bay</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.430825,-121.978154 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Because of its location at the mouth of the Guadalupe River, Alviso was deemed the ideal location for a seaport. In the 1830s and 40s, it was the only port where raw materials and crops could be shipped from the Santa Clara Valley to San Francisco.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24429 " target=_blank>Map</a><small> (db&nbsp;m24429)</small> HM</span></td></tr></table>
<table id=M24444><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>57</small></i> </font>&#9658; <a Name=24444>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24444'>Salt Ponds</a> — <i>Past, Present, Future</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.431089,-121.979163 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Beginning with the Ohlone people, who harvested salt for local use and regional trade, small scale salt production on San Francisco Bay expanded into one of the largest industrial solar evaporation complexes in the world. Salt production transformed  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24444 " target=_blank>Map</a><small> (db&nbsp;m24444)</small> HM</span></td></tr></table>
<table id=M24407><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>58</small></i> </font>&#9658; <a Name=24407>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24407'>The Port and Town of Alviso</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.429883,-121.979238 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>  
The Santa Clara County Parks and Recreation Department and the Santa Clara Valley Water District dedicated the Alviso Marina County Park on September 24, 2005.   

First known to the Ohlone Indians, the lands of The Alviso Marina County Park  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24407 " target=_blank>Map</a><small> (db&nbsp;m24407)</small> HM</span></td></tr></table>
<table id=M64389><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>59</small></i> </font>&#9658; <a Name=64389>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=64389'>The Steamboat Jenny Lind Disaster</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.430007,-121.98021 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Beginning in the 1840s, the dock at Alviso served as Santa Clara County’s access to the San Francisco Bay. From this port, passengers boarded steamboats loaded with goods and produce bound for San Francisco and points beyond. In the early days of  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=64389 " target=_blank>Map</a><small> (db&nbsp;m64389)</small> HM</span></td></tr></table>
<table id=M24345><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>60</small></i> </font>&#9658; <a Name=24345>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24345'>Union Warehouse and Docks</a> — <i>ca. 1850</i> — City of San Jose – Capital of the Silicon Valley &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.427936,-121.979173 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> During Alviso’s years as a major shipping port, this warehouse was used for the storage of goods prior to shipping. It was later incorporated into Bayside Cannery and used as a cold storage and refrigeration plant. Today it stands as one of the  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24345 " target=_blank>Map</a><small> (db&nbsp;m24345)</small> HM</span></td></tr></table>
<table id=M24414><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>61</small></i> </font>&#9658; <a Name=24414>California, Santa Clara County, Alviso &#8212;  <a href='../m.asp?m=24414'>Water Everywhere</a> — <i>Water Seeks Its Own Level</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.430117,-121.979731 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Water in Alviso is a complex issue that touches on the environment, economics, and life safety. Already susceptible to flooding, Alviso’s situation was worsened by regional development. Hard paving, which prevented water absorption into the ground,  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24414 " target=_blank>Map</a><small> (db&nbsp;m24414)</small> HM</span></td></tr></table>
<table id=M24125><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>62</small></i> </font>&#9658; <a Name=24125>California, Santa Clara County, Cupertino &#8212;  <a href='../m.asp?m=24125'>Captain Elisha Stephens</a> — <i>1804 - 1887</i> — A True Pioneer &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.3225,-122.06028 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Captain Stephens brought the first wagons
over the snow covered Sierra Nevada
Truckee Pass with no casualties in the
Stephens-Murphy-Townsend party of 1844,
arriving at Sutter's Fort with 11 wagons
and 51 people plus 2 infants born on the
way.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=24125 " target=_blank>Map</a><small> (db&nbsp;m24125)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 62 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M58977><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>63</small></i> </font>&#9658; <a Name=58977>California, Santa Clara County, Cupertino &#8212;  <a href='../m.asp?m=58977'>St. Joseph’s College</a> — <i>September 10, 1924 – October 17, 1989</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.334559,-122.0899 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This monument marks the former campus of St. Joseph’s College. Established with the primary purpose of training candidates for Catholic Priesthood, the college also educated thousands of young men who entered public service throughout this country  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=58977 " target=_blank>Map</a><small> (db&nbsp;m58977)</small> HM</span></td></tr></table>
<table id=M100298><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>64</small></i> </font>&#9658; <a Name=100298>California, Santa Clara County, Los Altos, Loyola Corners &#8212;  <a href='../m.asp?m=100298'>Historic Loyola Corners</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.354181,-122.088946 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>In the early 1900's, Southern Pacific Railroad ran tracks down what is now Foothill Expressway connecting Los Altos with San Jose and San Francisco. A railway depot was built not far from here and called " Loyola Corners ", after St. Ignatius Loyola,  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100298 " target=_blank>Map</a><small> (db&nbsp;m100298)</small> HM</span></td></tr></table>
<table id=M100303><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>65</small></i> </font>&#9658; <a Name=100303>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100303'>306 Main Street</a> — " Shoup Hall " &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.378373,-122.116891 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This two-story building, the second permanent business building in town, was built at the direction of Paul Shoup, often referred to as the " Father of Los Altos." It was the foresight of Mr. Shoup and his associates that led to a concept of the town  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100303 " target=_blank>Map</a><small> (db&nbsp;m100303)</small> HM</span></td></tr></table>
<table id=M100305><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>66</small></i> </font>&#9658; <a Name=100305>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100305'>316 Main Street</a> — " Eschenbruecher Hardware " &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.378308,-122.117049 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Built in 1908, this structure housed the first commercial business in the new town of Los Altos. William and Lillian Eschenbruecher operated their hardware store here for more than a year before electricity and water became general available to  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100305 " target=_blank>Map</a><small> (db&nbsp;m100305)</small> HM</span></td></tr></table>
<table id=M100302><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>67</small></i> </font>&#9658; <a Name=100302>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100302'>395/397 Main Street</a> — " Copeland Building " &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.377823,-122.117519 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Built in 1911, this two-story structure, often referred to as the " Copland Building ". was the forth building to appear along Main Street. At one time a side door existed on First Street near the rear of the building. Except for the removal of this  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100302 " target=_blank>Map</a><small> (db&nbsp;m100302)</small> HM</span></td></tr></table>
<table id=M100307><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>68</small></i> </font>&#9658; <a Name=100307>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100307'>398 and 388 Main Street</a> — <i>circa 1910</i> — " The Bank " and " The Grocery " &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.377984,-122.117677 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>This " double building " was built by Paul and Guy Shoup around 1910. It was the third building in downtown Los Altos and housed the offices of Altos Land Company, Los Altos Building and Loan Association, Los Altos Water Company, University Land  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100307 " target=_blank>Map</a><small> (db&nbsp;m100307)</small> HM</span></td></tr></table>
<table id=M100304><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>69</small></i> </font>&#9658; <a Name=100304>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100304'>Los Altos Elementary School</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.378399,-122.116843 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> Site of first  
Los Altos  
Elementary School  
Est. 1908  
Div. 54 C.R.T.A.<!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100304 " target=_blank>Map</a><small> (db&nbsp;m100304)</small> HM</span></td></tr></table>
<table id=M100299><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>70</small></i> </font>&#9658; <a Name=100299>California, Santa Clara County, Los Altos, North Los Altos &#8212;  <a href='../m.asp?m=100299'>Southern Pacific Railroad Station</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.377233,-122.1176 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The town of Los Altos as we know it owes its existence to the Southern Pacific Railroad. Needing a shortcut between Palo Alto and Los Gatos, the Southern Pacific Railroad acquired the downtown " triangle " in 1907 from Sarah Winchester, who refused to  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=100299 " target=_blank>Map</a><small> (db&nbsp;m100299)</small> HM</span></td></tr></table>
<table id=M182658><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>71</small></i> </font>&#9658; <a Name=182658>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182658'>Birthplace of Silicon Valley</a> — <i>IEEE Milestone</i> — 1956 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41478611,-122.07766667 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>At this location, 391 San Antonio Road, the Shockley Semiconductor
Laboratory manufactured the first silicon devices in what became
known as Silicon Valley. Some of the talented scientists and engineers
initially employed there left to found  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182658 " target=_blank>Map</a><small> (db&nbsp;m182658)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 71 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M182679><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>72</small></i> </font>&#9658; <a Name=182679>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182679'>Birthplace of Silicon Valley</a> — <i>IEEE Milestone</i> — 1956 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.40485,-122.11126667 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller> 
At this location, 391 San Antonio Road, the Shockley Semiconductor Laboratory manufactured the first silicon devices in what became known as Silicon Valley. Some of the talented scientists and engineers initially employed there left to found  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182679 " target=_blank>Map</a><small> (db&nbsp;m182679)</small> HM</span></td></tr></table>
<table id=M232027><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>73</small></i> </font>&#9658; <a Name=232027>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=232027'>City of Mountain View Adobe Building</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.39593333,-122.07726667 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller><i>Marker One:</i> 
Built 1934 Restored 2001 
Mario Ambra, Mayor • Sally J. Lieber, Vice Mayor • Ralph Faravelli • Rosemary Stasek • Matt Pear • Mary Lou Zoglin • R. Michael Kasperzak, Jr. • Kevin C. Duggan, City Manager • Cathy R. Lazarus, Public  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=232027 " target=_blank>Map</a><small> (db&nbsp;m232027)</small> HM</span></td></tr></table>
<table id=M154426><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>74</small></i> </font>&#9658; <a Name=154426>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=154426'>Computer History Museum</a> — <i>IEEE Special Citation in Electrical Engineering and Computing</i> — 1979 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41482352,-122.07762826 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The Computer History Museum's mission is to preserve and present for posterity the artifacts and stories of the Information Age. The museum houses the collection of computers and related software, and visual media. Public exhibits celebrate the  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=154426 " target=_blank>Map</a><small> (db&nbsp;m154426)</small> HM</span></td></tr></table>
<table id=M182662><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>75</small></i> </font>&#9658; <a Name=182662>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182662'>DIALOG Online Search System</a> — <i>IEEE Milestone</i> — 1966 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.4147868,-122.07768338 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>DIALOG was the first interactive, online search system addressing large
databases while allowing iterative refinement of results. DIALOG was
developed at Lockheed Palo Alto Research Laboratory in 1966, extended
through contracts with NASA, and  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182662 " target=_blank>Map</a><small> (db&nbsp;m182662)</small> HM</span></td></tr></table>
<table id=M182630><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>76</small></i> </font>&#9658; <a Name=182630>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182630'>First RISC Microprocessor</a> — <i>Reduced Instruction-Set Computing</i> — IEEE Milestone in Electrical Engineering and Computing &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41476944,-122.07768333 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>UC Berkeley students designed and built the first VLSI reduced
instruction-set computer in 1981. The simplified instructions of
RISC-I reduced the hardware for instruction decode and control,
which enabled a flat 32-bit address space, a large set  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182630 " target=_blank>Map</a><small> (db&nbsp;m182630)</small> HM</span></td></tr></table>
<table id=M183435><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>77</small></i> </font>&#9658; <a Name=183435>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=183435'>Immigrant House</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41365851,-122.0918882 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>  
This two-room dwelling, today known as the Immigrant House, was originally located at 166 Bryant Street. It was built in the 1880s with redwood from the Santa Cruz Mountains and restored to a modest style of the 1920s, where the front room  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=183435 " target=_blank>Map</a><small> (db&nbsp;m183435)</small> HM</span></td></tr></table>
<table id=M116026><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>78</small></i> </font>&#9658; <a Name=116026>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=116026'>Jagel Slough</a> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.426839,-122.040897 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>One mile north-west of this plaque lies Jagel Slough, named for the Jagel Family, who were hay and grain farmers in the area. It is believed Ozymandias P. Jagel, who settled here (1859), set up a still to carry on an illegal liquor business. He  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=116026 " target=_blank>Map</a><small> (db&nbsp;m116026)</small></span></td></tr></table>
<table id=M182655><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>79</small></i> </font>&#9658; <a Name=182655>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182655'>Moore’s Law</a> — <i>IEEE Milestone</i> — 1965 &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41480251,-122.07767466 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Gordon E. Moore, co-founder of Fairchild and Intel, began his work in silicon
microelectronics at Shockley Semiconductor Laboratory in 1956. His 1965
prediction at Fairchild Semiconductor, subsequently known as “Moore's Law,” that the number of  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182655 " target=_blank>Map</a><small> (db&nbsp;m182655)</small> HM</span></td></tr></table>
<table id=M182299><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>80</small></i> </font>&#9658; <a Name=182299>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182299'>NASA Ames Research Center</a> — <i>AIAA Historic Aerospace Site</i> &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.40927713,-122.06395203 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Ames Aeronautical Laboratory was established in 1939 as the second laboratory of the National Advisory Committee for Aeronautics. Ames achieved early fame in wind tunnel design and testing, flight testing, and supersonic and hypersonic aerodynamics.  . . . <!-- --> &#8212; <span class=bodysansserifsmaller> &#8212; <a href=" ../map.asp?markers=182299 " target=_blank>Map</a><small> (db&nbsp;m182299)</small> HM</span></td></tr></table>

<fieldset id=incopyAd class=adright style='width:300px'><legend class=adtitle>Paid Advertisement</legend>
<script async src=" https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js "></script>
<!-- Square display ad 80 -->
<ins class=" adsbygoogle "
     style=" display:block;"
     data-ad-client=" ca-pub-7431457857623754 "
     data-ad-slot=" 2131831404 "
     data-ad-format=" auto "
     data-full-width-responsive=" true "></ins>
<script>
     (adsbygoogle = window.adsbygoogle || []).push({});
</script>
                    </fieldset>


<table id=M182615><td colspan=4 class=bodysansserif style='padding:8 0 0 0;'><i><small><font color=gray>81</small></i> </font>&#9658; <a Name=182615>California, Santa Clara County, Mountain View &#8212;  <a href='../m.asp?m=182615'>Online Systems and Personal Computing</a> — <i>Public Demonstration, 1968</i> — IEEE Milestone in Electrical Engineering and Computing &#8212; <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.41476428,-122.07766881 " target=_blank rel='noopener'><img src=directions.png width=20 title='Driving directions to this location'></a></i></td></tr></table><table><tr><td width=12></td><td width=13></td><td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>Commonly termed the " Mother of All Demos,” Douglas Engelbart and his team demonstrated their oNLine System (NLS) at Brooks Hall in San Francisco on 9 December 1968. Connected via microwave link to the host computer and other remote users at SRI . . .<!-- --> &#8212;<span class=bodysansserifsmaller>
        &#8212;<a href="../map.asp?markers=182615" target=_blank>Map</a>
        <small>(db &nbsp;m182615)</small>
        HM
</span></td></tr></table>
<table id=M69152>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>82
            </small>
        </i>
</font>&#9658;
<a Name=69152>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=69152'>Rengstorff House</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.431556,-122.08725" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>A Pioneer Family
</b>Henry Rengstorff grew up in Germany. Like so many others of his generation, Henry was lured to California by stories of the Gold Rush. He left home at the age of 21, sailed around Cape Horn and arrived in San Francisco in 1850  . . . 
<!-- -->
&#8212;
<span class=bodysansserifsmaller>
    &#8212;<a href="../map.asp?markers=69152" target=_blank>Map</a>
    <small>(db &nbsp;m69152)</small>
    HM
</span>
</td></tr></table>
<table id=M154427>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>83
            </small>
        </i>
</font>&#9658;
<a Name=154427>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=154427'>Shakey: The World's First Mobile Intelligent Robot</a>
    — <i>IEEE Milestone In Electrical Engineering And Computing</i>
    — 1972 &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.414778,-122.07764" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Stanford Research Institute's Artificial Intelligence Center developed the world's first mobile intelligent robot, Shakey. It could perceive its surroundings, infer implicit facts from explicit ones, create plans, recover from errors in plan  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=154427" target=_blank>Map</a>
                <small>(db &nbsp;m154427)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M103035>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>84
            </small>
        </i>
</font>&#9658;
<a Name=103035>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=103035'>Shenandoah Plaza</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.410981,-122.059982" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Shenandoah Plaza 
In Memory of the Fourteen Officers and men 
USS Shenandoah 
Lost September 3, 1925   
Lord, guard and guide the men who fly 
through the great spaces in the sky. 
Be with them always in the air,  
in darkening storms or  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=103035" target=_blank>Map</a>
                <small>(db &nbsp;m103035)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M164637>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>85
            </small>
        </i>
</font>&#9658;
<a Name=164637>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=164637'>Site of the John W. Whisman Home</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41054928,-122.06354211" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Site of the John W. Whisman Home, headquarters and stopping place of the first stagecoach line between San Jose &San Francisco,
started in early autumn of 1849.
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=164637" target=_blank>Map</a>
                <small>(db &nbsp;m164637)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M182627>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>86
            </small>
        </i>
</font>&#9658;
<a Name=182627>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=182627'>SPARC RISC Architecture</a>
    — <i>IEEE Milestone in Electrical Engineering and Computing</i>
    — 1987 &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41474493,-122.07768722" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Sun Microsystems introduced SPARC (Scalable Processor
Architecture) RISC (Reduced Instruction-Set Computing) in 1987.
Building upon UC Berkeley RISC and Sun compiler and operating
system developments, SPARC architecture was highly adaptable  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=182627" target=_blank>Map</a>
                <small>(db &nbsp;m182627)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M182665>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>87
            </small>
        </i>
</font>&#9658;
<a Name=182665>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=182665'>SPICE</a>
    — <i>Simulation Program with Integrated Circuit Emphasis</i>
    — IEEE Milestone In Electrical Engineering And Computing &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41478333,-122.077675" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            SPICE was created at UC Berkeley as a class project in
1969-1970. It evolved to become the worldwide standard
integrated circuit simulator. SPICE has been used to train
many students in the intricacies of circuit simulation.
SPICE and its  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=182665" target=_blank>Map</a>
                <small>(db &nbsp;m182665)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M183428>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>88
            </small>
        </i>
</font>&#9658;
<a Name=183428>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=183428'>Star Steel Windmill</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41379576,-122.0917429" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            This windmill was the very last to stand at its original location in Mountain View — behind the residence at 944 San Leandro Avenue. It was purchased in 1936 from the Geo. W. Sohler &Son Company at 550 California Street. The 8-foot diameter  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=183428" target=_blank>Map</a>
                <small>(db &nbsp;m183428)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M182617>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>89
            </small>
        </i>
</font>&#9658;
<a Name=182617>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=182617'>The Floating Gate EEPROM</a>
    — <i>IEEE Milestone in Electrical Engineering and Computing</i>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41480579,-122.07761222" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            From 1976-1978, at Hughes Microelectronics in Newport Beach,
California, the practicality, reliability, manufacturability and
endurance of the Floating Gate EEPROM — an electrically erasable
device using a thin gate oxide and Fowler-Nordheim  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=182617" target=_blank>Map</a>
                <small>(db &nbsp;m182617)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M183399>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>90
            </small>
        </i>
</font>&#9658;
<a Name=183399>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=183399'>The NASA U-2 Story</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.41208788,-122.05461825" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            This U-2 aircraft, NASA 708, with its sister ship, NASA 709, was obtained from the U.S. Air Force in 1971 to test the instrumentation systems being developed for the early Landsat Earth-observing satellites. The results were so successful that  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=183399" target=_blank>Map</a>
                <small>(db &nbsp;m183399)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M69151>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>91
            </small>
        </i>
</font>&#9658;
<a Name=69151>
    California, Santa Clara County, Mountain View &#8212;<a href='../m.asp?m=69151'>The Spirit of the Times</a>
    — 
    <i>
        <i>Wild Marshes, Handy Scows &Mr. Henry Rengstorff</i>
    </i>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.431665,-122.08689" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>The Victorian Beauty
</b>The 16 room house that Henry Rengstorff built in 1867 is a fine example of the Bay Area’s late Victorian Italianate architecture. The facade of the house is symmetrical, with the central entrance defined by a pillared  . . . 
<!-- -->
&#8212;
<span class=bodysansserifsmaller>
    &#8212;<a href="../map.asp?markers=69151" target=_blank>Map</a>
    <small>(db &nbsp;m69151)</small>
    HM
</span>
</td></tr></table>
<table id=M100127>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>92
            </small>
        </i>
</font>&#9658;
<a Name=100127>
    California, Santa Clara County, Mountain View, Cuesta Park &#8212;<a href='../m.asp?m=100127'>Site of Witness Tree</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.373728,-122.078058" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Wild Cherry Tree  
on property of Benjamin Bubb, used in government survey of 1865 and by early settlers to locate their land. 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=100127" target=_blank>Map</a>
                <small>(db &nbsp;m100127)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M100129>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>93
            </small>
        </i>
</font>&#9658;
<a Name=100129>
    California, Santa Clara County, Mountain View, Monta Loma &#8212;<a href='../m.asp?m=100129'>Mariano Castro Adobe</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.403743,-122.097277" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Site of the original Mariano Castro Adobe, built in 1840 on Rancho Pastoría de las Borregas, a Mexican land grant.
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=100129" target=_blank>Map</a>
                <small>(db &nbsp;m100129)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M100128>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>94
            </small>
        </i>
</font>&#9658;
<a Name=100128>
    California, Santa Clara County, Mountain View, Old Mountain View &#8212;<a href='../m.asp?m=100128'>The Mountain View Train Depot</a>
    — <i>City of Mountain View</i>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.39497,-122.077934" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            In 1888, at the request of local residents, the Southern Pacific Railroad built a depot at Mountain View Station, at a cost of $4,000. The second floor was used as living quarters for the station agent.  
By 1892, ten passenger trains stopped daily  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=100128" target=_blank>Map</a>
                <small>(db &nbsp;m100128)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M150657>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>95
            </small>
        </i>
</font>&#9658;
<a Name=150657>
    California, Santa Clara County, Mountain View, Waverly Park &#8212;<a href='../m.asp?m=150657'>Blue and Gold Kennel Club</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.369746,-122.071353" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            This eighteen room farmhouse was built by Nicholas Kristmas in 1924. By 1928, it had entered into an era of local infamy during which it was known alternatively by the names Blue and Gold Kennel Club, Whitehall Distillery, and Burton's Gold Medal  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=150657" target=_blank>Map</a>
                <small>(db &nbsp;m150657)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M100130>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>96
            </small>
        </i>
</font>&#9658;
<a Name=100130>
    California, Santa Clara County, Palo Alto &#8212;<a href='../m.asp?m=100130'>From Sea Scouts to Environmental Volunteers</a>
    — <i>Desde Exploradores de Mar al Voluntario Ambiental</i>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.456431,-122.108439" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            In 1941, this unique building stood at the edge of a yacht harbor. It was constructed for the Sea Scouts, a part of the Boy Scouts of America. After 50 years of use, the Sea Scouts left the base in 1991. This was five years after Palo Alto residents  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=100130" target=_blank>Map</a>
                <small>(db &nbsp;m100130)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M54017>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>97
            </small>
        </i>
</font>&#9658;
<a Name=54017>
    California, Santa Clara County, Palo Alto &#8212;895 &#8212;<a href='../m.asp?m=54017'>Hostess House</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.44293333,-122.16548333" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            This building originally served Camp Fremont as a meeting place for servicemen and visitors. When moved from its original site to Palo Alto, it became the first municipally sponsored community center in the nation. It is the only remaining structure  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=54017" target=_blank>Map</a>
                <small>(db &nbsp;m54017)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M90884>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>98
            </small>
        </i>
</font>&#9658;
<a Name=90884>
    California, Santa Clara County, Palo Alto &#8212;<a href='../m.asp?m=90884'>Mayfield School</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.42357273,-122.14401677" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            Mayfield's first school built in 1855 it was a two-room log cabin which was soon outgrown. Mayfield later built two new schools to serve the increasing population of young people and even offered some adult education in mathematics. In 1923, on the  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=90884" target=_blank>Map</a>
                <small>(db &nbsp;m90884)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M113015>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>99
            </small>
        </i>
</font>&#9658;
<a Name=113015>
    California, Santa Clara County, Palo Alto &#8212;<a href='../m.asp?m=113015'>Motion Picture Research Commemoration</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.427917,-122.170283" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            In commemoration of the motion picture research conducted in 1878 and 1879 at the Palo Alto Farm now the site of Stanford University  

This extensive photographic experiment portraying the attitudes of men and animals in motion was conceived by  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=113015" target=_blank>Map</a>
                <small>(db &nbsp;m113015)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<table id=M54016>
    <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
        <i>
            <small>
                <font color=gray>100
            </small>
        </i>
</font>&#9658;
<a Name=54016>
    California, Santa Clara County, Palo Alto &#8212;524 &#8212;<a href='../m.asp?m=54016'>Site of Juana Briones de Miranda Home on Rancho La Purisima Concepcion</a>
    &#8212;
    <a href="https://www.google.com/maps/dir/?api=1&destination=37.39225,-122.13955" target=_blank rel='noopener'>
        <img src=directions.png width=20 title='Driving directions to this location'>
    </a>
</i></td></tr></table>
<table>
    <tr>
        <td width=12></td>
        <td width=13></td>
        <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
            In 1844 Juana de Briones de Miranda, a pioneer Latina property owner, businesswoman and humanitarian, purchased the 4,439 acre Rancho La Purisima Concepcion from Indian grantee Jose Gorgornio. The grant extended two miles south, encompassing  . . . 
            <!-- -->
            &#8212;
            <span class=bodysansserifsmaller>
                &#8212;<a href="../map.asp?markers=54016" target=_blank>Map</a>
                <small>(db &nbsp;m54016)</small>
                HM
            </span>
        </td>
    </tr>
</table>
<script>
    document.getElementById('ClickToDownload').innerHTML = "<form method=post name=MapIt action=../ListsDownload.asp style='display:inline;' onsubmit='pleasewait()'><input type=hidden name=markers value='0,24125,58977,24122,24267,24255,220526,220576,220643,220641,218886,61492,52932,52931,52897,52933,52898,52899,52900,52866,52907,53041,195457,154933,2627,24284,24288,24313,24337,24727,26970,26971,52856,52857,52858,52859,52860,52861,52862,52863,52865,52901,52903,52904,52905,52906,54026,57844,64940,68912,68914,81727,81869,100298,154934,154935,154936,154937,154950,155135,155316,155317,155550,195447,195448,195449,195452,195453,195455,200836,2716,2717,2718,11992,18463,18467,24342,24452,25052,30171,30251,30323,52934,52935,52936,53658,54014,54015,54016,54024,54027,72481,90786,90884,90886,90887,91031,91111,92466,92467,94490,94503,100127,100128,100129,100299,100302,100303,100304,100305,100307,103035,116026,132515,150657,154426,154427,154954,154957,155315,155318,155586,163129,163130,164637,178924,182299,182544,182615,182617,182627,182630,182655,182658,182662,182665,182679,183399,183428,183435,195668,220129,220212,220214,220220,220333,220334,220427,220429,220520,220572,220573,220686,220688,229648,231687,231890,231893,232000,232002,232006,232008,232015,232027,2483,2604,2715,3402,24345,24364,24367,24407,24408,24414,24429,24444,24450,25054,25526,25528,25569,32621,41255,41337,41353,41621,41623,48162,53650,54017,60491,62575,62577,62580,62598,62621,62622,62625,62671,62672,62846,64389,69151,69152,91257,93230,100130,113015,113017,115849,143594,174145,202493,202494,202495,202534,202536,202537,202538,202539,202540,202543,202544,202545,202732,206511,206566,206589,206591,206644,206740,206786,206827,231811,231889,231894,231896,231897,231994,232021'><input type=hidden name=markercount value='239'><input type=hidden name=title value='Near 37.422160 -122.084270'><img src='Download-to-Computer-200.png' width=45 style='margin-bottom:-48px;margin-left:5px;'>&nbsp;<br>&nbsp;<br>&nbsp;<span style='color:white; text-shadow:-1px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000; font-weight:bold;font-size:85%;'>239</span><button id=downloadButon type=submit name=submit class=linkAsAbutton title='Touch to download all  239 markers on this list' style='margin-top: -10px; display: block;'>Download</button></form>";
    function pleasewait() {
        setTimeout('document.getElementById("ClickToDownload").innerHTML=""; document.getElementById("TheListItself").innerHTML="<center><b>Download Requested</b><br><br>You must stay on this page until the download completes—or it won’t complete.<br><br><i>Return to <a href=/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0 style=\'background-color:yellow;\'>this&nbsp;list</i>&nbsp;&#9651;</a></center>"', 100);
    }
</script>
<script>
    document.getElementById('ClickToMap').innerHTML = "<form method=post name=MapIt action=../map.asp style='display:inline;'><input type=hidden name=markers value='0,24125,58977,24122,24267,24255,220526,220576,220643,220641,218886,61492,52932,52931,52897,52933,52898,52899,52900,52866,52907,53041,195457,154933,2627,24284,24288,24313,24337,24727,26970,26971,52856,52857,52858,52859,52860,52861,52862,52863,52865,52901,52903,52904,52905,52906,54026,57844,64940,68912,68914,81727,81869,100298,154934,154935,154936,154937,154950,155135,155316,155317,155550,195447,195448,195449,195452,195453,195455,200836,2716,2717,2718,11992,18463,18467,24342,24452,25052,30171,30251,30323,52934,52935,52936,53658,54014,54015,54016,54024,54027,72481,90786,90884,90886,90887,91031,91111,92466,92467,94490,94503,100127,100128,100129,100299,100302,100303,100304,100305,100307,103035,116026,132515,150657,154426,154427,154954,154957,155315,155318,155586,163129,163130,164637,178924,182299,182544,182615,182617,182627,182630,182655,182658,182662,182665,182679,183399,183428,183435,195668,220129,220212,220214,220220,220333,220334,220427,220429,220520,220572,220573,220686,220688,229648,231687,231890,231893,232000,232002,232006,232008,232015,232027,2483,2604,2715,3402,24345,24364,24367,24407,24408,24414,24429,24444,24450,25054,25526,25528,25569,32621,41255,41337,41353,41621,41623,48162,53650,54017,60491,62575,62577,62580,62598,62621,62622,62625,62671,62672,62846,64389,69151,69152,91257,93230,100130,113015,113017,115849,143594,174145,202493,202494,202495,202534,202536,202537,202538,202539,202540,202543,202544,202545,202732,206511,206566,206589,206591,206644,206740,206786,206827,231811,231889,231894,231896,231897,231994,232021'><button type=submit name=submit class=linkAsAbutton title='Touch to map all 239 markers'><img src='google-maps-icon-200.png' width=50 style='margin-bottom:-23px;'><span style='color:white; text-shadow:-1px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000; font-weight:bold;'>239</span><br>Map All</button></form>";
</script>
<br>
239 entries matched your criteria. The first 100 are listed above.  <a href='/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&Page=2' style='background-color:yellow;'>Next &nbsp;100 &nbsp;
</i>&#8883;<i>
</a><div class=shim12pt>&nbsp;</div>
<div style='margin-top:-38px;'>&nbsp;</div>
</div id=TheListItself></article><script>
    function tagclick(tag, markerID) {
        eval("var tagValue=tag" + tag + "_" + markerID + ".innerHTML");
        tagValue = htmlentities.encode(tagValue);
        if (tagValue.search("1010") == -1) // -1 indicates NOT tagged
        {
            //it is NOT tagged so tag it.
            var H = new XMLHttpRequest();
            H.open("GET", "https://www.hmdb.org/tagwork.asp?task=taguntag&u=&m=" + markerID + "&t=" + tag + "&v=1", true);
            // true for asynchronous request
            H.onload = function(e) {
                // fires when request completes
                if (H.readyState == 4) {
                    if (H.responseText == "Success") {
                        tagValue = tagValue.replace("1011", "1010");
                        eval("tag" + tag + "_" + markerID + ".innerHTML=htmlentities.decode(tagValue)");
                    } else {
                        eval("tagAlertErrorMessage_" + markerID + ".innerHTML=H.responseText.slice(0,14)");
                        eval("tagAlert_" + markerID + ".style.visibility='visible';");
                        setTimeout(function() {
                            eval("tagAlert_" + markerID + ".style.visibility='hidden';")
                        }, 4000);
                    }
                }
            }
            H.send(null);
        } else {
            //it is tagged, so untag it. 
            var H = new XMLHttpRequest();
            H.open("GET", "https://www.hmdb.org/tagwork.asp?task=taguntag&u=&m=" + markerID + "&t=" + tag + "&v=0", true);
            // true for asynchronous request
            H.onload = function(e) {
                if (H.readyState == 4) {
                    if (H.responseText == "Success") {
                        tagValue = tagValue.replace("1010", "1011");
                        eval("tag" + tag + "_" + markerID + ".innerHTML=htmlentities.decode(tagValue)");
                    } else {
                        eval("tagAlertErrorMessage_" + markerID + ".innerHTML=H.responseText.slice(0,14)");
                        eval("tagAlert_" + markerID + ".style.visibility='visible';");
                        setTimeout(function() {
                            eval("tagAlert_" + markerID + ".style.visibility='hidden';")
                        }, 4000);
                    }
                }
            }
            H.send(null);
        }
        ;
    }
    ;
    (function(window) {
        window.htmlentities = {
            /**
		 * Converts a string to its html characters completely.
		 *
		 * @param {String} str String with unescaped HTML characters
		 **/
            encode: function(str) {
                var buf = [];

                for (var i = str.length - 1; i >= 0; i--) {
                    buf.unshift(['&#', str[i].charCodeAt(), ';'].join(''));
                }

                return buf.join('');
            },
            /**
		 * Converts an html characterSet into its original character.
		 *
		 * @param {String} str htmlSet entities
		 **/
            decode: function(str) {
                return str.replace(/&#(\d+);/g, function(match, dec) {
                    return String.fromCharCode(dec);
                });
            }
        };
    }
    )(window);
</script>
</div>
<!-- Main division -->
<div class=noprint style='text-align:center; margin:5pt 0 5pt 0;'>
    <i>
        <a href=https://www.cera.net target=_blank>CeraNet Cloud Computing</a>
        sponsors the &nbsp;Historical &nbsp;Marker &nbsp;Database.
    </i>
</div>
<script>
    // MAKE SURE ALL SUBMIT BUTTONS ARE ENABLED (SAFARI WORKAROUND)
    window.onpageshow = function() {
        var submitReset = setTimeout(function() {
            var submitButtons = document.querySelectorAll('input[type="submit"]');
            for (var i = 0; i < submitButtons.length; i++) {
                submitButtons[i].disabled = false;
            }
            ;
        }, 200);
    }
    ;
</script>
<fieldset class=adgeneral style='text-align:center;'>
    <!-- ADVERTISEMENT -->
    <legend class=adtitle>Paid Advertisements</legend>
    <!–– AMAZON OUTLET ––>
<iframe src="//rcm-na.amazon-adsystem.com/e/cm?o=1&p=42&l=ur1&category=outlet&banner=1YJSY3SCWBMYRP2GH3R2&f=ifr&linkID=b7886ceadb287405d799d492abdfabd2&t=historicalmar-20&tracking_id=historicalmar-20" width="234" height="60" scrolling="no" border="0" marginwidth="0" style="border:none;" frameborder="0" sandbox="allow-scripts allow-same-origin allow-popups allow-top-navigation-by-user-activation" style="float:left;"></iframe>
    <div style='float:left; margin-bottom:5px;height:60px;'>&nbsp;</div>
    <iframe src="//rcm-na.amazon-adsystem.com/e/cm?o=1&p=42&l=ur1&category=audible&banner=1XV00GG6WPZSB7KRJV02&f=ifr&linkID=e70580a0107b42f60ae3cbd78b733b2d&t=historicalmar-20&tracking_id=historicalmar-20" width="234" height="60" scrolling="no" border="0" marginwidth="0" style="border:none;" frameborder="0" sandbox="allow-scripts allow-same-origin allow-popups allow-top-navigation-by-user-activation" style="float:left;"></iframe>
    <div style='float:left; margin-bottom:5px;height:60px;'>&nbsp;</div>
    <iframe src="//rcm-na.amazon-adsystem.com/e/cm?o=1&p=40&l=ur1&category=software&banner=1R7QJN153Z5BQYACEGG2&f=ifr&linkID=487b425fd1689e66fe31c85e5aa1431c&t=historicalmar-20&tracking_id=historicalmar-20" width="120" height="60" scrolling="no" border="0" marginwidth="0" style="border:none;" frameborder="0" sandbox="allow-scripts allow-same-origin allow-popups allow-top-navigation-by-user-activation" style="float:left;"></iframe>
    <iframe src="//rcm-na.amazon-adsystem.com/e/cm?o=1&p=40&l=ur1&category=software&banner=0X68EXBM5AM509TS1WR2&f=ifr&linkID=223aa3535e72cb1cbb6e12641f627fe2&t=historicalmar-20&tracking_id=historicalmar-20" width="120" height="60" scrolling="no" border="0" marginwidth="0" style="border:none;" frameborder="0" sandbox="allow-scripts allow-same-origin allow-popups allow-top-navigation-by-user-activation" style="float:left;"></iframe>
    <hr style='border-top: 1px solid silver; margin-left:-13px; margin-right:-13px;'>
    <!–– GOOGLE HORIZONTAL AD ––> 
<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7431457857623754" crossorigin="anonymous"></script>
    <!-- Horizontal Ad -->
    <ins class="adsbygoogle" style="display:block" data-ad-client="ca-pub-7431457857623754" data-ad-slot="5075438879" data-ad-format="auto" data-full-width-responsive="true"></ins>
    <script>
        (adsbygoogle = window.adsbygoogle || []).push({});
    </script>
</fieldset>
<div class=copyright>
    <a href=copyright.asp>Copyright &nbsp;&copy;&nbsp;2006 &#8211;2023,&nbsp;Some &nbsp;rights &nbsp;reserved.</a>
    <span class=noprint>
        &#8212;<a href=privacypolicy.asp>Privacy &nbsp;Policy</a>
        &#8212;<a href=terms.asp>Terms &nbsp;of &nbsp;Use</a>
        &#8212;<a href=about.asp>About &nbsp;Us</a>
        &#8212;<a href='Commentadd.asp?MarkerID=9&Editor=1'>Contact &nbsp;Us</a>
    </span>
    <br>&nbsp;
</div>
<div id:logininfo style='position: absolute; top: 5px; right: 10px; width: 790px; height: 25px;  opacity:1; z-index: 100;text-align: right; visibility: visible; font-size:75%;line-height:100%; color=silver;' class='bodysansserif onlyprint'>Oct. 6, 2023</div>
</div>
<!-- BOTTOM OF THE WRAPPER -->
<script>
    function createCookie(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            var expires = "; expires=" + date.toGMTString();
        } else
            var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    }
    createCookie("browserwidth", window.innerWidth, 30)
</script>
<!-- GOOGLE ANALYTICS -->
<script>
    (function(i, s, o, g, r, a, m) {
        i['GoogleAnalyticsObject'] = r;
        i[r] = i[r] || function() {
            (i[r].q = i[r].q || []).push(arguments)
        }
        ,
        i[r].l = 1 * new Date();
        a = s.createElement(o),
        m = s.getElementsByTagName(o)[0];
        a.async = 1;
        a.src = g;
        m.parentNode.insertBefore(a, m)
    }
    )(window, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');

    ga('create', 'UA-38070968-1', 'auto');
    ga('send', 'pageview');
</script>
<div id="fb-root"></div>
<script>
    (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id))
            return;
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));
</script>
<script type="text/javascript">
    (function() {
        var po = document.createElement('script');
        po.type = 'text/javascript';
        po.async = true;
        po.src = 'https://apis.google.com/js/plusone.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(po, s);
    }
    )();
</script>
<script>
    // used to suppress side-rail ads when printing
    function beforePrint() {
        var sideads = document.getElementsByClassName('adsbygoogle');
        for (var i = 0; i < sideads.length; i++) {
            sideads[i].style.display = 'none';
        }
    }
</script>
<!-- script>document.write(" Screen width " + window.innerWidth + "px");
document.write("<br>Screen height " + window.innerHeight + "px");</script -->
</body></html>
"""
