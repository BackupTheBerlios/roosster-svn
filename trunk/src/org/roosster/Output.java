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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.output.OutputMode;
import org.roosster.output.InvalidOutputModeException;
import org.roosster.util.StringUtil;


/**
 * TODO throw out that AVL_MODES stuff 
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Output
{
    private static Logger LOG = Logger.getLogger(Dispatcher.class.getName());

    private Registry     registry       = null;
    private OutputMode   mode           = null;
    private EntryList    entries        = new EntryList();
    private String       templateName   = null;
    private List         outputMessages = new ArrayList();
    private boolean      truncation     = true;

    /**
     *
     */
    public Output(Registry registry)  throws OperationException
    {
        if ( registry == null )
            throw new IllegalArgumentException("Output-object needs non-null Registry object ");

        this.registry = registry;
    }


    /**
     * absolutely no formatting is applied to this message
     */
    public void setOutputMessages(String msg)
    {
        outputMessages.clear();
        outputMessages.add(msg);
    }
    
    
    /**
     * @return a list of String-object, may be empty, but never null
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
        outputMessages.add(msg);
    }
    
    
    /**
     *
     */
    public void setTemplateName(String name)
    {
        templateName = name;
    }


    /**
     *
     */
    public String getTemplateName()
    {
        return templateName;
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
    public void output(String outputMode, PrintWriter writer) throws OperationException
    {
        if ( outputMode == null || "".equals(outputMode) )
            throw new IllegalArgumentException("No outputMode specified or set to null");

        try {
            loadOutputMode(outputMode, registry);
          
            Map reqArgs = registry.getConfiguration().getRequestArguments();
            
            if ( !truncation && reqArgs != null ) 
                reqArgs.put(StringUtil.PROP_TRUNCLENGTH, "-1");
            
            LOG.fine("Running OutputMode "+mode.getClass());
            mode.output(registry, this, writer, entries);
        } finally {
            writer.flush();
        }
    }


    /**
     *
     */
    public void output(String outputMode, PrintStream output) throws OperationException
    {
        this.output(outputMode, new PrintWriter(output));
    }


    /**
     */
    public String getContentType()
    {
        return mode != null ? mode.getContentType() : null;
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

        LOG.fine("Trying to load OutputMode: "+modeName); 
        
        modeName = modeName.trim();

        String propName  = "output."+modeName+".class";
        String typeName  = "output."+modeName+".ctype";
        String modeClass = conf.getProperty(propName);
        if ( modeClass == null || "".equals(modeClass) )
            throw new IllegalArgumentException("No property found for "+propName);

        try {

            mode = (OutputMode) Class.forName(modeClass).newInstance();
            mode.setContentType( conf.getProperty(typeName) );

        } catch (ClassCastException ex) {
            throw new OperationException("Class "+modeClass+" doesn't implement "+OutputMode.class, ex);
        } catch (Exception ex) {
            throw new OperationException(ex);
        }

        if ( mode == null )
            throw new InvalidOutputModeException(modeName);
    }


}
