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
package org.roosster.logging;

import java.io.Writer;

import org.roosster.util.StringUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class LoggerFactory 
{
    /**
     * The Level, that will be used, when init() hasn't been called yet, but a 
     * Logger needs to be created.
     */
    public static final Level DEFAULT_LEVEL = Level.WARNING;
  
    private static Writer loggerOut = null;
    private static Level  level     = null;
    
    /**
     */
    private LoggerFactory()
    {}
    
    
    /**
     * Configures the settings, with which Loggers created in the future will 
     * be initialized.
     *
     * @param out the Writer where Logging output, will be written to, must be 
     * not null
     * @param level the highest level at which messages will be logged, may not 
     * be null
     */
    public static void init(Writer out, Level level) throws InitializeException
    {
        if ( out == null || level == null )
            throw new IllegalArgumentException("No LoggerFactory arguments are allowed to be null!");
      
        loggerOut = out;
        //level = level;
    }
    
    
    /**
     * Configures this Logger to produce <code>Logger</code>-instances, that
     * write to <code>System.out</code>.
     */
    public static void init(Level level) //throws InitializeException
    {
        init(new PrintWriter(System.out, true), level);
    }
    
    
    /**
     * 
     */
    public static Logger getLogger(Class clazz)
    {
        if ( out == null || level == null )
            init(DEFAULT_LEVEL);
      
        if ( clazz == null )
            throw new IllegalArgumentException("A Class-instance is needed to construct a Logger");
        
        return new Logger(clazz, out, level);
    }
  


}


/*
    public String format(LogRecord record)
    {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss.SSS");
      
        String padded = StringUtil.leftPad("  ["+record.getLevel().getName()+"]", 12, ' ');
        
        StringBuffer msg = new StringBuffer( df.format(new Date()) );
        
        msg.append(padded).append(record.getMessage()).append("\n");

        if ( record.getThrown() != null ) {
            StringWriter strWriter = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(strWriter));
            msg.append(strWriter.toString());
        }

        return msg.toString();
    }
 */
