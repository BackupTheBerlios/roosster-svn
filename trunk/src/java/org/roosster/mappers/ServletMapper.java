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

import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration; 
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.roosster.util.MapperUtil;
import org.roosster.InitializeException;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.Dispatcher;
import org.roosster.store.EntryStore;
import org.roosster.commands.CommandNotFoundException;


/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ServletMapper extends HttpServlet
{
    private static Logger LOG = Logger.getLogger(ServletMapper.class.getName());
    
    public static final int PUT     = 1;
    public static final int POST    = 2;
    public static final int GET     = 3;
    public static final int DELETE  = 4;
    

    public static final String PROP_PROPFILE    = "roosster.properties";
    public static final String DEF_PROPFILE     = "/roosster-web.properties";

    public static final String PROP_OUTENC      = "default.output.encoding";
    
    public static final String DEF_CONTENT_TYPE = "text/xml";
    public static final String DEF_OUTPUT_MODE  = "atom";
    public static final String DEF_COMMAND      = "search";
    public static final String DEF_ENC          = "UTF-8";

    private static Properties properties = null;
    
    private Dispatcher dispatcher     = null;
    private Registry   registry       = null;
    private String     outputEncoding = null;  
    

    /**
     *
     */
    public void init(ServletConfig config) throws ServletException
    {
        try {
            InputStream propInput = null;
            
            // load default properties from classpath if no location is given in web.xml
            String propFile = config.getInitParameter(PROP_PROPFILE);
            if ( propFile != null )
                propInput = config.getServletContext().getResourceAsStream(propFile);
            else 
                propInput = getClass().getResourceAsStream(DEF_PROPFILE);
            
            properties = MapperUtil.loadProperties(propInput, new HashMap());
        
            registry = new Registry(properties); 
            dispatcher = new Dispatcher(registry);
            
            outputEncoding = registry.getConfiguration().getProperty(PROP_OUTENC, DEF_ENC);

        } catch(Exception ex) {
            throw new ServletException(ex);
        }
    }

     
    /**
     * TODO make a correct implementation here
     */
    public long getLastModified(HttpServletRequest req)
    {
        return System.currentTimeMillis();
    }

    
    /**
     *
     */
    public void doDelete(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
    {
        processRequest(DELETE, req, resp);
    }

    
    /**
     *
     */
    public void doPut(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
    {
        processRequest(PUT, req, resp);
    }

    
    /**
     *
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
    {
        processRequest(POST, req, resp);
    }

    
    /**
     *
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
               throws ServletException, IOException
    {
        processRequest(GET, req, resp);
    }

    
    /**
     * 
     */
    protected void processRequest(int method,
                                  HttpServletRequest req, 
                                  HttpServletResponse resp)
                           throws ServletException, IOException

    {
        try {
            String commandName = getCommandName(method, req);
            Map args = parseRequestArguments(req);
    
            // run commands            
            Output output = dispatcher.run(commandName, args);

            // determine content type
            String contentType = output.getContentType();
            contentType = contentType == null ? DEF_CONTENT_TYPE : contentType; 
            resp.setContentType(contentType+"; charset="+outputEncoding);

            // output everything
            output.output( getOutputMode(args), resp.getWriter() );
            
        } catch (CommandNotFoundException ex) {
          
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            resp.sendError(resp.SC_NOT_FOUND, ex.getMessage());
            
        } catch (Exception ex) {
          
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            resp.sendError(resp.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            
        }
    }
    
    
    /**
     *
     */
    protected String getCommandName(int method, HttpServletRequest req)
    {
        String commandName = null;
        
        String pathInfo = req.getPathInfo();
        if ( pathInfo != null && pathInfo.length() > 1 ) {
            int indexSlash = pathInfo.indexOf("/", 1);
            
            if ( indexSlash == -1 ) {
                commandName = pathInfo.substring(1);
            } else {
                commandName = pathInfo.substring(1, indexSlash);
            }
        }
        
        return commandName == null ? DEF_COMMAND : commandName;
    }


    /**
     *
     */
    protected String getOutputMode(Map args)
    {
        String mode = (String) args.get(MapperUtil.ARG_OUTPUTMODE);
        return mode == null || "".equals(mode) ? DEF_OUTPUT_MODE : mode ;

    }
    
    
    /**
     * TODO handle multiple value parameters correctly
     */
    protected Map parseRequestArguments(HttpServletRequest req)
    {
        Map args = new HashMap();

        Enumeration names = req.getParameterNames();
        while ( names.hasMoreElements() ) {
            String name  = (String) names.nextElement();
            String value = req.getParameter(name);
            args.put(name, value);
            
            LOG.fine("RequestParameter: "+name+" => "+value);
        }

        return args;
    }


    /**
     * 
     */
    protected String getBaseUrl(HttpServletRequest req)
    {
        return "http://"+ req.getServerName() +":"+ req.getServerPort()+ req.getContextPath()+"/" ;
    }

    
}

