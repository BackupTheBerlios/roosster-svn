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
package org.roosster.api;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import org.mortbay.http.HttpServer;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.http.handler.IPAccessHandler;
import org.apache.log4j.Logger;

import org.roosster.Registry;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class RoossterApiHttpd 
{
    private static Logger LOG = Logger.getLogger(RoossterApiHttpd.class);
    
    private static final String CONTEXT_PATH = "/roosster/api/";    
    private static String BASE_PATH          = null;    
  
    private HttpServer server   = null;
    private Registry   registry = null;
    private int        port     = 0;
    
    
    /**
     *
     */
    public RoossterApiHttpd(Registry registry, int port)
    {
        if ( registry == null ) 
            throw new IllegalArgumentException("Parameter registry is not allowed to be null");

        this.registry = registry;
        this.port = port;
        
        RoossterApiHttpd.BASE_PATH = "http://localhost:"+ port + CONTEXT_PATH;
    }
    
    
    /**
     * Construct a URL to fetch a cached copy of an Entry (uses raw output format for this 
     */
    public String constructCachedLink(URL url)
    {
        try {
            return BASE_PATH
                 +"entry?output.mode=raw&url="
                 + URLEncoder.encode(url.toString(), "UTF-8");
        } catch (Exception ex) {
            // this should not happen, can only be java.io.UnsupportedEncodingException
            return "";
        }
    }
    
    
    /**
     *
     */
    public void stop(boolean graceful) throws Exception
    {
        server.stop(graceful);
    }
    
    
    /**
     *
     */
    public void start() throws Exception
    {
        server = new HttpServer();

        SocketListener listener = new SocketListener();
        listener.setPort(port);
        server.addListener(listener);
        
        HttpContext context = server.addContext(CONTEXT_PATH);
        context.addHandler( new ApiHttpHandler(registry) );
        
        IPAccessHandler ipAccessHandler = new IPAccessHandler();
        ipAccessHandler.setStandard("deny");
        ipAccessHandler.setAllowIP("127.0.0.1");
        
        HttpContext ctx = server.addContext("/");
        ctx.addHandler( ipAccessHandler );
        ctx.addHandler( new NotFoundHandler() );

        server.start();
    }
}

