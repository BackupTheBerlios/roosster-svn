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

import org.roosster.util.StringUtil;
import org.roosster.Constants;

/**
 * TODO make dateformat configurable
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Entry
{
    public static final String TAG_SEPARATOR= Constants.TAG_SEPARATOR;
  
    public static final String ALL          = "all";
    public static final String CONTENT      = "content";
    public static final String LAST_MOD     = "lastmod";
    public static final String LAST_FETCHED = "lastfetch";
    public static final String LAST_EDITED  = "lastedit";
    public static final String ISSUED       = "issued";
    public static final String URL          = "url";
    public static final String TITLE        = "title";
    public static final String AUTHOR       = "author";
    public static final String AUTHOREMAIL  = "authormail";
    public static final String FILETYPE     = "filetype";
    public static final String NOTE         = "note";
    public static final String TAGS         = "tags";
    public static final String RAW          = "raw";

    /** enumerates the fields, by which result can be sorted
     */
    public static final String[] SORT_FIELDS     = new String[]
    {LAST_MOD, LAST_FETCHED, ISSUED, URL, TITLE, AUTHOR, AUTHOREMAIL, FILETYPE};
    

    private Date         issued	       = null;
    private Date         lastModified	 = null;
    private Date         lastFetched	   = null;
    private Date         lastEdited     = null;
    private URL          url            = null;
    private String       title          = "";
    private String       author         = "";
    private String       authorEmail    = "";
    private String       fileType	     = "";
    private StringBuffer content        = new StringBuffer();
    private StringBuffer note           = new StringBuffer();
    private StringBuffer raw            = new StringBuffer();
    private String[]     tags           = new String[0];


    /**
     *
     */
    public Entry(URL url)
    {
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
                setRaw( doc.getField(RAW).stringValue() );
                setNote( doc.getField(NOTE).stringValue() );
                setTags( StringUtil.split(doc.getField(TAGS).stringValue(), TAG_SEPARATOR) );

                setIssued( !"".equals(doc.getField(ISSUED).stringValue())
                           ? DateField.stringToDate(doc.getField(ISSUED).stringValue()) : null );

                setLastModified( !"".equals(doc.getField(LAST_MOD).stringValue())
                                 ? DateField.stringToDate(doc.getField(LAST_MOD).stringValue()) : null );

                setLastFetched( !"".equals(doc.getField(LAST_FETCHED).stringValue())
                                 ? DateField.stringToDate(doc.getField(LAST_FETCHED).stringValue()) : null );
                                 
                setLastEdited( !"".equals(doc.getField(LAST_EDITED).stringValue())
                                 ? DateField.stringToDate(doc.getField(LAST_EDITED).stringValue()) : null );
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
        String lastModStr  = lastModified != null ? DateField.dateToString(lastModified) : "";
        String lastFetStr  = lastFetched != null ? DateField.dateToString(lastFetched) : "";
        String lastEditStr = lastEdited != null ? DateField.dateToString(lastEdited) : "";
        String issuedStr   = issued != null ? DateField.dateToString(issued) : "";

        Document doc = new Document();

        doc.add( Field.Keyword(URL,        url.toString()) );
        doc.add( Field.Keyword(TITLE,         title) );
        doc.add( Field.Keyword(AUTHOR,        author) );
        doc.add( Field.Keyword(AUTHOREMAIL,   authorEmail) );
        doc.add( Field.Keyword(FILETYPE,      fileType) );
        doc.add( Field.Keyword(LAST_MOD,      lastModStr) );
        doc.add( Field.Keyword(LAST_FETCHED,  lastFetStr) );
        doc.add( Field.Keyword(LAST_EDITED,   lastEditStr) );
        doc.add( Field.Keyword(ISSUED,        issuedStr) );
        doc.add( Field.Text(CONTENT,          content.toString()) );
        doc.add( Field.Text(NOTE,             note.toString()) );
        doc.add( Field.Text(TAGS,             StringUtil.join(tags, TAG_SEPARATOR)) );
        
        doc.add( Field.UnIndexed(RAW, getRaw()) );

        doc.add( Field.Text(ALL,  url +" "+ title +" "+ author+" "+authorEmail+" "+
                                  content +" "+note+" "+StringUtil.join(tags, TAG_SEPARATOR)) );
        return doc;
    }


    /**
     *
     */
    public String toString()
    {
        return "Entry: "+url.toString();
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
    public void appendContent(char[] str, int offset, int len)
    {
        this.content.append(str, offset, len);
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
		 * Returns the value of lastEdited.
		 */
		public Date getLastEdited() {
				return lastEdited;
		}


		/**
		 * Sets the value of lastEdited.
		 * @param lastEdited The value to assign lastEdited.
		 */
		public void setLastEdited(Date lastEdited) {
				this.lastEdited = lastEdited;
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

    /**
     * Returns the value of tags.
     */
    public String[] getTags()
    {
      return tags;
    }
  
    /**
     * Sets the value of tags.
     * @param tags The value to assign tags.
     */
    public void setTags(String[] tags)
    {
      this.tags = tags;
    }
  
    /**
     * Returns the value of note.
     */
    public String getNote()
    {
      return note.toString();
    }
  
    /**
     * Sets the value of note.
     * @param note The value to assign note.
     */
    public void setNote(String note)
    {
      this.note = new StringBuffer(note);
    }

    /**
     * 
     */
    public void appendNote(String note)
    {
      this.note.append(note);
    }
    
    /**
     * 
     */
    public void appendNote(char[] str, int offset, int len) 
    {
      this.note.append(str, offset, len) ;
    }  
    
    /**
     * Returns the value of raw.
     */
    public String getRaw()
    {
      return raw.toString();
    }
  
    /**
     * Sets the value of raw.
     * @param note The value to assign raw.
     */
    public void setRaw(String raw)
    {
      this.raw = new StringBuffer(raw);
    }

    /**
     * 
     */
    public void appendRaw(String raw)
    {
      this.raw.append(raw);
    }
    
    /**
     * 
     */
    public void appendRaw(char[] str, int offset, int len) 
    {
      this.raw.append(str, offset, len) ;
    }
    
    
    /**
     * Overwrites this Entry with the values of the specified object, but only, 
     * if they are not null, and not empty
     * @param that not allowed to be null
     * @exception IllegalArgumentException if <code>that</code> is null
     */
    public void overwrite(Entry that)
    { 
        if ( that == null )
            throw new IllegalArgumentException("that-Entry-object not allowed to be null");
      
        String str = that.getContent();
        if ( str != null && !"".equals(str) )
            setContent(str);
        
        str = that.getTitle();
        if ( str != null && !"".equals(str) )
            setTitle(str);
        
        str = that.getFileType();
        if ( str != null && !"".equals(str) )
            setFileType(str);
        
        str = that.getNote();
        if ( str != null && !"".equals(str) )
            setNote(str);
        
        str = that.getAuthor();
        if ( str != null && !"".equals(str) )
            setAuthor(str);
        
        str = that.getAuthorEmail();
        if ( str != null && !"".equals(str) )
            setAuthorEmail(str);
        
        Date date = that.getIssued();
        if ( date != null )
            setIssued(date);
        
        date = that.getLastModified();
        if ( date != null )
            setLastModified(date);
        
        String[] tags = that.getTags();
        if ( tags != null && tags.length > 0 )
            setTags(tags);
    }
}


