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
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
import org.roosster.Constants;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.util.StringUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class PutEntriesCommand extends AbstractCommand implements Command, Constants
{

    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        validateArguments(arguments, new String[] {PARAM_ENTRIES});

        EntryList list = (EntryList) arguments.get(PARAM_ENTRIES);
        EntryStore store = (EntryStore) registry.getPlugin("store");

        if ( list != null ) {
            LOG.debug("Trying to update "+list.size()+" entries");
        
            for (int i = 0; i < list.size(); i++) {
                Entry entry = list.getEntry(i);
              
                Entry storedEntry = store.getEntry(entry.getUrl());
                
                LOG.debug("Got Entry "+storedEntry+" to update with Entry "+entry);
                
                if ( storedEntry != null ) {
                  
                    storedEntry.overwrite(entry);
                    
                    LOG.debug("Updated Entry, now trying to store it");
                    
                    //
                    // TODO what about first storing entries which should be updated 
                    // in extra list and then do a batch update ?
                    //
                    
                    // now finally update entry in index
                    store.addEntries(new Entry[] {storedEntry}, true);
                    
                    output.addOutputMessage("Entry saved! "+storedEntry.getUrl());
                    output.addEntry(storedEntry);
                  
                } else {
                    // TODO is this behaviour a bit tame?
                    output.addOutputMessage("Can't edit unstored entry: "+entry.getUrl());
                }
            }
            
        } else {
            // TODO make this an error
            output.addOutputMessage("Can't update empty entrylist");
        }
    }


    /**
     */
    public String getName()
    {
        return "Put Entries";
    }

}
