
const ATOM_CTYPE       = "application/x.atom+xml";
const ATOM_ENTRY_POINT = '$baseurl/atom';

var xmlhttp;

if ( !xmlhttp ) {
    try {
        xmlhttp = new XMLHttpRequest();
    } catch (e) {
        xmlhttp=false;
        alert("XmlHttp Not supported.");
    }
}


function doSearch() {
		xmlhttp.open("GET", ATOM_ENTRY_POINT + "/search?query="+ escape(document.searchform.query.value) , false);
		//xmlhttp.onreadystatechange = requestFeed;
		xmlhttp.setRequestHeader("Content-Type", ATOM_CTYPE);
		xmlhttp.send(null);
    
    alert("Content2:" + xmlhttp.responseXML);
}


function init() {}
