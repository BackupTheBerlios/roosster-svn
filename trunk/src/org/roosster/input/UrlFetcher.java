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
import java.util.logging.Logger;
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
 * @version $Id: UrlFetcher.java,v 1.1 2004/12/03 14:30:15 firstbman Exp $
 */
public class UrlFetcher implements Plugin, Constants
{
    private static Logger LOG = Logger.getLogger(UrlFetcher.class.getName());

    public static final String PROP_DEF_ENC    = "default.input.encoding";
    public static final String PROP_PROCESSORS = "fetcher.processors";

    private Registry              registry        = null;
    private String                defaultEncoding = null;
    private Map                   processors      = new Hashtable();
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
                LOG.log(Level.WARNING, "Error while shutting down "+proc, ex);
            }

        }
    }


    /**
     * 
     */
    public void preProcess(Map requestArgs) throws OperationException
    {}

    
    /**
     */
    public void postProcess(Map requestArgs, Output output) throws OperationException
    {}


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
                entries.addAll( Arrays.asList( fetch(urls[i], true) ) );
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "I/O Error while fetching URL "+urls[i]+": "+
                                       ex.getMessage(), ex);
            } catch (Exception ex) {
                LOG.warning("Error while processing URL "+urls[i]+": "+ex.getMessage());
            }

        }

        LOG.fine("Returning entries "+entries);
        
        return (Entry[]) entries.toArray(new Entry[0]);
    }


    // ============ private Helper methods ============


    /**
     * URLs will be fetched a second time, if the entry's lastFetched
     * object is <code>null</code>, when processed the first time.
     */
    private Entry[] fetch(URL url, boolean refetchContent) throws IOException, Exception
    {
        LOG.finest("Opening connection to URL "+url);

        URLConnection con = url.openConnection();

        long modified = con.getLastModified();
        String contentType = con.getContentType();
        String contentEnc  = con.getContentEncoding() != null ?  con.getContentEncoding() 
                                                              : defaultEncoding; 

        ContentTypeProcessor proc = getProcessor(contentType); 
        LOG.finest("ContentType: "+contentType+" - ContentEncoding: "+contentEnc);
        LOG.fine("Using Processor "+proc);

        Entry[] entries = proc.process(url, con.getInputStream(), contentEnc);
       
        List returnArr  = new ArrayList();
        List fetchLater = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if ( entries[i] == null ) 
                continue;

            if ( refetchContent && entries[i].getLastFetched() == null ) {
                fetchLater.add(entries[i]);
                continue;
            }

            Date modDate = new Date(modified);
            if ( entries[i].getLastModified() == null )
                entries[i].setLastModified(modDate);
            if ( entries[i].getIssued() == null ) 
                entries[i].setIssued(modDate);

            URL entryUrl = entries[i].getUrl();
            String fileType = entries[i].getFileType();
            if ( fileType == null || "".equals(fileType) ) {
                int dotIndex = entryUrl.getPath().lastIndexOf(".");
                if ( dotIndex != -1 ) {
                    String type = entryUrl.getPath().substring(dotIndex+1);
                    entries[i].setFileType(type.toLowerCase());
                    LOG.fine("Filetype is subsequently set to '"+type+"'");
                }
            }

            String title = entries[i].getTitle();
            if ( title == null || "".equals(title) ) 
                entries[i].setTitle(entryUrl.toString());
            
            returnArr.add(entries[i]);
            entries[i] = null;
        }

        if ( refetchContent ) {
            for (int i = 0; i < fetchLater.size(); i++) {
                Entry e = (Entry) fetchLater.get(i);
                LOG.fine("Refetching URL "+ e.getUrl());
                        
                returnArr.addAll( Arrays.asList( fetch(e.getUrl(), false) ) );
            }
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

        LOG.fine("Default ContentTypeProcessor is: "+defProcName);
        StringTokenizer tok = new StringTokenizer(procNames.trim(), " ");
        while ( tok.hasMoreTokens() ) {
            String name  = tok.nextToken();
            String clazz = conf.getProperty(PROP_PROCESSORS+"."+name+".class");
            String typeStr  = conf.getProperty(PROP_PROCESSORS+"."+name+".type");

            if ( clazz == null || typeStr == null ) {
                LOG.warning("No Class or Type property defined for processor '"+name+"'");
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
                LOG.fine("Trying to load ContentTypeProcessor "+clazz);

                ContentTypeProcessor proc = (ContentTypeProcessor) Class.forName(clazz)
                                                                        .newInstance();
                proc.init(registry);

                for (int i = 0; i < types.size(); i++) {
                    processors.put(types.get(i), proc);
                }

                if ( defProcName.equals(name) )
                    defaultProc = proc;

            } catch (ClassCastException ex) {
                LOG.log(Level.WARNING, "Processor "+name+" does not implement the "+
                        ContentTypeProcessor.class+" interface", ex);
                throw new InitializeException(ex);

            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error while loading processor "+
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
        ContentTypeProcessor proc = (ContentTypeProcessor) processors.get(contentType);
        if ( proc == null ) 
            return defaultProc;
        else
            return proc;
    }
}
