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

/**
 * <b>NOTE:</b> This class must always be thread-safe
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: Configuration.java,v 1.1 2004/12/03 14:30:15 firstbman Exp $
 */
public class Configuration
{

    private static ThreadLocal reqArgs = new ThreadLocal();
    
    /**
     */
    private Properties properties = null;


    /**
     *
     */
    public Configuration(Properties properties) throws IllegalArgumentException
    {
        if ( properties == null )
            throw new IllegalArgumentException("Can't create configuration with "+
                                               "'null' configuration");

        this.properties = properties;
    }


    /**
     * @return null if there is no such property
     */
    public String getProperty(String propName)
    {
        return properties.getProperty(propName);
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
        
        Enumeration propNames = properties.propertyNames();
        while ( propNames.hasMoreElements() ) {
            String name = (String) propNames.nextElement();
            if ( prefix == null || "".equals(prefix) || name.startsWith(prefix) )
                names.add(name);
        }
        
        Map args = getRequestArguments();
        if ( args != null ) {
            Iterator keys = args.keySet().iterator();
            while ( keys.hasNext() ) {
                String name = (String) keys.next();
                if ( names.contains(name) )
                    names.remove(name);

                names.add(name);
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
     */
    private Map getRequestArguments()
    {
        return (Map) reqArgs.get();
    }
}
