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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.*;

import org.roosster.RoossterTestCase;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a> 
 */
public class TestApi extends RoossterTestCase
{
    protected HttpClient client = null;
    
    protected static final String URL_TO_ADD = "http://blog.nur-eine-i.de/atom.xml";
    
    protected StringRequestEntity requestBody = null;
    
    /**
     * 
     */
    public void setUp() throws Exception
    {
         client = new HttpClient();
         
         requestBody = new StringRequestEntity(TEST_ENTRYXML, "text/xml", "UTF-8");
    }
  
    
    /**
     * 
     */
    public void testPost() throws Exception
    {
        System.out.println("Trying to POST '"+URL_TO_ADD+"' as a new entry to "+ API_ENDPOINT);
        
        PostMethod method = new PostMethod(API_ENDPOINT);
        method.addParameter("url", URL_TO_ADD);
        method.addParameter("force", "true");

        method.setRequestEntity(requestBody); 
        
        int statusCode = client.executeMethod(method);
        
        assertEquals("Adding "+URL_TO_ADD+" failed", 200, statusCode);
        
        logMethodResponse(method);
    }


    /**
     * 
     */
    public void testPut() throws Exception
    {
        System.out.println("Trying to PUT '"+TEST_ENTRYXML+"' as a new entry to "+ API_ENDPOINT);
        
        PutMethod method = new PutMethod(API_ENDPOINT);
        method.setRequestEntity(requestBody); 
        
        int statusCode = client.executeMethod(method);
        
        assertEquals("Adding "+URL_TO_ADD+" failed", 200, statusCode);
        
        logMethodResponse(method);
    }

    
    /**
     * 
     */
    public void testGet() throws Exception
    {
        System.out.println("Trying to GET '"+URL_TO_ADD+"' as a new entry to "+ API_ENDPOINT);
        
        GetMethod method = new GetMethod(API_ENDPOINT);
        
        int statusCode = client.executeMethod(method);
        
        assertEquals("Adding "+URL_TO_ADD+" failed", 200, statusCode);
        
        logMethodResponse(method);
    }
    
    
}
