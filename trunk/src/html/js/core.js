/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License. 
 * See http://www.opensource.org/licenses/artistic-license.php for details
 */
 
/**
 */
function toggleDisplay(currId) {
		thisMenu = getById(currId).style;
    thisMenu.display =  thisMenu.display == "block" ? "none" : "block";
		return false;
}
/**
 */
function getById(id) {
    return document.getElementById(id);  
}
/**
 */
function nullOrEmpty(testStr) {
    return testStr == null || testStr == '';
}
/**
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
function createLink(targetUrl, text, target) {
    var aHref = XmlCreateElement('a');
    aHref.href = targetUrl;
    if ( target ) aHref.target = target;
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
 */
function XmlCreateText(text) {
    return document.createTextNode(text);
}
/**
 */
function XmlCreateElement(nodeName) {
    return document.createElement(nodeName);
}
