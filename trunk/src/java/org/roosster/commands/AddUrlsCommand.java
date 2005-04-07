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
package org.roosster.commands;

import java.util.Map;
import java.util.Date;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
import org.roosster.Configuration;
import org.roosster.Command;
import org.roosster.Constants;
import org.roosster.Registry;
import org.roosster.Output;

/**
 *
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AddUrlsCommand extends AbstractCommand implements Command, Constants
{
    private static Logger LOG = Logger.getLogger(AddUrlsCommand.class);
    
    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        EntryList entryList = (EntryList) arguments.get(PARAM_ENTRIES);
        
        if ( entryList != null && entryList.size() > 0 ) {
            Configuration conf = registry.getConfiguration();
            
            // shall existing entries get overwritten, or an exception thrown?
            boolean force = Boolean.valueOf(conf.getProperty(ARG_FORCE, "false")).booleanValue();
            boolean pub = Boolean.valueOf(conf.getProperty(ARG_PUBLIC, "false")).booleanValue();
            boolean fetch = Boolean.valueOf(conf.getProperty(PROP_FETCH_CONTENT, "true")).booleanValue();

            Entry[] entries = new Entry[0];
            if ( fetch ) {
              
                URL[] urls = new URL[entryList.size()];
                for ( int i = 0; i < entryList.size(); i++ ) {
                    urls[i] = entryList.getEntry(i).getUrl();
                } 
                
                UrlFetcher fetcher = (UrlFetcher) registry.getPlugin(Constants.PLUGIN_FETCHER);
                entries = fetcher.fetch(urls);
                
                for ( int i = 0; i < entries.length; i++ ) {
                    Entry entry = entryList.getEntry(entries[i].getUrl());
                    
                    // overwrite the fetched values, if others have been provided (in entryList)
                    if ( entry != null ) 
                        entries[i].overwrite(entry);
                    
                    entries[i].setPublic(pub);
                } 
                    
            } else {
                entries = (Entry[]) entryList.toArray(new Entry[0]);
                for ( int i = 0; i < entries.length; i++ ) {
                    entries[i].setPublic(pub);
                }
            }
            
            // now finally store entries in index
            EntryStore store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
            store.addEntries(entries, force);
            
            // output informative message to user
            output.addOutputMessage("Number of added Entries: "+entries.length);
            output.addEntries(entries);
            
        } else {
            LOG.warn("Didn't add any Entries in "+getClass()+", as the list was empty!");
            output.addOutputMessage("Didn't add any Entries, as the list was empty!");
        }
        
        
    }


    /**
     */
    public String getName()
    {
        return "Add URL";
    }

}
