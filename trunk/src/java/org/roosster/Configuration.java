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
package org.roosster;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.log4j.Logger;

import org.roosster.InitializeException;

/**
 * <b>NOTE:</b> This class must always be thread-safe
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Configuration
{
    private static Logger LOG = Logger.getLogger(Configuration.class);
    
    private static ThreadLocal reqArgs = new ThreadLocal();
    
    public static final String PROP_FILE_ARG     = "conf";
  

    /**
     */
    private Properties properties = null;
    
    private String standardPropFileName = null;


    /**
     *
     */
    public Configuration(InputStream stream, Map commandLine) 
                  throws IllegalArgumentException, InitializeException
    {
        if ( stream == null )
            throw new IllegalArgumentException("Can't create configuration with "+
                                               "'null' configuration stream");

        try {          
            standardPropFileName = getHomeDir() + File.separator + "roosster.properties";
            this.properties = loadProperties(stream, commandLine);
        } catch (IOException ex) {
            throw new InitializeException("Exception while loading configuration", ex);
        }
    }


    /**
     * @return null if there is no such property
     */
    public String getProperty(String propName)
    {
        String returnStr = null;

        if ( propName != null ) {

            Map args = getRequestArguments();
            if ( args != null )
                returnStr = (String) args.get(propName);

            if ( returnStr == null )
                returnStr = properties.getProperty(propName);
        }

        return returnStr;
    }


    /**
     * @return
     */
    public Map getProperties()
    {
        Map map = new HashMap(properties);
        
        Map args = getRequestArguments();
        if ( args != null )
            map.putAll(args);
        
        return map;
    }


    /**
     * @return
     */
    public String getProperty(String propName, String defaultValue)
    {
        String returnStr = null;

        if ( propName != null ) {

            Map args = getRequestArguments();
            if ( args != null )
                returnStr = (String) args.get(propName);

            if ( returnStr == null )
                returnStr = properties.getProperty(propName, defaultValue);
        }

        return returnStr == null ? defaultValue : returnStr;
    }


    /**
     *
     */
    public String[] getPropertyNames(String prefix)
    {
        Set names = new HashSet();

        if ( prefix != null && !"".equals(prefix) )  {
            
            Enumeration propNames = properties.propertyNames();
            while ( propNames.hasMoreElements() ) {
                String name = (String) propNames.nextElement();
                if ( name.startsWith(prefix) )
                    names.add(name);
            }

            Map args = getRequestArguments();
            if ( args != null ) {
                Iterator keys = args.keySet().iterator();
                while ( keys.hasNext() ) {
                    String name = (String) keys.next();
                    if ( name.startsWith(prefix) ) {
                        names.remove(name);
                        names.add(name);
                    }
                }
            }

        }

        return (String[]) names.toArray(new String[0]);
    }


    /**
     *
     */
    public boolean containsProperty(String propName)
    {
        if ( propName == null )
            return false;
        else {

            Map args = getRequestArguments();
            boolean reqContainsKey = false;
            if ( args != null )
                reqContainsKey = args.containsKey(propName);

            return reqContainsKey ? true : properties.containsKey(propName);
        }
    }

    /**
     */
    public void setRequestArguments(Map args)
    {
        reqArgs.set(args);
    }


    /**
     * @return may be null, if {@link #setRequestArguments(Map)} hasn't been called 
     * during this thread'S execution yet.
     */
    public Map getRequestArguments()
    {
        return (Map) reqArgs.get();
    }


    /**
     *
     */
    public String getHomeDir()
    {
        return org.roosster.util.MapperUtil.getHomeDir();
    }

    
    /**
     * @param propNames array of property names which should be persisted, 
     * if this is null or empty, the method returns without executing any action
     */
    public synchronized void persist(String[] propNames) throws IOException
    {
        if ( propNames == null || propNames.length < 1 )
            return;
      
        Properties props = new Properties();

        File file = new File(standardPropFileName);
        if ( file.exists()  ) {
            // keep old persisted properties
            props.load(new FileInputStream(file));
        }

        // overwrite with new ones
        for(int i = 0; i < propNames.length; i++) {
            String tmp = getProperty(propNames[i]);
            if ( tmp != null ) {
                props.put(propNames[i], tmp);
                
                // make sure, the current properties are updated, if it was a request arg
                properties.put(propNames[i], tmp);
            }
        }

        // not what you'd call a transaction, I know, but works for now
        FileOutputStream outFile = new FileOutputStream(file, false);
        props.store(outFile, "Written programmatically by roosster at");
        outFile.flush();
        outFile.close();
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     */
    public Properties loadProperties(InputStream propInput, Map cmdLine)
                              throws IOException, IllegalArgumentException
    {
        if ( propInput == null )
            throw new IllegalArgumentException("Properties inputStream is not allowed to be null");

        Properties props = new Properties();
        props.load(propInput);      

        // try to load user defines properties
        String propFileName =  (String) cmdLine.get(PROP_FILE_ARG);

        if ( propFileName == null ) 
            propFileName = standardPropFileName;
        
        File propFile = new File(propFileName);

        if ( propFile.exists() && propFile.canRead() ) {
            System.out.println("Overriding default setting with user defined settings");
            propInput = new FileInputStream(propFile);
            props.load(propInput);
        } else {
            System.out.println("Can't use secondary configuration file: "+propFileName);
        }

        // now override with commandline parameters
        //props.putAll(cmdLine);
        setRequestArguments(cmdLine);

        if ( cmdLine.containsKey("-d") ) {
            Iterator keys = props.keySet().iterator();
            while ( keys.hasNext() ) {
                Object key = keys.next();
                System.out.println(key +" => "+ props.get(key));
            }

        }

        return props;
    }    
    
}
