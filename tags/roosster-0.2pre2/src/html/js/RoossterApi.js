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
const ATTR_ADDED      = 'added';
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
        
        debugConsole.addMsg("Parsed Entry: "+entry.toString());
        
        return entry;
        
    } else {
        __exception("__buildEntry() accepts only parameters of type 'Element'!");
    }
}


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
     * HTTP request made to API or null
     */
    this.lastExceptionText = null;    
    
    /**
     * analogous to this.lastHttpStatusLine, contains the Exception classname of last 
     * HTTP request made to API or null
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
        exceptionString.match(/RoossterException:\s<([\w\.]*)>\s(.*)/);
        
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
    
    this.total  = parseInt(total) || -1;
    this.limit  = parseInt(limit) || -1;
    this.offset = parseInt(offset) || -1;
  
    this.__listCount = 0;
    
    this.list = {};

    /**
     */
    this.length = function() { return this.__listCount; }
    
    
    /**
     */  
    this.put = function(key, value) { this.list[key] = value; this.__listCount++; }
    
    
    /**
     */
    this.get = function(key) { return this.list[key];  }
    
    
    /**
     */
    this.getList = function() { return this.list; }
    
    /**
     */
    this.attachPager = function(node) {
        if ( node == null )
            exception("Parameter 'node' to method EntryList.attachPager() must be 'not null'");
      
        var offset = this.offset <= 0 ? 0 : this.offset;
        var limit = this.limit <= 0 ? 0 : this.limit;
        
        var nextOffset = offset+limit;
        var prevOffset = offset-limit < 0 ? 0 : offset-limit;
        
        debugConsole.addMsg(" offset "+offset+" limit "+limit+" total "+this.total+" next "+ nextOffset +" prev "+prevOffset);
        
        var divPager = XmlCreateElement("div");
        divPager.id = 'entrylist-pager';
    
        var leftChild = XmlCreateElement('td');
        leftChild.className = 'tdalignleft';
        leftChild.appendChild( XmlCreateText("Showing results "+offset+"-"+
                                             (nextOffset > this.total ? this.total : nextOffset)+
                                             " of "+this.total) );
        
        var rightChild = XmlCreateElement('td');
        rightChild.className = 'tdalignright';
        
        if ( this.length() < this.total ) {
            var prevLink = createLink("javascript:doSearch(null,"+prevOffset+","+limit+");", "previous");
            var nextLink = createLink("javascript:doSearch(null,"+nextOffset+","+limit+");", "next");
            
            if ( offset < 1 ) { 
                prevLink.href = '#';
                prevLink.className = 'inactive-link';
            }
            if ( nextOffset > this.total ) {
                nextLink.href = '#';
                nextLink.className = 'inactive-link';
            }
            
            rightChild.appendChild(prevLink);
            rightChild.appendChild( XmlCreateText(" | ") );
            rightChild.appendChild(nextLink);
            
        }
        
        var table = XmlCreateElement('table');
        table.style.width = '100%';
        var row = XmlCreateElement('tr');
        table.appendChild(row);
        row.appendChild(leftChild);
        row.appendChild(rightChild);
        
        divPager.appendChild(table);
        node.appendChild(divPager);
    }
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
    this.addedDate    = null;
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
        node.content.value = this.content;
        node.issuedDate.value =  displayDate(this.issuedDate);
        node.modifiedDate.value = displayDate(this.modifiedDate);
        node.addedDate.value = displayDate(this.addedDate);
        node.editedDate.value = displayDate(this.editedDate);
        
        var linkSpan = getById(DIV_ID_ENTRYURLLINK);
        XmlRemoveAllChildren(linkSpan);
        linkSpan.appendChild(createLink(this.url, "Goto '"+this.title+"'", '_blank'));
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
        
        // entry-url table
        var table = XmlCreateElement('table');
        table.style.width = '100%';
        var row = XmlCreateElement('tr');
        var leftCell = XmlCreateElement('td');
        var rightCell = XmlCreateElement('td');
        table.appendChild(row);
        row.appendChild(leftCell);
        row.appendChild(rightCell);
        
        leftCell.appendChild( createLink(this.url, this.title, "_blank") );
        leftCell.className = 'entrylist-entry-url';
        rightCell.appendChild( createLink("javascript:doEdit('"+this.url+"')", 'Edit this Entry') );
        rightCell.className = 'entrylist-editlink';

        // <li class="entry-url">        
        var liEntryUrl = XmlCreateElement("li");
        liEntryUrl.className = 'entry-url-li';
        liEntryUrl.appendChild(table);
        ulEntryList.appendChild(liEntryUrl);
        
        // <li class="entry-content">        
        var liEntryContent = XmlCreateElement("li");
        liEntryContent.className = 'entry-content';
        liEntryContent.appendChild( XmlCreateText(this.content) );
        liEntryContent.appendChild( createLink("${baseurl}/cachedentryframeset.html?url="+this.url, " (cached page)", "_blank") );
        ulEntryList.appendChild(liEntryContent);

        // <li class="entry-dates">        
        var liEntryDates = XmlCreateElement("li");
        liEntryDates.className = 'entry-dates';
        liEntryDates.appendChild( XmlCreateText("MODIFIED: ") );
        liEntryDates.appendChild( createLink("javascript:doSearch('modified:"+this.searchableDate(this.modifiedDate)+"')", displayDate(this.modifiedDate)) );
        liEntryDates.appendChild( XmlCreateText("-- ISSUED: ") );
        liEntryDates.appendChild( createLink("javascript:doSearch('issued:"+this.searchableDate(this.issuedDate)+"')", displayDate(this.issuedDate)) );
        liEntryDates.appendChild( XmlCreateText("-- ADDED: ") );
        liEntryDates.appendChild( createLink("javascript:doSearch('added:"+this.searchableDate(this.addedDate)+"')", displayDate(this.addedDate)) );
        liEntryDates.appendChild( XmlCreateText("-- EDITED: ") );
        liEntryDates.appendChild( createLink("javascript:doSearch('edited:"+this.searchableDate(this.editedDate)+"')", displayDate(this.editedDate)) );
        ulEntryList.appendChild(liEntryDates);
        
        // <li class="entry-dates">        
        var liEntryTags = XmlCreateElement("li");
        liEntryTags.className = 'entry-tags';
        liEntryTags.appendChild( XmlCreateText(" tags: ") );
        
        for (var i = 0; i < this.tags.length; i++) {
            liEntryTags.appendChild( createLink("javascript:doSearch('tags:"+this.tags[i]+"')", this.tags[i]) );
            
            if ( i+1 < this.tags.length )
                liEntryTags.appendChild( XmlCreateText(" | ") );
        }
        ulEntryList.appendChild(liEntryTags);
        
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
    
    /**
     * 
     */
    this.searchableDate = function(date) {
        if ( date instanceof Date ) {
            var day = new String(date.getDate());
            day = day.length > 1 ? day : "0"+day;
            
            var month = new String(date.getMonth());
            month = month.length > 1 ? month : "0" + month;
            
            return date.getFullYear()+month+day;
            
        } else {
            return "";
        }        
    }
}

