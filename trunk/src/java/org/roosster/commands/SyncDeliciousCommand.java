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

import del.icio.us.Delicious;
import del.icio.us.beans.Post;
import org.apache.log4j.Logger;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.util.StringUtil;
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
        
        LOG.info("Using del.icio.us username: "+conf.getProperty(PROP_DELICIOUS_USER));
        String apiEndPoint = conf.getProperty(PROP_DELICIOUS_APIENDPOINT, DEF_DELICIOUS_APIENDPOINT);
        Delicious delicious = new Delicious(conf.getProperty(PROP_DELICIOUS_USER), 
                                            conf.getProperty(PROP_DELICIOUS_PASS),
                                            apiEndPoint);

        if ( timeMillisString == null ) {
            // if last update time of roosster is null, then roosster was never synced 
            // with del.icio.us so we get all posts and say a big "Sorry Joshua"

            LOG.info("Never synced before with del.icio.us, so this may take some time! Go, get yourself a coffee!");
            //syncDelicious(delicious, arguments, registry, output);

        } else {
            // * ask del.icio.us if something changed since then; if something 
            // changed, get all posts from del.icio.us and do the diff as above, 
            // * if nothing changed on del.icio.us but on roosster then just post
            // entries added to roosster to del.icio.us
             
            Date lastSync = new Date(Long.parseLong(timeMillisString));
            Date lastDelUpdate = delicious.getLastUpdate();
            
            LOG.info("Last sync was "+lastSync+", del.icio.us' last update was "+lastDelUpdate);

            if ( lastDelUpdate.after(lastSync) ) {
                LOG.info("Syncing del.icio.us and roosster because del.icio.us changed at later point");
                //syncDelicious(delicious, arguments, registry, output);

            } else {
                String lastUpdateStr = conf.getProperty(LAST_UPDATE);
    
                if ( lastUpdateStr != null ) {
                    Date lastUpdate = new Date(Long.parseLong(lastUpdateStr));

                    if ( lastSync.after(lastUpdate) ) {
                        LOG.debug("No entries changed on del.icio.us and in roosster. No action needed!");
                    } else {
                        
                        LOG.info("Syncing del.icio.us and roosster because roosster changed at later point than del.icio.us");
                        //syncDelicious(delicious, arguments, registry, output);
                    }
                } else {
                    LOG.warn("No Entries added to roosster yet! No sense in syncing yet!");
                }
            }
            
        }
        


        // TODO add number of synced posts to output messages
            
      
        conf.setRequestProperty(PROP_DELICIOUS_LASTSYNC, System.currentTimeMillis() +"");
        conf.persist(new String[] {PROP_DELICIOUS_LASTSYNC});
    }


    /**
     */
    public String getName()
    {
        return "Sync with del.icio.us";
    }


    // ============ private Helper methods ============


    /**
     *
     */
    private void syncDelicious(Delicious delicious, Map arguments, Registry registry, Output output)
                        throws Exception
    {
        Configuration conf = registry.getConfiguration();

        String syncMasterStr = conf.getProperty(PROP_DELICIOUS_SYNCMASTER);
        boolean roossterMaster = "roosster".equals(syncMasterStr) ? true : false;
        
        List allDelPosts = delicious.getAllPosts();

        EntryStore store = (EntryStore) registry.getPlugin("store");
        EntryList allEntries = store.getAllEntries(0, Integer.MAX_VALUE);


        // TODO what about deleted items ?
        
        if ( roossterMaster ) { 

            // parse del.icio.us posts into map, to allow easy access by URL
            Map allPostsMap = new HashMap();
            for (int i = 0; i < allDelPosts.size(); i++) {
                Post post = (Post) allDelPosts.get(i);
                allPostsMap.put(new URL(post.getHref()), post);
            }

            List addToDelicious = new ArrayList();
            for (int i = 0; i < allEntries.size(); i++) {
                Entry entry = (Entry) allEntries.get(i);

                if ( allPostsMap.containsKey(entry.getUrl()) ) {
                    // post in both systems, determine if post to slave
                    // system is needed and perform eventually
                    
                    LOG.debug("Syncing: "+entry);
                    
                    // delete post from allDelMap
                    allPostsMap.remove(entry.getUrl()); 
                    
                } else {
                    // entry not in del.icio.us yet
                    addToDelicious.add(entry); 
                }

            }

            // loop over addToDelicious and post Entries

            // see if allDelMap contains any posts, if this is the
            // case, delete them from del.icio.us if respective option
            // is set 

        } else {  
             
            // parse roosster entries into a map, to allow easy access by URL
            Map allEntriesMap = new HashMap();
            for (int i = 0; i < allEntries.size(); i++) {
                allEntriesMap.put(allEntries.getEntry(i).getUrl().toString(), allEntries.getEntry(i));
            }

            List addToRoosster  = new ArrayList();
            for (int i = 0; i < allDelPosts.size(); i++) {
                Post post = (Post) allDelPosts.get(i);

                if ( allEntriesMap.containsKey(post.getHref()) ) {
                    // post contained in both systems
                    // determine, if post in slave syste is needed

                    LOG.debug("Syncing: "+post);

                    // delete entry from allEntriesMap
                    allEntriesMap.remove(post.getHref())
                } else {
                    addToRoosster.add( post2Entry(post) ); 
                }
            }

            // loop over addToRoosster and store Entries contained within
            
            // see if allEntriesMap contains any entries, if this is the
            // case, delete them from roosster if respective option is set 

            Iterator entriesIter = allEntriesMap.values().iterator();
        } 
        
    }

            
    /**
     * overwrite the post object with data stored in entry object and
     * return post object
     */
    private Post overwritePost(Post post, Entry entry)
    {
        post.setTag( StringUtil.join(entry.getTags(), " ") );
        post.setDescription(entry.getTitle());
        post.setExtended(entry.getNote());
        return post;
    }
    
    
    /**
     * overwrite the entry object with data stored in post object and
     * return post object
     */
    private Entry overwriteEntry(Post post, Entry entry)
    {
        entry.setTags( StringUtil.split(post.getTag(), " ") );
        entry.setTitle(post.getDescription());
        entry.setNote(post.getExtended());
        entry.setAdded(post.getTimeAsDate());

        return entry;
    }
    
    
    /**
     *
     */
    private Entry post2Entry(Post post) throws Exception
    {
        Entry entry = new Entry(new URL(post.getHref()));
        entry.setTags( StringUtil.split(post.getTag(), " ") );
        entry.setTitle(post.getDescription());
        entry.setNote(post.getExtended());
        entry.setAdded(post.getTimeAsDate());

        return entry;
    }
    
    
}

        /*
        if ( timeMillisString == null ) {
            // if last update time of roosster is null, then roosster was never synced 
            // with del.icio.us so we get all posts and say a big "Sorry Joshua"
            
            LOG.info("Never synced before with del.icio.us, so this may take some time! Go, get yourself a coffee!");
            
            // build a diff between the set of entries stored in roosster and
            // the one fetched from del.icio.us
            // respect only the ones with public set to true
            
            List addToRoosster = new ArrayList();
            List addToDelicious = new ArrayList();
            
            List deliciousPosts = delicious.getAllPosts();
            Iterator postIter = deliciousPosts.iterator();

            while ( postIter.hasNext() ) {
                Post post = (Post) postIter.next();
               
                Entry entry = store.getEntry(new URL(post.getHref()));
                if ( entry != null) {
                    LOG.debug(entry +" stored in both roosster and del.icio.us. "+
                              "Overwriting data on del.icio.us with roosster's: "+roossterMaster);
                    
                    if ( roossterMaster )
                        addToDelicious.add( overwritePost(post, entry);
                    else
                        addToRoosster.add( overwriteEntry(post, entry) );
                      
                   
                } else {
                    LOG.debug(post.getHref() +" not found in roosster.  Storing in roosster! Add to queue for later fetching!");
                    addToRoosster.add( overwriteEntry(post, new Entry(new URL(post.getHref()))) );
                }
            }

            // put the entries into arguments, so the addurls command
            // adds them to the store
            arguments.put(PARAM_ENTRIES, new EntryList(addToRoosster)); 
               
           
            // TODO post post contained in addToDelicious to del.icio.us
            
            // TODO what about those posts not in del.icio.us but in
            // roosster? and what about posts deleted in one system
            // but not the other?
            
            
        } else {
            // * ask del.icio.us if something changed since 
            // then; if something changed, and if any entries in
            // roosster changed since then get all posts from
            // del.icio.us and do the diff as above, 
            // * if nothing changed on del.icio.us but on roosster then just post
            // entries added to roosster to del.icio.us
             
            Date lastDelUpdate = delicious.getLastUpdate();
            
            LOG.info("Last sync was "+lastSync+", del.icio.us' last update was "+lastDelUpdate);

            
            // get all posts 
            
            EntryList changedEntries = store.getChangedEntries(lastSync, null);
            output.setEntries(changedEntries);            
        }
*/
