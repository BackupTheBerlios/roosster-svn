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
package org.roosster.security;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import org.roosster.Output;
import org.roosster.Plugin;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.util.StringUtil;
import org.roosster.logging.VelocityLogSystem;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AuthenticationPlugin implements Plugin
{
    private static Logger LOG = Logger.getLogger(AuthenticationPlugin.class.getName());

    private static final String PROP_PASSWORD     = "authentication.password";
    private static final String PROP_USERNAME     = "authentication.username";
    
    private Registry  registry    = null;
    private boolean   initialized = false;
    
    private String username = null;
    private String password = null;

    
    /**
     */
    public void init(Registry registry) throws InitializeException
    {
        LOG.finest("Initializing "+getClass());

        this.registry = registry;
        
        password = registry.getConfiguration().getProperty(PROP_PASSWORD, "");
        username = registry.getConfiguration().getProperty(PROP_USERNAME, "");
        
        initialized = true;
    }

        
    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
        initialized = false;
    }


    /**
     *
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    
    /**
     */
    public void preProcess(Map requestArgs) throws OperationException
    {
        // if a username is set, we'll check it against the one provided in the request
        if ( !"".equals(username) ) {
              String reqPwd = (String) requestArgs.get(PROP_PASSWORD);
              String reqUsr = (String) requestArgs.get(PROP_USERNAME);
              
              if ( username.equals(reqUsr) && password.equals(reqPwd) ) {
                  LOG.config("Authenticated user: "+username);
              } else {
                  throw new AuthenticationException(reqUsr);
              }
        } 
    }

    
    /**
     */
    public void postProcess(Map requestArgs, Output output) throws OperationException
    {
    }

}
