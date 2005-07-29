/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */


//*****************************************************************************
// ONLOAD INITIALIZATION
//****************************************************************************

function roosster_init() {
    var hideDivs = ["advanced-add"];
    for (var i = 0; i < hideDivs.length; i++) {
        var d = getById(hideDivs[i]);
        if ( d ) 
          d.style.display = "none";
    }
    
    var queryString = new Querystring();
    
    document.addform.url.value = unescape(queryString.get('url', '')); 
    document.addform.title.value = unescape(queryString.get('title', '')); 
    document.addform.doneUrl.value = unescape(queryString.get('doneUrl', '')); 
	
    loadTags();
}



//*****************************************************************************
// POST NEW ENTRY TO API
//****************************************************************************

var xmlhttp = null;
var xmlhttpTags = null;

var collection = [];

function postEntry() {
    if ( !checkWICKForm() )
        return;
  
  
    var url = document.addform.url.value;
  
    if ( typeof url != undefined && url != null && url != '' ) {
        clearMessages();
        
        // build Entry Object
        var entry = new Entry(url);
        entry.title = document.addform.title.value;
        entry.pub   = getValue(document.addform.pub) == 'true' ? true : false;
        
        var tags = document.addform.tags.value;
        if ( typeof tags != undefined && tags != null && tags != '' )
            entry.tags = tags.split(',');
        else  
            entry.tags = [];
        
        // determine advanced parameters
        var fetch = getValue(document.addform.fetchcontent) 
        var force = getValue(document.addform.force) 
        
        // make HTTP request
        xmlhttp = newXmlHttp();
        xmlhttp.open("POST", API_ENDPOINT + "/entry?fetch.content="+fetch+"&force="+force, true);
        xmlhttp.onreadystatechange = postResponseHandler;        
        xmlhttp.setRequestHeader("Content-Type", API_CTYPE);
        xmlhttp.send( entry.asDomDocument() );
        
    } else {
        alert("Can't add new Entry without URL!");
    } 
    
}

function postResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
    
    if ( xmlhttp.status < 300 ) {
      
        var done = document.addform.doneUrl.value;
        if ( typeof done != undefined && done != null && done != '' ) 
            document.location = done;
        else
            document.location = 'http://localhost:8181/close.html';
        
    } else {
      
        var texts = decodeException(xmlhttp.statusText);
        showMessage('error', "An Error occurred: "+ texts[0] +", "+texts[1] );
        
    } 
}

function loadTags() {
    xmlhttpTags = newXmlHttp();
    
    xmlhttpTags.open("GET", API_ENDPOINT + "/tags", true);
    xmlhttpTags.onreadystatechange = loadTagsResponseHandler;        
    xmlhttpTags.send(null);    
}

function loadTagsResponseHandler() {
    if ( xmlhttpTags.readyState != 4) 
        return;
    
    if ( xmlhttpTags.status < 300 ) {
      
        var tagsDoc = xmlhttpTags.responseXML.documentElement;
        
        var tagElemList = tagsDoc.getElementsByTagName('tag'); 
        for (var i = 0; i < tagElemList.length; i++) {
            var tagElem = tagElemList.item(i);
            collection.push(XmlGetText(tagElem));
        }
        
    } else {
        throw "Roosster: Could not load Tags for autocomplete feature!";
    }
}


//*****************************************************************************
// HELPER CODE
//****************************************************************************

// code taken from http://www.bofe.org/wick/ 
// prevents form from being submitted too early, when user presses enter while in
// autocomple-enabled tags-input field
function checkWICKForm() {  
    return siw && siw.selectingSomething ? false : true;
}  


function toggleDisplay(currId) {
		thisMenu = getById(currId).style;
    thisMenu.display = thisMenu.display == "block" ? "none" : "block";
		return false;
}

function getById(id) { return document.getElementById(id);   }

function nullOrEmpty(testStr) { return testStr == null || testStr == ''; }

