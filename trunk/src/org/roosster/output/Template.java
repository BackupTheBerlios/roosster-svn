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

import java.io.Writer;

import org.apache.velocity.VelocityContext;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Template 
{
    private org.apache.velocity.Template tmpl       = null;
    private VelocityContext              context    = null;


    /**
     *
     */
    public Template(org.apache.velocity.Template tmpl, VelocityContext context)
    {
        if ( tmpl == null || context == null )
            throw new IllegalArgumentException("Arguments are not allowed to be null");
        
        this.tmpl    = tmpl;
        this.context = context;
    }

    
    /**
     *
     */
    public void set(String name, Object obj)
    {
        context.put(name, obj);
    }

    /**
     */
    public void write(Writer writer) throws Exception
    {
        tmpl.merge(context, writer);
    }
}

/*
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        TemplateFactory tmplFactory = (TemplateFactory) registry.getPlugin("templates");
        try {
            String tmplName = output.getTemplateName();
            if ( tmplName == null )
                tmplName = ENTRY_TMPL;
            
            LOG.fine("Using Template: "+tmplName);
            
            Template tmpl = tmplFactory.getTemplate(tmplName);
            
            tmpl.set("css_string", tmplFactory.getTemplateContent(CSS_TMPL));
            tmpl.set("num", new Integer(entries.length));
            
            Template enTmpl = tmpl.get("entries");
            for(int i = 0; i < entries.length; i++) {
                enTmpl.set("url", entries[i].getUrl());
                enTmpl.set("title", entries[i].getTitle());
                enTmpl.set("issued", entries[i].getIssued());
                
                String content = entries[i].getContent();
                enTmpl.set("content", StringUtil.truncate(content, output.getTruncateLength()));

                tmpl.append("entries", enTmpl);
            }            

            writer.println( tmpl.toString() );

        } catch(Exception ex) {
            throw new OperationException(ex);
        }
            
    }
*/
