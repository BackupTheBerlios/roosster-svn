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
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import thinlet.FrameLauncher;

import org.roosster.Constants;
import org.roosster.Registry;
import org.roosster.main.Roosster;
import org.roosster.logging.LogUtil;
import org.roosster.api.RoossterApiHttpd;
import org.roosster.gui.RoossterGui;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Roosster 
{
  
    private static final String PROP_NOGUI      = "nogui";
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
            final Map cmdLine = Roosster.parseCommandLineArguments(arguments);

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

            if ( !cmdLine.containsKey(PROP_NOGUI) )
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
        if ( gui != null )  
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
    

    /**
     * removes the first element, shortening the array by one
     */
    public static String[] shift(String[] array) 
    {
        List arr = Arrays.asList(array);
        return (String[]) arr.subList(1, arr.size()).toArray(new String[0]);
    }
    
    
    /**
     * prepends <code>newElem</code> to the array, shifting all elements one index up.
     */
    public static String[] unshift(String[] array, String newElem) 
    {
        List arr = new ArrayList( Arrays.asList(array) );
        arr.add(0, newElem);
        return (String[]) arr.toArray(new String[0]);
    }
    
    
    /**
     * As the name implies, this method parses command line arguments, provided
     * in the standard form of an array of <code>String</code>-objects into a
     * <code>Map</code>. <br/>
     * Strings from the array, that start with a single dash ("-") become keys
     * in the map, while the following array element becomes the value, except
     * when the dash-preceded String contains an equal sign ("="). In this case
     * the String will be split a the position of the equal sign. The
     * left-hand-side will be turned into the key of a Map-entry, while the
     * right-hand-side becomes the value of the corresponding Map-entry. <br />
     * Example:<br />
     *
     * @param args the parameters to be turned into a <code>Map</code>-object
     * @return a <code>Map</code>
     */
    public static Map parseCommandLineArguments(String[] args)
    {
        HashMap retMap = new HashMap();
        String lastArg = null;
        for (int i = 0; i < args.length; i++) {

            String arg = args[i];
            if ( arg.startsWith("-") ) {
                arg = arg.substring(1, arg.length());
                String[] values = arg.split("=", 2);
                lastArg = arg;
                retMap.put(values[0], values.length > 1 ? values[1] : "");
            } else if ( lastArg != null ) {
                String val = (String) retMap.get(lastArg);
                if ( "".equals(val) )
                    retMap.put(lastArg, arg);
            }
        }

        return retMap;
    }


    private static final String DEF_HOMEDIR      = ".roosster";
    private static String homeDir                = null;
    
    
    /**
     * This method returns the roosster home directory, where the
     * default location for index directory and other settings is.
     * By default this location is <code>$HOME/.roosster</code>. For
     * determining <code>$HOME</code>, the system property
     * <code>user.home</code> is used. If this is <code>null</code>,
     * the current directory is used.
     * @return a String that never ends with a slash &quot;/&quot;, never null
     */
    public static String getHomeDir()
    {
        if ( homeDir == null ) {
            homeDir = System.getProperty("user.home");
            if ( homeDir == null )
                homeDir = DEF_HOMEDIR;
            else
                homeDir += homeDir.endsWith("/") ? DEF_HOMEDIR : "/"+DEF_HOMEDIR;
            
            File dir = new File(homeDir);

            if ( dir.exists() ) {
                if ( !dir.isDirectory() ) {
                    throw new IllegalArgumentException("Roosster home directory "+
                                                       homeDir+" is not a directory");
                }
            } else {
                System.out.println("Creating Roosster home directory at "+homeDir);
                dir.mkdir();
                
                if ( !dir.exists() && !dir.isDirectory() )
                    throw new IllegalStateException("Roosster home dir "+homeDir
                                                   +" doesn't exist and can't be created");
            }
        
        }

        return homeDir;
    }
    

}