function startsWith(testStr, startStr) {
    return testStr != null && startStr != null ? testStr.indexOf(startStr, 0) == 0 : false; 
}

function getValue(radioGroup) {
    if ( typeof radioGroup != undefined && radioGroup != null ) { 
        for (var i = 0; i < radioGroup.length; i++) {
            if ( radioGroup[i].checked )
                return radioGroup[i].value;
        }
    }
}

function newXmlHttp() { 
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

function decodeException(statusLine) {
    var exceptionString = decodeStatusLine(statusLine);
  
    // would like to but can't use variable extrapolation here, then
    // I could use the constant ROOSSTER_EXCEPTION 
    exceptionString.match(/RoossterException:\s<([\w\.]*)>\s(.*)/);
    
    return [RegExp.$1, RegExp.$2];
}    

function decodeStatusLine(statusLine) {
    return unescape(statusLine).replace(/\+/g, " ");
}
    
function clearMessages() {
    XmlRemoveAllChildren(getById(OUTPUT_MESSAGES));
}

function showMessage(clazz, msg) {
    var p = document.createElement('p');
    p.className = clazz;
    p.appendChild(document.createTextNode(msg));
    getById(OUTPUT_MESSAGES).appendChild(p);
}

function XmlGetText(node) {
    var children = node.childNodes;
    var text     = "";
    for(var i = 0; i < children.length; i++) {
        var n = children.item(i);
        if ( n.nodeType == 3 ) // Text Node
            text += n.data;
    }
    return text;
}

function XmlCreateDocument(node, childName) {
    if (document.implementation && document.implementation.createDocument) {
        return document.implementation.createDocument("", "", null);
    } else if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLDOM");
    } else {
        throw "You can't create DOM Documents with your browser! Lameass";
    }
}

/**
 * @param node from which all children should be removed 
 */
function XmlRemoveAllChildren(node) {
    if ( node == null ) 
        return;

    while ( node.firstChild != null ) {
        node.removeChild(node.firstChild);
    }
}


/**
 * returns the contents of child nodes (of type TEXT).
 * 
 * @param childName name of child element, for which the text should be retrieved;
 * @param node the DOM Node on which to perform the action
 *
 * @return if there is more than one child node, an array of strings (representing the 
 * text nodes of the children) is returned; if there is only one child node with 
 * the specified name, returns only this nodes text; 
 * null, if the node has no children at all, or there is no node with this name.
 */
function XmlGetChildsText(node, childName) {
    var children = node.childNodes;
    
    if ( children != null ) {
      
        var childTexts = new Array();
        for(var i = 0; i < children.length; i++) {
            var child = children.item(i);
            if ( child.nodeType == 1 && child.nodeName == childName ) // Element Node
                childTexts.push( XmlGetText(child) );
        }
        
        if ( childTexts.length > 1 ) 
            return childTexts;
        else if ( childTexts.length == 1 )
            return childTexts[0];
    }
    
    return null;
}    


/**
 * Client-side access to querystring name=value pairs
 *	Version 1.2.3
 *	22 Jun 2005
 * Adam Vandenberg http://adamv.com/dev/javascript/querystring
 */
function Querystring(qs) { // optionally pass a querystring to parse
    this.params = new Object()
    this.get=Querystring_get
    
    if (qs == null)
        qs=location.search.substring(1,location.search.length)
  
    if (qs.length == 0) return
  
    // Turn <plus> back to <space>
    // See: http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1
    qs = qs.replace(/\+/g, ' ')
    var args = qs.split('&') // parse out name/value pairs separated via &
    
    // split out each name=value pair
    for (var i=0;i<args.length;i++) {
        var pair = args[i].split('=')
        var name = unescape(pair[0])
    
        var value = pair.length == 2 ? unescape(pair[1]) : name;
        
        this.params[name] = value
    }
}

function Querystring_get(key, default_) {
    // This silly looking line changes UNDEFINED to NULL
    if (default_ == null) default_ = null;
    
    var value=this.params[key]
    if (value==null) value=default_;
    
    return value
}
