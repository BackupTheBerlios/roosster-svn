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

const DIV_ID_OUTPUTMESSAGES  = "output-messages";
const DIV_ID_ENTRIESOUT      = "entries-out";
const DIV_ID_LOADINGNOTE     = "loading-notification";
const DIV_ID_ENTRYURLLINK    = "entry-url-link";
const DIV_ID_CACHEDENTRYLINK = 'cachedentrylink';
const FORM_ID_ENTRYFORM      = "entryform";

const ID_HEADER_QUERYSTRING = 'headerQueryStr';
const ID_QUERYSTRING        = 'queryStr';

const PARAM_JUMP      = "jump";
const PARAM_ACTION    = "action";
const PARAM_URL       = "url";
const PARAM_QUERY     = "query";

const TAB_SEARCH = 'search-tab';
const TAB_EDIT   = 'edit-tab';
const TAB_ADD    = 'add-tab';
const TAB_TAGS   = 'tags-tab';
const TAB_TOOLS  = 'tools-tab';

const TABMENU_SEARCH = 'searchTabMenu';
const TABMENU_EDIT   = 'editTabMenu';
const TABMENU_ADD    = 'addTabMenu';
const TABMENU_TAGS   = 'tagsTabMenu';
const TABMENU_TOOLS  = 'toolsTabMenu';

const DEFAULT_TAB     = TAB_SEARCH;

//
var currentTab     = DEFAULT_TAB;
var currentTabMenu = null;

//
var entryList = new EntryList();

// Entry-object, contains the currently edited entry
var currentEntry    = null;

// String, contains the url of the currently edited entry or the last added URL
var currentEntryUrl = null;

// String, contains the last search's query String
var lastQuery = null;

// needed for RoossterHttpState, don't make simultanious requests
var xmlhttp;

var httpstate = new RoossterHttpState();

var debugConsole = new Debug();

var outputMsgElem = null;

// =========================================================================
//
// Display Functions
//
// =========================================================================

  
/**
 * 
 */
function initClient() {
    outputMsgElem = getById(DIV_ID_OUTPUTMESSAGES);
    
    var qs = new Querystring();
    var jumpString = qs.get(PARAM_JUMP);
    if ( jumpString ) {
        var oldTab = currentTab;
        if ( setTab(jumpString) )
            return;
        else 
            currentTab = oldTab; // switch back tab, because it has been set to jumpString
    } else {  
        setTab(currentTab); // use default tab if a wrong tab was specified
    }
    
    var queryStr = qs.get(PARAM_QUERY);
    if ( queryStr != null ) {
        doSearch(queryStr);
        return;
    }
    
    var actionStr = qs.get(PARAM_ACTION);
    var urlStr = qs.get(PARAM_URL);
    if ( actionStr != null && urlStr != null ) {
        if ( actionStr == 'add' )
            doAdd(urlStr);
        else if ( actionStr == 'edit' ) 
            doEdit(urlStr);
    }
}
    

/**
 * 
 */
function setTab(tabid, clearOutputMessages) {
    // clear messages, except if it's explicitly specified that they should stay  
    if ( clearOutputMessages != false )
        __clearOutputMessages();
  
    var hidden = this.__hideTab(this.currentTab);
    var shown = this.__showTab(tabid);
    this.currentTab = tabid;
    if ( hidden && shown ) {
        switch(this.currentTab) {
            case TAB_SEARCH:
                if ( this.currentTabMenu != null ) getById(this.currentTabMenu).className = '';
                getById(TABMENU_SEARCH).className = 'active';
                this.currentTabMenu = TABMENU_SEARCH;
                break;
            case TAB_EDIT:
                if ( this.currentTabMenu != null ) getById(this.currentTabMenu).className = '';
                getById(TABMENU_EDIT).className = 'active';
                this.currentTabMenu = TABMENU_EDIT;
                break;
            case TAB_ADD:
                if ( this.currentTabMenu != null ) getById(this.currentTabMenu).className = '';
                getById(TABMENU_ADD).className = 'active';
                this.currentTabMenu = TABMENU_ADD;
                break;
            case TAB_TAGS:
                if ( this.currentTabMenu != null ) getById(this.currentTabMenu).className = '';
                getById(TABMENU_TAGS).className = 'active';
                this.currentTabMenu = TABMENU_TAGS;
                break;
            case TAB_TOOLS:
                if ( this.currentTabMenu != null ) getById(this.currentTabMenu).className = '';
                getById(TABMENU_TOOLS).className = 'active';
                this.currentTabMenu = TABMENU_TOOLS;
                break;
        }
    } else {
        return false;
    }
}    


/**
 * 
 */
