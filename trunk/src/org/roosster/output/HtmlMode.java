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
package org.roosster.output;

import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.Arrays;

import org.roosster.Output;
import org.roosster.OutputMode;
import org.roosster.OperationException;
import org.roosster.Registry;      
import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.store.EntryStore;      
import org.roosster.util.StringUtil;      

/**
 * TODO move contenttype stuff into abstract class
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class HtmlMode implements OutputMode
{
    private static Logger LOG = Logger.getLogger(HtmlMode.class.getName());
    
    private static final String PROP_SCREEN_TMPL = "output.html.screen_tmpl";

    private static final String SCREEN_TMPL = "main_screen.html";
    private static final String ENTRY_TMPL  = "entries.html";
    
    private String contentType = DEF_CONTENT_TYPE;

    /**
     *
     */
    public void output(Registry registry, Output output, PrintWriter writer, EntryList entries)
                throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        TemplateFactory tmplFactory = (TemplateFactory) registry.getPlugin("templates");
        try {
            String screenTmpl = registry.getConfiguration().getProperty(PROP_SCREEN_TMPL, SCREEN_TMPL);
            
            String tmplName = output.getTemplateName();
            if ( tmplName == null )
                tmplName = ENTRY_TMPL;

            LOG.fine("Using Templates: screen: "+screenTmpl+", tmpl: "+tmplName);
            LOG.info(StringUtil.joinStrings((String[]) output.getOutputMessages().toArray(new String[0]), "\n"));
            
            Template tmpl = tmplFactory.getTemplate(screenTmpl);
            tmpl.set("output_messages",  output.getOutputMessages());
            tmpl.set("content_template", tmplName);
            tmpl.set("entries",          entries);
            
            EntryStore store = (EntryStore) registry.getPlugin("store");
            tmpl.set("offset",           new Integer(store.getOffset()));
            tmpl.set("limit",            new Integer(store.getLimit()));

            tmpl.write(writer);

        } catch(Exception ex) {
            throw new OperationException(ex);
        }
            
    }

    /**
     *
     */
    public String getContentType() 
    {
        return contentType;
    }

    
    /**
     *
     */
    public void setContentType(String type)
    {
        this.contentType = type;
    }

    
}
