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
package org.roosster.xml;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.PrintWriter;
import java.io.IOException;
import org.roosster.store.Entry;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;


/**
 * Given a set of {@link org.roosster.store.Entry Entry}-objects this class
 * generates an Atom-Feed (version 0.3).
 *
 * @see <a href="http://atompub.org/2004/10/20/draft-ietf-atompub-format-03.html">Atom Syndication Format Spec v0.3</a>
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: AtomFeedGenerator.java,v 1.1 2004/12/03 14:30:14 firstbman Exp $
 */
public class AtomFeedGenerator
{
    public static final String ATOM_NS     = Constants.ATOM_NS;

    public static final String DEF_GEN_URI = Constants.APP_URI;
    public static final String DEF_GEN_TXT = Constants.APP_NAME;

    public static final String DEF_TITLE   = Constants.APP_NAME +" feed";
    public static final String PROP_TITLE  = "output.atom.title";

    private DateFormat df = new SimpleDateFormat(Constants.ATOM_DATEFORMAT);


    /**
     * @param entries
     */
    public void createFeed(Registry registry, PrintWriter stream, Entry[] entries)
                    throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(null, null);
            XmlSerializer serializer     = factory.newSerializer();

            serializer.setOutput(stream);

            serializer.startDocument(null, null);
            serializer.setPrefix("", ATOM_NS);

            serializer.startTag(ATOM_NS, "feed").attribute(null, "version", "0.3");

              serializer.startTag(ATOM_NS, "head");

                serializer.startTag(ATOM_NS, "title");
                serializer.text( registry.getConfiguration().getProperty(PROP_TITLE, DEF_TITLE) );
                serializer.endTag(ATOM_NS, "title");

                serializer.startTag(ATOM_NS, "generator");
                serializer.attribute(null, "uri", DEF_GEN_URI).text(DEF_GEN_TXT);
                serializer.endTag(ATOM_NS, "generator");

                Date newest = null;
                for(int i = 0; i < entries.length; i++) {
                    if ( entries[i].getLastModified() != null &&
                          (newest == null ||  newest.before(entries[i].getLastModified()) ) ) {
                        newest = entries[i].getLastModified();
                    }
                }

                serializer.startTag(ATOM_NS, "updated");
                serializer.text(formatDate(newest));
                serializer.endTag(ATOM_NS, "updated");

                // TODO add <link rel="alternate"> with url of this invocation

              serializer.endTag(ATOM_NS, "head");

            for(int i = 0; i < entries.length; i++) {
                writeEntry(serializer, entries[i]);
            }

            serializer.endTag(ATOM_NS, "feed");
            serializer.endDocument();

        } catch(Exception ex) {
            throw new OperationException(ex);
        }

    }


    // ============ private Helper methods ============


    /**
     * TODO what about escaping content?
     */
    private void writeEntry(XmlSerializer serializer, Entry entry)
                     throws XmlPullParserException, IOException
    {
        serializer.startTag(ATOM_NS, "entry");

          serializer.startTag(ATOM_NS, "title");
          serializer.text(entry.getTitle());
          serializer.endTag(ATOM_NS, "title");

          serializer.startTag(ATOM_NS, "author");
          serializer.startTag(ATOM_NS, "name").text(entry.getAuthor()).endTag(ATOM_NS, "name");
          serializer.startTag(ATOM_NS, "email").text(entry.getAuthorEmail()).endTag(ATOM_NS, "email");
          serializer.endTag(ATOM_NS, "author");

          serializer.startTag(ATOM_NS, "link");
          serializer.attribute(null, "rel", "alternate");
          serializer.attribute(null, "href", entry.getUrl()+"");
          serializer.endTag(ATOM_NS, "link");

          serializer.startTag(ATOM_NS, "published");
          serializer.text(formatDate(entry.getIssued()));
          serializer.endTag(ATOM_NS, "published");

          serializer.startTag(ATOM_NS, "updated");
          serializer.text(formatDate(entry.getLastModified()));
          serializer.endTag(ATOM_NS, "updated");

          serializer.startTag(ATOM_NS, "content");
          serializer.text(entry.getContent());
          serializer.endTag(ATOM_NS, "content");

        serializer.endTag(ATOM_NS, "entry");
    }


    /**
     *
     */
    private String formatDate(Date date)
    {
        return date == null ? "" : df.format(date);
    }
}
