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
package org.roosster.web;

import java.util.Date;
import org.apache.log4j.Logger;
 
import org.roosster.util.StringUtil;
import org.roosster.util.DateUtil;
import org.roosster.Constants;
import org.roosster.Registry;

/**
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class VelocityTemplateUtil 
{
    private static Logger LOG = Logger.getLogger(VelocityTemplateUtil.class);

    private Registry     registry       = null;
    private int          truncate       = -1;

    public VelocityTemplateUtil(Registry registry)
    {
        if ( registry == null )
            throw new IllegalArgumentException("VelocityTemplateHelper-object needs non-null Registry object ");

        this.registry    = registry;    

        String truncStr = registry.getConfiguration()
                                  .getProperty(Constants.PROP_TRUNCLENGTH, "-1");
        truncate = Integer.valueOf(truncStr).intValue();
    }

    
    /**
     * 
     */
    public String truncate(String str)  
    {
        return StringUtil.truncate(str, truncate);
    } 


    /**
     * 
     */
    public String join(String[] str, String separator)  
    {
        return StringUtil.join(str, separator);
    } 


    /**
     * 
     */
    public String searchableDate(Date date)  
    {
        return DateUtil.formatSearchableEntryDate(date);
    }  

    
    /**
     * 
     */
    public String displayDate(Date date)  
    {
        return DateUtil.formatDisplayDate(date);
    }  
}
