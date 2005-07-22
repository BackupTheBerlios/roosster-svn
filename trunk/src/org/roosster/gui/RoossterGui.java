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

import java.awt.Frame;
import java.awt.FileDialog;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
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

import org.htmlparser.util.Translate;

import org.apache.log4j.Logger;
import org.roosster.Output;
import org.roosster.Constants;
import org.roosster.Dispatcher;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.main.Roosster;
import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.store.DuplicateEntriesException;
import org.roosster.logging.LogUtil;
import org.roosster.util.StringUtil;
import org.roosster.util.DateUtil;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class RoossterGui extends Thinlet implements GuiConstants, BundleKeys
{
    private static Logger LOG = Logger.getLogger(RoossterGui.class);
    
    private static final String GUI_DEFINITON   = "/thinlet.xml";
    
    private static final String OUTPUT_MODE     = "text";

    private Roosster        roosster       = null;
    
    private ResourceBundle  resourceBundle = null; 
    private Configuration   configuration  = null;
    private Registry        registry       = null;
    private EntryStore      store          = null;
    private EntryList       currentEntries = null;
    private Entry           entry          = null;
    private TreeSet         allTags        = null; 
    private int             currentOffset  = 0; 

    /**
     * 
     */
    public RoossterGui(Roosster roosster, Registry registry, ResourceBundle bundle) throws Exception 
    {
        this.roosster = roosster;
        this.registry = registry;
        resourceBundle = bundle;
        
        setResourceBundle(bundle);
        
        store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
        configuration = registry.getConfiguration();
        
        allTags = new TreeSet( store.getAllTags() );
        
        currentEntries = new EntryList();
        
        add( parse( getClass().getResourceAsStream(GUI_DEFINITON) ) );

        requestFocus(find(QUERY_FIELD));
    }    


    // ============ GUI event methods ============


    
    /**
     * 
     */
    public void openIndex() throws Exception
    {
        /*
        Container frame = this;
        while ( !(frame instanceof Frame) ) { 
            frame = frame.getParent(); 
        }
               
        FileDialog fileDialog = new FileDialog((Frame)frame, "Open ...", FileDialog.LOAD);
        fileDialog.show();
        
        File fileSelected = new File(fileDialog.getDirectory());
        
        LOG.debug("Selected "+fileDialog.getDirectory()+" for opening the index");
        */
    }
    
    
    // ============ "Business" methods ============
    
    
    /**
     * 
     */
    public void saveEntry() throws Exception
    {
        doSaveEntry(getText(TITLE_FIELD),
                    getText(NOTE_FIELD),
                    getText(TYPE_FIELD),
                    getText(AUTHOR_FIELD),
                    getText(AUTHOREMAIL_FIELD),
                    getBool(PUBLICPRIVATE_FIELD, "selected"),
                    find(TAGS_LIST));
    }
    
    
    /**
     * 
     */
    public void deleteEntry() throws Exception
    {
        doDeleteEntry();
    }
    
    
    /**
     * 
     */
    public void addEntry()throws Exception
    {
        doAddEntry(getText(ADDURL_FIELD),
                   getBool(ADDFETCH_FIELD, "selected"),
                   getBool(ADDPUBLIC_FIELD, "selected"),
                   getBool(ADDFORCE_FIELD, "selected") );
    }   
    
    
    /**
     * 
     */
    public void searchEntries(String direction) throws Exception 
    {
        doSearchEntries(getText(QUERY_FIELD),
                        find(SEARCH_RESULT),
                        getText(PAGERSIZE_BOX),
                        direction,
                        find(SORT_BOX));
    }
    
    
    // ============ GUI control methods ============
    
    
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
                requestFocus(find(QUERY_FIELD));
                break;
            default:
        }
    
    }

    
    /**
     * 
     */
    public void fillEditForm(Object selectedRow) throws Exception
    {
        setEnabled(EDIT_TAB, true);
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
    public void determinePagerEnabled() 
    {
        Object selectedItem = getSelectedItem(find(PAGERSIZE_BOX));
        int selected = Integer.valueOf(getString(selectedItem, "text")).intValue();
        
        if ( (currentOffset + selected) < currentEntries.getTotalSize() )
            setEnabled(PAGERFORWARD_BUTTON, true);
        else 
            setEnabled(PAGERFORWARD_BUTTON, false);
        
        if ( (currentOffset - selected) < 0 )
            setEnabled(PAGERBACK_BUTTON, false);
        else 
            setEnabled(PAGERBACK_BUTTON, true);
    }
    
    
    /**
     * 
     */
    public void suggestTag(Object textField) 
    {
        String text = getString(textField, "text");

        if ( allTags.isEmpty() || StringUtil.isNullOrBlank(text) ) 
            return;
          
        String lastTag = (String) allTags.last();
        
        // only search if text is lexicographically smaller or equal than last tag
        if ( text.compareTo(lastTag) <= 0 ) {
        
            Iterator iter = allTags.iterator();
            while ( iter.hasNext() ) {
                String tag = (String) iter.next();
                
                if ( tag.startsWith(text) ) {
                    LOG.debug("Tag '"+tag+"' starts with '"+text+"'");
                    setString(textField, "text", tag);
                    setInteger(textField, "start", text.length());
                    setInteger(textField, "end", tag.length());                    
                } 
            }
        }
            
    }    
    
    
    /**
     * 
     */
    public void markTag(Object textField) 
    {
        String text = getString(textField, "text");
        setString(textField, "text", "");
      
        if ( StringUtil.isNullOrBlank(text) ) 
            return;
        
      
        Object tagsList = find(TAGS_LIST);
        Object[] tagItems = getItems(tagsList);
        
        String[] texts = new String[] {text};
        
        // if user submitted a text, that contains the TAG_SEPARATOR than
        // consider text as mutliple tags by splitting the text 
        if ( text.indexOf(Entry.TAG_SEPARATOR) != -1 ) 
            texts = StringUtil.split(text, Entry.TAG_SEPARATOR);
        
        boolean[] found = new boolean[texts.length];
        boolean[] allTrue = new boolean[texts.length];
        Arrays.fill(found, false);
        Arrays.fill(allTrue, true);
        
        // mark tags in tags list as selected if they were entered in textfield
        for (int i = 0; i < tagItems.length; i++) {
          
            for (int k = 0; k < texts.length; k++) {
                if ( texts[k].equals(getString(tagItems[i], "text")) ) {
                    found[k] = true;
                    setBoolean(tagItems[i], "selected", true);
                }
            }
            
            if ( Arrays.equals(allTrue, found) ) break;
        }
        
        // add the entered text as new tag to tags list, if it wasn't found in tags list
        for (int i = 0; i < texts.length; i++) {
            if ( !found[i] ) { 
                Object item = newtext("item", texts[i]);
                setBoolean(item, "selected", true);
                add(tagsList, item);
            }
        }
    }
    
   
    /**
     * 
     */
    public void runDeliciousSync(String username, String password, Object dialog, Object button) 
                          throws Exception
    {
        configuration.setProperty(Constants.PROP_DELICIOUS_PASS, password);
        configuration.setProperty(Constants.PROP_DELICIOUS_USER, username);
        
        setString(button, "text", string(DELICIOUS_RUNNING));
        
        Output output = new Dispatcher(registry).run("syncdelicious", OUTPUT_MODE, new HashMap());
        
        allTags = new TreeSet( store.getAllTags() );
                
        showInfo(BundleKeys.DELICIOUS_SUCCESS);
        closeDialog(dialog);
    }
    
    
    /**
     * 
     */
    public void openDeliciousSync() throws Exception
    {
        openDialog("delicious_sync_dialog");
        text(DELICIOUSPASS_FIELD, configuration.getProperty(Constants.PROP_DELICIOUS_PASS));
        text(DELICIOUSUSER_FIELD, configuration.getProperty(Constants.PROP_DELICIOUS_USER));
    }
    
        
    /**
     * 
     */
    public void closeDialog(Object dialog) 
    {
        remove(dialog);
    }
     
     
    /**
     * 
     */
    public void openDialog(String name) throws Exception
    {
        String resourceName = "/"+name+".xml";
        LOG.debug("Opening dialog defined in '"+resourceName+"'");
        add( parse( getClass().getResourceAsStream(resourceName) ) );
    }
    
    
    /**
     * 
     */
    public void goUrl(Object component) throws IOException
    {
        String url = (String) getProperty(component, "url");
        
        if ( url == null ) {
            LOG.info("goUrl() called on component that has no 'url' property set!");
        } else {
            LOG.debug("Launching URL "+url);
            BrowserLauncher.openURL(url);
        }
    }
    
    /**
     * 
     */
    public void setVisible(String componentName, boolean visible)
    {
        setBoolean(find(componentName), "visible", visible); 
    }
    
    
    /**
     * 
     */
    public void setEnabled(String componentName, boolean enabled)
    {
        setBoolean(find(componentName), "enabled", enabled); 
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
    public void exitRoosster()
    {
        System.exit(0);
    }
      
    
    // ============ overriden methods ============
    
    
    /**
     * 
     */
    protected void handleException(Throwable throwable) 
    {
        // TODO show dialog etc.
        LOG.warn("Exception occurred during event handling", throwable);
        
        try {
        
            if ( throwable instanceof MalformedURLException ) {
              
                showError(BundleKeys.URL_INVALID);
              
            } else if ( throwable instanceof OperationException ) {
              
                if ( throwable.getCause() instanceof DuplicateEntriesException ) {
                    DuplicateEntriesException e = (DuplicateEntriesException) throwable.getCause();
                    
                    URL[] urls = e.getUrls();
                    if ( urls != null && urls.length == 1 ) {
                        entry = store.getEntry(urls[0]);
                        fillForm();
                        
                        setEnabled(EDIT_TAB, true);
                        switchToTab(EDIT_TAB_INDEX);
                        showInfo(BundleKeys.DUPLICATE_URL);
                    } else {
                        showError(BundleKeys.MULTIPLE_DUPLICATE_URL);
                    }
                } 
                
            }
            
        } catch(Exception ex) {
            LOG.warn("Exception occurred while handling exception! Giving up!", ex);
        }
    }
    

    /**
     * 
     */
    public void setString(Object component, String key, String value) 
    {
        super.setString(component, key, Translate.decode(value));
    }
    
    
    // ============ protected Helper methods ============

    
    /**
     * 
     */
    protected void doAddEntry(String urlString, boolean fetchContent, boolean pub, boolean force) 
                       throws Exception 
    {
        LOG.debug("Trying to add Entry with URL "+urlString+", public: "+pub+", fetchContent: "+fetchContent);
        
        if ( StringUtil.isNullOrBlank(urlString) ) {
            showError(BundleKeys.URL_EMPTY);
            return;
        }
        
        configuration.setProperty(Constants.PROP_FETCH_CONTENT, String.valueOf(fetchContent));
        configuration.setProperty(Constants.ARG_PUBLIC, String.valueOf(pub));
        configuration.setProperty(Constants.ARG_FORCE, String.valueOf(force));
        
        List list = new EntryList();
        list.add(new Entry(new URL(urlString)));
        
        Map args = new HashMap();
        args.put(Constants.PARAM_ENTRIES, list);
        
        Output output = new Dispatcher(registry).run("addurls", OUTPUT_MODE, args);
        
        // if the entries were force-added then it's possible that some old 
        // entries were overwritten in the process, so we have to refresh the tags list 
        if ( force ) {
            allTags.clear();
            allTags.addAll( store.getAllTags() );
        }
        
        if ( output.entriesSize() > 1 ) {
            showInfo(BundleKeys.MULTIPLE_ADDED);
            
            // if we force-added the entries, and there was already an entry 
            // selected we have to make sure 
            if ( force && entry != null ) {
              
                // only if the current selected entry was overwritten by the newly
                // added entries, we have to refresh the display
                if ( output.getEntries().getEntry(entry.getUrl()) != null ) {
                    entry = output.getEntries().getEntry(entry.getUrl());
                    fillForm();
                }
            }
            
        } else if ( output.entriesSize() == 1 ) {
          
            entry = output.getEntries().getEntry(0);
            fillForm();
            setEnabled(EDIT_TAB, true);
            switchToTab(EDIT_TAB_INDEX);
            showInfo(BundleKeys.ADD_SUCCESS);
            
        } else {
            showError(BundleKeys.ADD_NOENTRIES);
        }
         
    }
    
    
    /**
     * 
     */
    protected void doSearchEntries(String query, Object resultTable, String limitStr, 
                                   String direction, Object sortBox) 
                            throws Exception 
    {
        removeAll(resultTable);
      
        LOG.debug("Executing search query: '"+query+"' direction "+direction+" limitstr "+limitStr);

        int[] limitOffset = handlePaging(limitStr, direction);        
        
        String sortStr = (String) getProperty(getSelectedItem(sortBox), "sortfield");
        
        LOG.debug(sortStr);
        
        // put together arguments
        Map args = new HashMap();
        args.put("query", query);
        
        configuration.setProperty(Constants.PROP_OFFSET, String.valueOf(limitOffset[1]));
        configuration.setProperty(Constants.PROP_LIMIT, String.valueOf(limitOffset[0]));
        configuration.setProperty(Constants.PROP_SORTFIELD, sortStr);
        
        // run search
        Output output = new Dispatcher(registry).run("search", OUTPUT_MODE, args);
        
        // extract result ... 
        currentEntries = output.getEntries();
        currentOffset = currentEntries.getOffset();

        // ... and display it in table 
        if ( currentEntries.size() > 0 ) {
            for (int i = 0; i < currentEntries.size(); i++) {
                Entry entry = currentEntries.getEntry(i);
              
                Object row = create("row");
                add(resultTable, row);
                
                Object emptyRow = create("row");
                add(resultTable, emptyRow);
                
                Object access = create("cell");
                setChoice(access, "alignment", "center");
                setIcon(access, "icon", getIcon( entry.getPublic() ? "/img/public.png" : "/img/private.png"));
                add(row, access);
                
                Object cell = create("cell");
                putProperty(cell, "id", new Integer(i));
                setString(cell, "text", entry.getTitle());
                setString(cell, "tooltip", entry.getUrl().toString());
                add(row, cell);
            
                add(row, newtext("cell", StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR)));
                add(row, newtext("cell", entry.getNote()));
            }
            setVisible(SEARCH_RESULT, true);
            setVisible(EMPTYRESULT_LABEL, false);
            
            text(PAGER_LABEL, string(ENTRIES) +" "+ currentOffset +" "+ string(TO) 
                            +" "+ (currentOffset+currentEntries.size()) +" "+ string(OF) 
                            +" "+ currentEntries.getTotalSize());

        } else {
            setVisible(SEARCH_RESULT, false);
            setVisible(EMPTYRESULT_LABEL, true);
            text(PAGER_LABEL, "");
        }
        
        determinePagerEnabled();
    }
    
    
    /**
     * 
     */
    public void doDeleteEntry() throws Exception
    {
        if ( entry == null ) {
            LOG.debug("Called doDelete() while no Entry being currently selected"); 
            return;
        }

        Map args = new HashMap();
        args.put(Constants.ARG_URL, entry.getUrl().toString());

        try {
          
            Output output = new Dispatcher(registry).run("delete", OUTPUT_MODE, args);
            showInfo(BundleKeys.DELETE_SUCCESS);
            
        } catch (OperationException ex) {
            LOG.warn("Exception occurred while deleting Entry", ex);
            showError(BundleKeys.DELETE_FAILURE);
        }
            
    }

    
    /**
     * 
     */
    protected void doSaveEntry(String title, String note, String type,
                          String author, String authorEmail, 
                          boolean pub, Object tagsList)
                   throws Exception
    {
        if ( entry == null )
            return;
          
        LOG.debug("Saving Entry "+entry);
        
        Set entryTags = new HashSet();
        Object[] selectedTags = getSelectedItems(tagsList);
        for (int i = 0; i < selectedTags.length; i++) {
           entryTags.add( getString(selectedTags[i], "text") ); 
        }
        
        entry.setTitle(title);
        entry.setTags((String[]) entryTags.toArray(new String[0]));
        entry.setNote(note);
        entry.setFileType(type);
        entry.setAuthor(author);
        entry.setAuthorEmail(authorEmail);
        entry.setPublic(pub);
        
        List list = new EntryList();
        list.add(entry);
        
        Map args = new HashMap();
        args.put(Constants.PARAM_ENTRIES, list);
        
        Output output = new Dispatcher(registry).run("putentries", OUTPUT_MODE, args);

        entry = output.getEntries().getEntry(0);

        // update GUI 
        updateTagsSet(entryTags);
        fillForm();
        setString(find(TAGS_FIELD), "text", "");
        showInfo(BundleKeys.SAVE_SUCCESS);
    }
    
    
    /**
     * 
     */
    protected void fillForm()
    {
        fillForm(true);
    }
    
    
    /**
     * 
     */
    protected void fillForm(boolean buildTags) 
    {
        if ( entry != null ) {  
            Object button = find(URL_BUTTON);
            setString(button, "text", StringUtil.truncate(entry.getUrl().toString(), 120));
            setString(button, "tooltip", entry.getUrl().toString());
            putProperty(button, "url", entry.getUrl().toString());
            
            putProperty(find(CACHEDCOPY_BUTTON), "url", roosster.constructCachedLink(entry.getUrl()));
            
            text(TAGS_LABEL, StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR));
            text(TITLE_FIELD, entry.getTitle());
            text(NOTE_FIELD, entry.getNote());
            text(TYPE_FIELD, entry.getFileType());
            text(AUTHOR_FIELD, entry.getAuthor());
            text(AUTHOREMAIL_FIELD, entry.getAuthorEmail());
            setBoolean(find(PUBLICPRIVATE_FIELD), "selected", entry.getPublic());
            
            text(ADDED_LABEL,    DateUtil.formatDisplayDate(entry.getAdded()));
            text(ISSUED_LABEL,   DateUtil.formatDisplayDate(entry.getIssued()));
            text(MODIFIED_LABEL, DateUtil.formatDisplayDate(entry.getModified()));
            text(EDITED_LABEL,   DateUtil.formatDisplayDate(entry.getEdited()));
            
            
            if ( buildTags ) 
                buildTagsList();
        }
    }
    
    /**
     *
     */
    protected void buildTagsList()
    {
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
                setBoolean(tagItems[i], 
                           "selected", 
                           entryTags.contains(getString(tagItems[i], "text")) 
                          );
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
    protected void showStatus(String bundleKey)
    {
        setVisible(ERR_MSG_LABEL, false);
        setVisible(INFO_MSG_LABEL, false);
        
        setVisible(STATUS_MSG_LABEL, true);
        text(STATUS_MSG_LABEL, resourceBundle.getString(bundleKey));
    }
    
    
    /**
     * 
     */
    protected void showInfo(String bundleKey)
    {
        setVisible(STATUS_MSG_LABEL, false);
        setVisible(ERR_MSG_LABEL, false);
        
        setVisible(INFO_MSG_LABEL, true);
        text(INFO_MSG_LABEL, resourceBundle.getString(bundleKey));
    }
    
    
    /**
     * 
     */
    protected void showError(String bundleKey)
    {
        setVisible(STATUS_MSG_LABEL, false);
        setVisible(INFO_MSG_LABEL, false);
        
        setVisible(ERR_MSG_LABEL, true);
        text(ERR_MSG_LABEL, resourceBundle.getString(bundleKey));
    }
    
    
    /**
     * 
     */
    protected String string(String bundleKey)
    {
        return resourceBundle.getString(bundleKey);
    }
    
    
    /**
     * 
     */
    protected int getInt(String objName, String key)
    {
        return getInteger(find(objName), key);
    }    

    
    /**
     * 
     */
    protected boolean getBool(String objName, String key)
    {
        return getBoolean(find(objName), key);
    }    

    
    /**
     * 
     */
    protected String getText(String objName)
    {
        return getString(find(objName), "text");
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
    
    
    /**
     * 
     */
    protected Object newtext(String widgetName, String textValue)
    {
        Object obj = create(widgetName);
        setString(obj, "text", textValue);
        return obj;
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * @return an array of int, that's never null, where the first element
     * is the limit, and the second the offset
     */
    private int[] handlePaging(String limitStr, String direction)
    {
        int limit = !StringUtil.isNullOrBlank(limitStr) ?  Integer.valueOf(limitStr).intValue() : 10;
        if ( limit <= 0 )
            limit = 10;        
        
        if ( currentEntries != null ) {
        
            if ( PAGERFORWARD_BUTTON.equals(direction) ) 
                currentOffset += limit;
            else if ( PAGERBACK_BUTTON.equals(direction) )
                currentOffset -= limit;
            else 
                currentOffset = 0;
            
        } else {
            currentOffset = 0;
        }
        
        LOG.debug("currentOffset "+currentOffset+" limit "+limit);
        
        return new int[] {limit, currentOffset};
    }
    
    
    /**
     * 
     */
    private void updateTagsSet(String[] tags)
    {
        updateTagsSet( new HashSet(Arrays.asList(tags)) );
    }

    
    /**
     * 
     */
    private void updateTagsSet(Set tags)
    {
        Iterator iter = tags.iterator();
        while ( iter.hasNext() ) {
            String tag = (String) iter.next();
            if ( !StringUtil.isNullOrBlank(tag) && !allTags.contains(tag) )
                allTags.add(tag);
        }
    }
    
}
