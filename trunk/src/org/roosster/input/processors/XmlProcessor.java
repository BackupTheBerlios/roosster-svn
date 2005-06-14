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
package org.roosster.input.processors;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.CopyUtils;
import org.apache.log4j.Logger;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.input.ContentTypeProcessor;
import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.xml.EntryParser;
import org.roosster.xml.FeedParser;
import org.roosster.xml.ParseException;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class XmlProcessor implements ContentTypeProcessor
{
    private static Logger LOG = Logger.getLogger(XmlProcessor.class.getName());


    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {}


    /**
     *
     */
    public boolean isInitialized()
    {
        return true;
    }


    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
    }


    /**
     *
     */
    public Entry[] process(URL url, InputStream stream, String encoding) throws Exception
    {
        if ( url == null || stream == null || encoding == null || "".equals(encoding) )
            throw new IllegalArgumentException("No parameter is allowed to be null");

        StringWriter strWriter = new StringWriter();
        CopyUtils.copy(stream, strWriter, encoding);
        try {
            
            EntryList entryList = new EntryParser().parse(strWriter.toString(), encoding);
            
            return (Entry[]) entryList.toArray(new Entry[0]);
            
        } catch( ParseException ex1 ) {
            LOG.debug("Parsing Error while trying to parse EntryList: "+ex1.getMessage());
            
            try {
                // TODO Try to determine, if the stream is a feed, and parses it, 
                // if this is the case. If not, it just indexes it
    
                FeedParser parser = new FeedParser();
                return parser.parse(url, strWriter.toString());
    
            } catch(Exception ex) {
                throw new OperationException(ex);
            }
        }

    }
}
