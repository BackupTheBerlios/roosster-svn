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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.roosster.util.StringUtil;

/**
 * Simple Formatter which returns a log message always in the format:<br/>
 * <pre>&lt;TIME&gt;  [&lt;LOG_LEVEL&gt;] &lt;LOG_MESSAGE&gt;</pre>
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ConsoleFormatter extends Formatter
{

    /**
     *
     */
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

}
