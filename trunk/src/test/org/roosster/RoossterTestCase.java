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

import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a> 
 */
public class RoossterTestCase extends TestCase
{
    public static final String SYSPROP_API_ENDPOINT = "api.endpoint";
  
    
    public static final String TEST_ISSUED   = "2004-03-09T21:32:58-05:00";
    public static final String TEST_MODIFIED = "2005-01-01T21:32:58-05:00";
    public static final String TEST_FETCHED  = "2005-01-09T21:32:58-05:00";
    
    public static final String TEST_TYPE  = "text/html";
    public static final String TEST_TITLE = "this is a test title";
    
    public static final String TEST_AUTHOR = "test author";
    public static final String TEST_EMAIL = "test email";
    
    public static final String TEST_NOTE = "test NOTE";
    public static final String TEST_TAG1 = "cat";
    public static final String TEST_TAG2 = "dog";
    public static final String TEST_TAG3 = "test";
    
    public static final String TEST_ENTRYXML = 
    "<entry href='http://example.com/?test'>"+
    "<type>"+ TEST_TYPE +"</type>"+
    "<title>"+ TEST_TITLE +"</title>"+
    "<authors> <author name='"+ TEST_AUTHOR +"' email='"+ TEST_EMAIL +"'/> </authors>"+
    "<tags> <tag>"+TEST_TAG1 +"</tag> <tag>"+TEST_TAG2 +"</tag> tag>"+ TEST_TAG3+"</tag> </tags>"+
    "<note>"+ TEST_NOTE +"</note>"+
    "<issued>"+ TEST_ISSUED +"</issued> "+
    "<modified>"+ TEST_MODIFIED +"</modified>"+
    "<fetched>"+ TEST_FETCHED +"</fetched>"+
    "</entry>"
    

    
    
    protected String API_ENDPOINT = null;
    
    public RoossterTestCase() 
    {
        API_ENDPOINT = System.getProperty(SYSPROP_API_ENDPOINT);
    }
    
}
