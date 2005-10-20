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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import org.roosster.Constants;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class DateUtil
{
    private static Logger LOG = Logger.getLogger(DateUtil.class);
    

    /**
     * @return a <code>SimpleDateFormat</code> instance initialized with the 
     * {@link org.roosster.Constants#W3C_DATEFORMAT Constants.W3C_DATEFORMAT} 
     * pattern.
     */
    public static DateFormat getDateFormat() 
    {
        return getDateFormat(Constants.W3C_DATEFORMAT);
    }

    
    /**
     * 
     */
    public static DateFormat getDateFormat(String format) 
    {
        return new SimpleDateFormat(format);
    }

    
    /**
     */
    public static String formatW3cDate(Date date) throws ParseException
    {
        if ( date == null )
            return "";
        
        String dateStr = getDateFormat().format(date);
        return dateStr.substring(0, dateStr.length()-2) +":"+ dateStr.substring(dateStr.length()-2);
    }
      
      
    /**
     */
    public static Date parseW3cDate(String dateStr) throws ParseException
    {
        if ( dateStr == null || "".equals(dateStr) )
            return null;
        
        int len = dateStr.length();
        
        if ( ":".equals(dateStr.substring(len-3, len-2)) )
            return getDateFormat().parse(dateStr.substring(0, len-3) + dateStr.substring(len-2));
        else
            return getDateFormat().parse(dateStr);
    }
    
    
    /**
     * 
     */
    public static String formatDisplayDate(Date date)
    {
        return formatDate(date, Constants.DISPLAY_DATE_FORMAT);
    }
    
    
    /**
     * 
     */
    public static String formatPreciseEntryDate(Date date)
    {   
        return formatDate(date == null ? new Date(0) : date, Constants.ENTRY_DATE_FORMAT_LONG);
    }
    
    
    /**
     * 
     */
    public static String formatSearchableEntryDate(Date date)
    {
        return formatDate(date == null ? new Date(0) : date, Constants.ENTRY_DATE_FORMAT_SHORT);
    }
    
    
    /**
     * 
     */
    public static Date parseEntryDate(String dateStr)
    {
        Date date = parseDate(dateStr, Constants.ENTRY_DATE_FORMAT_SHORT);
        if ( date == null )
            date = parseDate(dateStr, Constants.ENTRY_DATE_FORMAT_LONG);
        
        return date;
    }

    
    /**
     * 
     */
    public static Date parseSearchableEntryDate(String dateStr)
    {
        return parseDate("".equals(dateStr) || dateStr == null ? "19700101" : dateStr, 
                         Constants.ENTRY_DATE_FORMAT_SHORT);
    }

    
    /**
     * 
     */
    public static Date parsePreciseEntryDate(String dateStr)
    {
        return parseDate("".equals(dateStr) || dateStr == null ? "197001010000-0000" : dateStr, 
                         Constants.ENTRY_DATE_FORMAT_LONG);
    }

    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private static String formatDate(Date date, String dateFormat)
    {
        if ( dateFormat == null )
            throw new IllegalArgumentException("Parameter 'dateFormat' is not allowed to be null");
      
        if ( date == null )
            return "";
        
        try {
            return getDateFormat(dateFormat).format(date);
        } catch (Exception ex) {
            LOG.warn("Exception while converting Date to String", ex);
            return "";
        }      
    }    
    
    
    /**
     * 
     */
    private static Date parseDate(String dateStr, String dateFormat)
    {
        if ( dateFormat == null )
            throw new IllegalArgumentException("Parameter 'dateFormat' is not allowed to be null");
      
        if ( dateStr == null || "".equals(dateStr) )
            return null;
        
        try {
            return getDateFormat(dateFormat).parse(dateStr);
        } catch(ParseException ex) {
            LOG.warn("Exception while converting String to Date", ex);
            return null;
        }   
    }
}
