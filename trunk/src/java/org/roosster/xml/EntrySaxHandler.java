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
        if ( TAGS.equals(qName) ) {
            currentEntry.setTags( (String[]) tagList.toArray(new String[0]) );
            tagList.clear();
        }
        
        else if ( TAG.equals(qName) )
            tagList.add(currentText.toString());
        
        else if ( ENTRY.equals(qName) && currentEntry != null ) 
            entries.add(currentEntry);
        
        else if ( NOTE.equals(qName) )
            currentEntry.setNote(currentText.toString());
        
        else if ( TITLE.equals(qName) )
            currentEntry.setTitle(currentText.toString());
        
        else if ( TYPE.equals(qName) )
            currentEntry.setFileType(currentText.toString());
        
        else if ( ISSUED.equals(qName) ) 
            currentEntry.setIssued( parseDate(currentText.toString()) );
        
        else if ( MODIFIED.equals(qName) ) 
            currentEntry.setLastModified( parseDate(currentText.toString()) );
        
        else if ( FETCHED.equals(qName) ) 
            currentEntry.setLastFetched( parseDate(currentText.toString()) );
          
        
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
            currentEntry = new Entry(new URL( atts.getValue(HREF_ATTR) ));
        } catch(java.net.MalformedURLException ex) {
            throwException("Not a valid URL in "+ENTRY, ex);
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
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, AUTHORS, AUTHOR} ) )
                throwException(AUTHOR+" must be child Element of "+ENTRY+"/"+AUTHORS);
        }
        
        // note
        else if ( NOTE.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, NOTE} ) )
                throwException(NOTE+" must be child Element of "+ENTRY);
        }
        
        // tags
        else if ( TAGS.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, TAGS} ) )
                throwException(TAGS+" must be child Element of "+ENTRY);
        }
        
        // tag
        else if ( TAG.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, TAGS, TAG} ) )
                throwException(TAG+" must be child Element of "+ENTRY+"/"+TAGS);
        }
        
        // issued
        else if ( ISSUED.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, ISSUED} ) )
                throwException(ISSUED+" must be child Element of "+ENTRY);
        }
        
        // modified
        else if ( MODIFIED.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, MODIFIED} ) )
                throwException(MODIFIED+" must be child Element of "+ENTRY);
        }
        
        // fetched
        else if ( FETCHED.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, FETCHED} ) )
                throwException(FETCHED+" must be child Element of "+ENTRY);
        }
        
        // type
        else if ( TYPE.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, TYPE} ) )
                throwException(TYPE+" must be child Element of "+ENTRY);
        }
        
        // title
        else if ( TITLE.equals(currentTag) ) {
            if ( !compareToStack( new String[] {ENTRYLIST, ENTRY, TITLE} ) )
                throwException(TITLE+" must be child Element of "+ENTRY);
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
