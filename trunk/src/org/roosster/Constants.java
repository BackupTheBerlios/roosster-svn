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
package org.roosster;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public interface Constants
{
  
    /**
     */
    public static final String PROP_LOCALE = "locale";
  
    /**
     */
    public static final String DEFAULT_INPUT_ENCODING = "UTF-8";
    
    /**
     */
    public static final String PLUGIN_STORE      = "store";
    
    /**
     */
    public static final String PLUGIN_FETCHER = "fetcher";
    
    /**
     */
    public static final String APP_URI      = "http://www.roosster.org";
    
    /**
     */
    public static final String APP_NAME     = "roosster search";

    /**
     */
    public static final String APP_VERSION  = "roosster.app.version";

    /**
     */
    public static final String ENTRY_DATE_FORMAT_SHORT = "yyyyMMdd";

    /**
     */
    public static final String ENTRY_DATE_FORMAT_LONG  = "yyyyMMddHHmmZ";
        
    /** TODO make this configurable
     */
    public static final String DISPLAY_DATE_FORMAT  = "dd/MM/yyyy HH:mm";
    
    /**
     */
    public static final String W3C_DATEFORMAT   = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     */
    public static final String TAG_SEPARATOR    = ",";
    
    /**
     */
    public static final String OUTPUTMSG_LEVEL_INFO  = "info";

    /**
     */
    public static final String OUTPUTMSG_LEVEL_ERROR = "error";
    
    /** for internal use only
     */
    public static final String PARAM_ENTRIES    = "internal.param.entry";
    
    /** Command-Line switches only
     */
    public static final String DEBUG_LOGGING    = "d";
    public static final String VERBOSE_LOGGING  = "v";

    /**
     */
    public static final String LAST_UPDATE      = "roosster.last.update";
    
    /**
     */
    public static final String PROP_LIMIT       = "output.limit";

    /**
     */
    public static final String PROP_OFFSET      = "output.offset";

    /**
     */
    public static final String PROP_SORTFIELD   = "output.sortfield";

    /**
     */
    public static final String PROP_TIMEZONE    = "date.timezone";

    /**
     */
    public static final String PROP_OUTPUTMODE  = "output.mode";
    
    /** if the value of this property is set to a value below zero, no output is truncated.
     */
    public static final String PROP_TRUNCLENGTH = "output.truncate.length";

    /**
     */
    public static final String PROP_DEF_ENC    = "default.input.encoding";

    /**
     */
    public static final String PROP_PROCESSORS = "fetcher.processors";

    /**
     */
    public static final String PROP_INPUT_PROC = "input.processor";

    /**
     */
    public static final String PROP_FETCH_CONTENT = "fetch.content";
    
    public static final String PROP_DELICIOUS_USER       = "delicious.username";
    public static final String PROP_DELICIOUS_PASS       = "delicious.password";
    public static final String PROP_DELICIOUS_LASTSYNC   = "delicious.lastsync";
    
    /** specifies if entries/posts are deleted from either system during sync, true by default 
     */
    public static final String PROP_DELICIOUS_DELETE     = "delicious.sync.delete";
    
    public static final String PROP_DELICIOUS_APIENDPOINT= "delicious.api.endpoint";


    // args are used in commands, and only an a request basis (although they could be
    // defined in property files, this wouldn't make much sense
    public static final String ARG_QUERY        = "query";
    public static final String ARG_JUMPTO       = "jumpto";
    
    public static final String ARG_URL          = "url";
    public static final String ARG_PUBLIC       = "pub";
    public static final String ARG_ISSUED       = "issued";
    public static final String ARG_MODIFIED     = "modified";
    public static final String ARG_FORCE        = "force";
    public static final String ARG_NOTE         = "note";
    public static final String ARG_TAGS         = "tags";
    public static final String ARG_TITLE        = "title";    
    public static final String ARG_AUTHOR       = "author";    
    public static final String ARG_AUTHOREMAIL  = "email";    
    public static final String ARG_FILETYPE     = "type";    
    public static final String ARG_FILE         = "file";
    
    
}
