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

import java.util.*;
import org.apache.log4j.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import org.roosster.OperationException;
import org.roosster.InitializeException;
import org.roosster.Registry;
import org.roosster.Plugin;
import org.roosster.Output;
import org.roosster.Configuration;
import org.roosster.Constants;
import org.roosster.store.Entry;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class UrlFetcher implements Plugin, Constants
{
    private static Logger LOG = Logger.getLogger(UrlFetcher.class.getName());

    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String USER_AGENT        = "Mozilla/5.0 (compatible; roosster - personal search; http://roosster.org/dev)";
    
    private Registry              registry        = null;
    private String                defaultEncoding = null;
    private Map                   processors      = new Hashtable();
    private Map                   procsByName     = new Hashtable();
    private ContentTypeProcessor  defaultProc     = null;

    private boolean  initialized     = false;

    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {
        this.registry = registry;
        initProcessors(registry);

        defaultEncoding = registry.getConfiguration().getProperty(PROP_DEF_ENC);
        if ( defaultEncoding == null )
            throw new InitializeException("Must provide default encoding via "+PROP_DEF_ENC);
        
        LOG.info("Initialized UrlFetcher: \ndefaultEncoding: "+defaultEncoding +
                 "\ndefaultProcessor: "+defaultProc +
                 "\nContentTypeProcessors: "+processors );
        
        initialized = true;
    }


    /**
     *
     */
    public boolean isInitialized()
    {
        return initialized;
    }


    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
        Iterator procIter = processors.values().iterator();

        while ( procIter.hasNext() ) {

            ContentTypeProcessor proc = null;
            try {
                proc = (ContentTypeProcessor) procIter.next();
                proc.shutdown(registry);
            } catch (Exception ex) {
                LOG.warn("Error while shutting down "+proc, ex);
            }

        }
    }


    /**
     *
     */
    public Entry[] fetch(URL[] urls) throws OperationException
    {
        if ( urls == null )
            throw new IllegalArgumentException("No Parameter is allowed to be null!");

        List entries = new ArrayList();
        for(int i = 0; i < urls.length; i++) {

            try {
                entries.addAll( Arrays.asList( fetch(urls[i]) ) );
            } catch (IOException ex) {
                LOG.warn("I/O Error while fetching URL "+urls[i]+": "+ex.getMessage(), ex);
            } catch (Exception ex) {
                LOG.warn("Error while processing URL "+urls[i]+": "+ex.getMessage(), ex);
            }

        }

        LOG.debug("Returning entries "+entries);
        
        return (Entry[]) entries.toArray(new Entry[0]);
    }


    // ============ private Helper methods ============


    /**
     * URLs will be fetched a second time, if the entry's lastFetched
     * object is <code>null</code>, when processed the first time.
     */
    private Entry[] fetch(URL url) throws IOException, Exception
    {
        LOG.debug("Opening connection to URL "+url);

        URLConnection con = url.openConnection();
        con.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);

        
        long modified = con.getLastModified();

        
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
        
        String contentEnc  = con.getContentEncoding();
        if ( contentEnc == null ) 
            contentEnc = embeddedContentEnc != null ? embeddedContentEnc : defaultEncoding;

        
        ContentTypeProcessor proc = getProcessor(contentType); 
        LOG.debug("ContentType: '"+contentType+"' - ContentEncoding: '"+contentEnc+"'");
        LOG.debug("Using Processor "+proc);

        Entry[] entries = proc.process(url, con.getInputStream(), contentEnc);
       
        Date modDate = new Date(modified);
        Date now = new Date();
        
        List returnArr  = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if ( entries[i] == null ) 
                continue;

            URL entryUrl = entries[i].getUrl();
            
            String title = entries[i].getTitle();
            if ( title == null || "".equals(title) ) 
                entries[i].setTitle(entryUrl.toString());
            
            if ( entries[i].getModified() == null )
                entries[i].setModified(modDate);
            
            if ( entries[i].getIssued() == null ) 
                entries[i].setIssued(modDate);

            if ( entries[i].getAdded() == null ) 
                entries[i].setAdded(now);

            String fileType = entries[i].getFileType();
            if ( fileType == null || "".equals(fileType) ) {
              
                int dotIndex = entryUrl.getPath().lastIndexOf(".");
                if ( dotIndex != -1 ) {
                    String type = entryUrl.getPath().substring(dotIndex+1);
                    entries[i].setFileType(type.toLowerCase());
                    LOG.debug("Filetype is subsequently set to '"+type+"'");
                }
                
            }

            returnArr.add(entries[i]);
            entries[i] = null;
        }

        return (Entry[]) returnArr.toArray(new Entry[0]);
    }


    /**
     *
     */
    private void initProcessors(Registry registry) throws InitializeException
    {
        Configuration conf = registry.getConfiguration();
        String procNames = conf.getProperty(PROP_PROCESSORS);

        if ( procNames == null ) 
            throw new InitializeException("UrlFetcher needs ContentTypeProcessors");

        String defProcName = conf.getProperty(PROP_PROCESSORS+".default");
        if ( defProcName == null || "".equals(defProcName) )
            throw new InitializeException("No default processor defined");

        StringTokenizer tok = new StringTokenizer(procNames.trim(), " ");
        while ( tok.hasMoreTokens() ) {
            String name  = tok.nextToken();
            String clazz = conf.getProperty(PROP_PROCESSORS+"."+name+".class");
            String typeStr  = conf.getProperty(PROP_PROCESSORS+"."+name+".type");

            if ( clazz == null || typeStr == null ) {
                LOG.warn("No Class or Type property defined for processor '"+name+"'");
                continue;
            }

            // split types by spaces, to allow single proc to
            // process multiple types
            List types = new ArrayList();
            StringTokenizer typeTok = new StringTokenizer(typeStr);
            while ( typeTok.hasMoreTokens() ) {
                types.add( typeTok.nextToken() );
            }

            try {
                LOG.debug("Trying to load ContentTypeProcessor "+clazz);

                ContentTypeProcessor proc = (ContentTypeProcessor) Class.forName(clazz)
                                                                        .newInstance();
                proc.init(registry);

                procsByName.put(name, proc);

                for (int i = 0; i < types.size(); i++) {
                    processors.put(types.get(i), proc);
                }

                if ( defProcName.equals(name) )
                    defaultProc = proc;
                
            } catch (ClassCastException ex) {
                LOG.warn("Processor "+name+" does not implement the "+
                        ContentTypeProcessor.class+" interface", ex);
                throw new InitializeException(ex);

            } catch (Exception ex) {
                LOG.warn("Error while loading processor "+
                        name+" ; Message: "+ex.getMessage(), ex);
                
                throw new InitializeException(ex);
            }
        }

        if ( defaultProc == null )
            throw new InitializeException("Invalid default processor defined (misspelled class?)");
        
    }


    /**
     *
     */
    private ContentTypeProcessor getProcessor(String contentType)
    {
        ContentTypeProcessor proc = null;

        String forcedProc = registry.getConfiguration().getProperty(PROP_INPUT_PROC);
        if ( forcedProc != null ) {
            proc = (ContentTypeProcessor) procsByName.get(forcedProc);
            if ( proc == null )
                throw new IllegalArgumentException("Wrong input processor "+
                                                   "specified by "+PROP_INPUT_PROC);
            else 
                return proc;
        }
        
        proc = (ContentTypeProcessor) processors.get(contentType);
        if ( proc == null ) 
            return defaultProc;
        else
            return proc;
    }
}
