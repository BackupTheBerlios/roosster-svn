
const API_CTYPE    = "text/xml";
const API_ENDPOINT = '$baseurl/api';

var xmlhttp;

var entries = new Array();

if ( !xmlhttp ) {
    try {
        xmlhttp = new XMLHttpRequest();
    } catch (e) {
        xmlhttp=false;
        alert("XmlHttp Not supported.");
    }
}


function doSearch() {
		xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(document.searchform.query.value) , false);
		//xmlhttp.onreadystatechange = parseFeed;
		xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
		xmlhttp.send(null);
    
    parseFeed();
}


function parseFeed() {
    alert("Content2:" + xmlhttp.responseXML);
    
}

function init() {}
