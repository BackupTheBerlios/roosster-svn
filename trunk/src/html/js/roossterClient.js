/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */
 
 
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


/*
 *
 */
function doSearch() {
		xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(document.searchform.query.value) , false);
		//xmlhttp.onreadystatechange = parseFeed;
		xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
		xmlhttp.send(null);
    
    parseEntries();

    debugConsole.show();
}


/*
 *
 */
function parseEntries() {
    var doc = xmlhttp.responseXML;
    
    var list = doc.getElementsByTagName(TAG_ENTRY); // NodeList
    for (var i = 0; i < list.length; i++) {
        var entryElem = list.item(i);
        entries.push( buildEntry(entryElem) );
    }
    
    displayEntries();
    
    //buildEntry( new Date() );
}


/**
 * 
 */
function displayEntries() {
    if ( entries != null ) {
        var entriesOut = doc.getElementById(DIV_ID_ENTRIESOUT);
        
        for(var i = 0; i < entries.length; i++) {
            entries[i].attach(entriesOut);
        }
    }
}

/**
 * Build Entry-Object from an <entry> DOM-Element
 *
 * @param entryElem is of type Element
 */
function buildEntry(entryElem) {
  
    if ( entryElem instanceof Element ) {
        var entry = new Entry( entryElem.getAttribute(ATTR_HREF) ); 
        
        entry.title     = getChildsText(entryElem, TAG_TITLE) || "";
        entry.content   = getChildsText(entryElem, TAG_CONTENT) || "";
        entry.note      = getChildsText(entryElem, TAG_NOTE) || "";
        
        return entry;
    } else {
        exception("function buildEntry accepts only parameters of type 'Element'!");
    }
}


/*
 *
 */
function init() {}
