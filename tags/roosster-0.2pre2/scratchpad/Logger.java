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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.roosster.util.StringUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Logger 
{
    public static final String DEF_DATEFORMAT = "EEE MMM dd yyyy HH:mm:ss.SSS";
  
    private Class   clazz = null;
    private Writer  out   = null;
    private Level   level = null; 
    
    /**
     * 
     */
    protected Logger(Class clazz, Writer out, Level level)
    {
        if ( clazz == null || out == null || level == null )
            throw new IllegalArgumentException("Arguments to 'Logger' are not allowed to be null!");
        
        this.clazz = clazz;
        this.out   = out;
        this.level = level;
    }
  

    /**
     * 
     */
    public void log(Level level, String message)
    {
        
    }

    
    /**
     * 
     */
    public void log(Level level, String message, Throwable exception)
    {
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
