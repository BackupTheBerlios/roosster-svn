/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */
 
// =========================================================================
//
// global variables / Constants
//
// =========================================================================

const DIV_ID_ENTRIESOUT   = "entries-out";
const FORM_ID_ENTRYFORM   = "entryform";

const PARAM_JUMP      = "jump";

const TAB_SEARCH = 'search-tab';
const TAB_EDIT   = 'edit-tab';
const TAB_ADD    = 'add-tab';
const TAB_TAGS   = 'tags-tab';

const DEFAULT_TAB     = TAB_SEARCH;

//
var currentTab = DEFAULT_TAB;

//
var entryList = new EntryList();

//
var currentEntry    = null;
var currentEntryUrl = null;

// needed for RoossterHttpState, don't make simultanious requests
var xmlhttp;

var httpstate = new RoossterHttpState();

var debugConsole = new Debug();

// =========================================================================
//
// Display Functions
//
// =========================================================================

  
/**
 * 
 */
function initClient() {
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
    var hidden = this.__hideTab(this.currentTab);
    var shown = this.__showTab(tabid);
    this.currentTab = tabid;
    return hidden && shown;
}    

    

/**
 * 
 */
function displayDate(date) {
    // TODO implement this
    return date;
}


// =========================================================================
//
// API Functions
//
// =========================================================================


/**
 * 
 */
function doSearch(queryStr) {
    __clearState(true);
        
    if ( queryStr == null || queryStr == '' )  
        return null;
  
    xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(queryStr) , true);     
    xmlhttp.onreadystatechange = searchResponseHandler;
    xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
    xmlhttp.send(null);   
}


/**
 * 
 */
function searchResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
      
    if ( httpstate.checkHttpState()  ) {
      
        entryList = parseEntryList(xmlhttp.responseXML);
        
        var entriesOut = getById(DIV_ID_ENTRIESOUT);
        
        if ( entriesOut == null )
            _exception("Can't find Element '"+DIV_ID_ENTRIESOUT+"' to output entries");
        
        var entriesFound = false;
        if ( entryList != null ) {
            var entries = entryList.getList();
            
            XmlRemoveAllChildren(entriesOut);
            
            var ulEntryList = XmlCreateElement("ul");
            ulEntryList.id = 'entry-list';
            entriesOut.appendChild(ulEntryList);
            
            for(var url in entries) {
                entriesFound = true;
                var li = XmlCreateElement("li");
                ulEntryList.appendChild(li);
                
                entries[url].attachAsList(li);
            }
        }
        
        if ( !entriesFound ) 
            entriesOut.appendChild( XmlCreateText("No Entries found for this search!") );
        
    } else {
        // TODO display better error 
        alert("error");
    }

    setTab(TAB_SEARCH);    
    debugConsole.show();
}


/**
 * 
 */
function postEdit() {
    __clearState(false);
    
    if ( currentEntry != null && currentEntry instanceof Entry ) {
        currentEntry.overwriteWithForm(document.entryform);
        
        xmlhttp.open("PUT", API_ENDPOINT + "/entry" , true);
        xmlhttp.onreadystatechange = putResponseHandler;
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send(currentEntry.asDomDocument());          
        
        setTab(TAB_EDIT);
    } else {
        // TODO make this better
        alert("Bad Error! Can't post non-existant entry!");
    }
  
    debugConsole.addMsg("Posting edited "+currentEntry);
    debugConsole.show();
}


/**
 * 
 */
function putResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
      
    if ( httpstate.checkHttpState()  ) {
        
        // TODO write this into message area
        alert("updated successfully");
        
    } else {
        // TODO display better error 
        alert("error");
    }
    setTab(TAB_EDIT);
    debugConsole.show();
}


/**
 * 
 */
function doEdit(url) {
    __clearState(false);
        
    if ( url == null && url == '' )
        __exception("You must provide an Entry's URL when you want to edit an Entry");   
    
    if ( entryList.get(url) ) {
      
        currentEntry = entryList.get(url)
        currentEntry.fillIntoEditForm(document.entryform);
        setTab(TAB_EDIT);
        debugConsole.addMsg("Showing edit form for cached "+currentEntry);
        debugConsole.show();
    
    } else {
      
        // entry is not cached, retrieve it from server and put it in cache
        
        xmlhttp.open("GET", API_ENDPOINT + "/entry?url="+ escape(url) , true);
        xmlhttp.onreadystatechange = function() {
        
            if ( xmlhttp.readyState == 4) {
              
                if ( !httpstate.checkHttpState()  ) {
                    // TODO display better error 
                    alert("error");
                }
                
                var list = parseEntryList(xmlhttp.responseXML);
                
                debugConsole.addMsg("List returned from /entry?url="+url+" is: "+list);
                
                currentEntry = list.get(url);
                currentEntry.fillIntoEditForm(document.entryform);
                
                setTab(TAB_EDIT);
                debugConsole.show();
           }
        };
        
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send(null);        
        
    }
}


/**
 *
 */
function doAdd(url) {
    __clearState(true);  
  
    if ( url != null && url != '' ) {
        currentEntryUrl = url;
      
        xmlhttp.open("POST", API_ENDPOINT + "/addurl", true);
        xmlhttp.onreadystatechange = postResponseHandler;        
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send( new Entry(url).asDomDocument() );
    }
}


/**
 * 
 */
function postResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return
     
    if ( httpstate.checkHttpState() ) {
        
        var list = parseEntryList(xmlhttp.responseXML);
        currentEntry = list.get(currentEntryUrl);
        currentEntry.fillIntoEditForm(document.entryform);
        
    } else {
        // TODO
        alert("error while posting, probably a duplicate entry! "+httpstate.lastExceptionText);
      
    }
    
    setTab(TAB_EDIT);
    debugConsole.show();    
}


// =========================================================================
//
// private Functions
//
// =========================================================================


/*
 * 
 */
function __clearState(withListAndEntry) {
    httpstate.clearState();
    xmlhttp = __newXmlHttp();
    
    if ( withListAndEntry ) {
        entryList = new EntryList();
        currentEntry = null;
        currentEntryUrl = null;
    }
}
    
    
/**
 * 
 */
function __newXmlHttp() { 
    var req = false;
    try {
      req = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        req = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (E) {
        req = false;
      }
    }
  
    if ( !req && typeof XMLHttpRequest != 'undefined' ) {
      req = new XMLHttpRequest();
    }
  
    return req;
}  


/**
 * 
 */
function __showTab(tabid) {
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
function __hideTab(tabid) {
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

