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
package org.roosster.main;

import java.net.URL;
import java.util.Map;
import java.util.Locale;
import java.util.ResourceBundle;

import thinlet.FrameLauncher;

import org.roosster.Constants;
import org.roosster.Registry;
import org.roosster.util.MapperUtil;
import org.roosster.logging.LogUtil;
import org.roosster.api.RoossterApiHttpd;
import org.roosster.gui.RoossterGui;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Roosster 
{
  
    private static final String PROP_FILE       = "/roosster.properties";
    private static final String RES_BUNDLE      = "roosster_resources";
    
    public static final String DEF_PORT         = "8181";
    public static final String PROP_PORT        = "server.port";


    private RoossterApiHttpd httpd   = null;
    private RoossterGui      gui     = null;

    
    
    /**
     * 
     */
    public Roosster()
    {
    }
    
    
    /**
     *
     */
    public static void main(String[] arguments)
    {
        try {
            final Map cmdLine = MapperUtil.parseCommandLineArguments(arguments);

            LogUtil.configureLogging(cmdLine);

            // read arguments 
            if ( cmdLine.containsKey(Constants.PROP_LOCALE) ) 
                Locale.setDefault( new Locale((String) cmdLine.get(Constants.PROP_LOCALE)) );

            String portStr = (String) cmdLine.get(PROP_PORT);
            int port = Integer.valueOf( portStr == null ? DEF_PORT : portStr ).intValue();

            // configure and intialize important objects
            ResourceBundle resbundle = ResourceBundle.getBundle(RES_BUNDLE, Locale.getDefault());
            
            Registry registry = new Registry(Roosster.class.getResourceAsStream(PROP_FILE), cmdLine);
            
           
            // now plumb everything together
            Roosster roosster = new Roosster();
            
            roosster.setHttpd( new RoossterApiHttpd(registry, port) );
            roosster.setGui( new RoossterGui(roosster, registry, resbundle) );            
           
            // ... and off it goes 
            roosster.start();            

        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }  
    
    
    /**
     * 
     */
    public void stop() throws Exception
    {
        httpd.stop(true);
        //Thread.sleep(1000);
        System.exit(0);
    }
  

    /**
     * 
     */
    public void start() throws Exception
    {
        // start Thinlet GUI
        new FrameLauncher("Roosster - personal search ", gui, 800, 600);
        
        // start Jetty HTTP server
        httpd.start(); 
    }
  
    
    /**
     * 
     */
    public String constructCachedLink(URL url)
    {
        return httpd.constructCachedLink(url);
    }

		/**
		 * Sets the value of httpd.
		 * @param httpd The value to assign httpd.
		 */
		public void setHttpd(RoossterApiHttpd httpd) {
				this.httpd = httpd;
		}


		/**
		 * Sets the value of gui.
		 * @param gui The value to assign gui.
		 */
		public void setGui(RoossterGui gui) {
				this.gui = gui;
		}
    

}
