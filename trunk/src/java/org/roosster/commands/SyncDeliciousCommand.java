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
 * The syncing process is as follows:
 * <ol>
 * <li>Determine if sync is needed (true if last update of roosster or del.icio.us
 * is after last sync, and of course if roosster contains Entries)</li>
 * <li>If sync is needed, get all public entries from roosster and del.icio.us</li>
 * <li>now compare all del.icio.us posts with all roosster entries, determining
 * if posts, that are contained in both systems changed, and therefore need to 
 * be synced and then updated on both systems</li>
 * <li>if a post is only contained in one system, determine if it's a delete
 * (date or addedDate in the remaining system is before lastSync) or a new post
 * (date or addedDate in the containing system is after lastSync)</li>
 * <li>delete, add and update the respective items</li>
 * </ol>
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class SyncDeliciousCommand extends AbstractCommand implements Command, Constants
{
    private static Logger LOG = Logger.getLogger(SyncDeliciousCommand.class);
    
    public static final String DELICIOUS_TAG_SEP   = " ";
    public static final String DELICIOUS_TAG_EMPTY = "system:unfiled";
    
    public static final String PROP_DELICIOUS_USER       = "delicious.username";
    public static final String PROP_DELICIOUS_PASS       = "delicious.password";
    public static final String PROP_DELICIOUS_LASTSYNC   = "delicious.lastsync";
    
    public static final String PROP_DELICIOUS_APIENDPOINT= "delicious.api.endpoint";
    public static final String DEF_DELICIOUS_APIENDPOINT = "http://del.icio.us/api/";    
    
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
            syncDelicious(delicious, registry, new Date(0));

        } else {
            String dateStr = conf.getProperty(LAST_UPDATE);
            
            // time of last update in roosster
            Date lastRsstUpdate = dateStr != null ? new Date(Long.parseLong(dateStr)) : new Date();
            
            // time of last sync between roosster and del.icio.us
            Date lastSync       = new Date(Long.parseLong(timeMillisString));
            
            // time of last update of del.icio.us 
            Date lastDelUpdate  = delicious.getLastUpdate(); // remote call
            
            LOG.info("Last sync was "+lastSync+", del.icio.us' last update was "+lastDelUpdate);

            
            if ( lastSync.after(lastDelUpdate) && lastSync.after(lastRsstUpdate) ) 
                LOG.info("No entries changed on del.icio.us and in roosster. No action needed!");
            else 
                syncDelicious(delicious, registry, lastSync);
            
        }

        // store current time in properties
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
     */
    private void syncDelicious(Delicious delicious, Registry registry, Date lastSync)
                        throws Exception
    {
        Configuration conf = registry.getConfiguration();
        
        List allDelPosts = delicious.getAllPosts(); // remote call

        EntryStore store = (EntryStore) registry.getPlugin("store");
        EntryList allRsstEntries = store.getAllEntries(0, Integer.MAX_VALUE);
          
        // parse del.icio.us posts into map, to allow easy access by URL
        Map allPostsMap = new HashMap();
        for (int i = 0; i < allDelPosts.size(); i++) {
            Post post = (Post) allDelPosts.get(i);
            fixPost(post);
            allPostsMap.put(new URL(post.getHref()), post);
        }
        
        
        List volatileDelicious = new ArrayList(); // contains Post objects
        List volatileRoosster = new ArrayList(); // contains Entry objects
        
        List deleteDelicious = new ArrayList(); // contains Post objects
        List deleteRoosster = new ArrayList(); // contains Entry objects
        
        List updateDelicious = new ArrayList(); // contains Entry objects
        List updateRoosster = new ArrayList(); // contains Entry objects
        
        for (int i = 0; i < allRsstEntries.size(); i++) {
            Entry entry = (Entry) allRsstEntries.get(i);
            Post post = (Post) allPostsMap.get(entry.getUrl());
            
            if ( post != null ) {
                if ( !equal(post, entry) ) {
                    // post and entry need to be merged and written to both systems
                    LOG.debug(entry+" changed! Will be merged and updated in both systems!");
                    updateDelicious.add(post);
                    updateRoosster.add(entry);
                } else {  
                    LOG.debug(entry+" didn't change! No action needed!");
                }
                    
                allPostsMap.remove(entry.getUrl()); 
            } else {
                volatileRoosster.add(entry);
            }
        }
        
        volatileDelicious.addAll(allPostsMap.values());
        
        // for entries/posts in volatile*, determine if they are deletes or 
        // adds and act accordingly
        
        for(int i = 0; i < volatileRoosster.size(); i++) {
            Entry entry = (Entry) volatileRoosster.get(i);
            if ( lastSync.after(entry.getAdded()) ) {
                LOG.debug(entry+" was deleted from del.icio.us! Will be deleted from roosster!");
                deleteRoosster.add(entry);
            } else {
                LOG.debug(entry+" was added to roosster! Will be added to del.icio.us");
                updateDelicious.add(entry);
            }
        }
        
        
        for(int i = 0; i < volatileDelicious.size(); i++) {
            Post post = (Post) volatileDelicious.get(i);
            if ( lastSync.after(post.getTimeAsDate()) ) {
                LOG.debug(post+" was deleted from roosster! Will be deleted from del.icio.us!");
                deleteDelicious.add(post);
            } else {
                LOG.debug(post+" was added to del.icio.us! Will be added to roosster");
                updateRoosster.add( post2Entry(post) );
            }
        }
        
        
    }

    
    /**
     * does not use href/URL to determine equality, but only Title/Description,
     * Note/Extended and tags (not respecting their order).
     */
    private boolean equal(Post post, Entry entry)
    {
        boolean equal = true;
        
        if ( !entry.getNote().equals(post.getExtended()) ) 
            return false;
        if ( !entry.getTitle().equals(post.getDescription()) )
            return false;
        
        String[] entryTags = entry.getTags();
        Arrays.sort(entryTags);
        String[] postTags = StringUtil.split(post.getTag(), DELICIOUS_TAG_SEP);
        Arrays.sort(postTags);
        
        // del.icio.us has no concept of "zero tags", if a post has no tags,
        // it has the tag defined in DELICIOUS_TAG_EMPTY (cool, eeh?)
        if ( postTags.length == 1 && DELICIOUS_TAG_EMPTY.equals(postTags[0]) )
            postTags = new String[0];
        
        return Arrays.equals(entryTags, postTags);
    }
            
    
    /**
     * 'fix' post object, so that it contains no <code>null</code>-String objects
     */
    private void fixPost(Post post)
    {
        if ( post.getExtended() == null )
            post.setExtended("");
        if ( post.getDescription() == null )
            post.setDescription("");
        if ( post.getTag() == null )
            post.setTag("");
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
        entry.setTags( StringUtil.split(post.getTag(), DELICIOUS_TAG_SEP) );
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
        entry.setTags( StringUtil.split(post.getTag(), DELICIOUS_TAG_SEP) );
        entry.setTitle(post.getDescription());
        entry.setNote(post.getExtended());
        entry.setAdded(post.getTimeAsDate());

        return entry;
    }
    
}


