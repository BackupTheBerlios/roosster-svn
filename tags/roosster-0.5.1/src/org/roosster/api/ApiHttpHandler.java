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

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;
import org.roosster.Configuration;
import org.roosster.Output;
import org.roosster.Dispatcher;
import org.roosster.store.EntryList;
import org.roosster.util.StringUtil;
import org.roosster.xml.EntryParser;
import org.roosster.xml.TagGenerator;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ApiHttpHandler extends AbstractHttpHandler
{
    private static Logger LOG = Logger.getLogger(ApiHttpHandler.class);    

    
    /**
     */
    public static final String DEF_CONTENT_TYPE = "text/xml";    
    public static final String DEF_ENC          = "UTF-8";
    public static final String DEF_OUTPUTMODE   = "roossterxml";
    
    public static final String PROP_OUTPUTMODE  = "output.mode";
    public static final String PROP_OUTENC      = "default.output.encoding";
    public static final String PROP_INENC       = "default.input.encoding";
    
    /**
     */
    private static Map methodCommandMatrix  = null; 
  
    private Registry      registry      = null;
    private Configuration configuration = null;

       

    /**
     *
     */
    public ApiHttpHandler(Registry registry)
    {
        if ( registry == null ) 
            throw new IllegalArgumentException("Parameter registry is not allowed to be null");

        this.registry = registry;
        configuration = registry.getConfiguration();
        
        methodCommandMatrix = new HashMap();
        
        methodCommandMatrix.put(HttpRequest.__POST,   new String[] {"/entry", "addurls"});
        
        methodCommandMatrix.put(HttpRequest.__PUT,    new String[] {"/entry", "putentries"});
        
        methodCommandMatrix.put(HttpRequest.__DELETE, new String[] {"/entry", "delete"});
        
        methodCommandMatrix.put(HttpRequest.__GET,    new String[] {"/entry", "entry", 
                                                                    "/tags",  "tags",
                                                                    "/search", "search"});
    }

    
    /**
     *
     */
    public String getName()
    {
        return getClass().getName();
    }


    /**
     *
     */
    public void handle(String pathInContext, String pathParams, 
                       HttpRequest request, HttpResponse response)
                throws HttpException, IOException
    {
        try {
          
            String commandName = getCommandName(pathInContext, request.getMethod());
          
            if ( commandName != null ) {
              
                // special behaviour for 'tags' command
                if ( "tags".equals(commandName) ) {
                  
                    TagGenerator tagGen = new TagGenerator();
                    tagGen.outputAllTags(registry,
                                         new PrintStream(response.getOutputStream(), true));
                    
                } else {
              
                    Map args = parseRequestArguments(request);
                    
                    // override configuration for this request
                    configuration.setRequestArguments(args);
                    
                    String outputEncoding = configuration.getProperty(PROP_OUTENC, DEF_ENC);
                    String outputMode     = configuration.getProperty(PROP_OUTPUTMODE, DEF_OUTPUTMODE);
                    
                    
                    // ... let it roll
                    Output output = new Dispatcher(registry).run(commandName, outputMode, args);
                    
                    if ( output.entriesSize() < 1 ) {
    
                        // save bandwidth and be RESTian ;)
                        response.setStatus(HttpResponse.__204_No_Content);
                        response.setReason("Request returned no Entries");
                        
                    } else {
                    
                        String type = output.getContentType();
                        
                        response.setCharacterEncoding(outputEncoding);
                        response.setContentType(type == null ? DEF_CONTENT_TYPE : type);
                        
                        output.output( new PrintStream(response.getOutputStream(), true)  );
                    }
                    
                }
                
            } else {
                response.sendError(HttpResponse.__404_Not_Found, request.getPath()+" is not available");
            } 

            
        } catch (Exception ex) {
          
            LOG.warn("Exception occured while serving an API request", ex);
            response.sendError(HttpResponse.__500_Internal_Server_Error, 
                               "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
            
        } finally {
            configuration.clearRequestArguments();
            request.setHandled(true);         
        }
        
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    protected Map parseRequestArguments(HttpRequest request) throws Exception
    {
        Map args = new HashMap();
      
        Iterator iter = request.getParameterNames().iterator();
        while ( iter.hasNext() ) {
            String name = (String) iter.next();
            args.put(name, request.getParameter(name));
        }
        
        configuration.setRequestArguments(args);
        
        InputStream stream = request.getInputStream();
        
        if (  stream != null ) {
        
            String bodyEnc = request.getCharacterEncoding();
            if ( bodyEnc == null ) 
                bodyEnc = configuration.getProperty(PROP_INENC, DEF_ENC);;
            
            String body = IOUtils.toString(stream, bodyEnc);
            
            if ( body != null && !"".equals(body) ) {
              
                // parse request body through entry parser
                EntryList entryList = new EntryParser().parse(body, bodyEnc);
                args.put(Constants.PARAM_ENTRIES, entryList);
                
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("**************************************************");
                    LOG.debug("RequestBody: encoding "+bodyEnc+"\n");
                    LOG.debug(body);
                    LOG.debug("**************************************************");
                }
            }
        }
        
        return args;
    }
    
    
    /**
     * a method/command combination that's not listed in the methodCommandMatrix
     * is denied
     */
    private String getCommandName(String pathInContext, String method)
    {
        String command = null;
        
        if ( !StringUtil.isNullOrBlank(pathInContext) ) {
            String[] commands = (String[]) methodCommandMatrix.get(method);
        
            if ( commands != null && commands.length > 0 ) {
              
                for(int i = commands.length-2; i >= 0 ; i -= 2) {
                    if ( pathInContext.equals(commands[i]) ) { 
                        command = commands[i+1];
                        break;
                    }
                }
                
            }
        }

        LOG.debug("Is path "+pathInContext+" available via method "+method+"? Command: "+command);
        
        return command;
    }


}


