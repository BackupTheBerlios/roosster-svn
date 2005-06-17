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
package org.roosster.main;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import thinlet.Thinlet;
import thinlet.FrameLauncher;

import org.apache.log4j.Logger;
import org.roosster.Output;
import org.roosster.Constants;
import org.roosster.Dispatcher;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.logging.LogUtil;
import org.roosster.util.MapperUtil;
import org.roosster.util.StringUtil;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class RoossterGui extends Thinlet implements GuiConstants
{
    private static Logger LOG;
    
    private static final String PROP_FILE       = "/roosster.properties";
    private static final String GUI_DEFINITON   = "/thinlet.xml";
    
    private static final String OUTPUT_MODE     = "text";

    private Registry  registry        = null;
    
    private EntryList currentEntries = null;
    private Entry     entry          = null;

    /**
     * 
     */
    public Roosster(Registry registry) throws java.io.IOException 
    {
        this.registry = registry;
        add( parse( getClass().getResourceAsStream(GUI_DEFINITON) ) );
    }    
    
    
    /**
     *
     */
    public static void main(String[] arguments)
    {
        try {
            Map cmdLine = MapperUtil.parseCommandLineArguments(arguments);

            LogUtil.configureLogging(cmdLine);
            LOG = Logger.getLogger(Roosster.class);
        
            new FrameLauncher("Thinlet", 
                              new Roosster(
                                  new Registry(Roosster.class.getResourceAsStream(PROP_FILE), cmdLine)
                              ), 
                              640, 480);            
          
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }

    // ============ GUI event methods ============
    
        
    /**
     * 
     */
    public void eventTabChanged(Object tabbedPane) throws Exception 
    {
        Object selected = getSelectedItem(tabbedPane);
        LOG.debug("Tab changed to "+ getString(selected, "name"));
    }
    
    
    /**
     * 
     */
    public void save() throws Exception
    {
        LOG.debug("Saving Entry");
    }

    
    /**
     * 
     */
    public void edit(Object selectedRow) throws Exception
    {
        // switch to "Edit" Tab
        Object tabPane = find(TABBED_PANE);
        setInteger(tabPane, "selected", EDIT_TAB_INDEX);
        
        Object[] cells = getItems(selectedRow);
        
        for (int i = 0; i < cells.length; i++) {
            Integer id = (Integer) getProperty(cells[i], "id");
            if ( id != null ) {
                entry = currentEntries.getEntry( id.intValue() );
                break;
            }
        }
        
        LOG.debug("Select Entry "+entry);

        setString(find(URL_FIELD), "text", entry.getUrl().toString());
        setString(find(TITLE_FIELD), "text", entry.getTitle());
        setString(find(TAGS_FIELD), "text", StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR));
        setString(find(NOTE_FIELD), "text", entry.getNote());
        setString(find(TYPE_FIELD), "text", entry.getFileType());
        setString(find(AUTHOR_FIELD), "text", entry.getAuthor());
        setString(find(AUTHOREMAIL_FIELD), "text", entry.getAuthorEmail());
    }
    
    
    /**
     * 
     */
    public void search(String query, Object resultTable) throws Exception 
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
            
            Object cell1 = create("cell");
            putProperty(cell1, "id", new Integer(i));
            setString(cell1, "text", entry.getTitle());
            setString(cell1, "tooltip", entry.getUrl().toString());
            add(row, cell1);
        
            Object cell2 = create("cell");
            setString(cell2, "text", StringUtil.join(entry.getTags(), Entry.TAG_SEPARATOR) );
            add(row, cell2);
            
            Object cell3 = create("cell");
            setString(cell3, "text", entry.getNote());
            add(row, cell3);
        }
        
    }
    
    protected void handleException(Throwable throwable) 
    {
        // TODO show dialog etc.
        LOG.warn("Exception occurred during event handling", throwable);
    }
    

}
