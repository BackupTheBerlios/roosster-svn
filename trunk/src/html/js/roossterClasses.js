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
    this.type         = '';
    this.author       = '';
    this.authorEmail  = '';
    this.issuedDate   = null;
    this.modifiedDate = null;
    this.fetchedDate  = null;
    
    //
    //methods
    //
    
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
        node.issuedDate.value =  displayDate(this.issuedDate);
        node.modifiedDate.value = displayDate(this.modifiedDate);
        node.fetchedDate.value = tdisplayDate(this.fetchedDate);
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
      
        var ulEntryList = doc.createElement("ul");
        ulEntryList.className = 'entry';
        
        // <li class="entry-url">        
        var liEntryUrl = doc.createElement("li");
        liEntryUrl.className = 'entry-url';
        liEntryUrl.appendChild( createLink("javascript:doEdit('"+this.url+"')", this.title) );
        ulEntryList.appendChild(liEntryUrl);
        
        // <li class="entry-content">        
        var liEntryContent = doc.createElement("li");
        liEntryContent.className = 'entry-content';
        liEntryContent.appendChild( doc.createTextNode(this.content) );
        ulEntryList.appendChild(liEntryContent);

        // <li class="entry-info">        
        var liEntryInfo = doc.createElement("li");
        liEntryInfo.className = 'entry-info';
        
        var infoLine = doc.createElement('span');
        infoLine.appendChild( 
                      doc.createTextNode((this.issued ? displayDate(this.issued) : "(no issued date)")+" tags: ")
                            );
        
        for (var i = 0; i < this.tags.length; i++) {
            infoLine.appendChild( createLink("javascript:doSearch('tags:"+this.tags[i]+"')", this.tags[i]) );
            
            if ( i+1 < this.tags.length )
                infoLine.appendChild( doc.createTextNode(" | ") );
        }
        liEntryInfo.appendChild(infoLine);
        ulEntryList.appendChild(liEntryInfo);
        
        node.appendChild(ulEntryList);
    }
    
}


/**
 * EntryList-Objects
 */
function EntryList() {
}



