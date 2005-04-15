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
package org.roosster.mappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.roosster.Constants;
import org.roosster.OperationException;
import org.roosster.Output;
import org.roosster.commands.CommandNotFoundException;
import org.roosster.store.EntryList;
import org.roosster.xml.EntryParser;
import org.roosster.xml.ParseException;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class ApiServletMapper extends ServletMapper
{
    private static final long serialVersionUID = 3977577017724057911L;

    private static Logger LOG = Logger.getLogger(ApiServletMapper.class);

    private static final String ENTRY_CMD     = "entry";
    private static final String PUTENTRY_CMD  = "putentries";
    private static final String SEARCH_CMD    = "search";
    private static final String ADD_CMD       = "addurls";
    private static final String DEL_CMD       = "delete";
    
    /**
     * @throws MethodNotAllowedException 
     */
    protected String getCommandName(int method, HttpServletRequest req) throws MethodNotAllowedException
    {
        String commandName = super.getCommandName(method, req); 
       
        if ( ENTRY_CMD.equals(commandName) ) {
            switch (method) {
                case GET:
                    commandName = ENTRY_CMD;
                    break;
                case PUT:
                    commandName = PUTENTRY_CMD;
                    break;
                case POST:
                    commandName = ADD_CMD;
                    break;
                case DELETE:
                    commandName = DEL_CMD;
                    break;
            }
        } else if ( SEARCH_CMD.equals(commandName) && method == GET ) {
            return SEARCH_CMD;
        } else {
            commandName = null;
        }
        
        return commandName;
    }


    /**
     */
    protected Map parseRequestArguments(HttpServletRequest req) throws Exception
    {
        Map args = super.parseRequestArguments(req);
       
        if ( "POST".equals(req.getMethod()) || "PUT".equals(req.getMethod()) ) {
            InputStream stream = req.getInputStream();
            
            if ( stream != null ) {
            
                String bodyEnc = req.getCharacterEncoding();
                if ( bodyEnc == null ) 
                    bodyEnc = inputEncoding;
                
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
        }

          return args;
    }
    
    
    /**
     * 
     */
    protected void processException(int method, 
                                    HttpServletRequest req, 
                                    HttpServletResponse resp,
                                    Output output, 
                                    String commandName,
                                    Exception ex)
                             throws ServletException, IOException
    {
        if ( ex instanceof OperationException ) {
            ex = (Exception) ex.getCause();
        }
          
        if ( ex instanceof CommandNotFoundException ) {
          
            LOG.warn(ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
            
        } else if ( ex instanceof ParseException ) {

            LOG.warn("Sending HTTP Status Code 400: "+ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
                           
        } else if ( ex instanceof MethodNotAllowedException ) {

            LOG.warn("Sending HTTP Status Code 405: "+ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
                           
        } else if ( ex instanceof IllegalArgumentException ) {

            LOG.warn("Sending HTTP Status Code 400: "+ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                           "RoossterException: <"+ex.getClass().getName()+"> "+ex.getMessage());
                           
        } else {
            
            Throwable t = ex.getCause() == null ? ex : ex.getCause();
            LOG.warn("Sending HTTP Status Code 500: "+t.getMessage(), t);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                           "RoossterException: <"+t.getClass().getName()+"> "+t.getMessage());
        }
      
    }
    
    
    /**
     *
     */
    protected boolean returnEmptyList()
    {
        return false;
    }
    
    
    /**
     *
     */
    protected String getOutputMode()
    {
        return "roossterxml";
    }
    
}

