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
package org.roosster.input;

import java.io.InputStream;
import java.net.URL;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Resource 
{
    public static final int DEFAULT_RESPONSE_CODE = -1;
    
    private URL         url                 = null;
    private InputStream stream              = null;
    private long        lastModified        = 0;
    private String      contentType         = null;
    private String      contentEncoding     = null;
    
    /** -1 i default and means "not defined"
     */
    private int         responseCode        = DEFAULT_RESPONSE_CODE;
    
    
    /**
     * 
     * @param url not allowed to be null
     */
    public Resource(URL url, InputStream stream, String contentType, 
                    String contentEncoding, long lastModified, int responseCode)
    {
        if ( url == null )
            throw new IllegalArgumentException("Parameter url is not allowed to be null!");
        
        this.url = url;
        this.stream = stream;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.lastModified = lastModified;
        this.responseCode = responseCode;
    }
    
    
    /**
     * @return Returns the responseCode.
     */
    public int getResponseCode() 
    {
        return responseCode;
    }


    /**
     * @return Returns the contentEncoding.
     */
    public String getContentEncoding() 
    {
        return contentEncoding;
    }

    
    /**
     * @return Returns the contentType.
     */
    public String getContentType() 
    {
        return contentType;
    }
    
    
    /**
     * @return Returns the lastModified.
     */
    public long getLastModified() 
    {
        return lastModified;
    }
    
    
    /**
     * @return Returns the stream.
     */
    public InputStream getStream()
    {
        return stream;
    }
    
    
    /**
     * @return Returns the url.
     */
    public URL getUrl() 
    {
        return url;
    }
    
}