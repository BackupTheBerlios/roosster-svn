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

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import thinlet.Thinlet;
import thinlet.FrameLauncher;

import org.apache.log4j.Logger;
import org.roosster.Output;
import org.roosster.Constants;
import org.roosster.Dispatcher;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.logging.LogUtil;
import org.roosster.util.MapperUtil;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Roosster extends Thinlet
{
    private static Logger LOG;
    
    private static final String PROP_FILE       = "/roosster.properties";
    private static final String GUI_DEFINITON   = "/thinlet.xml";

    private Registry registry = null;

    /**
     * 
     */
    public Roosster(Registry registry) throws java.io.IOException 
    {
        this.registry = registry;
        add( parse( getClass().getResourceAsStream(GUI_DEFINITON) ) );
    }    
    
    
    /**
     *
     */
    public static void main(String[] arguments)
    {
        try {
            Map cmdLine = MapperUtil.parseCommandLineArguments(arguments);

            LogUtil.configureLogging(cmdLine);
            LOG = Logger.getLogger(Roosster.class);
        
            new FrameLauncher("Thinlet", 
                              new Roosster(
                                  new Registry(Roosster.class.getResourceAsStream(PROP_FILE), cmdLine)
                              ), 
                              640, 480);            
          
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }

    // ============ GUI event methods ============
    
        
    public void search(String query, Object result) throws Exception 
    {
        try {
          
            Map args = new HashMap();
            args.put("query", query);
            
            Output output = new Dispatcher(registry).run("search", "text", args);
            
            setString(result, "text", String.valueOf(output.entriesSize()));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    

}
