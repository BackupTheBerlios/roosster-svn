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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.log4j.Logger;

import org.roosster.commands.CommandNotFoundException;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Dispatcher
{
    private static Logger LOG = Logger.getLogger(Dispatcher.class.getName());

    /**
     */
    private Registry registry  = null;

    /**
     */
    private Map commandChains = new HashMap();

    /**
     */
    private Map commands = new HashMap();

    
    /**
     *
     */
    public Dispatcher(Registry registry) throws InitializeException
    {
        if ( registry == null )
            throw new IllegalArgumentException("Can't create dispatcher with "+
                                               "'null' registry");

        this.registry = registry;
        loadCommands(registry.getConfiguration());
    }


    /**
     */
    public Output run(String commandName, String outputMode, Map reqArgs) 
               throws IOException, IllegalStateException,
                      IllegalArgumentException, OperationException
    {
        Configuration conf = registry.getConfiguration();

        try {
            conf.setRequestArguments(reqArgs);

            // command chains have precedence over normal command definitions
            List classes = (List) commandChains.get(commandName);
            if ( classes == null )
                classes = (List) commands.get(commandName);
            
            if ( classes == null || classes.size() < 1 )
                throw new CommandNotFoundException(commandName);

            Output output = new Output(registry, outputMode);
            
            for(int i = 0; i < classes.size(); i++) {
                Command command = (Command) classes.get(i);
                LOG.debug("Executing Command: "+ command);
                
                command.execute(reqArgs, registry, output);
            }

            return output;

        } catch (Exception ex) {
            throw new OperationException(ex);
        }
    }


    // ============ private Helper methods ============


    /**
     * 
     */
    private void loadCommands(Configuration conf) throws InitializeException
    {
        commands.clear();
        
        String[] names = conf.getPropertyNames("command.");
        
        for (int i = 0; i < names.length; i++) {
            boolean isChain = false;
            
            List tmp = new ArrayList();
            String commandName = null;
            try  {
                
                //LOG.debug("Trying to load command "+names[i]);
                        
                if ( names[i].endsWith(".chain") ) {
                    isChain = true;

                    String value = conf.getProperty(names[i]);
                    if ( value == null )
                        throw new IllegalArgumentException("No chain value for chain: "+names[i]);
                        
                    StringTokenizer tok = new StringTokenizer(value, " ");
                    while ( tok.hasMoreTokens() ) {
                        String name  = tok.nextToken();
                        //LOG.debug("Trying to load class for chain command "+name);
                      
                        String className = conf.getProperty("command."+name+".class");
                        if ( className != null )
                            tmp.add( Class.forName(className).newInstance() );
                        else
                            LOG.fatal("No class found for command '"+ name +"' in chain: "+commandName);
                    }

                } else {
                    String className = conf.getProperty(names[i]);
                    if ( className != null ) {
                        tmp.add( (Command) Class.forName(className).newInstance() );
                    }
                }
                
            } catch (Exception ex) {
                throw new InitializeException(ex);
            }
            
            if ( tmp.size() < 1 )
                throw new InitializeException("No class property found for "+ commandName);
            
            int firstPoint = names[i].indexOf("."); 
            int lastPoint  = names[i].lastIndexOf("."); 

            commandName = names[i].substring(firstPoint+1, lastPoint);
           
            if ( isChain ) 
                commandChains.put(commandName, tmp);
            else
                commands.put(commandName, tmp);
        }

    }



}
