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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.roosster.InitializeException;

/**
 * This class provides static utility methods for <code>Mapper</code>-classes
 * (classes in the <code>org.roosster.mappers</code> package), like parsing
 * arguments, initializing logging or loading <code>Properties</code>.
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class MapperUtil
{
    private static Logger LOG = Logger.getLogger(MapperUtil.class.getName());
    

    public static final String ARG_OUTPUTMODE    = "output.mode";

    public static final String PROP_FILE_ARG     = "conf";

    public static final String DEFAULT_LOG_CONF  = "/default_logging.properties";
    public static final String VERBOSE_LOG_CONF  = "/verbose_logging.properties";
    public static final String DEBUG_LOG_CONF    = "/debug_logging.properties";

    private static final String DEF_HOMEDIR      = ".roosster";
    private static String homeDir                = null;

    
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

        LOG.finest("cliMap "+ retMap);
        return retMap;
    }


    /**
     *
     */
    public static void initLogging(String[] arguments) throws IOException, InitializeException
    {
        InputStream stream = null;
        try {

            String fileName = null;
            for(int i = 0; i < arguments.length; i++) {
                if ( "-v".equals(arguments[i]) ) {
                    fileName = VERBOSE_LOG_CONF;
                    break;
                } else if ( "-d".equals(arguments[i]) ) {
                    fileName = DEBUG_LOG_CONF;
                    break;
                }
            }

            if ( fileName == null )
                fileName = DEFAULT_LOG_CONF;

            stream = MapperUtil.class.getResourceAsStream(fileName);
            if ( stream == null )
                throw new InitializeException("File '"+fileName+"' not on classpath");

            LogManager.getLogManager().readConfiguration(stream);

        } catch (SecurityException ex) {
            throw new InitializeException("Exception while configuring logging", ex);
        } finally {
            if ( stream != null )
                stream.close();
        }
    }


    /**
     * @param fileName name of standard property file name
     */
    public static Properties loadProperties(InputStream propInput, Map cmdLine)
                               throws IOException, IllegalArgumentException
    {
        if ( propInput == null )
            throw new IllegalArgumentException("Properties inputStream is not allowed to be null");

        Properties props = new Properties();
        props.load(propInput);      

        // try to load user defines properties
        String propFileName =  (String) cmdLine.get(PROP_FILE_ARG);

        if ( propFileName == null ) {
            // if the user didn't specify a file, use standard file in $HOME/.roosster
            propFileName = getHomeDir() + File.separator + "roosster.properties"; 
        }
        
        File propFile = new File(propFileName);

        if ( propFile.exists() && propFile.canRead() ) {
            LOG.fine("Overriding default setting with user defined settings");
            propInput = new FileInputStream(propFile);
            props.load(propInput);
        } else {
            LOG.config("Can't use secondary configuration file: "+propFileName);
        }

        // now override with commandline parameters
        props.putAll(cmdLine);

        if ( LOG.isLoggable(Level.FINE) ) {
            LOG.fine("Configuration Properties are: \n");

            Iterator keys = props.keySet().iterator();
            while ( keys.hasNext() ) {
                Object key = keys.next();
                LOG.fine(key +" => "+ props.get(key));
            }

        }

        return props;
    }
      
    
    /**
     * @param fileName name of standard property file name
     */
    public static Properties loadProperties(File file, Map cmdLine)
                               throws IOException, IllegalArgumentException
    {
        return loadProperties(new FileInputStream(file), cmdLine);
    }
    

    /**
     * This method returns the roosster home directory, where the
     * default location for index directory and other settings is.
     * By default this location is <code>$HOME/.roosster</code>. For
     * determining <code>$HOME</code>, the system property
     * <code>user.home</code> is used. If this is <code>null</code>,
     * the current directory is used.
     * @return a String that never ends with a slash &quot;/&quot;
     */
    public static String getHomeDir()
    {
        if ( homeDir == null ) {
            homeDir = System.getProperty("user.home");
            if ( homeDir == null )
                homeDir = DEF_HOMEDIR;
            else
                homeDir += homeDir.endsWith("/") ? DEF_HOMEDIR : "/"+DEF_HOMEDIR;
            
            LOG.finest("Checking if "+homeDir+" exists, if not create it");
            File dir = new File(homeDir);

            if ( dir.exists() ) {
                if ( !dir.isDirectory() ) {
                    throw new IllegalArgumentException("Roosster home directory "+
                                                       homeDir+" is not a directory");
                }
            } else {
                LOG.info("Creating Roosster home directory at "+homeDir);
                dir.mkdir();
                
                if ( !dir.exists() && !dir.isDirectory() )
                    throw new IllegalStateException("Roosster home dir "+homeDir
                                                   +" doesn't exist and can't be created");
            }
        
        }

        return homeDir;
    }

    
}
