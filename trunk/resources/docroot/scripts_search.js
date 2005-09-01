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
    var query = unescape(new Querystring().get('query'));
    if ( query != null ) 
        searchEntries(query);
}
  
//*****************************************************************************
// SEARCH ENTRIES
//****************************************************************************

var xmlhttp = null;


function searchEntries(query) {
  
    if ( typeof query != undefined && query != null && query != '' ) { 
        // make HTTP request
        xmlhttp = newXmlHttp();
        xmlhttp.open("GET", API_ENDPOINT + "/search?query="+query, true);
        xmlhttp.onreadystatechange = searchEntriesResponseHandler;        
        xmlhttp.send(null);  
        
        document.searchform.query.value = query;
        
    } else {
        alert("No sense in searching when no query is specified! Don't you think?!");
    }
}


function searchEntriesResponseHandler() {
    if ( xmlhttp.readyState != 4) 
        return;
    
    if ( xmlhttp.status < 300 ) {
      
        var list = xmlhttp.status == 204 ? new EntryList() : parseEntryList(xmlhttp.responseXML);
      
        var entriesOut = getById(ENTRIESOUT);
        XmlRemoveAllChildren(entriesOut);
        
        var entries = list.getList();
        
        if ( list.length() < 1 ) {
            entriesOut.appendChild( XmlCreateText("No Entries found for this search!") );
        } else {  
        
            var ulEntryList = XmlCreateElement("ul");
            ulEntryList.id = 'entry-list';
            entriesOut.appendChild(ulEntryList);
              
            for(var url in entries) {
                var li = XmlCreateElement("li");
                ulEntryList.appendChild(li);
                entries[url].attachAsList(li);
            }
        }        
        
    } else {
        var texts = decodeException(xmlhttp.statusText);
        showMessage('error', "An Error occurred: "+ texts[0] +", "+texts[1] );
    }
}


