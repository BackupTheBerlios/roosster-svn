/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License.
 *
 * You should have received a copy of the Artistic License
 * along with ROOSSTER; if not, go to
 * http://www.opensource.org/licenses/artistic-license.php for details
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.roosster.xml;

import java.net.URL;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.text.DateFormat;
import java.text.ParseException;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

import org.roosster.OperationException;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.util.XmlUtil;

/**
 * 
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntrySaxHandler extends DefaultHandler implements EntryTags
{
    private static Logger LOG = Logger.getLogger(EntrySaxHandler.class.getName());

    private DateFormat  dateFormat         = XmlUtil.getDateFormat();
    
    private EntryList   entries            = new EntryList();
    
    private LinkedList  elementStack       = new LinkedList();
    
    private List        tagList            = new ArrayList();
    
    private String      currentTag         = null;
    
    private StringBuffer currentText       = null;
    
    private Entry       currentEntry       = null;
    
    
    /**
     * 
     */
    public void startElement(String nsURI, String localName, String qName, Attributes atts)
                      throws SAXException
    {
        LOG.debug("PARSE: --> Push <"+qName+"> onto element stack");

        elementStack.add(qName);
        
        currentTag = qName;
        currentText = new StringBuffer();
        
        validate(atts);
        
        if ( ENTRY.equals(currentTag) ) 
            processEntryTag(atts);
        
        else if ( AUTHOR.equals(currentTag) )
            processAuthorTag(atts);
        
    }
    
    
    /**
     * 
     */
    public void endElement(String nsURI, String localName, String qName)
                    throws SAXException
    {
        if ( ENTRY.equals(qName) && currentEntry != null ) {
            entries.add(currentEntry);
            
            currentEntry.setTags( (String[]) tagList.toArray(new String[0]) );
            tagList.clear();
        }
        
        else if ( TAG.equals(qName) )
            tagList.add(currentText.toString());
        
        else if ( NOTE.equals(qName) )
            currentEntry.setNote(currentText.toString());
        
        else if ( CONTENT.equals(qName) )
            currentEntry.setContent(currentText.toString());
        
        LOG.debug("PARSE: <-- Pop <"+qName+"> from element stack");
        elementStack.removeLast();
        
        currentTag = elementStack.isEmpty() ? null : (String) elementStack.getLast();
        currentText = new StringBuffer();
    }
    
    
    /**
     * 
     */
    public void characters(char[] text, int start, int length) throws SAXException 
    {
        currentText.append(text, start, length);
    }
    
    
    /**
     * @return empty if the parsed stream didn't contain an &lt;entrylist&gt;, 
     * never <code>null</code>
     */
    public EntryList getEntries()
    {
        return entries;
    }

    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private void processAuthorTag(Attributes atts) throws SAXException
    {
        String author = atts.getValue(NAME_ATTR);
        String email  = atts.getValue(EMAIL_ATTR);
        
        currentEntry.setAuthor( author == null ? "" : author );
        currentEntry.setAuthorEmail( email == null ? "" : email );
    }
    
    
    /**
     * 
     */
    private void processEntryTag(Attributes atts) throws SAXException
    {
        try {
            // EDITED_ATTR and FETCHED_ATTR are ignored by server, 
            // these can't be set/modfied by client
          
            currentEntry = new Entry(new URL( atts.getValue(HREF_ATTR) ));
            
            currentEntry.setTitle( atts.getValue(TITLE_ATTR) );
            currentEntry.setFileType( atts.getValue(TYPE_ATTR) );
            
            currentEntry.setIssued( parseDate(atts.getValue(ISSUED_ATTR)) );
            currentEntry.setModified( parseDate(atts.getValue(MODIFIED_ATTR)) );
        
        } catch(java.net.MalformedURLException ex) {
            throwException("Not a valid URL in attribute '"+HREF_ATTR+"' tag <"+ENTRY+">", ex);
        }
    }
    
    
    /**
     * 
     */
    private Date parseDate(String dateStr) throws SAXException
    {
        try {
            return XmlUtil.parseW3cDate(dateStr);
        } catch(ParseException ex) {
            throw new SAXException("Invalid date format: "+dateStr, ex);
        }
    }


    /**
     * validate the current position in the document  
     */
    private void validate(Attributes atts) throws SAXException
    {
        // entry
        if ( ENTRY.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY} ) )
                  throwException(ENTRY+" must be child of root tag "+ENTRYLIST);
        }
        
        // author
        else if ( AUTHOR.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, AUTHOR} ) )
                throwException(AUTHOR+" must be child Element of "+ENTRY);
        }
        
        // note
        else if ( NOTE.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, NOTE} ) )
                throwException(NOTE+" must be child Element of "+ENTRY);
        }
        
        // note
        else if ( CONTENT.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, CONTENT} ) )
                throwException(CONTENT+" must be child Element of "+ENTRY);
        }
        
        // tag
        else if ( TAG.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, TAG} ) )
                throwException(TAG+" must be child Element of "+ENTRY);
        }
    }


    /**
     * 
     */
    private boolean compareToStack(String[] tags)
    {
        return Arrays.equals(elementStack.toArray(), tags);
    }
    
    
    /**
     * 
     */
    public void throwException(String message) throws SAXException
    {
        throwException(message, new IllegalArgumentException(message));
    }
    

    /**
     * 
     */
    public void throwException(String message, Exception ex) throws SAXException
    {
        throw new SAXException(message, ex);
    }    
}
