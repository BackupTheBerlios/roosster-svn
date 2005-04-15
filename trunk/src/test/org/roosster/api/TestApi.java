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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;

import org.roosster.RoossterTestCase;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a> 
 */
public class TestApi extends RoossterTestCase
{
    protected HttpClient client = null;
    
    protected StringRequestEntity putRequestBody = null;
    protected StringRequestEntity postRequestBody = null;
    
    /**
     * 
     */
    public void setUp() throws Exception
    {
         client = new HttpClient();
         
         putRequestBody = new StringRequestEntity(TEST_ENTRYXML, "text/xml", "UTF-8");
         postRequestBody = new StringRequestEntity(TEST_ENTRYXML_EMPTY, "text/xml", "UTF-8");
    }
  
    
    /**
     * 
     */
    public void testApi() throws Exception
    {
        // FIRST ADD URL
        System.out.println("Trying to POST '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        PostMethod post = new PostMethod(API_ENDPOINT+"?force=true");
        post.setRequestEntity(postRequestBody); 
        
        int statusCode = client.executeMethod(post);
        
        assertEquals("POST to "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(post);
        
        
        
        // ... THEN GET IT
        System.out.println("Trying to GET '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        GetMethod get = new GetMethod(API_ENDPOINT+"/entry?url="+TEST_URL);
        statusCode = client.executeMethod(get);
        
        assertEquals("GET from "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(get);
        
        
        
        // ... THEN UPDATE IT
        System.out.println("Trying to PUT '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        PutMethod put = new PutMethod(API_ENDPOINT);
        put.setRequestEntity(putRequestBody); 
        
        statusCode = client.executeMethod(put);
        
        assertEquals("PUT to "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(put);
        
        

        // ... THEN GET IT AGAIN
        System.out.println("Trying to GET '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        get = new GetMethod(API_ENDPOINT+"/entry?url="+TEST_URL);
        
        statusCode = client.executeMethod(get);
        
        assertEquals("GET from "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(get);
        
        
        
        // ... AND DELETE IT AGAIN
        System.out.println("Trying to DELETE '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        DeleteMethod del = new DeleteMethod(API_ENDPOINT+"?url="+TEST_URL);
        
        statusCode = client.executeMethod(del);
        
        assertEquals("GET from "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(get);
        
        
        // ... THEN GET IT AGAIN
        System.out.println("Trying to GET '"+TEST_URL+"' as a new entry to "+ API_ENDPOINT);
        
        get = new GetMethod(API_ENDPOINT+"/entry?url="+TEST_URL);
        
        statusCode = client.executeMethod(get);
        
        assertEquals("GET from "+TEST_URL+" failed", 200, statusCode);
        
        logMethodResponse(get);
        
        
    }
    
    
}
