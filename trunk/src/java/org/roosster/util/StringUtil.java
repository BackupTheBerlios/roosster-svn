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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

import org.roosster.Registry;


/**
 * Class that provides basic String functions
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class StringUtil
{
    private static Logger LOG = Logger.getLogger(StringUtil.class.getName());


    /** if the value of this property is set to a value below zero, no output is truncated.
     * Value is: <pre>output.truncate.length</pre>
     */
    public static final String PROP_TRUNCLENGTH = "output.truncate.length";

    private Registry registry       = null;
    private int      truncateLength = -1;
    
    
    /**
     * 
     */
    public StringUtil(Registry registry)
    {
        if ( registry == null )
            throw new IllegalArgumentException("Argument 'registry' cannot be null");
        
        this.registry = registry;
        String truncLength = registry.getConfiguration().getProperty(PROP_TRUNCLENGTH, "-1");
        truncateLength = Integer.valueOf(truncLength).intValue();
    }
    
    
    /**
     * It truncates the specified string to a length specified in the property
     * {@link PROP_TRUNCLENGTH}. This method is thread safe. 
     */
    public String truncate(String str)
    {
        return StringUtil.truncate(str, truncateLength);
    }
    

    /**
     * @return null if an exception occurred
     */
    public static String[] splitString(String str, String regex)
    {
        try {
            if ( str != null && regex != null ) {
                String[] strings = str.split(regex);
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = strings[i].trim();
                }
                return strings;
            }
        } catch (PatternSyntaxException ex) {
            LOG.log(Level.WARNING, "Exception while splitting string", ex);
        }
        
        return null;
    }
    
    
    /**
     * 
     */
    public static String joinStrings(String[] strings, String joinStr)
    {
        if ( strings == null )
            return null;
        else if ( strings.length < 1 || joinStr == null )
            return "";
        
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < strings.length; i++) {
            sb.append(strings[i].trim());
            if ( i+1 < strings.length )
                sb.append(joinStr);
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 
     */
    public static String leftPad(String s, int length, char c){
        int needed = length - s.length();
        if (needed <= 0) 
            return s;
        
        StringBuffer sb = new StringBuffer(length);
        sb.append(s);
        for (int i = 0; i < needed; i++) {
            sb.append(c);
        }
        
        return sb.toString();
    }
    
    
    /**
     * Truncate a String to a specified length
     *
     * @param length the string length, the provided String should be
     * truncated to. If this is below zero, the String isn't truncated
     * at all, and returned without any modification
     * @param str the String that is to be truncated. If this is
     * <code>null</code>, no operation is performed
     * @return the truncated String, or <code>null</code> if the
     * provided String was <code>null</code>
     */
    public static String truncate(String str, int length)
    {
        if ( str == null || length < 0 )
            return str;
        else
            return str.length() > length+1 ? str.substring(0, length) : str;
    }
    
    
    /**
     */
    public String join(String[] str, String joinStr)
    {
        return StringUtil.joinStrings(str, joinStr);
    }
    
    
    /**
     */
    public String[] split(String str, String regex)
    {
        return StringUtil.splitString(str, regex);
    }
    
    
    /**
     */
    public String pad(String str, int length, char c)
    {
        return StringUtil.leftPad(str, length, c);
    }
}
