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

import org.roosster.store.EntryStore;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
import org.roosster.Command;
import org.roosster.Constants;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.util.StringUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AddUrlCommand extends AbstractCommand implements Command, Constants
{

    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        validateArguments(arguments, new String[] {ARG_URL});

        UrlFetcher fetcher = (UrlFetcher) registry.getPlugin("fetcher");
        Entry[] entries = fetcher.fetch(new URL[] {new URL( (String) arguments.get(ARG_URL) )});
        
        if ( entries.length > 0 ) {
            // read in and set optional fields
            String title = (String) arguments.get(ARG_TITLE);
            if ( title != null && !"".equals(title) ) 
                for(int i = 0; i < entries.length; i++) { entries[i].setTitle(title); }
            
            String note  = (String) arguments.get(ARG_NOTE);
            String tags  = (String) arguments.get(ARG_TAGS);
            if ( note != null || tags != null ) {
                for(int i = 0; i < entries.length; i++) {
                    if ( note != null )
                        entries[i].setNote(note);
                    if ( tags != null ) 
                        entries[i].setTags( StringUtil.splitString(tags, Entry.TAG_SEPARATOR) );
                }
            }

            // shall existing entries get overwritten, or an exception thrown?
            String forceStr = (String) arguments.get(ARG_FORCE);
            boolean force = false;
            if ( "1".equals(forceStr) || "true".equalsIgnoreCase(forceStr) )
                force = true;

            // now finally store entries in index
            EntryStore store = (EntryStore) registry.getPlugin("store");
            store.addEntries(entries, force);
            
            output.addOutputMessage("Number of added Entries: "+entries.length);
            output.addEntries(entries);
        }
    }


    /**
     */
    public String getName()
    {
        return "Add URL";
    }

}
