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



// an associative array of Entry-objects keyed by their respective URL
var entries = new EntryList();

var currentEntry = null;

var currentEntryUrl = null;

// Reference of the current tab being displayed
var currentTab = "search-tab";


/**
 *
 */
function init() { 
    _newXmlHttp();
  
    var jumpString = new Querystring().get(PARAM_JUMP);
    if ( jumpString ) {
        var oldTab = currentTab;
        if ( setTab(jumpString) )
            return;
        else 
            currentTab = oldTab; // switch back tab, because it has been set to jumpString
    }    
        
    setTab(currentTab); // use default tab if a wrong tab was specified
}


/**
 * 
 */
function setTab(tabid) {
    var hidden = _hideTab(currentTab);
    var shown = _showTab(tabid);
    currentTab = tabid;
    return hidden && shown;
}


/**
 *
 */
function doAdd(url) {
    // use form value as url to add if no explicit value is provided
    if ( url == null || url == '' ) 
        url = document.addform.url.value;
    
    if ( url != null && url != '' ) {
        currentEntryUrl = url;
      
        var xmlhttp = _newXmlHttp();
        xmlhttp.open("POST", API_ENDPOINT + "/addurl", true);
        xmlhttp.onreadystatechange = function() {
          
            if ( _checkHttpState(xmlhttp) ) {
              
                var list = new Array();
                _parseEntries(xmlhttp, list);
                
                if ( list != null ) {
                    
                    currentEntry = list[url];
                    currentEntry.fillIntoEditForm(document.entryform);
                    setTab('edit-tab');
                    
                    debugConsole.show();
                }
            }
            
        };
        
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send( new Entry(url).asDomDocument() );
    }
}


/**
 *
 */
function doSearch(queryString) {
  
    // use form value as query string if no explicit value is provided
    if ( queryString == null || queryString == '' ) 
        queryString = document.searchform.query.value;
  
    var xmlhttp = _newXmlHttp();
    xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(queryString) , true);
    xmlhttp.onreadystatechange = function() {
        
        if ( _checkHttpState(xmlhttp) ) {
        
            entries = new Array();
            _parseEntries(xmlhttp, entries);
        
            // display the search term in the input field
            document.searchform.query.value = queryString;
            
            var entriesOut = doc.getElementById(DIV_ID_ENTRIESOUT);
            
            if ( entriesOut == null )
                _exception("Can't find Element '"+DIV_ID_ENTRIESOUT+"' to output entries");
            
            
            if ( entries != null ) {
                
                removeAllChildren(entriesOut);
                
                var ulEntryList = doc.createElement("ul");
                ulEntryList.id = 'entry-list';
                entriesOut.appendChild(ulEntryList);
                
                var entriesFound = false;
                for(var url in entries) {
                    entriesFound = true;
                    var li = doc.createElement("li");
                    ulEntryList.appendChild(li);
                    
                    entries[url].attachAsList(li);
                }
                
                if ( !entriesFound ) {
                    entriesOut.appendChild( doc.createTextNode("No Entries found for this search!") );
                }
            }
            
            setTab('search-tab');
            
            debugConsole.show();
        }
    };
    
    xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
    xmlhttp.send(null);
    
}


/**
 * 
 */
function doEdit(url) {
    if ( url == null && url == '' )
        _exception("You must provide an Entry's URL when you want to edit an Entry");   
    
    currentEntryUrl = url;
    
    if ( entries[url] ) {
      
        currentEntry = entries[url];
        currentEntry.fillIntoEditForm(document.entryform);
        setTab('edit-tab');
    
    } else {
      
        // entry is not cached, retrieve it from server and put it in cache
        var xmlhttp = _newXmlHttp();
        xmlhttp.open("GET", API_ENDPOINT + "/entry?url="+ escape(url) , true);
        xmlhttp.onreadystatechange = function() {
        
            if ( _checkHttpState(xmlhttp) ) {
            
                var list = new Array();
                _parseEntries(xmlhttp, list);
                
                debugConsole.addMsg("List returned from /entry?url="+url+" is: "+list)
                
                currentEntry = list[url];
                entries[url] = currentEntry;
                
                currentEntry.fillIntoEditForm(document.entryform);
                setTab('edit-tab');
                debugConsole.show();
            }
        };
        
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send(null);        
        
    }
}


