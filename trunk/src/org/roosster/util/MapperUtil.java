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

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
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
 * @version $Id: MapperUtil.java,v 1.1 2004/12/03 14:30:16 firstbman Exp $
 */
public class MapperUtil
{
    private static Logger LOG = Logger.getLogger(MapperUtil.class.getName());

    public static final String PROP_FILE_ARG  = "conf";

    public static final String PROP_FILE        = "/roosster.properties";
    public static final String DEFAULT_LOG_CONF = "/default_logging.properties";
    public static final String VERBOSE_LOG_CONF = "/verbose_logging.properties";
    public static final String DEBUG_LOG_CONF   = "/debug_logging.properties";


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
     *
     */
    public static Properties loadProperties(Map cmdLine)
                               throws IOException, IllegalArgumentException
    {
        InputStream propInput = MapperUtil.class.getResourceAsStream(PROP_FILE);

        if ( propInput == null )
            throw new IllegalArgumentException("Can't load default properties file");

        Properties props = new Properties();
        props.load(propInput);

        // try to load user defines properties
        String propFileName =  (String) cmdLine.get(PROP_FILE_ARG);

        if ( propFileName != null ) {
            File propFile = new File(propFileName);

            if ( propFile.canRead() ) {
                LOG.fine("Overriding default setting with user defined settings");
                propInput = new FileInputStream(propFile);
                props.load(propInput);
            } else {
                LOG.warning("Can't read from user specified properties file "+propFileName);
            }
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
}
