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
package org.roosster.util;

import java.util.Map;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class LogUtil 
{
    public static final String DEBUG_LOGGING_PROPS   = "/debug_log4j.properties";
    public static final String VERBOSE_LOGGING_PROPS = "/verbose_log4j.properties";
    public static final String DEFAULT_LOGGING_PROPS = "/default_log4j.properties";
    
    public static final String DEBUG_LOGGING   = "d";
    public static final String VERBOSE_LOGGING = "v";
    
    
    /**
     * 
     */
    public static void configureLogging(Map cmdLine) throws java.io.IOException
    {
        String propFile = DEFAULT_LOGGING_PROPS;
      
        if ( cmdLine.containsKey(DEBUG_LOGGING) )
            propFile = DEBUG_LOGGING_PROPS;
        else if ( cmdLine.containsKey(VERBOSE_LOGGING) )
            propFile = VERBOSE_LOGGING_PROPS;
      
        Properties props = new Properties();
        props.load( LogUtil.class.getResourceAsStream(propFile) );
        PropertyConfigurator.configure(props);
    }
    
}
