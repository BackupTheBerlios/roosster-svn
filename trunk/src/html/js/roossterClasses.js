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
 * Entry-Object
 */
function Entry(url) {
    if ( url == null || url == '' )
        throw "Can't create an Entry-instance without URL";
    
    this.url = url;
    
    this.title        = '';
    this.note         = '';
    this.tags         = new Array();
    this.author       = '';
    this.authorEmail  = '';
    this.issuedDate   = null;
    this.modifiedDate = null;
    this.fetchedDate  = null;
    
    //
    //methods
    //
    
    /**
     * 
     */
    this.attach = function(node) {
        var ulEntryList = doc.createElement("ul");
        ulEntryList.id = 'entry';
        
        var liEntryUrl = doc.createElement("li");
        liEntryUrl.className = 'entry-url';
            var aHref = doc.createElement('a');
            aHref.href = this.url
            aHref.appendChild( doc.createTextNode(this.title) );
        liEntryUrl.appendChild( aHref );
        ulEntryList.appendChild(liEntryUrl);
        
        var liEntryContent = doc.createElement("li");
        liEntryContent.className = 'entry-content';
        liEntryContent.appendChild( doc.createTextNode(this.content) );
        ulEntryList.appendChild(liEntryContent);
        
        var liEntryInfo = doc.createElement("li");
        liEntryInfo.className = 'entry-info';
        liEntryInfo.appendChild( doc.createTextNode(this.issued) );
        ulEntryList.appendChild(liEntryInfo);
        
        node.appendChild(ulEntryList);
    }
    
  /*  
    <ul id="entry-list">
    #foreach($entry in $entries)
        <li>
            <ul class="entry">
                <li class="entry-url"><a href="$entry.url">$entry.title</a></li> 
                <li class="entry-content">#truncate($entry.content)<strong>[...]</strong></li> 
                <li class="entry-info">$entry.issued (<a href="#url('entry')?url=$entry.url">show all details</a>)</li>
            </ul>
        </li>
    #end
  </ul>
  */
    
}
