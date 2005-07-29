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
import del.icio.us.DeliciousUtils;
import org.apache.log4j.Logger;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryHitList;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
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
    
    // default for the pub: field for newly added entries in roosster
    public static final boolean DEF_PUBLIC_STATUS        = true;
    
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


        // store current time in properties
        conf.setProperty(PROP_DELICIOUS_LASTSYNC, System.currentTimeMillis() +"");
        conf.persist(new String[] {PROP_DELICIOUS_LASTSYNC});
                                            
                                            
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
        
        String deletePropStr = conf.getProperty(PROP_DELICIOUS_DELETE, "true");
        boolean deleteProp = "true".equalsIgnoreCase(deletePropStr) ? true : false;
        
        LOG.debug("Getting all Posts from del.icio.us now");
        List allDelPosts = delicious.getAllPosts(); // remote call
        LOG.info("Fetched "+allDelPosts.size()+" from del.icio.us");

        EntryStore store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
        EntryHitList allRsstEntries = store.getAllEntries(true);
          
        // parse del.icio.us posts into map, to allow easy access by URL
        LOG.debug("Building lookup Map for "+allDelPosts.size()+" del.icio.us posts");
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
                        LOG.info(post.getHref()+" CHANGED and is public! Will be added/updated in del.icio.us!");
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
        
        // free index searcher
        allRsstEntries.close();
        
        volatileDelicious.addAll(allPostsMap.values());
        
        // for entries/posts in volatile*, determine if they are deletes or 
        // adds and act accordingly
        
        for(int i = 0; i < volatileRoosster.size(); i++) {
            Entry entry = (Entry) volatileRoosster.get(i);
            if ( lastSync.after(entry.getAdded()) && entry.getPublic() ) {
                LOG.info(entry+" was deleted from del.icio.us! Will be deleted from roosster!\n");
                deleteRoosster.add(entry);
            } else if ( entry.getPublic() ) {
                LOG.info(entry+" was added to roosster and is public! Will be added to del.icio.us\n");
                updateDelicious.add( entry2Post(entry) );
            }
        }
        
        
        for(int i = 0; i < volatileDelicious.size(); i++) {
            Post post = (Post) volatileDelicious.get(i);
            if ( lastSync.after(post.getTimeAsDate()) ) {
                LOG.info(post.getHref()+" -- was deleted from roosster! Will be deleted from del.icio.us! Added: "+post.getTimeAsDate()+"\n");
                deleteDelicious.add(post);
            } else {
                LOG.info(post.getHref()+" -- was added to del.icio.us! Will be added to roosster! Added: "+post.getTimeAsDate()+"\n");
                Entry entry = post2Entry(post); 
                addToRoosster.put(entry.getUrl().toString(), entry);
            }
        }

        // 
        // write now to delicious
        //
        for(int i = 0; i < updateDelicious.size(); i++) {
            Post post = (Post) updateDelicious.get(i);
            
            LOG.info("Updating/Adding <"+post.getHref()+"> to del.icio.us!");
            
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
                
                LOG.info("DELETING "+post.getHref()+" from del.icio.us!");
                            
                // fulfill Joshua's restriction by posting calling API once per second 
                Thread.sleep(1000);
                if ( !delicious.deletePost(post.getHref()) )
                    LOG.warn("Failed to DELETE "+post.getHref()+" from del.icious");
            }
            
            // delete from roosster
            LOG.info("DELETING "+deleteRoosster.size()+" Entries from roosster now\n\n");
            store.deleteEntries((Entry[]) deleteRoosster.toArray(new Entry[0]));
        }
        
    }

    
    /**
     * merges only tag list for now, and reports if title/description or note/extended
     * are different
     */
    private void merge(Post post, Entry entry)
    {
        if ( !entry.getNote().equals(post.getExtended()) ) {
            String note = entry.getNote();
            String ext = post.getExtended();
            
            if ( "".equals(note) && !"".equals(ext) )
                entry.setNote(ext);
            else if ( !"".equals(note) && "".equals(ext) )
                post.setExtended(note);
            
            LOG.debug("Synchronization: Note does not match on Entry/Post with URL "+entry.getUrl());
        }
        
        if ( !entry.getTitle().equals(post.getDescription()) ) {
            String title = entry.getTitle();
            String desc = post.getDescription();
            
            if ( "".equals(title) && !"".equals(desc) )
                entry.setTitle(desc);
            else if ( !"".equals(title) && "".equals(desc) )
                post.setDescription(title);
          
            LOG.debug("Synchronization: Title does not match on Entry/Post with URL "+entry.getUrl());
        }

        Set tagSet = new HashSet( Arrays.asList(entry.getTags()) );
        tagSet.addAll( Arrays.asList( getTags(post) ) );
        
        String[] tags = (String[]) tagSet.toArray(new String[0]);
        entry.setTags(tags);
        post.setTag( StringUtil.join(tags, DELICIOUS_TAG_SEP) );
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
        String[] postTags = getTags(post);
        Arrays.sort(postTags);
        
        return Arrays.equals(entryTags, postTags);
    }
            
    
    /**
     * 
     */
    private String[] getTags(Post post)
    {
        String[] postTags = StringUtil.split(post.getTag(), DELICIOUS_TAG_SEP);
        
        // del.icio.us has no concept of "zero tags", if a post has no tags,
        // it has the tag defined in DELICIOUS_TAG_EMPTY (cool, eeh?)
        if ( postTags.length == 1 && DELICIOUS_TAG_EMPTY.equals(postTags[0]) )
            postTags = new String[0];  
        
        return postTags;
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
     *
     */
    private Post entry2Post(Entry entry) throws Exception
    {
        return new Post(entry.getUrl().toString(), entry.getTitle(), entry.getNote(),
                        "", StringUtil.join(entry.getTags(), DELICIOUS_TAG_SEP), 
                        DeliciousUtils.getUTCDate(entry.getAdded()) );      
    }
    
    
    /**
     *
     */
    private Entry post2Entry(Post post) throws Exception
    {
        Entry entry = new Entry(new URL(post.getHref()));
        entry.setTags( getTags(post) );
        entry.setTitle(post.getDescription());
        entry.setNote(post.getExtended());
        entry.setAdded(post.getTimeAsDate());
        entry.setPublic(true);

        return entry;
    }
    
}

