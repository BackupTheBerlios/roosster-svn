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
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
 

/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class VelocityServlet extends org.apache.velocity.servlet.VelocityServlet
{
    private static Logger LOG = Logger.getLogger(VelocityServlet.class.getName());
    
    
    public static final String VELOCITY_PROP = "org.apache.velocity.properties";
  
    private ServletContext servletContext = null;
    
    /**
     * 
     */
    protected  Properties loadConfiguration(ServletConfig config)
                                     throws FileNotFoundException, IOException
    {
        InputStream propInput = null; 
      
        servletContext = config.getServletContext();
        
        String propFile = config.getInitParameter(VELOCITY_PROP);
        if ( propFile != null )
            propInput = servletContext.getResourceAsStream(propFile);
        else
            throw new FileNotFoundException("Property "+VELOCITY_PROP+" is not set");
        
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
                           throws java.lang.Exception
    {
        String path = req.getServletPath();
        if (  path == null || "/".equals(path) )
            path = "index.html";
        
        InputStream inStream = servletContext.getResourceAsStream(path);
        if ( inStream != null ) {
            LOG.fine("Evaluating template "+path+" for servletPath "+req.getServletPath());
            Velocity.evaluate(context, resp.getWriter(), path, new InputStreamReader(inStream));
        } else {
            LOG.warning("Velocity: Can't load '"+path+"' for request "+
                        req.getRequestURL().append("?").append(req.getQueryString()) );
                        
            resp.sendError(resp.SC_NOT_FOUND, path+" not found");
        }
        
        return null;
    }

  
}
