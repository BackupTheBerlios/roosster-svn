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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.roosster.Configuration;
import org.roosster.Constants;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Plugin;
import org.roosster.Registry;
import org.roosster.store.Entry;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class UrlFetcher implements Plugin, Constants
{
    private static Logger LOG = Logger.getLogger(UrlFetcher.class.getName());

    private Registry              registry        = null;
    private Map                   processors      = new Hashtable();
    private Map                   procsByName     = new Hashtable();
    private ResourceFetcher       fetcher         = null;
    private ContentTypeProcessor  defaultProc     = null;

    private boolean  initialized     = false;

    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {
        this.registry = registry;
        initProcessors(registry);

        String defaultEncoding = registry.getConfiguration().getProperty(PROP_DEF_ENC);
        if ( defaultEncoding == null )
            throw new InitializeException("Must provide default encoding via "+PROP_DEF_ENC);

        fetcher = new ResourceFetcher(defaultEncoding);
        
        if ( LOG.isInfoEnabled() ) {
            LOG.info("Initialized UrlFetcher: \ndefaultEncoding: "+defaultEncoding +
                     "\ndefaultProcessor: "+defaultProc+"\nContentTypeProcessors: \n");
            
            Iterator iter = processors.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                LOG.info(key+" => "+processors.get(key));
            }
        }
        
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

        Resource resource = fetcher.fetchResource(url);
        
        LOG.debug("Got Reponse Code "+resource.getResponseCode()+" for URL "+url);
        // TODO respect HTTP response codes here, especially 301 (moved permanently) and 4xx and 5xx
        
        ContentTypeProcessor proc = getProcessor(resource.getContentType()); 
        LOG.debug("Using Processor "+proc);

        Entry[] entries = null;
        if ( proc == null )
            entries = new Entry[] { new Entry(url) };
        else
            entries = proc.process(url, resource.getStream(), resource.getContentEncoding());
       
        Date modDate = new Date(resource.getLastModified());
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
     * @return null if no appropriate processor is found
     */
    private ContentTypeProcessor getProcessor(String contentType)
    {
        if ( contentType == null || "".equals(contentType) )
            throw new IllegalArgumentException("Parameter contentType is not allowed to be null");
        
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
        
        return  (ContentTypeProcessor) processors.get(contentType);
        /*
        if ( proc == null ) 
            return defaultProc;
        else
            return proc;
        */
    }
}
