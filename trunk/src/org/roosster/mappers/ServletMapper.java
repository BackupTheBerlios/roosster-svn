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
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration; 
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

import org.roosster.util.MapperUtil;
import org.roosster.InitializeException;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.Dispatcher;


/**
 * TODO implement shutdown method
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: ServletMapper.java,v 1.1 2004/12/03 14:30:14 firstbman Exp $
 */
public class ServletMapper extends HttpServlet
{
    private static Logger LOG = Logger.getLogger(ServletMapper.class.getName());

    public static final String PROP_OUTENC = "default.output.encoding";
    public static final String PROP_PORT   = "server.port";

    public static final String DEF_CONTENT_TYPE = "text/html";
    public static final String DEF_OUTPUT_MODE  = "html";
    public static final String DEF_COMMAND      = "searchform";
    public static final String DEF_ENC          = "UTF-8";
    public static final String DEF_PORT         = "8181";
    
    public static final String CONTEXT_PATH     = "/roosster";
    
    public static final String ARG_MODE    = "output.mode";
    public static final String ARG_BASEURL = "internal.baseurl";

    private static Properties properties = null;
    
    private Dispatcher dispatcher     = null;
    private Registry   registry       = null;
    private String     outputEncoding = null;  

    /**
     *
     */
    public static void main(String[] args)
    {
        try {
            MapperUtil.initLogging(args);
            Map cmdLine = MapperUtil.parseCommandLineArguments(args);
            properties = MapperUtil.loadProperties(cmdLine);

            String port = properties.getProperty(PROP_PORT, DEF_PORT);
            
            
            Server server = new Server();

            SocketListener listener=new SocketListener();
            listener.setPort( Integer.valueOf(port).intValue() );
            server.addListener(listener);
           
            ServletHttpContext context = (ServletHttpContext) server.getContext("/");
            context.addServlet("roosster", CONTEXT_PATH+"/*","org.roosster.mappers.ServletMapper");
            
            server.start ();

        } catch (Exception ex) {
            if ( LOG.isLoggable(Level.CONFIG) )
                ex.printStackTrace();
            else
                System.out.println("ERROR:  "+ ex.getMessage());
        }
    }

    /**
     *
     */
    public void init(ServletConfig config) throws ServletException
    {
        if ( properties == null ) {
            // TODO get config location from web.xml config
        }
      
        try {
            registry = new Registry(properties); 
            dispatcher = new Dispatcher(registry);
            
            outputEncoding = registry.getConfiguration().getProperty(PROP_OUTENC, DEF_ENC);

        } catch(InitializeException ex) {
            throw new ServletException(ex);
        }
    }

     
    /**
     *
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
    {
        PrintWriter writer = null;
        
        try {
            String commandName = getCommandName(req);
            Map args = parseRequestArguments(req);

            LOG.fine("BASE URL is "+getBaseUrl(req));
            args.put(ARG_BASEURL, getBaseUrl(req));

            registry.getConfiguration().setRequestArguments(args);
            
            Output output = dispatcher.run(commandName, args);

            // TODO see if conf.getRequestArguments() contains a
            // different outputmode
            
            output.setOutputMode(DEF_OUTPUT_MODE);
            String contentType = output.getContentType();
            contentType = contentType == null ? DEF_CONTENT_TYPE : contentType; 
            resp.setContentType(contentType+"; charset="+outputEncoding);

            writer = resp.getWriter();
            output.output(writer);
            
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    
    /**
     *
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
               throws ServletException, IOException
    {
        doPost(req, resp);
    }
   

    /**
     * TODO make a correct implementation here
     */
    public long getLastModified(HttpServletRequest req)
    {
        return System.currentTimeMillis();
    }

    
    // ============ private Helper methods ============


    private String getBaseUrl(HttpServletRequest req)
    {
        return "http://"+ req.getServerName() +":"+ req.getServerPort()+ CONTEXT_PATH+"/" ;
    }

    
    /**
     *
     */
    private String getCommandName(HttpServletRequest req)
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
     * TODO handle multiple value parameters correctly
     */
    private Map parseRequestArguments(HttpServletRequest req)
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

}

