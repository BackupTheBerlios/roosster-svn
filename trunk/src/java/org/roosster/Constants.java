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
    public static final String ENTRY_DATE_FORMAT_SHORT = "yyyyMMddHHmm";
    public static final String ENTRY_DATE_FORMAT_LONG  = "yyyyMMdd";
    
    public static final String TAG_SEPARATOR    = ",";
  
    public static final String W3C_DATEFORMAT   = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static final String APP_URI          = "http://www.roosster.org";
    public static final String APP_NAME         = "roosster search";
    
    // keys for objects that are stored in ServletContext
    public static final String CTX_REGISTRY     = "servletcontext.param.registry";
    public static final String CTX_DISPATCHER   = "servletcontext.param.dispatcher";

    // for internal use only
    public static final String PARAM_ENTRIES    = "internal.param.entry";
    
    // props can be specified in properties or as request parameter
    public static final String PROP_LIMIT       = "output.limit";
    public static final String PROP_OFFSET      = "output.offset";
    public static final String PROP_SORTFIELD   = "output.sortfield";
    public static final String PROP_OUTPUTMODE  = "output.mode";
    
    // Properties specific for SyncDeliciousCommand
    public static final String PROP_DELICIOUS_USER       = "delicious.username";
    public static final String PROP_DELICIOUS_PASS       = "delicious.password";
    public static final String PROP_DELICIOUS_LASTUPDATE = "delicious.lastupdate";
    
    public static final String PROP_APPVERSION  = "roosster.app.version";

    /** if the value of this property is set to a value below zero, no output is truncated.
     */
    public static final String PROP_TRUNCLENGTH = "output.truncate.length";


    // args are used in commands, and only an a request basis
    public static final String ARG_URL          = "url";
    public static final String ARG_ISSUED       = "issued";
    public static final String ARG_MODIFIED     = "modified";
    public static final String ARG_FORCE        = "force";
    public static final String ARG_NOTE         = "note";
    public static final String ARG_TAGS         = "tags";
    public static final String ARG_TITLE        = "title";;    
    public static final String ARG_AUTHOR       = "author";;    
    public static final String ARG_AUTHOREMAIL  = "email";;    
    public static final String ARG_FILE         = "file";
    
    
}
