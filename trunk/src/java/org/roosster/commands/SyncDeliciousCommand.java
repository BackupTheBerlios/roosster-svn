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
import java.util.List;

import del.icio.us.Delicious;
import org.apache.log4j.Logger;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.Constants;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.Output;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class SyncDeliciousCommand extends AbstractCommand implements Command, Constants
{
    private static Logger LOG = Logger.getLogger(SyncDeliciousCommand.class);
    
    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        Configuration conf = registry.getConfiguration();
      
        String timeMillisString = conf.getProperty(PROP_DELICIOUS_LASTSYNC);
        
        Delicious delicious = new Delicious(conf.getProperty(PROP_DELICIOUS_USER), 
                                            conf.getProperty(PROP_DELICIOUS_PASS));
        
        List posts = null;
        if ( timeMillisString == null ) {
            // if last update time of roosster is null, then roosster was never synced 
            // with del.icio.us so we get all posts and say a big "Sorry Joshua"
            
            LOG.info("Never synced before with del.icio.us, so this may take some time! Go, get yourself a coffee!");
            
            // build a diff between the set of entries stored in roosster and
            // the one fetched from del.icio.us
            // respect only the ones with public set to true
            
        } else {
            // ok we synced once, now ask del.icio.us if something changed since 
            // then; if something changed, then get all posts since the roosster
            // last update time
            
            Date lastSync = new Date(Long.parseLong(timeMillisString));
            Date lastDelUpdate = delicious.getLastUpdate();
            
            LOG.info("Last sync was "+lastSync+", del.icio.us' last update was "+lastDelUpdate);

            
            // get items from del.icio.us since lastSync, or if lastSync is after
            // lastDelUpdate get all Posts since lastDelUpdate
            
            
            
            // build a diff between the two sets, post new links to del.icio.us, 
            // update changed ones, and store new links in roosster
            // respect only the ones with public set to true
            
            
            EntryStore store = (EntryStore) registry.getPlugin("store");
            EntryList changedEntries = store.getChangedEntries(lastSync, null);
            output.setEntries(changedEntries);            
        }
            
      
        conf.setRequestProperty(PROP_DELICIOUS_LASTSYNC, System.currentTimeMillis() +"");
        conf.persist(new String[] {PROP_DELICIOUS_LASTSYNC});
    }


    /**
     */
    public String getName()
    {
        return "Sync with del.icio.us";
    }
    
    
}


