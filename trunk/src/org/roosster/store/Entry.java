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
package org.roosster.store;

import java.util.Date;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Field;

/**
 * TODO make dateformat configurable
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: Entry.java,v 1.1 2004/12/03 14:30:15 firstbman Exp $
 */
public class Entry
{
    public static final String ALL          = "all";
    public static final String CONTENT      = "content";
    public static final String LAST_MOD     = "lastmod";
    public static final String LAST_FETCHED = "lastfetch";
    public static final String ISSUED       = "issued";
    public static final String URL          = "url";
    public static final String TITLE        = "title";
    public static final String AUTHOR       = "author";
    public static final String AUTHOREMAIL  = "authormail";
    public static final String FILETYPE     = "filetype";


    private Date         issued	       = null;
    private Date         lastModified	 = null;
    private Date         lastFetched	 = null;
    private URL          url           = null;
    private String       title         = "";
    private String       author        = "";
    private String       authorEmail   = "";
    private String       fileType	     = "";
    private StringBuffer content       = new StringBuffer();


    /**
     *
     */
    public Entry(String content, URL url)
    {
        setContent(content);
        setUrl(url);
    }


    /**
     *
     */
    protected Entry(Document doc)
    {
        String urlStr = null;
        try {

            if ( doc != null ) {
                urlStr = doc.getField(URL).stringValue();
                setUrl( new URL(urlStr) );
                setTitle( doc.getField(TITLE).stringValue() );
                setAuthor( doc.getField(AUTHOR).stringValue() );
                setAuthorEmail( doc.getField(AUTHOREMAIL).stringValue() );
                setFileType( doc.getField(FILETYPE).stringValue() );
                setContent( doc.getField(CONTENT).stringValue() );

                setIssued( !"".equals(doc.getField(ISSUED).stringValue())
                           ? DateField.stringToDate(doc.getField(ISSUED).stringValue()) : null );

                setLastModified( !"".equals(doc.getField(LAST_MOD).stringValue())
                                 ? DateField.stringToDate(doc.getField(LAST_MOD).stringValue()) : null );

                setLastFetched( !"".equals(doc.getField(LAST_FETCHED).stringValue())
                                 ? DateField.stringToDate(doc.getField(LAST_FETCHED).stringValue()) : null );
            }

        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL '"+urlStr+"' is not a valid URL: "+ex.getMessage());
        }

    }


    /**
     *
     */
    protected Document getDocument()
    {
        Document doc = new Document();
        doc.add( Field.Keyword(URL,        url.toString()) );
        doc.add( Field.Text(TITLE,         title) );
        doc.add( Field.Text(AUTHOR,        author) );
        doc.add( Field.Text(AUTHOREMAIL,   authorEmail) );
        doc.add( Field.Text(FILETYPE,      fileType) );
        doc.add( Field.Text(CONTENT,       content.toString()) );

        String lastModStr = lastModified != null ? DateField.dateToString(lastModified) : "";
        String lastFetStr = lastFetched != null ? DateField.dateToString(lastFetched) : "";
        String issuedStr  = issued != null ? DateField.dateToString(issued) : "";
        doc.add( Field.Text(LAST_MOD,      lastModStr) );
        doc.add( Field.Text(LAST_FETCHED,  lastFetStr) );
        doc.add( Field.Text(ISSUED,        issuedStr) );

        doc.add( Field.Text(ALL,  url +" "+ title +" "+ author+" "+authorEmail+" "+content) );
        return doc;
    }


    /**
     *
     */
    public String toString()
    {
        return url.toString();
    }


    // ============== Accessors ==============


    /**
     *
     */
    public void setContent(String content)
    {
        this.content = new StringBuffer(content);
    }


    /**
     *
     */
    public String getContent()
    {
        return content.toString();
    }


    /**
     *
     */
    public void appendContent(String content)
    {
        this.content.append(content);
    }


    /**
     * Returns the value of issued.
     */
    public Date getIssued()
    {
        return issued;
    }


    /**
     * Sets the value of issued.
     * @param issued The value to assign issued.
     */
    public void setIssued(Date issued)
    {
        this.issued = issued;
    }


    /**
     * Returns the value of lastModified.
     */
    public Date getLastModified()
    {
        return lastModified;
    }


    /**
     * Sets the value of lastModified.
     * @param lastModified The value to assign lastModified.
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }


    /**
     * Returns the value of lastFetched.
     */
    public Date getLastFetched()
    {
        return lastFetched;
    }


    /**
     * Sets the value of lastFetched.
     * @param lastFetched The value to assign lastFetched.
     */
    public void setLastFetched(Date lastFetched)
    {
        this.lastFetched = lastFetched;
    }


    /**
     * Returns the value of url.
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * Sets the value of url.
     * @param url The value to assign url.
     */
    public void setUrl(URL url)
    {
        this.url = url;
    }


    /**
     * Returns the value of title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the value of title.
     * @param url The value to assign title.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }


    /**
     * Returns the value of author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Sets the value of author.
     * @param url The value to assign author.
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }


    /**
     * Returns the value of author.
     */
    public String getAuthorEmail()
    {
        return authorEmail;
    }

    /**
     * Sets the value of author.
     * @param url The value to assign author.
     */
    public void setAuthorEmail(String mail)
    {
        this.authorEmail = mail;
    }


    /**
     * Returns the value of fileType.
     */
    public String getFileType()
    {
      return fileType;
    }

    /**
     * Sets the value of fileType.
     * @param fileType The value to assign fileType.
     */
    public void setFileType(String fileType)
    {
      this.fileType = fileType;
    }

}
