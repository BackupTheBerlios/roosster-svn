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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration; 
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import org.roosster.util.MapperUtil;
import org.roosster.util.ServletUtil;
import org.roosster.util.StringUtil;
import org.roosster.logging.LogUtil;
import org.roosster.commands.CommandNotFoundException;
import org.roosster.store.DuplicateEntryException;
import org.roosster.xml.ParseException;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
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


    public static final String PROP_OUTENC      = "default.output.encoding";
    public static final String PROP_INENC       = "default.input.encoding";
    public static final String DEF_ENC          = "UTF-8";
    
    public static final String DEF_CONTENT_TYPE = "text/html";
    public static final String DEF_OUTPUT_MODE  = "html";
    
    public static final String DEF_COMMAND      = "search";
    
    private Properties properties = null;
    
    protected Dispatcher dispatcher     = null;
    protected Registry   registry       = null;
    
    protected String     outputEncoding = null;  
    protected String     inputEncoding  = null;  
    

    /**
     *
     */
    public void init(ServletConfig config) throws ServletException
    {
        try {
            // ... pull out certain objects from servlet context
            registry = (Registry) config.getServletContext().getAttribute(Constants.CTX_REGISTRY);
            dispatcher = (Dispatcher) config.getServletContext().getAttribute(Constants.CTX_DISPATCHER);

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
     */
    protected void processRequest(int method,
                                  HttpServletRequest req, 
                                  HttpServletResponse resp)
                           throws ServletException, IOException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("======================================================");
            LOG.debug("Processing Request: "+req.getMethod()+" "+req.getPathInfo());
            LOG.debug("Using: "+this);
        }
        
        Output output = null;
        String commandName = null;
        try {
          
            commandName = getCommandName(method, req);
            Map args = parseRequestArguments(req);
            
            // run commands            
            output = dispatcher.run(commandName, getOutputMode(), args);

            if ( output.entriesSize() < 1 && !returnEmptyList() ) {
                resp.setStatus(resp.SC_NO_CONTENT);
            } else {
                
                prepareOutput(req, resp, output);
                
                // output everything
                output.output( resp.getWriter() );
            }
          
        } catch (Exception ex) {
            processException(method, req, resp, output, commandName, ex);
        }
    }
    
    
    /**
     * 
     */
    protected void processException(int method, 
                                    HttpServletRequest req, 
                                    HttpServletResponse resp,
                                    Output output, 
                                    String commandName,
                                    Exception ex)
                             throws ServletException, IOException
    {
        if ( ex instanceof OperationException ) 
            ex = (Exception) ex.getCause();
            
        LOG.warn(ex.getMessage(), ex);
          
        if ( ex instanceof DuplicateEntryException ) {
            
            // argument 'url' is still in request, no need to add it
            DuplicateEntryException duplEx = (DuplicateEntryException) ex;
            addMessageToRequest(req, Constants.OUTPUTMSG_LEVEL_INFO, "Entry "+ex.getMessage()+" already stored");
            internalRedirect(req, resp, "/application/entry");
          
        } else if ( ex instanceof CommandNotFoundException ) {
            resp.sendError(resp.SC_NOT_FOUND, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
            
        } else if ( ex instanceof ParseException ) {
            resp.sendError(resp.SC_BAD_REQUEST, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
                           
        } else if ( ex instanceof IllegalArgumentException ) {
            resp.sendError(resp.SC_BAD_REQUEST, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
                           
        } else {
            resp.sendError(resp.SC_INTERNAL_SERVER_ERROR, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
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
     * if <code>true</code>, then empty result sets are returned as normal results,
     * going through the OutputMode process; if <code>false</code>, then a 
     * <code>204 No-Content</code> response is sent.
     */
    protected boolean returnEmptyList()
    {
        return true;
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
    
    
    /**
     * this method does not return
     */
    protected void internalRedirect(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    String destination)
                             throws ServletException, IOException 
    {
        LOG.warn("Internal redirect to: "+destination);
        RequestDispatcher dispatcher = req.getRequestDispatcher(destination);
        dispatcher.forward(req, resp);
    }
    
    
    /**
     * 
     */
    protected void addMessageToRequest(HttpServletRequest req, String level, String msgStr)
    {
        List msg = Arrays.asList(new String[] { level, msgStr} );
        req.setAttribute(Constants.REQ_OUTPUT_MESSAGES, Arrays.asList(new List[] {msg}));
    }

    
    // ============ private Helper methods ============
    
    /**
     * 
     */
    private void prepareOutput(HttpServletRequest req,  
                               HttpServletResponse resp, 
                               Output output)
    {
        // determine content type
        String contentType = output.getContentType();
        contentType = contentType == null ? DEF_CONTENT_TYPE : contentType;
        String contentHeader = contentType+"; charset="+outputEncoding;
        resp.setContentType(contentHeader);
        
        LOG.debug("Set Content-Type Header field to: "+contentHeader);
    
        output.setOutputProperty(Constants.VELCTX_BASEURL,  getBaseUrl(req));
        output.setOutputProperty(Constants.VELCTX_HTTPREQ,  req);
        
        // add output messages, stored in request
        List outputMessages = (List) req.getAttribute(Constants.REQ_OUTPUT_MESSAGES);
        if ( outputMessages != null ) {
            LOG.debug("Preserving output messages from request: "+outputMessages);
            for (int i = 0; i < outputMessages.size(); i++) {
                List msg = (List) outputMessages.get(i); 
                output.addOutputMessage((String) msg.get(0), (String) msg.get(1));
            }
        }
    }

    
}

/*
try {
    if ( output == null )
        output = dispatcher.getOutput(commandName, getOutputMode());
    
    prepareOutput(req, resp, output);
    output.addOutputMessage(Constants.OUTPUTMSG_LEVEL_ERROR, ex.getMessage());
    output.output( resp.getWriter() );
} catch(Exception ex2) {
    throw new ServletException(ex2);
}
*/
