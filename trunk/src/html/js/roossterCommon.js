/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */

const DEBUG = true;

const TAG_ENTRYLIST   = 'entrylist';
const TAG_ENTRY       = 'entry';
const TAG_NOTES       = 'notes';
const TAG_AUTHOR      = 'author';
const TAG_AUTHORS     = 'authors';
const TAG_TAG         = 'tag';
const TAG_TAGS        = 'tags';
const TAG_NOTE        = 'note';
const TAG_TITLE       = 'title';
const TAG_ISSUED      = 'issued';
const TAG_MODIFIED    = 'modified';
const TAG_FETCHED     = 'fetched';
const TAG_CONTENT     = 'content';

const ATTR_HREF       = 'href';
const ATTR_URL        = 'url';
const ATTR_NAME       = 'name';
const ATTR_EMAIL      = 'email';

const DIV_ID_DEBUGOUT = "debug-out";

const DIV_ID_ENTRIESOUT = "entries-out";

// global reference to window's document
var doc = window.document;

/**
 * 
 */    
function Debug() {
    this.messageString = '';
    
    // methods
    this.addMsg = function(message) {
        this.messageString += message +"\n";
    }
    
    this.show = function() {
        if ( DEBUG ) {
            var debugOut = doc.getElementById(DIV_ID_DEBUGOUT);
            
            if ( debugOut != null ) {
              
                if ( this.messageString ) {
                    var text = doc.createTextNode("DEBUG: "+this.messageString)
                    
                    var p = doc.createElement("p");
                    p.style.backgroundColor = "#FF8064";
                    p.style.fontSize        = "small";
                    p.style.padding         = "10px";
                    
                    p.appendChild(text);

                    debugOut.appendChild(p);
                }
                
            } else {  
                alert("DEBUG \n"+this.messageString);
            }
        }
    }
}

var debugConsole = new Debug();

function exception(message) {
    debugConsole.addMsg("Exception: "+message+"\n\n (Stopping execution)");
    debugConsole.show();
    throw message;
}



/**
 * 
 */
function w3cDate(){
  var date = new Date();
  
  var day = new String(date.getUTCDate());
  day = day.length > 1 ? day : "0"+day;
  
  var month = new String(date.getUTCMonth()+1);
  month = month.length > 1 ? month : "0" + month;
  
  var hours =date.getUTCHours();
  hours = hours.length > 1 ? hours : "0" + hours;
  
  var time = hours + ":" + date.getUTCMinutes() + ":" + date.getUTCSeconds();
  
  return date.getUTCFullYear() + "-" + month + "-" + day + "T" + time;
}


/**
 * @param childName name of child element, for which the text should be retrieved
 */
function getChildsText(node, childName) {
    var children = node.childNodes;
    var text     = "";
    for(var i = 0; i < children.length; i++) {
        var child = children.item(i);
        if ( child.nodeType == 1 && child.nodeName == childName ) // Element Node
            return getText(child);
    }
    
    return null;
}


/*
 *
 */
function getText(node) {
    var children = node.childNodes;
    var text     = "";
    for(var i = 0; i < children.length; i++) {
        var n = children.item(i);
        if ( n.nodeType == 3 ) // Text Node
            text += n.data;
    }
    return text;
}

