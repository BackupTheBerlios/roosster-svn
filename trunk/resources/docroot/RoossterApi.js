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

var TAG_ENTRYLIST   = 'entrylist';
var TAG_ENTRY       = 'entry';
var TAG_AUTHOR      = 'author';
var TAG_TAG         = 'tag';
var TAG_NOTE        = 'note';
var TAG_CONTENT     = 'content';

var ATTR_TYPE       = 'type';
var ATTR_TITLE      = 'title';
var ATTR_ISSUED     = 'issued';
var ATTR_MODIFIED   = 'modified';
var ATTR_ADDED      = 'added';
var ATTR_EDITED     = 'edited';
var ATTR_HREF       = 'href';
var ATTR_NAME       = 'name';
var ATTR_EMAIL      = 'email';
var ATTR_PUBLIC     = 'public';

var ATTR_TOTAL      = 'total';
var ATTR_LIMIT      = 'limit';
var ATTR_OFFSET     = 'offset';

var API_CTYPE    = "text/xml";


//*****************************************************************************
// Roosster Classes
//*****************************************************************************

function EntryList(total, limit, offset) {
    
    this.total  = parseInt(total) || -1;
    this.limit  = parseInt(limit) || -1;
    this.offset = parseInt(offset) || -1;
  
    this.__listCount = 0;
    
    this.list = {};

    this.length = function() { return this.__listCount; }
    
    this.put = function(key, value) { this.list[key] = value; this.__listCount++; }
    
    this.del = function(key) { 
        delete this.list[key]; 
        this.__listCount--; 
        this.total--;
    }
    
    this.get = function(key) { return this.list[key];  }
    
    this.getList = function() { return this.list; }
}

function Entry(url) {
    if ( url == null || url == '' )
        throw "Can't create an Entry-instance without URL";
    
    this.url = url;
    
    this.title        = '';
    this.note         = '';
    this.tags         = new Array();
    this.type         = '';
    this.pub          = false;
    this.author       = '';
    this.authorEmail  = '';
    this.issuedDate   = null;
    this.modifiedDate = null;
    this.addedDate    = null;
    this.editedDate   = null;
    
    this.asDomDocument = function() {
        var xmlDoc = XmlCreateDocument();
		
        var elemEntryList = xmlDoc.createElement(TAG_ENTRYLIST);
        xmlDoc.appendChild(elemEntryList);
        
        var elemEntry = xmlDoc.createElement(TAG_ENTRY);
        elemEntry.setAttribute(ATTR_HREF, this.url);
        elemEntry.setAttribute(ATTR_TITLE, this.title);
        elemEntry.setAttribute(ATTR_TYPE, this.type);
        elemEntry.setAttribute(ATTR_PUBLIC, this.pub == true ? 'true' : 'false' );
        elemEntryList.appendChild(elemEntry);
        
        var elemAuthor = xmlDoc.createElement(TAG_AUTHOR);
        elemAuthor.setAttribute(ATTR_NAME, this.author);
        elemAuthor.setAttribute(ATTR_EMAIL, this.authorEmail);
        elemEntry.appendChild(elemAuthor);
        
        var elemNote = xmlDoc.createElement(TAG_NOTE);
        elemNote.appendChild( xmlDoc.createTextNode(this.note) );
        elemEntry.appendChild(elemNote);
        
        for ( var i = 0; i < this.tags.length; i++) {
            var elemTag = xmlDoc.createElement(TAG_TAG);
            elemTag.appendChild( xmlDoc.createTextNode(this.tags[i]) );
            elemEntry.appendChild(elemTag);
        }
        
        return xmlDoc;
    }
    
}

//*****************************************************************************
// Roosster API specific XML Functions
//*****************************************************************************

function parseEntryList(entryListDoc) {
    if ( entryListDoc == null )
        return null;

    var entryListElem = entryListDoc.documentElement;
    var list = new EntryList(entryListElem.getAttribute(ATTR_TOTAL),
                             entryListElem.getAttribute(ATTR_LIMIT),
                             entryListElem.getAttribute(ATTR_OFFSET) );
    
    var elemList = entryListElem.getElementsByTagName(TAG_ENTRY); 
    for (var i = 0; i < elemList.length; i++) {
        var entryElem = elemList.item(i);
        
        var e = __buildSingleEntry(entryElem);
        list.put(e.url, e);
    }
    
    return list;
}
    
function __buildSingleEntry(entryElem) {
  
    var entry = new Entry( entryElem.getAttribute(ATTR_HREF) );
    
    entry.title       = entryElem.getAttribute(ATTR_TITLE) || "";
    entry.type        = entryElem.getAttribute(ATTR_TYPE) || "";
    entry.pub         = entryElem.getAttribute(ATTR_PUBLIC) == 'true' ? true : false;

    // TODO make real dates here 
    entry.issuedDate    = parseW3cDate(entryElem.getAttribute(ATTR_ISSUED));
    entry.modifiedDate  = parseW3cDate(entryElem.getAttribute(ATTR_MODIFIED));
    entry.addedDate     = parseW3cDate(entryElem.getAttribute(ATTR_ADDED));
    entry.editedDate    = parseW3cDate(entryElem.getAttribute(ATTR_EDITED));
    
    // <content> and <note>
    entry.content     = XmlGetChildsText(entryElem, TAG_CONTENT) || "";
    entry.note        = XmlGetChildsText(entryElem, TAG_NOTE) || "";
    
    // <author>
    var authorList = entryElem.getElementsByTagName(TAG_AUTHOR);
    if ( authorList != null && authorList.length > 0) {
        var authorElem = authorList.item(0);
        
        entry.author      = authorElem.getAttribute(ATTR_NAME) || "";
        entry.authorEmail = authorElem.getAttribute(ATTR_EMAIL) || "";
    }
    
    // <tag>
    var tags = XmlGetChildsText(entryElem, TAG_TAG)
    if ( tags instanceof Array ) 
        entry.tags = tags;
    else 
        entry.tags = tags ? new Array(tags) : new Array();
    
    return entry;
}



//*****************************************************************************
// HELPER CODE
//*****************************************************************************

/**
 * parses Dates in the format '2005-02-23T00:00:00+01:00'
 * omits timezones
 */
function parseW3cDate(dateStr) {
    
    if ( dateStr == null || dateStr == '' )
        return null;
    
    var date = null;
    
    var dateAndTime = dateStr.split("T");
    if ( dateAndTime.length == 2 ) {
        var date = new Date();
        var ymd = dateAndTime[0].split("-"); // year,month,day
        if ( ymd.length == 3 ) {
            date.setFullYear(ymd[0]);
            date.setMonth(parseInt(ymd[1]));
            date.setDate(ymd[2]);
        } else {
            date = null;
        }
        
        var hms = dateAndTime[1].split(":"); // hour,minute,second
        if ( hms.length < 3 ) {
            date.setHours(hms[0]);
            date.setMinutes(hms[1]);
            date.setSeconds(hms[2]);
        } else {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
        }
        date.setMilliseconds(0);
    } 

    return date;
}


/**
 * 
 */
function formatAsW3cDate(date) {
    var date = new Date();
    
    var day = new String(date.getUTCDate());
    day = day.length > 1 ? day : "0"+day;
    
    var month = new String(date.getUTCMonth()+1);
    month = month.length > 1 ? month : "0" + month;
    
    var hours =date.getUTCHours();
    hours = hours.length > 1 ? hours : "0" + hours;
    
    var time = hours + ":" + date.getUTCMinutes() + ":" + date.getUTCSeconds();
    
    return date.getUTCFullYear() +"-"+ month +"-"+ day +"T"+ time;
}


