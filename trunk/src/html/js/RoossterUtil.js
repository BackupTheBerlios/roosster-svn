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


const DEBUG = true;

const DIV_ID_DEBUGOUT = "debug-out";


// =========================================================================
//
// global functions
//
// =========================================================================

/**
 * 
 */
function __exception(message) {
    debugConsole.addException(message);
    debugConsole.show();
    throw message;
}


/**
 * 
 */
function getById(id) {
    return document.getElementById(id);  
}


// =========================================================================
//
// Classes
//
// =========================================================================

/**
 * 
 */    
function Debug() {
  
    this.messagesElement = XmlCreateElement('ul');
    this.messageString   = '';
    
    this.isDebugEnabled = DEBUG;
    
    /**
     * 
     */
    this.clear  = function() {
        this.messagesElement = XmlCreateElement('ul');
        this.messageString   = '';
    }
    
    
    /**
     * 
     */
    this.addException = function(message) {
        var listElem = XmlCreateElement('li');
        var bElem = XmlCreateElement('b');
        bElem.appendChild( XmlCreateText( this.getTimeString() +" - EXCEPTION - ") );
        
        listElem.appendChild(bElem);
        listElem.appendChild( XmlCreateText(message) );
        
        this.messagesElement.appendChild(listElem);
        
        this.messageString += message +"\n";
    }
    
    
    /**
     * 
     */
    this.addMsg = function(message) {
        var listElem = XmlCreateElement('li');
        listElem.appendChild( XmlCreateText( this.getTimeString() ) );
        listElem.appendChild( XmlCreateText(message) );
        this.messagesElement.appendChild(listElem);
        
        this.messageString += message +"\n";
    }

    
    /**
     * 
     */
    this.show = function() {
        if ( DEBUG ) {
            var debugOut = getById(DIV_ID_DEBUGOUT);
            
            if ( debugOut != null ) {
                XmlRemoveAllChildren(debugOut);
              
                if ( this.messageString ) {
                    var text = XmlCreateText("DEBUG: ");
                    
                    var p = XmlCreateElement("p");
                    p.style.backgroundColor = "#FF8064";
                    p.style.fontSize        = "small";
                    p.style.padding         = "10px";
                    
                    p.appendChild(text);
                    p.appendChild(this.messagesElement);

                    debugOut.appendChild(p);
                }
                
            } else {  
                alert("DEBUG \n"+this.messageString);
            }
        }
        
        this.clear();
    }
    
    
    /**
     * 
     */
    this.getTimeString = function() {
        var d = new Date();
        return d.getHours()+"."+d.getMinutes()+"."+d.getSeconds()+'.'+d.getMilliseconds()+": ";
    }
}


// =========================================================================
//
// Common XML/Helper functions
//
// =========================================================================


/**
 * 
 */
function nullOrEmpty(testStr) {
    return testStr == null || testStr == '';
}

/**
 * 
 */
function startsWith(testStr, startStr) {
    if ( testStr != null && startStr != null ) 
        return testStr.indexOf(startStr, 0) == 0; 
    else 
        return false;
}


/**
 *  @return an Element object that represents a html link (<a href="targetUrl">text</a>)
 */
function createLink(targetUrl, text) {
    var aHref = XmlCreateElement('a');
    aHref.href = targetUrl;
    aHref.appendChild( XmlCreateText(text) );
    return aHref;
}


/**
 * @return the value of all Text-type child nodes of the specified node 
 */
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


/**
 * 
 */
function XmlCreateText(text) {
    return document.createTextNode(text);
}

/**
 * 
 */
function XmlCreateElement(nodeName) {
    return document.createElement(nodeName);
}


/**
 * @param node from which all children should be removed 
 */
function XmlRemoveAllChildren(node) {
    if ( node == null ) 
        return;
    
    var children = node.childNodes;
    if ( children != null ) {
        for(var i = 0; i < children.length; i++) {
            node.removeChild( children.item(i) );
        }
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
 * 
 */
function XmlCreateDocument(node, childName) {
    if (document.implementation && document.implementation.createDocument) {
      
        return document.implementation.createDocument("", "", null);
        //xmlDoc.onload = function() { createEntryDocument(this) };
        
    } else if (window.ActiveXObject) {
      
        return new ActiveXObject("Microsoft.XMLDOM");
        //xmlDoc.onreadystatechange = function () { if (xmlDoc.readyState == 4) createEntryDocument(this) };
              
    } else {
        __exception("You can't create DOM Documents with your browser! Lameass");
    }
}
    
/*
function appendChild(elem, child) {
  if (browser.isIE) { 
    elem.childNodes.item(0).appendChild(child);
  } else {
    elem.appendChild(child);
  }
}    
*/



/**
 * QueryString object taken from http://flangy.com/dev/javascript/
 */
function Querystring() {
    // get the query string, ignore the ? at the front.
    var querystring = location.search.substring(1, location.search.length);
  
    // parse out name/value pairs separated via &
    var args = querystring.split('&');
  
    // split out each name = value pair
    for (var i=0; i< args.length;i++) {
        var pair = args[i].split('=');
    
        // Fix broken unescaping
        temp = unescape(pair[0]).split('+');
        name = temp.join(' ');
    
        temp = unescape(pair[1]).split('+');
        value = temp.join(' ');
    
        this[name]=value;
    }

    
    /**
     * 
     */
    this.get = function(strKey, strDefault) {
        return this[strKey] == null ? strDefault : this[strKey];
    }
}
