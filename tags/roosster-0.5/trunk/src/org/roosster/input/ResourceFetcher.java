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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.roosster.Constants;

public class ResourceFetcher 
{
    private static Logger LOG = Logger.getLogger(ResourceFetcher.class);
    
    // code largely "inspired" by Jakarta Commons Feedparser 
    public static int    READ_TIMEOUT      = 1 * 60 * 1000;
    public static int    CONNECT_TIMEOUT   = READ_TIMEOUT;
    public static String USER_AGENT_HEADER = "User-Agent";
    public static String USER_AGENT        = "Mozilla/5.0 (compatible; roosster - personal search; http://roosster.org/dev)";
    
    private String defaultEncoding = null;
    
    
    /**
     * 
     * @param defaultEncoding
     */
    public ResourceFetcher(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding == null ? Constants.DEFAULT_INPUT_ENCODING 
                                                       : defaultEncoding;
    }
    
    
    /**
     * 
     * @param url
     * @return
     * @throws IOException
     * @throws Exception
     */
    public Resource fetchResource(URL url) throws IOException, Exception 
    {
        if ( url == null )
            throw new IllegalArgumentException("Parameter url is not allowed to be null");

        System.setProperty("http.agent", USER_AGENT);
        System.setProperty("sun.net.client.defaultReadTimeout", Integer.toString(READ_TIMEOUT) );
        System.setProperty("sun.net.client.defaultConnectTimeout", Integer.toString(CONNECT_TIMEOUT) );
        
        URLConnection con = url.openConnection();
        con.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);

        int responseCode = Resource.DEFAULT_RESPONSE_CODE;
        if ( con instanceof HttpURLConnection ) {
            HttpURLConnection httpConnection = (HttpURLConnection) con;
            
            HttpURLConnection.setFollowRedirects(true);
            httpConnection.setInstanceFollowRedirects(true);
            
            httpConnection.connect();
            
            responseCode = httpConnection.getResponseCode();
            
            // TODO implement handling of gzip encoding here
        }
        
        String contentType = con.getContentType();
        
        // find out if Content-Type String contains encoding information
        String embeddedContentEnc = null;
        if ( contentType != null && contentType.indexOf(";") > -1 ) {
            LOG.debug("Content-type string ("+contentType+") contains charset; strip it!");
            contentType = contentType.substring(0, contentType.indexOf(";")).trim();
            
            String cType = con.getContentType();
            if ( cType.indexOf("=") > -1 ) {
                embeddedContentEnc = cType.substring(cType.indexOf("=")+1).trim();
            }
        }
        
        // no content type defined the standard way, so let's guess a bit 
        if ( contentType == null ) {
            LOG.debug("Guessing Content-Type for URL "+url);
        }
        
        String contentEnc  = con.getContentEncoding();
        if ( contentEnc == null ) 
            contentEnc = embeddedContentEnc != null ? embeddedContentEnc : defaultEncoding;

        
        Resource resource = new Resource(url, 
                                         con.getInputStream(),
                                         contentType,
                                         contentEnc,
                                         con.getLastModified(),
                                         responseCode);
        
        LOG.debug("Fetched Resource with ContentType: '"+contentType+"' - ContentEncoding: '"+contentEnc+"'");
        
        return resource;
    }
    
}