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
package org.roosster.mappers;

import java.io.InputStream;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.log4j.Logger;

import org.roosster.util.MapperUtil;
import org.roosster.util.ServletUtil;
import org.roosster.util.StringUtil;
import org.roosster.logging.LogUtil;
import org.roosster.commands.CommandNotFoundException;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.Output;
import org.roosster.Dispatcher;
import org.roosster.Constants;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ServletContextListener implements javax.servlet.ServletContextListener	
{
    private static Logger LOG;    

    public static final String PROP_PROPFILE    = "roosster.properties";
    public static final String DEF_PROPFILE     = "/roosster-web.properties";
    
    
    public void contextInitialized(ServletContextEvent sce)
    {
        try {
            // configure and initialize Logging
            LogUtil.configureLogging( System.getProperty(Constants.CLI_LOGGING) );
            LOG = Logger.getLogger(ServletContextListener.class);          
          
            InputStream propInput = null;
            
            ServletContext context = sce.getServletContext();
            
            // load default properties from classpath if no location is given in web.xml
            String propFile = context.getInitParameter(PROP_PROPFILE);
            if ( propFile != null )
                propInput = context.getResourceAsStream(propFile);
            else 
                propInput = getClass().getResourceAsStream(DEF_PROPFILE);
            
            // Create primary roosster worker objects and ... 
            Registry registry = new Registry(propInput, new HashMap()); 
            Dispatcher dispatcher = new Dispatcher(registry);
            
            // ... store them in servlet context, so other servlet can access them
            context.setAttribute(Constants.CTX_REGISTRY, registry);
            context.setAttribute(Constants.CTX_DISPATCHER, dispatcher);


        } catch(Exception ex) {
            LOG.fatal("Exception while initializing "+getClass(), ex);
            throw new RuntimeException(ex);
        }      
    }
    
    
    /**
     * 
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        // TODO call registry.shutdown() here 
    }
}
