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
package org.roosster.input.processors;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.io.CopyUtils;
import org.apache.log4j.Logger;
import org.roosster.InitializeException;
import org.roosster.Registry;
import org.roosster.input.ContentTypeProcessor;
import org.roosster.store.Entry;
import org.roosster.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TODO respect img-alt-tags
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class HtmlProcessor implements ContentTypeProcessor
{
    public static final String FILE_TYPE = "text/html";

    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {}


    /**
     *
     */
    public boolean isInitialized()
    {
        return true;
    }


    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
    }


    /**
     *
     */
    public Entry[] process(URL url, InputStream stream, String encoding) throws Exception
    {
        Entry entry = new Entry(url);

        // set some basic properties
        entry.setFileType(FILE_TYPE);
        entry.setAdded(new Date());
        
        // copy raw contents before processing stream
        Writer rawContents = new StringWriter();
        CopyUtils.copy(stream, rawContents);
        
        String rawString = rawContents.toString();
        entry.setRaw(rawString);

        // now process stream, and fill content
        HtmlParser parser = new HtmlParser(entry);
        parser.parse(new InputSource(new StringReader(rawString)));
        
        return new Entry[] { entry };
    }

    
    /**
     * Fills content and title of this Entry with the content
     */
    public static class HtmlParser extends org.ccil.cowan.tagsoup.Parser
    {
        private static Logger LOG = Logger.getLogger(HtmlParser.class.getName());
        
        private Entry entry = null;
        
        private LinkedList  elementStack       = new LinkedList();
        
        private String      currentTag         = null;
        
        private StringBuffer currentText       = new StringBuffer();
        
        private boolean isBody                 = false;
        private boolean isScript               = false;
        private boolean isStyle                = false;
        private boolean isTitle                = false;
        
        
        /**
         */
        public HtmlParser(Entry entry)
        {
            if ( entry == null )
                throw new IllegalArgumentException("Parameter 'entry' is not allowed to be null");
            
            this.entry = entry;
        }
        
        
        /**
         */
        public Entry getEntry() { return entry; }
        
        
        /**
         * 
         */
        public void startElement(String nsURI, String localName, String qName, Attributes atts)
                          throws SAXException
        {
            //LOG.debug("HTMLPARSE: --> Push <"+qName+"> onto element stack");
    
            elementStack.add(qName);
            
            currentTag = qName;
            
            if ( "title".equalsIgnoreCase(qName) )
                isTitle = true;
            else if ( "body".equalsIgnoreCase(qName) ) 
                isBody = true;
            else if ( "style".equalsIgnoreCase(qName) ) 
                isStyle = true;
            else if ( "script".equalsIgnoreCase(qName) ) 
                isScript = true;
        }
        
        
        /**
         * 
         */
        public void endElement(String nsURI, String localName, String qName)
                        throws SAXException
        {
            //LOG.debug("HTMLPARSE: <-- Pop <"+qName+"> from element stack\nCurrent Text is: "+currentText);
            
            if ( "title".equalsIgnoreCase(qName) ) {
                isTitle = true;
                entry.setTitle(currentText.toString());    
            } 
            else if ( "body".equalsIgnoreCase(qName) ) 
                isBody = false;
                
            else if ( "style".equalsIgnoreCase(qName) ) 
                isStyle = false;
            
            else if ( "script".equalsIgnoreCase(qName) ) 
                isScript = false;


            elementStack.removeLast();
        
            currentTag = elementStack.isEmpty() ? null : (String) elementStack.getLast();
            currentText = new StringBuffer();
        }
        
        
        /**
         * 
         */
        public void characters(char[] text, int start, int length) throws SAXException 
        {
            if ( isBody && !isScript && !isStyle ) {
                String str = StringUtil.strip(new String(text, start, length)).trim();
                if ( !"".equals(str) ) {
                    entry.appendContent(str);
                    entry.appendContent(" ");
                }
            } else if ( isTitle )
                currentText.append(StringUtil.strip(new String(text, start, length)).trim());
        }
    }
    
}