function displayDate(date) {
    if ( date == null ) 
        return "no date";
  
    if ( date instanceof Date ) {
        var day = new String(date.getDate());
        day = day.length > 1 ? day : "0"+day;
        
        var month = new String(date.getMonth());
        month = month.length > 1 ? month : "0"+month;
        
        var hours = new String(date.getHours());
        hours = hours.length > 1 ? hours : "0"+hours;
        
        var minutes = new String(date.getMinutes());
        minutes = minutes.length > 1 ? minutes : "0"+minutes;
        
        return day+"/"+month+"/"+date.getFullYear()+" "+hours+":"+minutes;
        
    } else {
        return "no date";
    }
}


/**
 * 
 */
function toggleDisplay(currId) {
		thisMenu = getById(currId).style;
    thisMenu.display =  thisMenu.display == "block" ? "none" : "block";
		return false;
}

// =========================================================================
//
// API Functions
//
// =========================================================================


/**
 * 
 */
function doDelete(url) {
    __clearAll();
        
    if ( url == null || url == '' ) 
        return null;
  
    toggleDisplay(DIV_ID_LOADINGNOTE);
    xmlhttp.open("DELETE", API_ENDPOINT + "/delete?url="+ escape(url) , true);     
    xmlhttp.onreadystatechange = deleteResponseHandler;
    xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
    xmlhttp.send(null);
}


/**
 * 
 */
function deleteResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;

    if ( httpstate.checkHttpState()  ) {
        __outputMessage("Entry deleted successfully");
    } else {
        __outputMessage("Error while executing DELETE! Server said: <"+httpstate.lastExceptionText+">", true);
    }
    
    toggleDisplay(DIV_ID_LOADINGNOTE);
    debugConsole.show();    
}


/**
 * 
 */
function doSearch(queryStr, offset, limit) {
    __clearAll();
        
    if ( queryStr == null || queryStr == '' ) {
        if ( lastQuery )
            queryStr = lastQuery;
        else
            return null;
    }
  
    var pagerArgs = offset ? "&output.offset="+ offset : "";
    pagerArgs += limit ? "&output.limit="+limit : "";
    
    toggleDisplay(DIV_ID_LOADINGNOTE);
    xmlhttp.open("GET", API_ENDPOINT + "/search?query="+ escape(queryStr)+pagerArgs , true);     
    xmlhttp.onreadystatechange = searchResponseHandler;
    xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
    xmlhttp.send(null);
    
    lastQuery = queryStr;
    getById(ID_HEADER_QUERYSTRING).value = queryStr;
    getById(ID_QUERYSTRING).value = queryStr;
}


/**
 * 
 */
function searchResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
      
    if ( httpstate.checkHttpState()  ) {
        
        var entriesOut = getById(DIV_ID_ENTRIESOUT);
        XmlRemoveAllChildren(entriesOut);
        
        if ( httpstate.lastHttpState == 204 ) {
            entriesOut.appendChild( XmlCreateText("No Entries found for this search!") );
        } else {
      
          entryList = parseEntryList(xmlhttp.responseXML);

          entryList.attachPager(entriesOut);
          
          var entries = entryList.getList();
          
          var ulEntryList = XmlCreateElement("ul");
          ulEntryList.id = 'entry-list';
          entriesOut.appendChild(ulEntryList);
          
          for(var url in entries) {
              var li = XmlCreateElement("li");
              ulEntryList.appendChild(li);
              entries[url].attachAsList(li);
          }
        }
        
        setTab(TAB_SEARCH);    
    } else {
        __outputMessage("Error while executing search! Server said: <"+httpstate.lastExceptionText+">", true);
    }
    
    toggleDisplay(DIV_ID_LOADINGNOTE);
    debugConsole.show();
}


/**
 * 
 */
function doEdit(url, clearOutputMsg) {
    __clearHttp(false);
    if ( clearOutputMsg )
        __clearOutputMessages();
        
    if ( url == null && url == '' )
        __exception("You must provide an Entry's URL when you want to edit an Entry");   
    
    toggleDisplay(DIV_ID_LOADINGNOTE);
    currentEntryUrl = url;    
    xmlhttp.open("GET", API_ENDPOINT + "/entry?url="+ escape(url) , true);
    xmlhttp.onreadystatechange = getEntryResponseHandler;
    xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
    xmlhttp.send(null);        

    setTab(TAB_EDIT, clearOutputMsg);
}


/**
 * 
 */
function getEntryResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
      
    if ( httpstate.checkHttpState()  ) {
      
        if ( httpstate.lastHttpState == 204 ) { 
            setTab(TAB_EDIT, true);
            __clearEntryForm();
            __outputMessage("Could not find an entry for URL "+currentEntryUrl, true);
        } else {
            var list = parseEntryList(xmlhttp.responseXML);
            currentEntry = list.get(currentEntryUrl);
            currentEntry.fillIntoEditForm(document.entryform);
            debugConsole.addMsg("List returned from /entry?url="+currentEntryUrl+" is: "+list);
        }
        
        
    } else {
        __outputMessage("Can't fetch Entry for URL: "+currentEntryUrl+
                        " ! Server said: <"+httpstate.lastExceptionText+">", true);
    }
    
    toggleDisplay(DIV_ID_LOADINGNOTE);
    debugConsole.show();
}


/**
 * 
 */
function putEdit() {
    __clearHttp();
    
    if ( currentEntry != null && currentEntry instanceof Entry ) {
        toggleDisplay(DIV_ID_LOADINGNOTE);
        currentEntry.overwriteWithForm(document.entryform);
        
        xmlhttp.open("PUT", API_ENDPOINT + "/entry" , true);
        xmlhttp.onreadystatechange = putResponseHandler;
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send(currentEntry.asDomDocument());          
        
        setTab(TAB_EDIT);
        debugConsole.addMsg("Posting edited "+currentEntry);
    } else {
        __outputMessage("Can't edit non-existant entry!", true);
    }
  
    debugConsole.show();
}


/**
 * 
 */
function putResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
      
    if ( httpstate.checkHttpState()  ) 
        __outputMessage("Entry updated successfully");
    else 
        __outputMessage("Error while trying to update! Server said: <"+httpstate.lastExceptionText+">", true);
    
    setTab(TAB_EDIT, false);
    toggleDisplay(DIV_ID_LOADINGNOTE);
    debugConsole.show();
}


/**
 *
 */
function doAdd(url) {
    __clearAll();  
    
    if ( url != null && url != '' ) {
        currentEntryUrl = url;
        toggleDisplay(DIV_ID_LOADINGNOTE);
        xmlhttp.open("POST", API_ENDPOINT + "/entry", true);
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
        
        if ( list == null ) {
            __outputMessage("Some undefined error occurred while adding URL "+currentEntryUrl, true);
            return;
        }
          
        if ( list.length() > 1 ) {
            __outputMessage("URL "+currentEntryUrl+" represented a feed! "+list.length()+" URLs were added");
        } else {
            __outputMessage("Successfully added URL '"+currentEntryUrl +"'!");
            doEdit(currentEntryUrl, false);
        }
        
    } else {
        if ( httpstate.lastException == EXC_DUPLICATE ) {
            debugConsole.addMsg("Editing instead of adding duplicate Entry "+httpstate.lastExceptionText);
            __outputMessage("Entry with URL '"+httpstate.lastExceptionText+
                            "' was previously added. Loaded Entry-data!");
            doEdit(httpstate.lastExceptionText, false);
        } else {
            __outputMessage("Error while trying to add Entry! Server said: <"+httpstate.lastExceptionText+">", true);
        } 
      
    }
    toggleDisplay(DIV_ID_LOADINGNOTE);
    debugConsole.show();    
}


// =========================================================================
//
// private Functions
//
// =========================================================================


/**
 * 
 */
function __clearHttp(withListAndEntry) {
    httpstate.clearState();
    xmlhttp = __newXmlHttp();
}

function __clearOutputMessages() {
    XmlRemoveAllChildren(outputMsgElem);
}

function __clearEntries() {
    entryList = new EntryList();
    currentEntry = null;
    currentEntryUrl = null;
}

function __clearEntryForm() {
    document.entryform.url.value = '';
    document.entryform.title.value = '';
    document.entryform.type.value = '';
    document.entryform.tags.value = '';
    document.entryform.author.value = '';
    document.entryform.authorEmail.value = '';
    document.entryform.note.value = '';
    document.entryform.content.value = '';
    document.entryform.issuedDate.value =  '';
    document.entryform.modifiedDate.value = '';
    document.entryform.addedDate.value = '';
    document.entryform.editedDate.value = '';
    XmlRemoveAllChildren(getById(DIV_ID_CACHEDENTRYLINK));
    XmlRemoveAllChildren(getById(DIV_ID_ENTRYURLLINK));
}

function __clearAll() {
    __clearEntries();
    __clearHttp();
    __clearOutputMessages();
}


/**
 *
 */
function __outputMessage(msg, error) {
    var p = XmlCreateElement('p');
    p.className = error ? 'error-messages' : 'output-messages';
    p.appendChild(XmlCreateText(msg));
    outputMsgElem.appendChild(p);
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
    tabid = getById(tabid);
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
    tabid = getById(tabid);
    if ( tabid ) {
        if (tabid.style) 
            tabid.style.display = "none";
        else 
            tabid.display = "none";
        
        return true;
    }
    
    return false;
}

