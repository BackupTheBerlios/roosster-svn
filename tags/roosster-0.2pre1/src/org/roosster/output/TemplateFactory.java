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

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import org.roosster.Output;
import org.roosster.Plugin;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.util.StringUtil;
import org.roosster.logging.VelocityLogSystem;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class TemplateFactory implements Plugin
{
    private static Logger LOG = Logger.getLogger(TemplateFactory.class.getName());

    private static final String VELOCITY_PROP_FILE = "/velocity.properties";
    private static final String PROP_TMPL_PATH     = "templates.path";
    
    private static final String DEF_TMPL_PATH      = "templates";
    
    private Registry  registry    = null;
    private boolean   initialized = false;

    /**
     */
    public void init(Registry registry) throws InitializeException
    {
        LOG.finest("Initializing "+getClass());

        this.registry = registry;

        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream(VELOCITY_PROP_FILE));
            
            Configuration conf = registry.getConfiguration();
            String path = conf.getProperty(PROP_TMPL_PATH);

            if ( path == null || "".equals(path) ) 
                path = conf.getHomeDir() + "/"+DEF_TMPL_PATH;

            LOG.finest("Setting template path to '"+path+"'");
            props.setProperty("file.resource.loader.path", path);
            
            Velocity.setProperty("runtime.log.logsystem", new VelocityLogSystem());
            Velocity.init(props);

        } catch (Exception ex) {
            throw new InitializeException(ex);
        }
        
        initialized = true;
    }

        
    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
        initialized = false;
    }


    /**
     *
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    
    /**
     * This method is called once for every request, just before the actual 
     * command chain is executed.
     */
    public void preProcess(Map requestArgs) throws OperationException
    {
    }

    
    /**
     * This method is called once for every request, just before the OutputMode
     * object is selected and called to generate the actual output.<br/>
     * Could be used to filter out certain entries from output, or to hardcode 
     * a certain <code>OutputMode</code>.
     */
    public void postProcess(Map requestArgs, Output output) throws OperationException
    {
    }


    /**
     */
    public Template getTemplate(String fileName) throws Exception
    {
        org.apache.velocity.Template tmpl = Velocity.getTemplate(fileName);
        
        if ( tmpl == null )
            throw new FileNotFoundException("Can't find template file "+fileName);
        
        return new Template(tmpl, getContext());
    }

    
    /**
     */
    public String getTemplateContent(String fileName) throws Exception
    {
        StringWriter writer = new StringWriter();
        Velocity.getTemplate(fileName).merge(getContext(), writer);
        return writer.toString();
    }

    
    // ============ private Helper methods ============

    
    /**
     * used to construct the default application Context
     */
    private VelocityContext getContext()
    {
        VelocityContext context = new VelocityContext();
        context.put("stringutil", new StringUtil(registry));

        context.put("props", registry.getConfiguration().getProperties());
        
        return context;
    }

}
