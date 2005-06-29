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
package org.roosster.api;

import java.io.IOException;
import java.io.InputStream;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.apache.log4j.Logger;
import org.apache.commons.io.CopyUtils;

import org.roosster.Registry;

/**
 * This Handler supports only GET requests, and serves files
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ClasspathResourceHandler extends AbstractHttpHandler
{
    private static Logger LOG = Logger.getLogger(ClasspathResourceHandler.class);
    
    private Registry   registry = null;
    private String     basePath = null;    
    
    
    /**
     *
     */
    public ClasspathResourceHandler(Registry registry, String basePath)
    {
        if ( registry == null || basePath == null ) 
            throw new IllegalArgumentException("Parameter 'registry' and/or 'basePath' "+
                                               "are not allowed to be null");

        this.registry = registry;
        this.basePath = basePath.startsWith("/") ? basePath : "/"+basePath;
    }
    
    
    
    /**
     *
     */
    public String getName()
    {
        return getClass().getName();
    }


    /**
     * This handler does not respect:
     * <ul>
     * <li>Ranges</li>
     * <li>GZip encoding</li>
     * <li>if-Modified-Since or even ETag</li>
     * </ul>
     * It simply opens a stream from the classpath, and streams it to the 
     * OutputStream. No other fancy stuff.
     */
    public void handle(String pathInContext, String pathParams, 
                       HttpRequest request, HttpResponse response)
                throws HttpException, IOException
    {
        if ( !HttpRequest.__GET.equals(request.getMethod()) ) {
            response.sendError(HttpResponse.__405_Method_Not_Allowed);
            response.setField(HttpFields.__Allow, "GET");
            return;
        }
        
        String loc = basePath+request.getPath();
        InputStream input = getClass().getResourceAsStream(loc);

        LOG.debug("Found Resource '"+loc+"' in classpath? "+ (input != null ? "YES" : "NO"));
        
        if ( input != null ) {
            int bytesCopied = CopyUtils.copy(input, response.getOutputStream());
            
            if ( bytesCopied > 0 )
                request.setHandled(true);
            else 
                response.setStatus(HttpResponse.__204_No_Content);
        } 
    }
}

