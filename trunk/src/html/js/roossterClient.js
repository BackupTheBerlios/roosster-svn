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

// an associative array of Entry-objects keyed by their respective URL
var entries = new EntryList();

var currentEntry = null;

// Reference of the current tab being displayed
var currentTab = "search-tab";

if ( !xmlhttp ) {
    try {
        xmlhttp = new XMLHttpRequest();
    } catch (e) {
        xmlhttp=false;
        alert("XmlHttp Not supported. Using this application will go horribly wrong!");
    }
}


/*
 *
 */
function init() { showTab(currentTab); }


/**
 * 
 */
function showTab(tabid) {
    tabid = document.getElementById(tabid);
    if (tabid) {
        if (tabid.style)
            tabid.style.display="block";
        else
            tabid.display="block";
    }
}


/**
 * 
 */
function hideTab(tabid) {
    tabid = document.getElementById(tabid);
    if (tabid) {
        if (tabid.style) 
            tabid.style.display="none";
        else 
            tabid.display="none";
    }
}


/**
 * 
 */
function setTab(tabid) {
    hideTab(currentTab);
    showTab(tabid);
    currentTab = tabid;
    return false;
}


/*
 *
 */
function doSearch(queryString) {
  
    // use form value as query string if no explicit value is provided
    if ( queryString == null || queryString == '' ) 
        queryString = document.searchform.query.value;
  
		xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(queryString) , false);
		xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
		xmlhttp.send(null);
    
    _parseEntries();

    // display the search term in the input field
    document.searchform.query.value = queryString;
    
    if ( entries != null ) {
        var entriesOut = doc.getElementById(DIV_ID_ENTRIESOUT);
        
        if ( entriesOut == null )
            exception("Can't find Element '"+DIV_ID_ENTRIESOUT+"' to output entries");
        
        removeAllChildren(entriesOut);
        
        var ulEntryList = doc.createElement("ul");
        ulEntryList.id = 'entry-list';
        entriesOut.appendChild(ulEntryList);
        
        for(var i in entries) {
            var li = doc.createElement("li");
            ulEntryList.appendChild(li);
            
            entries[i].attachAsList(li);
        }
    }
    
    setTab('search-tab');
    
    debugConsole.show();
}


/**
 * 
 */
function doEdit(url) {
    if ( url == null && url == '' )
        exception("You must provide an Entry's URL when you want to edit an Entry");   
    
    setTab('edit-tab');
    currentEntry = entries[url];
    currentEntry.fillIntoEditForm(document.entryform);
}


// ***********************************************************************
//
// private funciton
//
// ***********************************************************************

/*
 *
 */
function _parseEntries() {
    var doc = xmlhttp.responseXML;
    
    // clear old entry list
    entries = new Array();
    
    var list = doc.getElementsByTagName(TAG_ENTRY); // NodeList
    for (var i = 0; i < list.length; i++) {
        var entryElem = list.item(i);
        
        var e = _buildSingleEntry(entryElem);
        if ( e != null )
            entries[e.url] = e;
    }
}


/**
 * Build Entry-Object from an <entry> DOM-Element
 *
 * @param entryElem is of type Element
 */
function _buildSingleEntry(entryElem) {
  
    if ( entryElem instanceof Element ) {
        var entry = new Entry( entryElem.getAttribute(ATTR_HREF) ); 
        
        entry.title       = getChildsText(entryElem, TAG_TITLE) || "";
        entry.content     = getChildsText(entryElem, TAG_CONTENT) || "";
        entry.note        = getChildsText(entryElem, TAG_NOTE) || "";
        entry.type        = getChildsText(entryElem, TAG_TYPE) || "";
        
        // TODO get authors here
        //entry.author      = getChildsText(entryElem, TAG_TYPE) || "";
        //entry.authorEmail = getChildsText(entryElem, TAG_TYPE) || "";
        
        
        
        // <entry> is allowed to have only one <tags> childnode
        var tagsElem = entryElem.getElementsByTagName(TAG_TAGS)[0];
        
        if ( tagsElem != null ) {
            var tags = getChildsText(tagsElem, TAG_TAG)
            
            if ( tags instanceof Array ) {
                entry.tags = tags;
            } else {
                entry.tags = tags ? new Array(tags) : new Array();
            }
        }
        
        // TODO make real dates here 
        entry.issuedDate    = getChildsText(entryElem, TAG_ISSUED) || "";
        entry.modifiedDate  = getChildsText(entryElem, TAG_MODIFIED) || "";
        entry.fetchedDate   = getChildsText(entryElem, TAG_FETCHED) || "";
        
        return entry;
    } else {
        exception("function buildEntry accepts only parameters of type 'Element'!");
    }
}

