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
package org.roosster.util;

import java.io.*;
import java.util.*;

import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

import org.roosster.InitializeException;
import org.roosster.util.MapperUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class JettyStarter
{
    public static final String DEF_PORT         = "8181";
    public static final String PROP_PORT        = "server.port";
    
    /**
     * The (optional) command line property, which specifies the location of the 
     * .war file to start.
     */
    public static final String PROP_WEBAPP_WAR  = "server.webapp";

    /**
     * the default location, the starter looks for the .war file to start. 
     */    
    public static final String DEF_WEBAPP_WAR   = "./roosster.war";
    
    /**
     * The (optional) command line property, which specifies the location of the 
     * .war file to start.
     */
    public static final String PROP_CONTEXT_PATH  = "webapp.context";
    
    /**
     * The default context path under which the application will be available 
     * in the server, if nothing is specified on the command line.
     */
    public static final String DEF_CONTEXT_PATH     = "/roosster";
    
    
    /**
     * 
     */
    public static void main(String[] args) 
    {  
        try {
            Map cmdLine = MapperUtil.parseCommandLineArguments(args);

            String port = (String) cmdLine.get(PROP_PORT);
            if ( port == null )
                port = DEF_PORT;
            
            String webapp = (String) cmdLine.get(PROP_WEBAPP_WAR);
            if ( webapp == null ) 
                webapp = DEF_WEBAPP_WAR;
            
            String contextPath = (String) cmdLine.get(PROP_CONTEXT_PATH);
            if ( contextPath == null ) {
                contextPath = DEF_CONTEXT_PATH;
            } else {
                // make sure the path starts with a /
                if ( !contextPath.startsWith("/") )
                    contextPath = "/"+contextPath;
            }
            
            File webAppWarFile = new File(webapp);
            if ( !webAppWarFile.exists() && !webAppWarFile.canRead() )
                throw new FileNotFoundException("Web Application '"+webapp+"' not found");
            
            System.out.println("Starting Server with WebApplication "+webapp);
            
            Server server = new Server();

            SocketListener listener = new SocketListener();
            listener.setPort( Integer.valueOf(port).intValue() );
            server.addListener(listener);
           
            server.addWebApplication(contextPath, webapp);
            
            server.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }      
    }
}
