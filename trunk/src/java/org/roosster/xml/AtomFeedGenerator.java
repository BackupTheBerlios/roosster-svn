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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;
import org.roosster.store.EntryList;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.WireFeedOutput; 


/**
 * Given a set of {@link org.roosster.store.Entry Entry}-objects this class
 * generates an Atom-Feed (version 0.3).
 *
 * @see <a href="http://atompub.org/2004/10/20/draft-ietf-atompub-format-03.html">Atom Syndication Format Spec v0.3</a>
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AtomFeedGenerator
{
    public static final String ATOM_VERSION  = "0.3";

    public static final String DEF_TITLE   = Constants.APP_NAME +" feed";
    public static final String PROP_TITLE  = "output.atom.title";

    
    /**
     * @param entries
     */
    public void createFeed(Registry registry, PrintWriter writer, EntryList entries)
                    throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        try {

            Feed feed = new Feed("atom_"+ ATOM_VERSION);
            
            feed.setTitle( registry.getConfiguration().getProperty(PROP_TITLE, DEF_TITLE) );
            feed.setModified( entries.getLastModified() );
            
            // TODO add Generator
            // TODO add <link rel="alternate"> with url of this invocation

            List atomEntries = new ArrayList();
            for(int i = 0; i < entries.size(); i++) {
                atomEntries.add( getEntry((org.roosster.store.Entry) entries.get(i)) );
            }
            feed.setEntries(atomEntries);

            WireFeedOutput output = new WireFeedOutput();
            output.output(feed, writer);
            
        } catch(Exception ex) {
            throw new OperationException(ex);
        }

    }


    // ============ private Helper methods ============


    /**
     * TODO what about escaping content?
     */
    private Entry getEntry(org.roosster.store.Entry entry)
    {
        Person author = new Person();
        author.setName(entry.getAuthor());
        author.setEmail(entry.getAuthorEmail());
        
        Link link = new Link();
        link.setHref(entry.getUrl().toString());
        link.setRel("alternate");
        
        List linkList = new ArrayList();
        linkList.add(link);

        Content content = new Content();
        content.setValue(entry.getContent());
        content.setType(entry.getFileType());
        
        List contentList = new ArrayList();
        contentList.add(content);
        
        // now create Atom Entry Element        
        Entry atomEntry = new Entry();
        
        atomEntry.setTitle(entry.getTitle());
        atomEntry.setAuthor(author);
        atomEntry.setAlternateLinks(linkList);
        

        atomEntry.setCreated( entry.getIssued());
        atomEntry.setModified( entry.getLastModified());
        atomEntry.setContents(contentList);
        
        return atomEntry;
    }

}
