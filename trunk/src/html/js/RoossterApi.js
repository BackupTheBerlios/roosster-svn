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

const TAG_ENTRYLIST   = 'entrylist';
const TAG_ENTRY       = 'entry';
const TAG_AUTHOR      = 'author';
const TAG_TAG         = 'tag';
const TAG_NOTE        = 'note';
const TAG_CONTENT     = 'content';

const ATTR_TYPE       = 'type';
const ATTR_TITLE      = 'title';
const ATTR_ISSUED     = 'issued';
const ATTR_MODIFIED   = 'modified';
const ATTR_FETCHED    = 'fetched';
const ATTR_EDITED     = 'edited';
const ATTR_HREF       = 'href';
const ATTR_NAME       = 'name';
const ATTR_EMAIL      = 'email';

const ATTR_TOTAL      = 'total';
const ATTR_LIMIT      = 'limit';
const ATTR_OFFSET     = 'offset';

const EXC_DUPLICATE   = "org.roosster.store.DuplicateEntryException";

const ROOSSTER_EXCEPTION = "RoossterException: ";

const API_CTYPE    = "text/xml";
const API_ENDPOINT = '$baseurl/api';


// =========================================================================
//
// Roosster API specific XML Functions
//
// =========================================================================


/**
 * 
 */
function parseEntryList(entryListDoc) {
    
    if ( entryListDoc == null )
        return null;

    if ( entryListDoc instanceof Document ) {
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
    } else {
        __exception("parseEntryList() accepts only parameters of type 'Element'!");
    }
}
    
    
/**
 * 
 */
function __buildSingleEntry(entryElem) {
  
    if ( entryElem instanceof Element ) {
        var entry = new Entry( entryElem.getAttribute(ATTR_HREF) );
        
        entry.title       = entryElem.getAttribute(ATTR_TITLE) || "";
        entry.type        = entryElem.getAttribute(ATTR_TYPE) || "";

        // TODO make real dates here 
        entry.issuedDate    = entryElem.getAttribute(ATTR_ISSUED);
        entry.modifiedDate  = entryElem.getAttribute(ATTR_MODIFIED);
        entry.fetchedDate   = entryElem.getAttribute(ATTR_FETCHED);
        entry.editedDate    = entryElem.getAttribute(ATTR_EDITED);
        
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
        
        debugConsole.addMsg("Parsed Entry: "+entry.toString());
        
        return entry;
        
    } else {
        __exception("__buildEntry() accepts only parameters of type 'Element'!");
    }
}


/**
 * 
 */
function parseInputDate(date) {
    // TODO implement this
    return date;
}


/**
 * 
 */
function parseW3cDate(date) {
    // TODO implement this
    return date;
}


/**
 * 
 */
