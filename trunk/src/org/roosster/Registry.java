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

import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.sql.Connection;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <b>NOTE:</b> This class must always be thread-safe
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: Registry.java,v 1.1 2004/12/03 14:30:15 firstbman Exp $
 */
public class Registry
{
    private static Logger LOG = Logger.getLogger(Registry.class.getName());

    private Map plugins = new Hashtable();

    /** Denotes actions that can be performed on plugins
     */
    public static final int PLUGIN_INIT     = 1;
    public static final int PLUGIN_SHUTDOWN = 2;

    public static final String PROP_PLUGIN = "plugins";

    /**
     */
    private Configuration conf       = null;

    /** This field should only be set in the constructor and
      * shutdownPlugins
     */
    private boolean initialized = false;

    /**
     *
     */
    public Registry(Properties properties) throws InitializeException, IllegalArgumentException
    {
        if ( properties == null )
            throw new IllegalArgumentException("Can't initialize Registry with 'null' properties");

        conf = new Configuration(properties);

        try {
            callPlugins(PLUGIN_INIT);
            initialized = true;
        } catch (Exception ex) {
            throw new InitializeException("Exception while initializing plugins", ex);
        }
    }


    /**
     *
     */
    public Configuration getConfiguration()
    {
        return conf;
    }


    /**
     * @return null if there is no plugin with this name
     */
    public Plugin getPlugin(String name)
    {
        if ( name == null ) 
            throw new IllegalArgumentException("Can't get Plugin with 'null' name");
            
        Plugin plugin = (Plugin) plugins.get(name);
        if ( plugin == null )
            throw new IllegalStateException("Plugin "+name+" not found"); 

        return plugin;
    }


    /**
     *
     */
    public void shutdown() throws Exception
    {
        if ( initialized ) {
            LOG.info("Shutting down NOW!");
            callPlugins(PLUGIN_SHUTDOWN);
            initialized = false;
        }
    }


    // ============ private Helper methods ============

    /**
     *
     */
    private void callPlugins(int command) throws Exception
    {
        switch (command)
        {
            case 1: //init
                initPlugins();
                break;

            case 2: // shutdown
                shutdownPlugins();
                break;

            default:
                throw new IllegalArgumentException("Illegal plugin command "+command);
        }
    }


    /**
     *
     */
    private void initPlugins() throws Exception
    {
        String pluginNames = conf.getProperty(PROP_PLUGIN);

        if ( pluginNames != null ) {

            StringTokenizer tok = new StringTokenizer(pluginNames.trim(), " ");
            while ( tok.hasMoreTokens() ) {
                String pluginName  = tok.nextToken();
                String pluginClass = conf.getProperty(pluginName+".class");

                if ( pluginClass == null ) {
                    LOG.warning("No Class property for plugin "+pluginName+
                                "! Check Property "+pluginName+".class");
                    continue;
                }

                try {

                    Plugin plugin = (Plugin) Class.forName(pluginClass).newInstance();
                    plugin.init(this);
                    plugins.put(pluginName, plugin);

                } catch (ClassCastException ex) {

                    LOG.log(Level.WARNING, "Plugin "+pluginName+" does not implement the "+
                              Plugin.class+" interface", ex);
                }
            }

        }
    }


    /**
     *
     */
    private void shutdownPlugins() throws Exception
    {
        Iterator values = plugins.values().iterator();
        while ( values.hasNext() ) {
            Plugin plugin = null;
            try {
                plugin = (Plugin) values.next();
                plugin.shutdown(this);
                values.remove();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error while shutting down plugin "+plugin, ex);
            }

        }
    }




}