// ***********************************************************************
//
// private funciton
//
// ***********************************************************************


/**
 * 
 */
function _newXmlHttp() { 
    try {
        return new XMLHttpRequest();
    } catch (e) {
        xmlhttp=false;
        alert("XmlHttp Not supported. Using this application will go horribly wrong!");
    }
}

/**
 * this function checks the return code of the http request and  shows an error
 * if something went wrong 
 */
function _checkHttpState(xmlhttp) {
    debugConsole.addMsg("Checking HTTP state: "+xmlhttp.status);
    if ( xmlhttp.status >= 400 ) {
        var isHandled = false;
        
        var exceptionText = _decodeException(xmlhttp.statusText);
        debugConsole.addMsg("HTTP status text: "+exceptionText);
        
        if ( xmlhttp.status == 500 ) {
          
            if ( _startsWith(exceptionText, EXC_DUPLICATE) ) {
                isHandled = true;
                doEdit(currentEntryUrl);
            }
            
        } else if ( xmlhttp.status == 404 ) {
        } else if ( xmlhttp.status == 400 ) {
        }
        
        if ( !isHandled ) {
            _exception("An Error occurred while communicating with the server:\n"+exceptionText);
            return false;
        }
    } 
    
    return true;    
}


/**
 * 
 */
function _decodeException(exceptionString) {
    exceptionString = unescape(exceptionString);
    exceptionString = exceptionString.replace(/\+/g, " ");
    return exceptionString;
}


/**
 *
 */
function _parseEntries(xmlhttp, list) {
    
    if ( list != null || list instanceof Array ) {
  
        debugConsole.addMsg("Response: "+xmlhttp.responseText);
        
        var doc = xmlhttp.responseXML;
        
        if ( doc != null ) {
            var elemList = doc.getElementsByTagName(TAG_ENTRY); // NodeList
            for (var i = 0; i < elemList.length; i++) {
                var entryElem = elemList.item(i);
                
                var e = _buildSingleEntry(entryElem);
                if ( e != null )
                    list[e.url] = e;
            }
        } else {
            debugConsole.addMsg("WARNING: HTTP response contained no XML");
        }
        
        return list;
        
    } else {
        _exception("Parameter 'list' must be not-null and an  Array");
    }
}


/**
 * Build Entry-Object from an <entry> DOM-Element
 *
 * @param entryElem is of type Element
 * @return an Entry object
 * @exception if the provided parameter is not an instance of Element
 */
function _buildSingleEntry(entryElem) {
  
    if ( entryElem instanceof Element ) {
        var entry = new Entry( entryElem.getAttribute(ATTR_HREF) );
        
        entry.title       = entryElem.getAttribute(ATTR_TITLE) || "";
        entry.type        = entryElem.getAttribute(ATTR_TYPE) || "";

        // TODO make real dates here 
        entry.issuedDate    = entryElem.getAttribute(ATTR_ISSUED);
        entry.modifiedDate  = entryElem.getAttribute(ATTR_MODIFIED);
        entry.fetchedDate   = entryElem.getAttribute(ATTR_FETCHED);
        entry.editedDate    = entryElem.getAttribute(ATTR_EDITED);
        
        
        entry.content     = getChildsText(entryElem, TAG_CONTENT) || "";
        entry.note        = getChildsText(entryElem, TAG_NOTE) || "";
        
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
        
        return entry;
    } else {
        _exception("function buildEntry accepts only parameters of type 'Element'!");
    }
}


/**
 * 
 */
function _showTab(tabid) {
    tabid = document.getElementById(tabid);
    if ( tabid ) {
        if (tabid.style)
            tabid.style.display="block";
        else
            tabid.display="block";
        
        return true;
    }
    
    return false;
}


/**
 * 
 */
function _hideTab(tabid) {
    tabid = document.getElementById(tabid);
    if ( tabid ) {
        if (tabid.style) 
            tabid.style.display = "none";
        else 
            tabid.display = "none";
        
        return true;
    }
    
    return false;
}


/**
 * 
 */
function _print() {
    var text = '';
    for(var i = 0; i < arguments.length; i++) {
        for(var k in arguments[i]) {
            text += k+": "+arguments[i][k]+", ";
        }
    }
    
    return text;
}


