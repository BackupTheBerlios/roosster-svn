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
package org.roosster.output.modes;

import java.io.PrintWriter;
import java.util.List;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.log4j.Logger;

import org.roosster.web.VelocityConstants;
import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.util.VelocityUtil;
import org.roosster.util.ServletUtil;
import org.roosster.Output;
import org.roosster.output.OutputMode;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class HtmlMode extends AbstractOutputMode implements OutputMode, VelocityConstants
{
    private static Logger LOG = Logger.getLogger(HtmlMode.class);
    
    public static final String TMPL_ROOT = "includes";
    
    /**
     *
     */
    public void output(Output output, PrintWriter writer, EntryList entries)
                throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        Context context = new VelocityContext();
        VelocityUtil.initContext(getRegistry(), context);
        
        context.put(VELCTX_COMMAND,   output.getCommandName());
        context.put(VELCTX_OUTPUTMSG, output.getOutputMessages());         
        context.put(VELCTX_ENTRYLIST, entries);         

        Iterator iter = output.getOutputPropertyNames().iterator();
        while ( iter.hasNext() ) {
            String name = (String) iter.next();
            context.put(name, output.getOutputProperty(name));
        }
        
        // determine which template to use
        String jumpToCommand = getRegistry().getConfiguration().getProperty(Constants.ARG_JUMPTO);
        String nextCommand = jumpToCommand == null ? output.getCommandName() : jumpToCommand;
        
        String templateName = TMPL_ROOT +"/"+ nextCommand +".html";
        
        try {
            LOG.debug("HtmlMode: Merging context and template '"+templateName+"'");
            Velocity.getTemplate(templateName).merge(context, writer);
        } catch (Exception ex) {
            throw new OperationException("Can't find or merge template '"+templateName+"'", ex);
        }
    }

}
