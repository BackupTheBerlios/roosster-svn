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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration; 
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import org.roosster.util.MapperUtil;
import org.roosster.util.ServletUtil;
import org.roosster.util.StringUtil;
import org.roosster.commands.CommandNotFoundException;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.Dispatcher;
import org.roosster.Constants;


/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ServletMapper extends HttpServlet
{
    private static Logger LOG = Logger.getLogger(ServletMapper.class);
    
    public static final int PUT     = 1;
    public static final int POST    = 2;
    public static final int GET     = 3;
    public static final int DELETE  = 4;
    

    public static final String PROP_PROPFILE    = "roosster.properties";
    public static final String DEF_PROPFILE     = "/roosster-web.properties";

    public static final String PROP_OUTENC      = "default.output.encoding";
    public static final String PROP_INENC       = "default.input.encoding";
    public static final String DEF_ENC          = "UTF-8";
    
    public static final String DEF_CONTENT_TYPE = "text/xml";
    public static final String DEF_OUTPUT_MODE  = "atom";
    
    public static final String DEF_COMMAND      = "search";
    
    private Properties properties = null;
    
    private Dispatcher dispatcher     = null;
    private Registry   registry       = null;
    
    protected String     outputEncoding = null;  
    protected String     inputEncoding  = null;  
    

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
            
            // Create primary roosster worker objects and ... 
            registry = new Registry(properties); 
            dispatcher = new Dispatcher(registry);
            
            // ... store them in servlet context, so other servlet can access them
            config.getServletContext().setAttribute(Constants.CTX_REGISTRY, registry);
            config.getServletContext().setAttribute(Constants.CTX_DISPATCHER, dispatcher);


            // and now get some values, to make our life easier            
            outputEncoding = registry.getConfiguration().getProperty(PROP_OUTENC, DEF_ENC);
            inputEncoding  = registry.getConfiguration().getProperty(PROP_INENC, DEF_ENC);

        } catch(Exception ex) {
            LOG.fatal("Exception while initializing "+getClass(), ex);
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
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("======================================================");
            LOG.debug("Processing Request: "+req.getMethod()+" "+req.getPathInfo());
        }
        
        try {
            String commandName = getCommandName(method, req);
            Map args = parseRequestArguments(req);
            
            // add request arguments to configuration
            registry.getConfiguration().setRequestArguments(args);
            
            // run commands            
            Output output = dispatcher.run(commandName, getOutputMode(), args);

            if ( output.entriesSize() < 1 ) {
                resp.setStatus(resp.SC_NO_CONTENT);
            } else {
                // determine content type
                String contentType = output.getContentType();
                contentType = contentType == null ? DEF_CONTENT_TYPE : contentType;
                String contentHeader = contentType+"; charset="+outputEncoding;
                resp.setContentType(contentHeader);
                
                LOG.debug("Set Content-Type Header field to: "+contentHeader);
    
                // output everything
                output.output( resp.getWriter() );
            }
            
        } catch (CommandNotFoundException ex) {
          
            LOG.warn(ex.getMessage(), ex);
            resp.sendError(resp.SC_NOT_FOUND, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
            
        } catch (Exception ex) {
            
            Throwable t = ex.getCause() == null ? ex : ex.getCause();
            LOG.warn("Sending HTTP Status Code 500: "+t.getMessage(), t);
            resp.sendError(resp.SC_INTERNAL_SERVER_ERROR, 
                           "RoossterException: <"+t.getClass().getName()+"> "+t.getMessage());
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
    protected String getOutputMode()
    {
        return registry.getConfiguration()
                       .getProperty(Constants.PROP_OUTPUTMODE, DEF_OUTPUT_MODE);
    }
    
    
    /**
     */
    protected Map parseRequestArguments(HttpServletRequest req) throws Exception
    {
        Map args = new HashMap();

        Enumeration names = req.getParameterNames();
        while ( names.hasMoreElements() ) {
          
            String   name  = (String) names.nextElement();
            String[] values = req.getParameterValues(name);
            
            if ( values == null )
                args.put(name, "");
            else
                args.put(name, StringUtil.join(values, " ") );       
        }

        return args;
    }


    /**
     * 
     */
    protected String getBaseUrl(HttpServletRequest req)
    {
        return ServletUtil.getBaseUrl(req);
    }

    
}

