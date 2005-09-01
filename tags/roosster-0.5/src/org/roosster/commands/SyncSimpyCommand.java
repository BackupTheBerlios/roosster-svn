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

import java.util.*;
import java.net.URL;

import org.apache.log4j.Logger;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
import org.roosster.util.StringUtil;
import org.roosster.Constants;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.Output;

/**
 * look at SyncDeliciousCommand for details how the sync process is
 * run.
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class SyncSimpyCommand extends AbstractCommand implements Command, Constants
{
    private static Logger LOG = Logger.getLogger(SyncDeliciousCommand.class);
    
    // default for the pub: field for newly added entries in roosster
    public static final boolean DEF_PUBLIC_STATUS        = true;
    
    public static final String DEF_SIMPY_APIENDPOINT = "http://del.icio.us/api/";    
    
    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        Configuration conf = registry.getConfiguration();
      
        String timeMillisString = conf.getProperty(PROP_SIMPY_LASTSYNC);
        
        LOG.info("Using simpy username: "+conf.getProperty(PROP_SIMPY_USER));
        String apiEndPoint = conf.getProperty(PROP_SIMPY_APIENDPOINT, 
                                              DEF_SIMPY_APIENDPOINT);
        
        // construct Simpy object here

        if ( timeMillisString == null ) {

            LOG.info("Never synced before with simpy, so this may take some time! "+
                    "Go, get yourself a coffee!");

            //syncSimpy(null, registry, new Date(0));

        } else {
            String dateStr = conf.getProperty(LAST_UPDATE);
            
            // time of last update in roosster
            Date lastRsstUpdate = dateStr != null ? new Date(Long.parseLong(dateStr)) : new Date();
            
            // time of last sync between roosster and del.icio.us
            Date lastSync       = new Date(Long.parseLong(timeMillisString));
            
            // time of last update of del.icio.us 

            /*
            Date lastDelUpdate  = delicious.getLastUpdate(); // remote call
            
            LOG.info("Last sync was "+lastSync+", del.icio.us' last update was "+lastDelUpdate);

            
            if ( lastSync.after(lastDelUpdate) && lastSync.after(lastRsstUpdate) ) 
                LOG.info("No entries changed on del.icio.us and in roosster. No action needed!");
            else 
                syncDelicious(delicious, registry, lastSync);
            
            */
        }

        // store current time in properties
        conf.setProperty(PROP_SIMPY_LASTSYNC, System.currentTimeMillis() +"");
        conf.persist(new String[] {PROP_SIMPY_LASTSYNC, LAST_UPDATE});
    }


    /**
     */
    public String getName()
    {
        return "Sync with simpy.com";
    }


    // ============ private Helper methods ============


    /**
     */
        /*
    private void syncSimpy(Registry registry, Date lastSync)
                        throws Exception
    {
        Configuration conf = registry.getConfiguration();
        
        String deletePropStr = conf.getProperty(PROP_DELICIOUS_DELETE, "true");
        boolean deleteProp = "true".equalsIgnoreCase(deletePropStr) ? true : false;
        
        LOG.debug("Getting all Posts from del.icio.us now");
        List allDelPosts = delicious.getAllPosts(); // remote call
        LOG.debug("Fetched "+allDelPosts.size()+" from del.icio.us");

        EntryStore store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
        EntryList allRsstEntries = store.getAllEntries(0, Integer.MAX_VALUE, false);
          
        // parse del.icio.us posts into map, to allow easy access by URL
        LOG.debug("Building lookup Map for del.icio.us posts");
        Map allPostsMap = new HashMap();
        for (int i = 0; i < allDelPosts.size(); i++) {
            Post post = (Post) allDelPosts.get(i);
            fixPost(post);
            allPostsMap.put(post.getHref(), post);
        }
        
        
        List volatileDelicious = new ArrayList(); // contains Post objects
        List volatileRoosster = new ArrayList(); // contains Entry objects
        
        List deleteDelicious = new ArrayList(); // contains Post objects
        List deleteRoosster = new ArrayList(); // contains Entry objects
        
        List updateDelicious = new ArrayList(); // contains Post objects
        List updateRoosster = new ArrayList(); // contains Entry objects
        Map addToRoosster = new HashMap(); // contains Entry objects, keyed by URL
        
        LOG.debug("Looping over "+allRsstEntries.size()+" roosster entries");
        for (int i = 0; i < allRsstEntries.size(); i++) {
            Entry entry = (Entry) allRsstEntries.get(i);
            Post post = (Post) allPostsMap.get(entry.getUrl().toString());
            
            if ( post != null ) {
                if ( !equal(post, entry) ) {
                    merge(post, entry);
                    updateRoosster.add(entry);
                    
                    // only post to del.icio.us if it's a public bookmark
                    if ( entry.getPublic() ) {
                        updateDelicious.add(post);
                        LOG.debug(post.getHref()+" is public! Will be added/updated in del.icio.us!");
                    }
                } else {  
                    LOG.debug(entry+" didn't change! No action needed!");
                }
                    
                if ( entry.getPublic() ) 
                    allPostsMap.remove(entry.getUrl().toString()); 
            } else {
                volatileRoosster.add(entry);
            }
        }
        
        volatileDelicious.addAll(allPostsMap.values());
        
        // for entries/posts in volatile*, determine if they are deletes or 
        // adds and act accordingly
        
        for(int i = 0; i < volatileRoosster.size(); i++) {
            Entry entry = (Entry) volatileRoosster.get(i);
            if ( lastSync.after(entry.getAdded()) && entry.getPublic() ) {
                LOG.debug(entry+" was deleted from del.icio.us! Will be deleted from roosster!\n");
                deleteRoosster.add(entry);
            } else if ( entry.getPublic() ) {
                LOG.debug(entry+" was added to roosster and is public! Will be added to del.icio.us\n");
                updateDelicious.add( entry2Post(entry) );
            }
        }
        
        
        for(int i = 0; i < volatileDelicious.size(); i++) {
            Post post = (Post) volatileDelicious.get(i);
            if ( lastSync.after(post.getTimeAsDate()) ) {
                LOG.debug(post.getHref()+" -- was deleted from roosster! Will be deleted from del.icio.us!\nAdded: "+post.getTimeAsDate()+"\n");
                deleteDelicious.add(post);
            } else {
                LOG.debug(post.getHref()+" -- was added to del.icio.us! Will be added to roosster!\n Added: "+post.getTimeAsDate()+"\n");
                Entry entry = post2Entry(post); 
                addToRoosster.put(entry.getUrl().toString(), entry);
            }
        }

        // 
        // write now to delicious
        //
        for(int i = 0; i < updateDelicious.size(); i++) {
            Post post = (Post) updateDelicious.get(i);
            
            LOG.debug("Updating/Adding <"+post.getHref()+"> to del.icio.us!");
            
            // fulfill Joshua's restriction by posting calling API once per second 
            Thread.sleep(1000);
            boolean added = delicious.addPost(post.getHref(), post.getDescription(), 
                                              post.getExtended(), post.getTag(), 
                                              post.getTimeAsDate());
            if ( !added )
                LOG.warn("Failed to post "+post.getHref()+" to del.icious");
        }        
        
        // write changed entries to roosster
        store.addEntries((Entry[]) updateRoosster.toArray(new Entry[0]), true); 
        
        // fetch contents of new URLs and add Entries to roosster
        String[] urls = (String[]) addToRoosster.keySet().toArray(new String[0]);
        
        if ( urls.length > 0 ) {
            UrlFetcher fetcher = (UrlFetcher) registry.getPlugin(Constants.PLUGIN_FETCHER);
            
            for ( int i = 0; i < urls.length; i++ ) {
                Entry[] fetchedEntries =  fetcher.fetch( new URL[] {new URL(urls[i])} );
                
                for ( int k = 0; k < fetchedEntries.length; k++ ) {
                    Entry entry = (Entry) addToRoosster.get(fetchedEntries[k].getUrl().toString());
                    if ( entry != null ) 
                        fetchedEntries[k].overwrite(entry);
                }
                store.addEntries(fetchedEntries, true);
            }
        }
     
        // should we delete?
        if ( deleteProp ) {
          
            // delete from del.icio.us
            for(int i = 0; i < deleteDelicious.size(); i++) {
                Post post = (Post) deleteDelicious.get(i);        
                
                LOG.debug("DELETING "+post.getHref()+" from del.icio.us!");
                            
                // fulfill Joshua's restriction by posting calling API once per second 
                Thread.sleep(1000);
                if ( !delicious.deletePost(post.getHref()) )
                    LOG.warn("Failed to DELETE "+post.getHref()+" from del.icious");
            }
            
            // delete from roosster
            LOG.debug("Deleting "+deleteRoosster.size()+" Entries from roosster now\n\n");
            store.deleteEntries((Entry[]) deleteRoosster.toArray(new Entry[0]));
        }
    }

    */    
    
}

