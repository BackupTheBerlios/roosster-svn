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
package org.roosster.commands;

import java.util.Map;

import org.mortbay.jetty.Server;

import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.mappers.ServletMapper;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class JettyShutdownCommand extends AbstractCommand implements Command
{
    private static final String ARG_CONFIRM = "confirm.shutdown"; 
    private static final String TMPL        = "shutdown.html"; 
    
    /**
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        String confArg = (String) arguments.get(ARG_CONFIRM);
        
        if ( "true".equalsIgnoreCase(confArg) || "1".equals(confArg) ) {
            LOG.warning("The server is shutting down NOW !!!");
          
            Server server = (Server) registry.getProperty(ServletMapper.RT_PROP_SERVER);
            server.stop(true); // graceful shutdown
            
            output.setTemplateName(TMPL);
        } else {
            output.setTemplateName(TMPL);
        }
    }


    /**
     */
    public String getName()
    {
        return "Shutdown Webserver";
    }
}

