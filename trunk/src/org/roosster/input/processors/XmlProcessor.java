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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.net.URL;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.roosster.store.Entry;
import org.roosster.Registry;
import org.roosster.InitializeException;
import org.roosster.input.ContentTypeProcessor;
import org.roosster.xml.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: XmlProcessor.java,v 1.1 2004/12/03 14:30:16 firstbman Exp $
 */
public class XmlProcessor implements ContentTypeProcessor
{
    private static Logger LOG = Logger.getLogger(XmlProcessor.class.getName());

    private static final String ATOM_FEED = "feed";
    private static final String RSS_FEED  = "rss";
    private static final String RDF_FEED  = "rdf";

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

        try {
            XmlPullParser parser          = null;
            int           eventType       = 0;
            String        currentTag      = null;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);

            parser = factory.newPullParser();
            parser.setInput(stream, encoding);
            eventType    = parser.getEventType();

            List entries = new ArrayList();
            do {
                currentTag = parser.getName();
                switch (eventType) {
                   case XmlPullParser.START_TAG:
                      if ( ATOM_FEED.equals(currentTag) ) {

                          AtomFeedParser atomParser = new AtomFeedParser(parser);
                          entries.addAll( Arrays.asList(atomParser.parse()) );

                      } else if ( RSS_FEED.equals(currentTag) || RDF_FEED.equals(currentTag) ) {

                          RssFeedParser rssParser = new RssFeedParser(parser);
                          entries.addAll( Arrays.asList(rssParser.parse()) );

                      }
                      break;

                    default:

                }

                eventType = parser.next();
            } while(eventType != XmlPullParser.END_DOCUMENT);

            return (Entry[]) entries.toArray(new Entry[0]);

        } catch(Exception ex) {
            throw new Exception(ex);
        }

    }
}
