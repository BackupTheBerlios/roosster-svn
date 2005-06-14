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
package org.roosster.mappers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
public class CliMapper
{
    private static Logger LOG;
    
    private static final String PROP_FILE       = "/roosster-cli.properties";

    public static final String ARG_MODE         = "output.mode";
    public static final String DEF_OUTPUT_MODE  = "text";

    /**
     *
     */
    public static void main(String[] args)
    {
        try {
              new CliMapper().run(args, System.out);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.flush();
        }
    }


    /**
     *
     */
    public void run(String[] arguments, PrintStream outputStream)
                    throws IOException, OperationException,
                           IllegalArgumentException, InitializeException
    {
        if ( arguments.length == 0 ) {
            printUsage();
            return;
        }

        String commandName = arguments[0] ;

        // remove command name from args list
        List args = Arrays.asList(arguments);
        arguments = (String[]) args.subList(1, args.size()).toArray(new String[0]);

        // parse command line
        Map cmdLine = MapperUtil.parseCommandLineArguments(arguments);

        // configure and initialize Logging
        LogUtil.configureLogging( (String) cmdLine.get(Constants.CLI_LOGGING) );
        LOG = Logger.getLogger(CliMapper.class);
        
        // now on with the action
        Registry registry = new Registry(getClass().getResourceAsStream(PROP_FILE), cmdLine);

        registry.getConfiguration().setRequestArguments(cmdLine);
        
        String outputMode = registry.getConfiguration().getProperty(Constants.PROP_OUTPUTMODE, 
                                                                    DEF_OUTPUT_MODE);
        
        new Dispatcher(registry).run(commandName, outputMode, cmdLine).output(outputStream);
    }


    /**
     *
     */
    public static void printUsage()
    {
        System.out.println("USAGE: <to be defined>");
    }


    // ============ private Helper methods ============

    
}