function formatW3cDate(date) {
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


// =========================================================================
//
// HTTP Classes
//
// =========================================================================


/**
 * This class needs a global xmlhttp variable
 */
function RoossterHttpState() {

    /**
     * 0 if no state is available (no request made yet?) or if last request to 
     * this object (not to the API), was invalid, else contains status code
     * of last HTTP request made to API
     */
    this.lastHttpState = 0;
    
    /**
     * analogous to this.lastHttpState, contains the status line of last 
     * HTTP request made to API or null
     */
    this.lastHttpStatusLine = null;
    
    /**
     * analogous to this.lastHttpStatusLine, contains the Exception text of last 
     * HTTP request made to API or null0
     */
    this.lastExceptionText = null;    
    
    /**
     * analogous to this.lastHttpStatusLine, contains the Exception classname of last 
     * HTTP request made to API or null0
     */
    this.lastException = null;  

    
    /**
     * 
     */
    this.init = function() {
        this.clearState();
    }
    
    
    /**
     * 
     */
    this.clearState = function() {
        this.lastHttpStatusLine = null;
        this.lastHttpState      = 0;
        this.entries            = null;
    }
    
    
    /**
     * @return true if the request has a statuscode < 400, false otherwise
     * if statuscode is greater or equal 400, the statusLine is decoded with
     * __decodeException()
     */
    this.checkHttpState = function() {
        if ( xmlhttp == null ) 
            return true;
      
        this.lastHttpStatusLine = xmlhttp.statusText;
        this.lastHttpState = xmlhttp.status;

        debugConsole.addMsg("Checking HTTP state: "+ this.lastHttpState +" "+this.__decodeStatusLine(this.lastHttpStatusLine));
        //debugConsole.addMsg("Response: "+ xmlhttp.responseText);
        
        if ( this.lastHttpState >= 400 ) {
            this.__decodeException(this.lastHttpStatusLine);
            return false;
        } else {
            return true;
        }
    }    
    

    /**
     * @return void
     */
    this.__decodeException = function(statusLine) {
        var exceptionString = this.__decodeStatusLine(statusLine);
      
        // would like to but can't use variable extrapolation here, then
        // I could use the constant ROOSSTER_EXCEPTION 
        exceptionString.match(/RoossterException:\s<([\w\.]*)>\s.*/);
        
        this.lastException =  RegExp.$1;
        this.lastExceptionText =  RegExp.$2;
    }    

    
    /**
     * 
     */
    this.__decodeStatusLine = function(exceptionString) {
        return unescape(exceptionString).replace(/\+/g, " ");
    }
    

    /**
     * 
     */
    this.__isException = function(testStr) {
        if ( RoossterUtil.startsWith(text, EXC_DUPLICATE) 
              || RoossterUtil.startsWith(text, EXC_DUPLICATE) ) 
            return true; 
        else 
            return false;
    }
 
}



// =========================================================================
//
// Roosster Classes
//
// =========================================================================


/**
 * EntryList-Objects
 */
function EntryList(total, limit, offset) {
    
    this.total  = total || -1;
    this.limit  = limit || -1;
    this.offset = offset || -1;
  
    this.list = {};

    /**
     */  
    this.put = function(key, value) { this.list[key] = value; }
    
    
    /**
     */
    this.get = function(key) { return this.list[key];  }
    
    
    /**
     */
    this.getList = function() { return this.list; }
}




/**
 * 
 */
function Entry(url) {
    if ( url == null || url == '' )
        throw "Can't create an Entry-instance without URL";
    
    this.url = url;
    
    this.title        = '';
    this.note         = '';
    this.tags         = new Array();
    this.type         = '';
    this.author       = '';
    this.authorEmail  = '';
    this.issuedDate   = null;
    this.modifiedDate = null;
    this.fetchedDate  = null;
    this.editedDate   = null;
    
    /**
     * 
     */
    this.toString = function() {
        return "Entry: \nURL: "+this.url+"\nTitle: "+this.title+"\nTags: "+this.tags.join()+"\n\n";
    }
    
    
    /**
     * fills this object into a form (specified by the node param)
     */
    this.fillIntoEditForm = function(node) {    
        node.url.value = this.url;
        node.title.value = this.title;
        node.type.value = this.type;
        node.tags.value = this.tags.join();
        node.author.value = this.author;
        node.authorEmail.value = this.authorEmail;
        node.note.value = this.note;
        node.issuedDate.value =  displayDate(this.issuedDate);
        node.modifiedDate.value = displayDate(this.modifiedDate);
        node.fetchedDate.value = displayDate(this.fetchedDate);
        node.editedDate.value = displayDate(this.editedDate);
    }    
    
    
    /**
     * fills this object into a form (specified by the node param)
     */
    this.overwriteWithForm = function(node) {    
        this.title = nullOrEmpty(node.title.value) ? this.title : node.title.value;
        this.type = nullOrEmpty(node.type.value) ? this.type : node.type.value;
        this.tags = nullOrEmpty(node.tags.value) ? this.tags : node.tags.value.split();
        this.author = nullOrEmpty(node.author.value) ? this.author : node.author.value;
        this.authorEmail = nullOrEmpty(node.authorEmail.value) ? this.authorEmail : node.authorEmail.value;
        this.note = nullOrEmpty(node.note.value) ? this.note : node.note.value;
        // implement this
        //this.issuedDate = nullOrEmpty(node.issuedDate.value) ? this.issuedDate : parseInputDate(node.issuedDate.value);
        //this.modifiedDate = nullOrEmpty(node.modifiedDate.value) ? this.modifiedDate : parseInputDate(node.modifiedDate.value);
    }
    
    
    /**
     * Appends the following structure to the provided node:
     * 
     * <ul class="entry">
     *     <li class="entry-url"><a href="$entry.url">$entry.title</a></li> 
     *     <li class="entry-content">#truncate($entry.content)<strong>[...]</strong></li> 
     *     <li class="entry-info">$entry.issued (<a href="#url('entry')?url=$entry.url">show all details</a>)</li>
     * </ul>
     */
    this.attachAsList = function(node) {
        if ( node == null )
            exception("Parameter 'node' to method Entry.attachAsList() must be 'not null'");
      
        var ulEntryList = XmlCreateElement("ul");
        ulEntryList.className = 'entry';
        
        // <li class="entry-url">        
        var liEntryUrl = XmlCreateElement("li");
        liEntryUrl.className = 'entry-url';
        liEntryUrl.appendChild( createLink("javascript:doEdit('"+this.url+"')", this.title) );
        ulEntryList.appendChild(liEntryUrl);
        
        // <li class="entry-content">        
        var liEntryContent = XmlCreateElement("li");
        liEntryContent.className = 'entry-content';
        liEntryContent.appendChild( XmlCreateText(this.content) );
        ulEntryList.appendChild(liEntryContent);

        // <li class="entry-info">        
        var liEntryInfo = XmlCreateElement("li");
        liEntryInfo.className = 'entry-info';
        
        var infoLine = XmlCreateElement('span');
        infoLine.appendChild( 
                      XmlCreateText((this.modifiedDate ? displayDate(this.modifiedDate) 
                                                            : "(no modified date)")
                                         +" tags: ")
                            );
        
        for (var i = 0; i < this.tags.length; i++) {
            infoLine.appendChild( createLink("javascript:doSearch('tags:"+this.tags[i]+"')", this.tags[i]) );
            
            if ( i+1 < this.tags.length )
                infoLine.appendChild( XmlCreateText(" | ") );
        }
        liEntryInfo.appendChild(infoLine);
        ulEntryList.appendChild(liEntryInfo);
        
        node.appendChild(ulEntryList);
    }

    
    /**
     * 
     */
    this.asDomDocument = function() {
        var xmlDoc = XmlCreateDocument();
		
        var elemEntryList = xmlDoc.createElement(TAG_ENTRYLIST);
        xmlDoc.appendChild(elemEntryList);
        
        var elemEntry = xmlDoc.createElement(TAG_ENTRY);
        elemEntry.setAttribute(ATTR_HREF, this.url);
        elemEntry.setAttribute(ATTR_TITLE, this.title);
        elemEntry.setAttribute(ATTR_TYPE, this.type);
        // TODO implement this
        //elemEntry.setAttribute(ATTR_ISSUED, this.issuedDate);
        //elemEntry.setAttribute(ATTR_MODIFIED, this.modifiedDate);
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

