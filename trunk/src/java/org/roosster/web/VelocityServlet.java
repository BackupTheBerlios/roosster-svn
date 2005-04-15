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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.roosster.Registry;
import org.roosster.util.ServletUtil;
import org.roosster.util.VelocityUtil;

/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class VelocityServlet extends org.apache.velocity.servlet.VelocityServlet 
                          implements VelocityConstants, ServletConstants
{
    private static final long serialVersionUID = 3257289132211712567L;

    private static Logger LOG = Logger.getLogger(VelocityServlet.class.getName());

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
                        
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path+" not found");
        }
        
        return null;
    }
    
    
    /**
     * 
     */
    protected void initContext(HttpServletRequest req, Context context) throws Exception
    {
        Registry registry = (Registry) servletContext.getAttribute(CTX_REGISTRY);
        
        if ( registry == null )
            throw new IllegalStateException("Servlet Environment not properly initialized! No Registry!");
        
        VelocityUtil.initContext(registry, context);
        context.put(VELCTX_BASEURL, ServletUtil.getBaseUrl(req));
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