/* 


        List addToDelicious = new ArrayList(); // contains Entry objects
        for (int i = 0; i < allEntries.size(); i++) {
            Entry entry = (Entry) allEntries.get(i);
            
            Post post = (Post) allPostsMap.get(entry.getUrl());
            
            if ( post != null ) {
                if ( !equal(post, entry) ) 
                    addToDelicious.add( post2Entry(post).overwrite(entry) );
                else  
                    LOG.debug(entry+" didn't change! No action needed!");
                    
                allPostsMap.remove(entry.getUrl()); 
            } else {
                // entry not in del.icio.us yet, add it
                addToDelicious.add(entry); 
            }

        }

        // loop over addToDelicious and post Entries
        Iterator addEntriesIter = addToDelicious.iterator();
        while ( addEntriesIter.hasNext() ) {
            Entry entry = (Entry) addEntriesIter.next();
            
            LOG.debug("Updating/Adding <"+entry+"> to del.icio.us!");
            
            Thread.sleep(1000);
            boolean added = delicious.addPost(entry.getUrl().toString(), 
                                      entry.getTitle(), 
                                      entry.getNote(),
                                      StringUtil.join(entry.getTags(), DELICIOUS_TAG_SEP), 
                                      entry.getAdded());
                                      
            if ( !added )
                LOG.warn(entry+" NOT posted to del.icious");
        }
        LOG.info("Updated/Added "+addToDelicious.size()+" entries to del.icio.us");

        // see if allDelMap contains any posts, if this is the
        // case, delete them from del.icio.us if respective option is set
        if ( deleteFromSlave ) {
            Iterator deleteEntriesIter = allPostsMap.values().iterator();
            while ( deleteEntriesIter.hasNext() ) {
                Post post = (Post) deleteEntriesIter.next();
                
                LOG.debug("Deleting <"+post.getHref()+"> from del.icio.us! Not in roosster!");
             
                Thread.sleep(1000);
                if ( !delicious.deletePost(post.getHref()) )
                    LOG.warn("Post "+post.getHref()+" NOT deleted from del.icious");
            }
            
            LOG.info("Deleted "+allPostsMap.size()+" entries from del.icio.us");
        }
 */  
