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

import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.roosster.OperationException;
import org.roosster.input.processors.HtmlProcessor;
import org.roosster.store.Entry;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class FeedParser 
{
    private static Logger LOG = Logger.getLogger(FeedParser.class.getName());
    
    private static final String ATOM_TYPE_TEXT  = "TEXT";
    private static final String ATOM_TYPE_HTML  = "HTML";
    private static final String ATOM_TYPE_XHTML = "XHTML";
    
    private static final String MIME_TYPE_TEXT  = "text/plain";
    private static final String MIME_TYPE_HTML  = "text/html";
    private static final String MIME_TYPE_XHTML = "application/xhtml+xml"; // TODO fix this
    
    
    /**
     * 
     * @param url
     * @param stream
     * @return
     * @throws OperationException
     */
    public Entry[] parse(URL url, String stream)  
                 throws OperationException
    {
        return parse(url, new StringReader(stream));
    }
    

    /**
     * 
     * @param url
     * @param stream
     * @return
     * @throws OperationException
     */
    public Entry[] parse(URL url, Reader stream)  
                                    throws OperationException
    {
        try {
          
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(stream);
        
            String feedTitle  = feed.getTitle() == null  ? "" : feed.getTitle();
            String feedAuthor = feed.getAuthor() == null ? "" : feed.getAuthor();
            
            List entryList = new ArrayList();
            
            Iterator iter = feed.getEntries().iterator();
            
            // if an feedEntry has no valid URL, the entry is skipped
            while ( iter.hasNext() ) {
                entryList.add( getEntry((SyndEntry) iter.next(), feedAuthor, feedTitle) );
            }
            
            return (Entry[]) entryList.toArray(new Entry[0]);
            
        } catch (Exception ex) {
            throw new OperationException(ex);
        }
    }
    
    
    // ============ private Helper methods ============
    
  
    /**
     * 
     * * If there is no HTML content element, the description is used regardless
     * what type it is. (even if it's empty)
     * 
     * * If there is more than one content element, the one with HTML type
     * has precedence
     * 
     * @return null if no entry can be created due to wrong or missing URL, valid
     * entry otherwise
     */
    private Entry getEntry(SyndEntry feedEntry, String feedAuthor, String feedTitle)
    {
        URL url = getURL(feedEntry);
        

        Entry entry = null; 

        // we can't create instances without valid URL
        if ( url != null ) {
            
            // now get the html content, if there are more than one content elements
            String content  = "";
            String fileType = "text/plain";
            
            SyndContent syndContent = null;
            
            String oldType = null;
            Iterator contentIter = feedEntry.getContents().iterator();
            
            while ( contentIter.hasNext() ) {
                SyndContent syn = (SyndContent) contentIter.next();
                
                String type = getFileType(syn.getType());
                if ( type != null && !MIME_TYPE_HTML.equals(oldType) ) {
                    syndContent = syn;
                    oldType = type;
                }
            }
            
            // use description as content, if none other is set
            if ( syndContent == null ) 
                syndContent = feedEntry.getDescription();
                
            // determine content and type, use text/plain type if content
            // is empty, or mime type is not recognized
            // I know that's far from acting sensibly -- that's TODO
            if ( syndContent != null && syndContent.getValue() != null ) {
              
                content = syndContent.getValue();
                
                String type = getFileType(syndContent.getType());
                fileType = type == null || "".equals(content) ? MIME_TYPE_TEXT : type;
                
            } else {
                LOG.warn("Can't determine content (or it's null) for URL "+url);
            }
            
        
            // now create roosster Entry                
            entry = new Entry(url);
            
            try {
                // spoof my stypid ContentHandler in thinking this is a normal HTML doc
                LOG.debug("content "+ content+"\n\n");
                new HtmlProcessor.HtmlParser(entry)
                                 .parse(new InputSource(new StringReader(content)));
                
            } catch (Exception ex) {
                LOG.warn("Exception while parsing HTML to get entry's content", ex);
                entry.setContent(content);
            }

            entry.setRaw(content);
            entry.setFileType(fileType);
            entry.setAuthor( feedEntry.getAuthor() == null ? feedAuthor : feedEntry.getAuthor() );
            
            // TODO cast to WireFeed and pull out author email
            
            entry.setTitle( feedEntry.getTitle() == null ? feedTitle : feedEntry.getTitle() );
            
            Date now = new Date();
            
            entry.setIssued( feedEntry.getPublishedDate() == null ? now : feedEntry.getPublishedDate() );
            entry.setAdded(now);
            
            // TODO fix this, create a WireFeed and use the modified date of that
            entry.setModified( now );
        
        } 
        
        return entry;
    }
    
    
    /**
     * 
     */
    private String getFileType(String syndType)
    {
        String returnStr = null;
      
        if ( ATOM_TYPE_HTML.equals(syndType) || MIME_TYPE_HTML.equals(syndType) )
            returnStr = MIME_TYPE_HTML;
        else if ( ATOM_TYPE_XHTML.equals(syndType) || MIME_TYPE_XHTML.equals(syndType) )
            returnStr = MIME_TYPE_XHTML;
        else if ( ATOM_TYPE_TEXT.equals(syndType) || MIME_TYPE_TEXT.equals(syndType) )
            returnStr = MIME_TYPE_TEXT;
        
        return returnStr;
    }


    /**
     * Some damn ugly code, because ROME's SyndFeed class combines RSS and ATOM
     * which in turn can't be compared when it comes to an entry's id and/or link.
     * They simply put it in different places, and code has to try out both 
     * alternatives
     */
    private URL getURL(SyndEntry feedEntry)
    {
        URL url = null;
        String uriString = null;
        try {
            
            if ( feedEntry.getLink() != null ) {
                url = new URL(feedEntry.getLink());
            } else {
                
                uriString = feedEntry.getUri();
                if ( uriString != null ) 
                    url = new URI(uriString).toURL();
                else 
                    LOG.warn("Feed Entry has 'null' URI and no link element. Can't create roosster Entry");
                
            }
            
        } catch (URISyntaxException ex) {
            LOG.warn("Skipping Entry! Can't create URL for Entry with "+uriString+
                     "/"+feedEntry.getLink(), 
                     ex);
        } catch (MalformedURLException ex) {
               LOG.warn("Skipping Entry! Can't create URL for Entry with "+uriString +
                         "/"+feedEntry.getLink(), 
                         ex);
        }
        
        return url;
    }

}
