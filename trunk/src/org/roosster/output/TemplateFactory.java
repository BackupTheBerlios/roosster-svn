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
package org.roosster.output;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.javaby.jbyte.Template;
import org.javaby.jbyte.TemplateCreationException;
import org.roosster.Plugin;
import org.roosster.InitializeException;
import org.roosster.Registry;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: TemplateFactory.java,v 1.1 2004/12/03 14:30:14 firstbman Exp $
 */
public class TemplateFactory implements Plugin
{
    private static Logger LOG = Logger.getLogger(TemplateFactory.class.getName());

    private static final String PROP_TMPL_PATH = "templates.path";
    
    private Map     templates   = new Hashtable();
    private Map     modTimes    = new Hashtable();
    private String  path        = null;
    private boolean initialized = false;

    /**
     */
    public void init(Registry registry) throws InitializeException
    {
        LOG.finest("Initializing "+getClass());

        path = registry.getConfiguration().getProperty(PROP_TMPL_PATH);
        if ( path == null || "".equals(path) )
            throw new InitializeException
                      ("Property "+PROP_TMPL_PATH+" must be specified");

        if ( !path.endsWith("/") )
            path = path +"/";
        
        initialized = true;
    }

        
    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
        initialized = false;
    }


    /**
     *
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    
    /**
     */
    public Template getTemplate(String fileName) 
                                 throws IOException, TemplateCreationException
    {
        String tmplStr = loadTemplate(fileName);
        if ( tmplStr == null )
            throw new FileNotFoundException("Can't find template file "+fileName);

        return new Template(new BufferedReader(new StringReader(tmplStr)) );
    }

    
    /**
     */
    public String getTemplateContent(String fileName) throws IOException
    {
        return loadTemplate(fileName);
    }

    
    /**
     * see if file exists, if not try to load from file or
     * classpath, if yes see if it changed, if yes or if no
     * contents is cached try to load from file or classpath
     * @param fileName name of file to load from  
     * @exception IOException if an IOException occurs while loading
     * the file 
     */
    private String loadTemplate(String fileName) throws IOException
    {
        String returnStr = null;
        String tmplContents = (String) templates.get(fileName);

        File tmplFile = new File(path + fileName);
        LOG.finest("Trying to open file "+tmplFile);

        if ( tmplFile.exists() && tmplFile.canRead() ) {
            
            Long modTime = (Long) modTimes.get(fileName);
            long modFile = tmplFile.lastModified();
            LOG.finest("File exists: cached modtime "+modTime+", fileModTime "+modFile);
            
            if ( modTime == null 
                   || modTime.longValue() < modFile 
                   || tmplContents == null) {
                LOG.finest("File has changed or isn't cached yet, loading now: "+tmplFile);
                returnStr = IOUtils.toString(new FileReader(tmplFile));
                templates.put(fileName, returnStr);
                modTimes.put(fileName, new Long(System.currentTimeMillis()));
            } else {
                LOG.finest("File hasn't changed and is cached");
                returnStr = tmplContents;
            }
            

        } 
        
        if ( tmplContents != null && returnStr == null ) {

            LOG.finest("File cached, formerly loaded from classpath");
            returnStr = tmplContents;
            
        } else if ( returnStr == null ) {

            LOG.finest("No file found or cached, trying to load "+fileName+" from classpath"); 
            
            InputStream stream = null;
            try {

                stream = getClass().getResourceAsStream("/"+fileName); 
                if ( stream != null ) {
                    returnStr = IOUtils.toString(stream);
                    templates.put(fileName, returnStr);
                    modTimes.put(fileName, new Long(System.currentTimeMillis()));
                }
                
            } finally {
                if ( stream != null )
                    stream.close();
            }
        }

        return returnStr;
    }
}
