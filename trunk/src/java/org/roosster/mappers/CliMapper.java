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

import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.roosster.OperationException;
import org.roosster.InitializeException;
import org.roosster.Dispatcher;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.Output;
import org.roosster.output.OutputMode;
import org.roosster.util.MapperUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class CliMapper
{
    private static Logger LOG = Logger.getLogger(CliMapper.class.getName());
    
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
            if ( LOG.isLoggable(Level.CONFIG) )
                ex.printStackTrace();
            else
                System.out.println("ERROR:  "+ ex.getMessage());
        
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

        MapperUtil.initLogging(arguments);

        String commandName = arguments[0] ;

        // remove command name from args list
        List args = Arrays.asList(arguments);
        arguments = (String[]) args.subList(1, args.size()).toArray(new String[0]);

        Map cmdLine = MapperUtil.parseCommandLineArguments(arguments);

        InputStream propInput = getClass().getResourceAsStream(PROP_FILE);
        Registry registry = new Registry( MapperUtil.loadProperties(propInput, cmdLine) );

        String outputMode = registry.getConfiguration().getProperty(MapperUtil.ARG_OUTPUTMODE, 
                                                                    DEF_OUTPUT_MODE);
        
        new Dispatcher(registry).run(commandName, cmdLine).output(outputMode, outputStream);
    }


    // ============ private Helper methods ============

    
    /**
     *
     */
    public static void printUsage()
    {
        System.out.println("USAGE: <to be defined>");
    }

}
