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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import org.roosster.store.Entry;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 *
 * @see <a href="http://atompub.org/2004/10/20/draft-ietf-atompub-format-03.html">Atom Syndication Format Spec v0.3</a>
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: AtomFeedParser.java,v 1.1 2004/12/03 14:30:14 firstbman Exp $
 */
public class AtomFeedParser implements Constants
{
    private static Logger LOG = Logger.getLogger(AtomFeedParser.class.getName());

    public static final String ENTRY       = "entry";
    public static final String CONTENT     = "content";
    public static final String MODIFIED    = "modified";
    public static final String ISSUED      = "issued";
    public static final String UPDATED     = "updated";
    public static final String PUBLISHED   = "published";
    public static final String LINK        = "link";
    public static final String TITLE       = "title";
    public static final String AUTHOR      = "author";
    public static final String AUTHORNAME  = "name";
    public static final String AUTHOREMAIL = "email";

    private DateFormat df                 = new SimpleDateFormat(ATOM_DATEFORMAT);
    private XmlPullParser parser          = null;
    private int           eventType       = 0;
    private String        currentTag      = null;


    /**
     * @exception IllegalArgumentException if any parameter is <code>null</code>
     */
    public AtomFeedParser(XmlPullParser parser)
    {
        if ( parser == null ) 
            throw new IllegalArgumentException("Parser is not allowed to be null");

        this.parser = parser;
    }


    /**
     *
     */
    public Entry[] parse() throws OperationException
    {
        try {
            eventType    = parser.getEventType();

            List entries = new ArrayList();
            do {
                currentTag = parser.getName();

                switch (eventType) {

                   case XmlPullParser.START_DOCUMENT:
                      break;

                   case XmlPullParser.START_TAG:
                      if ( ENTRY.equals(currentTag) ) {
                          entries.add( processEntry() );
                      }
                      break;

                   case XmlPullParser.END_TAG:
                      break;

                   case XmlPullParser.TEXT:
                      break;

                   default:

                }

                eventType = parser.next();

            } while (eventType != XmlPullParser.END_DOCUMENT);

            return (Entry[]) entries.toArray(new Entry[0]);

        } catch (Exception ex) {
            throw new OperationException("Exception while parsing atom feed", ex);

        }

    }


    // ============ private Helper methods ============


    /**
     *
     */
    private Entry processEntry() throws XmlPullParserException, IOException,
                                        MalformedURLException, PatternSyntaxException
    {
        Date   issued   = null;
        Date   modified = null;
        StringBuffer contentSrcUrl  = new StringBuffer("");
        StringBuffer url            = new StringBuffer("");
        StringBuffer content        = new StringBuffer("");
        StringBuffer author   = new StringBuffer("");
        StringBuffer email    = new StringBuffer("");
        StringBuffer title    = new StringBuffer("");

        String currentText = null;
        boolean stop       = false;

        while ( !stop ) {
            currentTag = parser.getName() == null ? currentTag : parser.getName();
            currentText = parser.getText();
            if ( currentText != null )
                currentText = currentText.trim();

            switch (eventType) {

               case XmlPullParser.END_TAG:
                  if ( ENTRY.equals(currentTag) )
                      stop = true;
                  break;

               case XmlPullParser.START_TAG:

                  // Section 3.5
                  if ( LINK.equals(currentTag) ) {
                      String rel = parser.getAttributeValue(null, "rel");
                      if ( rel == null || "alternate".equals(rel)  )
                          url.append( parser.getAttributeValue(null, "href") );
                  }

                  break;

               case XmlPullParser.TEXT:
                  if ( CONTENT.equals(currentTag) )
                      content.append(currentText);

                  else if ( PUBLISHED.equals(currentTag) || ISSUED.equals(currentTag) )
                      issued = parseDate(currentText);

                  else if ( UPDATED.equals(currentTag) || MODIFIED.equals(currentTag) )
                      modified = parseDate(currentText);

                  else if ( TITLE.equals(currentTag) )
                      title.append(currentText);

                  else if ( AUTHOREMAIL.equals(currentTag) )
                      email.append(currentText);

                  else if ( AUTHORNAME.equals(currentTag) )
                      author.append(currentText);

                  break;

               case XmlPullParser.CDSECT:
                  if ( CONTENT.equals(currentTag) )
                      content.append(currentText);

                  break;

               default:
                  // these are events that we are not interested in

            }

            eventType = parser.nextToken();
        }


        Entry entry = new Entry(content.toString().replaceFirst("[\n\r]", ""), new URL(url.toString()));
        entry.setTitle(title.toString().replaceAll("[\n\r]", ""));
        entry.setAuthor(author.toString().replaceAll("[\n\r]", ""));
        entry.setAuthorEmail(email.toString().replaceAll("[\n\r]", ""));
        entry.setLastModified(modified);
        entry.setIssued(issued);

        return entry;
    }


    /**
     *
     */
    private Date parseDate(String dateStr)
    {
        Date date = null;

        try {
            date = df.parse(dateStr);
        } catch (ParseException ex) { /* not fatal, just return date of today */ }

        return date == null ? new Date() : date;
    }
}
