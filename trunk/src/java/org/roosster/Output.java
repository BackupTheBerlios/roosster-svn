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
package org.roosster;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roosster.output.InvalidOutputModeException;
import org.roosster.output.OutputMode;
import org.roosster.store.Entry;
import org.roosster.store.EntryList;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Output
{
    private static Logger LOG = Logger.getLogger(Dispatcher.class.getName());

    private String       commandName    = null;
    private Registry     registry       = null;
    private OutputMode   mode           = null;
    private EntryList    entries        = new EntryList();
    private List         outputMessages = new ArrayList();
    private Map          properties     = new HashMap();
    private boolean      truncation     = true;

    /**
     *
     */
    public Output(Registry registry, String outputMode, String commandName)  
           throws OperationException
    {
        if ( registry == null )
            throw new IllegalArgumentException("Output-object needs non-null Registry object ");

        this.commandName = commandName == null ? "" : commandName;
        this.registry    = registry;
        loadOutputMode(outputMode, registry);
    }


    /**
     */
    public void setOutputProperty(String name, Object obj)
    {
        if ( name != null && !"".equals(name) )
            properties.put(name, obj);
    }
    

    /**
     * 
     */
    public Object getOutputProperty(String name)
    {
        return properties.get(name);
    }
    

    /**
     * 
     */
    public Collection getOutputPropertyNames()
    {
        return properties.keySet();
    }


    /**
     * 
     */
    public String getCommandName()
    {
        return commandName;
    }
    

    /**
     * absolutely no formatting is applied to this message
     */
    public void setOutputMessages(String msg)
    {
        setOutputMessages(Constants.OUTPUTMSG_LEVEL_INFO, msg);
    }
    
    
    /**
     * absolutely no formatting is applied to this message
     */
    public void setOutputMessages(String level, String msg)
    {
        if ( level == null  || "".equals(level) )
            throw new IllegalArgumentException("'level' is not allowed to be null");
        
        outputMessages.clear();
        outputMessages.add(Arrays.asList(new String[] {level, msg}));
    }
    
    
    /**
     * @return a list of List-objects, may be empty, but never null
     */
    public List getOutputMessages()
    {
        return outputMessages;
    }
    
    
    /**
     * absolutely no formatting is applied to this message
     */
    public void addOutputMessage(String msg)
    {
        addOutputMessage(Constants.OUTPUTMSG_LEVEL_INFO, msg);
    }


    /**
     * absolutely no formatting is applied to this message
     */
    public void addOutputMessage(String level, String msg)
    {
        if ( level == null  || "".equals(level) )
            throw new IllegalArgumentException("'level' is not allowed to be null");
        
        outputMessages.add(Arrays.asList(new String[] {level, msg}));
    }


    /**
     *
     */
    public int entriesSize()
    {
        return entries.size();
    }
    
    
    /**
     *
     */
    public void addEntry(Entry obj)
    {
        if ( obj != null )
            entries.addEntry(obj);
    }


    /**
     *
     */
    public void addEntries(Entry[] entries)
    {
        if ( entries != null && entries.length > 0 ) {
            for(int i = 0; i < entries.length; i++) {
                this.entries.addEntry(entries[i]);
            }
        }
    }


    /**
     *
     */
    public void setEntries(EntryList entries)
    {
        if ( entries != null )
            this.entries = entries;
    }

    
    /**
     *
     */
    public boolean getTruncation()
    {
        return truncation;
    }

    
    /**
     *
     */
    public void setTruncation(boolean truncation)
    {
        this.truncation = truncation;
    }


    /**
     *
     */
    public void output(PrintWriter writer) throws OperationException
    {
        try {
            Map reqArgs = registry.getConfiguration().getRequestArguments();
            
            if ( !truncation && reqArgs != null ) 
                reqArgs.put(Constants.PROP_TRUNCLENGTH, "-1");
            
            LOG.debug("Running OutputMode "+mode.getClass());
            mode.output(this, writer, entries);
        } finally {
            writer.flush();
        }
    }


    /**
     *
     */
    public void output(PrintStream output) throws OperationException
    {
        this.output(new PrintWriter(output));
    }


    /**
     */
    public String getContentType()
    {
        return mode != null ? mode.getContentType(entries) : null;
    }


    /**
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(entries.get(i));
        }
        return sb.toString();
    }


    // ============ private Helper methods ============


    /**
     *
     */
    private void loadOutputMode(String modeName, Registry registry) throws OperationException
    {
        Configuration conf = registry.getConfiguration();

        LOG.debug("Trying to load OutputMode: "+modeName); 
        
        modeName = modeName.trim();

        String propName  = "output."+modeName+".class";
        String typeName  = "output."+modeName+".ctype";
        String modeClass = conf.getProperty(propName);
        if ( modeClass == null || "".equals(modeClass) )
            throw new IllegalArgumentException("No property found for "+propName);

        try {

            mode = (OutputMode) Class.forName(modeClass).newInstance();
            mode.setContentType( conf.getProperty(typeName) );
            
            LOG.debug("Loaded OutputMode "+mode+" with class "+modeClass+
                      " and set Content-Type to "+conf.getProperty(typeName));
            
            mode.setRegistry(registry);
            
        } catch (ClassCastException ex) {
            throw new OperationException("Class "+modeClass+" doesn't implement "+OutputMode.class, ex);
        } catch (Exception ex) {
            throw new OperationException(ex);
        }

        if ( mode == null )
            throw new InvalidOutputModeException(modeName);
    }


}
