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
import java.util.logging.Logger;

import org.roosster.store.Entry;
import org.roosster.store.EntryList;


/**
 * TODO throw out that AVL_MODES stuff 
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Output
{
    private static Logger LOG = Logger.getLogger(Dispatcher.class.getName());

    private static final String[] AVL_MODES = new String[] {"atom", "html", "text"};

    /** if the value of this property is set to a value below zero, no output is truncated
     */
    public static final String PROP_TRUNCLENGTH = "output.truncate.length";

    private Registry    registry       = null;
    private OutputMode  mode           = null;
    private EntryList   entries        = new EntryList();
    private String      templateName   = null;
    private int         truncateLength = -1;

    /**
     *
     */
    public Output(Registry registry)  throws OperationException
    {
        if ( registry == null )
            throw new IllegalArgumentException("Output-object needs non-null Registry object ");

        this.registry = registry;
        String truncLength = registry.getConfiguration().getProperty(PROP_TRUNCLENGTH, "-1");
        truncateLength = Integer.valueOf(truncLength).intValue();
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
    public void output(PrintWriter writer)
                throws OperationException
    {
        if ( mode == null )
            loadOutputMode("", registry);

        try {
            Entry[] outEntries = new Entry[entries.size()];
            for(int i = 0; i < outEntries.length; i++) {
                outEntries[i] = entries.getEntry(i);
            }

            LOG.fine("Running OutputMode "+mode.getClass());
            mode.output(registry, this, writer, outEntries);
        } finally {
            writer.flush();
        }
    }


    /**
     *
     */
    public void output(PrintStream output)
                throws OperationException
    {
        this.output(new PrintWriter(output));
    }


    /**
     *
     */
    public void setOutputMode(String modeName) throws OperationException
    {
        loadOutputMode(modeName, registry);
    }


    /**
     */
    public void setTruncateLength(int truncateLength)
    {
        this.truncateLength= truncateLength;
    }


    /**
     * OutputMode-classes may opt to ignore the truncation setting
     * completely
     */
    public int getTruncateLength()
    {
        return truncateLength;
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


    /**
     */
    public String getContentType()
    {
        return mode != null ? mode.getContentType() : null;
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
        boolean correctMode = false;
        for(int i = 0; i < AVL_MODES.length; i++) {
            if ( AVL_MODES[i].equals(modeName) )
                correctMode = true;
        }

        if ( !correctMode ) {
            modeName = OutputMode.DEF_OUTPUT_MODE;
            LOG.warning("wrong or No output mode specified. Default: "+OutputMode.DEF_OUTPUT_MODE);
        }

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

    }


}
