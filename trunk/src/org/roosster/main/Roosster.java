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

import java.util.Map;
import java.util.HashMap;

import org.roosster.util.LogUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Roosster implements Runnable
{
  
    private Map              cmdLine  = null;
    
    
    /**
     * 
     */
    public Roosster(Map args) 
    {
        this.cmdLine = args;
    }
    
    
    /**
     *
     */
    public static void main(String[] arguments)
    {
        try {
            final Map cmdLine = Roosster.parseCommandLineArguments(arguments);

            LogUtil.configureLogging(cmdLine);
           
            Roosster roosster = new Roosster(cmdLine).init();
            
            Runtime.getRuntime().addShutdownHook( new Thread(roosster) );
            
            roosster.start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }  
    
    
    /**
     * 
     */
    public Roosster init() throws Exception
    {
        /*
        if ( cmdLine.containsKey(Constants.PROP_LOCALE) ) 
            Locale.setDefault( new Locale((String) cmdLine.get(Constants.PROP_LOCALE)) );
        */
        
        // to allow easy call chaining
        return this;
    }
    
    
    /**
     */
    public void start() throws Exception
    {
    }
  

    /**
     */
    public void stop() throws Exception
    {
    }
  

    /**
     * Implemented to use this class as shutdown hook for Runtime. Simply calls
     * {@link #stop() stop()}.
     */
    public void run()
    {
        try {
            this.stop();
        } catch (Exception ex) {
            System.out.println("ERROR while shutting down application!");
            ex.printStackTrace();
        }
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
            } else if ( lastArg != null && "".equals( retMap.get(lastArg) ) ) {
                retMap.put(lastArg, arg);
            }
        }

        return retMap;
    }

}
