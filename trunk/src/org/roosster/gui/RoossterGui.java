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
package org.roosster.gui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.net.URL;
import java.net.MalformedURLException;

import thinlet.Thinlet;

import org.apache.log4j.Logger;
import org.roosster.Output;
import org.roosster.Constants;
import org.roosster.Dispatcher;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.store.DuplicateEntryException;
import org.roosster.logging.LogUtil;
import org.roosster.util.MapperUtil;
import org.roosster.util.StringUtil;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class RoossterGui extends Thinlet implements GuiConstants
{
    private static Logger LOG = Logger.getLogger(RoossterGui.class);
    
    private static final String GUI_DEFINITON   = "/thinlet.xml";
    
    private static final String OUTPUT_MODE     = "text";

    private ResourceBundle  resourceBundle = null; 
    private Configuration   configuration  = null;
    private Registry        registry       = null;
    private EntryStore      store          = null;
    private EntryList       currentEntries = null;
    private Entry           entry          = null;
    private Set             allTags        = null; 

    /**
     * 
     */
    public RoossterGui(Registry registry, ResourceBundle bundle) throws Exception 
    {
        this.registry = registry;
        resourceBundle = bundle;
        
        store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
        configuration = registry.getConfiguration();
        
        allTags = new TreeSet( store.getAllTags() );
        
        add( parse( getClass().getResourceAsStream(GUI_DEFINITON) ) );
    }    


    // ============ GUI event methods ============
    
        
    /**
     * 
     */
    public void eventTabChanged(Object tabbedPane) throws Exception 
    {
        Object selected = getSelectedItem(tabbedPane);
        int selectedIndex = getSelectedIndex(tabbedPane);
        
        LOG.debug("Tab changed to "+ getString(selected, "name"));
        
        resetMessages();
                
        switch (selectedIndex) {
            case EDIT_TAB_INDEX:
                break;
            case SEARCH_TAB_INDEX: 
                // TODO find a way to set the focus on query field
                break;
            default:
        }
    
    }

    
    /**
     * 
     */
    public void fillEditForm(Object selectedRow) throws Exception
    {
        switchToTab(EDIT_TAB_INDEX);
        
        Object[] cells = getItems(selectedRow);
        
        for (int i = 0; i < cells.length; i++) {
            Integer id = (Integer) getProperty(cells[i], "id");
            if ( id != null ) {
                entry = currentEntries.getEntry( id.intValue() );
                break;
            }
        }
        
        LOG.debug("Select Entry "+entry);

        fillForm();
    }

    
    /**
     * 
     */
    public void doSave(String title, String tagStr, String note, String type,
                       String author, String authorEmail, Object tagsList) 
                throws Exception
    {
        if ( entry == null )
            return;
          
        LOG.debug("Saving Entry "+entry);
        
        Object[] selectedTags = getSelectedItems(tagsList);
        LOG.debug("SELECTED TAGS "+Arrays.asList(selectedTags));
        
        String[] tags = StringUtil.split(tagStr, Entry.TAG_SEPARATOR);
        updateTagsSet(tags);
        
        entry.setTitle(title);
        entry.setTags(tags);
        entry.setNote(note);
        entry.setFileType(type);
        entry.setAuthor(author);
        entry.setAuthorEmail(authorEmail);
        
        List list = new EntryList();
        list.add(entry);
        
        Map args = new HashMap();
        args.put(Constants.PARAM_ENTRIES, list);
        
        // TODO exception handling
        new Dispatcher(registry).run("putentries", OUTPUT_MODE, args);
        
        showInfo(BundleKeys.SAVE_SUCCESS);
    }
    
    
    /**
     * 
     */
    public void doAdd(String urlString, boolean fetchContent, boolean pub) throws Exception 
    {
        LOG.debug("Trying to add Entry with URL "+urlString+", public: "+pub+", fetchContent: "+fetchContent);
        
        if ( StringUtil.isNullOrBlank(urlString) ) {
            showError(BundleKeys.URL_EMPTY);
            return;
        }
        
        try {
            configuration.setProperty(Constants.ARG_PUBLIC, String.valueOf(pub));
            configuration.setProperty(Constants.ARG_FORCE, String.valueOf(false));
            configuration.setProperty(Constants.PROP_FETCH_CONTENT, String.valueOf(fetchContent));
            
            List list = new EntryList();
            list.add(new Entry(new URL(urlString)));
            
            Map args = new HashMap();
            args.put(Constants.PARAM_ENTRIES, list);
            
            Output output = new Dispatcher(registry).run("addurls", OUTPUT_MODE, args);
            
            if ( output.entriesSize() > 1 ) {
                showInfo(BundleKeys.MULTIPLE_ADDED);
            } else {
                entry = output.getEntries().getEntry(0);
                fillForm();
                switchToTab(EDIT_TAB_INDEX);
                showInfo(BundleKeys.ADD_SUCCESS);
            }
             
          
        } catch(MalformedURLException ex) {
          
            LOG.debug("URL has wrong format", ex);
            showError(BundleKeys.URL_INVALID);
          
        } catch(OperationException ex) {
          
            if ( ex.getCause() instanceof DuplicateEntryException ) {
                LOG.debug("URL already stored in index!");
                
                DuplicateEntryException e = (DuplicateEntryException) ex.getCause();
                entry = store.getEntry(e.getUrl());
                fillForm();
                
                switchToTab(EDIT_TAB_INDEX);
                showInfo(BundleKeys.DUPLICATE_URL);
            } else {
                throw ex;
            }
            
        }
    }
    
    
    /**
     * 
     */
    public void doSearch(String query, Object resultTable) throws Exception 
    {
        removeAll(resultTable);
      
        Map args = new HashMap();
        args.put("query", query);
        
        Output output = new Dispatcher(registry).run("search", OUTPUT_MODE, args);
        
        currentEntries = output.getEntries();
        
        for (int i = 0; i < currentEntries.size(); i++) {
            Entry entry = currentEntries.getEntry(i);
          
            Object row = create("row");
            add(resultTable, row);
            
            Object emptyRow = create("row");
            add(resultTable, emptyRow);
            
            Object cell = create("cell");
            putProperty(cell, "id", new Integer(i));
            setString(cell, "text", entry.getTitle());
            setString(cell, "tooltip", entry.getUrl().toString());
            add(row, cell);
        
            cell = create("cell");
            setString(cell, "text", StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR) );
            add(row, cell);
            
            cell = create("cell");
            setString(cell, "text", entry.getNote());
            add(row, cell);
        }
        
    }
    
   
    /**
     * 
     */
    public void toggleVisible(Object component)
    {
        boolean currentValue = getBoolean(component, "visible");
        setBoolean(component, "visible", !currentValue); 
    }
    
    
    /**
     * 
     */
    protected void handleException(Throwable throwable) 
    {
        // TODO show dialog etc.
        LOG.warn("Exception occurred during event handling", throwable);
    }
    

    // ============ protected Helper methods ============

    
    /**
     * 
     */
    protected void fillForm()
    {
        if ( entry != null ) {
            text(URL_LABEL, entry.getUrl().toString());
            text(TITLE_FIELD, entry.getTitle());
            //text(TAGS_FIELD, StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR));
            text(NOTE_FIELD, entry.getNote());
            text(TYPE_FIELD, entry.getFileType());
            text(AUTHOR_FIELD, entry.getAuthor());
            text(AUTHOREMAIL_FIELD, entry.getAuthorEmail());
            
            Object tagsList = find(TAGS_LIST);
            Object[] tagItems = getItems(tagsList);
            
            // rebuild tagsList if size doesn't match number of current allTags
            if ( tagItems.length != allTags.size() ) {
                removeAll(tagsList);
                
                Iterator iter = allTags.iterator();
                while ( iter.hasNext() ) {
                    Object item = create("item");
                    setString(item, "text", (String) iter.next());
                    add(tagsList, item);
                }
                
                tagItems = getItems(tagsList);
            }
            
            // now mark the selected tags
            if ( tagItems.length > 0 ) {
                Set entryTags = new HashSet( Arrays.asList(entry.getTags()) );
              
                for (int i = 0; i < tagItems.length; i++) {
                    setBoolean(tagItems[i], "selected", entryTags.contains(getString(tagItems[i], "text")) );
                }
            }
        }
    }
    
    /**
     * 
     */
    protected void switchToTab(int tabIndex)
    {
        setInteger(find(TABBED_PANE), "selected", tabIndex);
    }
    
    
    /**
     * 
     */
    protected void resetMessages()
    {
        text(ERR_MSG_LABEL, "");
        text(INFO_MSG_LABEL, "");
        
    }
    
    
    /**
     * 
     */
    protected void showInfo(String bundleKey)
    {
        text(ERR_MSG_LABEL, "");
        text(INFO_MSG_LABEL, resourceBundle.getString(bundleKey));
    }
    
    
    /**
     * 
     */
    protected void showError(String bundleKey)
    {
        text(INFO_MSG_LABEL, "");
        text(ERR_MSG_LABEL, resourceBundle.getString(bundleKey));
    }
    
    
    /**
     * 
     */
    protected Object text(String objName, String value)
    {
        Object obj = find(objName);
        setString(obj, "text", value);
        return obj;
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private void updateTagsSet(String[] tags)
    {
        for (int i = 0; i < tags.length; i++) {
            if ( !StringUtil.isNullOrBlank(tags[i]) && !allTags.contains(tags[i]) )
                allTags.add(tags[i]);
        }
    }
    
}
