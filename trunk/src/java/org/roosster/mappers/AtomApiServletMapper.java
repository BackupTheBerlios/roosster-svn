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

import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration; 
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.roosster.util.MapperUtil;
import org.roosster.InitializeException;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.Dispatcher;
import org.roosster.store.EntryStore;
import org.roosster.commands.CommandNotFoundException;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AtomApiServletMapper extends ServletMapper
{
    private static Logger LOG = Logger.getLogger(AtomApiServletMapper.class.getName());

    private static final String ENTRY_CMD     = "entry";
    private static final String PUTENTRY_CMD  = "putentry";
    private static final String SEARCH_CMD    = "search";
    private static final String ADD_CMD       = "addurl";
    private static final String DEL_CMD       = "del";
    
    
    /**
     *
     */
    protected String getCommandName(int method, HttpServletRequest req)
    {
        String commandName = SEARCH_CMD;
        
        switch (method) {
            case GET:
                commandName = super.getCommandName(method, req);
                System.out.println(commandName);
                if ( !ENTRY_CMD.equals(commandName) && !SEARCH_CMD.equals(commandName) )
                    commandName = null;
                
                break;
            case PUT:
                commandName = PUTENTRY_CMD;
                break;
            case POST:
                commandName = ADD_CMD;
                break;
            case DELETE:
                commandName = DEL_CMD;
                break;
        }
        
        return commandName;
    }


    /**
     * @ex
     */
    //protected Map parseRequestArguments(HttpServletRequest req)
    //{
    //}
    
    
    /**
     * @return always <code>atom</code>
     */
    protected String getOutputMode(Map args)
    {
        return "atom";

    }

}

