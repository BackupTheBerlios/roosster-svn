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
package org.roosster.web;

import java.io.*;
import java.util.Properties;
import org.apache.log4j.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
 
import org.roosster.util.ServletUtil;
import org.roosster.store.EntryStore;
import org.roosster.Constants;
import org.roosster.Registry;

/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class VelocityServlet extends org.apache.velocity.servlet.VelocityServlet
{
    private static Logger LOG = Logger.getLogger(VelocityServlet.class.getName());
    
    
    public static final String VELCTX_BASEURL     = "baseurl";
    public static final String VELCTX_APPVERSION  = "roossterVersion";
    public static final String VELCTX_INDEXNUM    = "roossterIndexContains";
    
    private ServletContext servletContext = null;
    
    /**
     * 
     */
    protected  Properties loadConfiguration(ServletConfig config)
                                     throws FileNotFoundException, IOException
    {
        InputStream propInput = null; 
      
        servletContext = config.getServletContext();
        
        String propFile = config.getInitParameter(INIT_PROPS_KEY);
        if ( propFile != null )
            propInput = servletContext.getResourceAsStream(propFile);
        else
            throw new FileNotFoundException("Property "+INIT_PROPS_KEY+" is not set");
        
        Properties props = new Properties();
        props.load(propInput);
        
        return props;
    }
  
  
    /**
     * 
     */
    public Template handleRequest(HttpServletRequest req, 
                                  HttpServletResponse resp,
                                  Context context)
                           throws Exception
    {
        initContext(req, context);
      
        String path = getPath(req);

        if ( Velocity.resourceExists(path) ) {
            resp.setContentType( getContentType(req)+"; charset=UTF-8");
            return getTemplate(path);
        }
          
        InputStream inStream = servletContext.getResourceAsStream(path);
        if ( inStream != null ) {
            LOG.debug("Evaluating template "+path+" for servletPath "+req.getServletPath());
            
            resp.setContentType( getContentType(req)+"; charset=UTF-8");
            Velocity.evaluate(context, resp.getWriter(), path, new InputStreamReader(inStream));
        } else {
            LOG.warn("Velocity: Can't load '"+path+"' for request "+
                        req.getRequestURL().append("?").append(req.getQueryString()) );
                        
            resp.sendError(resp.SC_NOT_FOUND, path+" not found");
        }
        
        return null;
    }
    
    
    /**
     * 
     */
    protected void initContext(HttpServletRequest req, Context context) throws Exception
    {
        Registry registry = (Registry) servletContext.getAttribute(Constants.CTX_REGISTRY);
        
        if ( registry == null )
            throw new IllegalStateException("Servlet Environment not properly initialized! No Registry!");
        
        
        context.put(VELCTX_APPVERSION, registry.getConfiguration().getProperty(Constants.PROP_APPVERSION));
        context.put(VELCTX_BASEURL,    ServletUtil.getBaseUrl(req));
        
        try {
            EntryStore store = (EntryStore) registry.getPlugin("store");
            context.put(VELCTX_INDEXNUM,   String.valueOf(store.getDocNum()) );
        } catch (IOException ex) {
            context.put(VELCTX_INDEXNUM,   "no docs there yet");
        }
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private String getContentType(HttpServletRequest req)
    {
        String path = getPath(req);
        
        if ( path.endsWith(".html") )
            return "text/html";    
        if ( path.endsWith(".js") )
            return "text/javascript";
        else if ( path.endsWith(".css") )
            return "text/css";
        else 
            return "text/plain";
    }
    
    
    /**
     * 
     */
    private String getPath(HttpServletRequest req)
    {
        String path = req.getServletPath();
        if (  path == null || "/".equals(path) )
            path = "index.html";
        
        return path;
    }

  
}
