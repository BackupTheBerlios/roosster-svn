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
import org.apache.log4j.Logger;

import org.roosster.util.StringUtil;
import org.roosster.util.XmlUtil;
import org.roosster.Constants;

/**
 * TODO make dateformat configurable
 * TODO silently remove duplicates when setting tags
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class Entry
{
    private static Logger LOG = Logger.getLogger(Entry.class);

    public static final String TAG_SEPARATOR= Constants.TAG_SEPARATOR;
  
    // this is used internally, to provide an (disk-) paging iterator
    // over all entries (every entry has this set 
    public static final String ENTRY_MARKER = "roosster.entry";

    public static final String ALL          = "all";
    public static final String CONTENT      = "content";
    public static final String MODIFIED     = "modified";
    public static final String ADDED        = "added";
    public static final String EDITED       = "edited";
    public static final String ISSUED       = "issued";
    public static final String URL          = "url";
    public static final String TITLE        = "title";
    public static final String AUTHOR       = "author";
    public static final String AUTHOREMAIL  = "authormail";
    public static final String FILETYPE     = "filetype";
    public static final String NOTE         = "note";
    public static final String TAGS         = "tags";
    public static final String PUBLIC       = "pub";
    public static final String RAW          = "raw";

    public static final float  TITLE_BOOST  = 100;
    public static final float  TAG_BOOST    = 5;
    public static final float  NOTE_BOOST   = 5;
    
    // enumerate the fields, by which result can be sorted
    public static final String[] STRING_SORT_FIELDS     = new String[]
    {URL, TITLE, AUTHOR, AUTHOREMAIL, FILETYPE};
    
    public static final String[] INTEGER_SORT_FIELDS     = new String[]
    {MODIFIED, ADDED, ISSUED};
    
    private float score = 0;
    
    private Date         issued	       = null;
    private Date         modified     	 = null;
    private Date         added	         = null;
    private Date         edited         = null;
    private URL          url            = null;
    private String       title          = "";
    private String       author         = "";
    private String       authorEmail    = "";
    private String       fileType	     = "";
    private StringBuffer content        = new StringBuffer();
    private StringBuffer note           = new StringBuffer();
    private StringBuffer raw            = new StringBuffer();
    private String[]     tags           = new String[0];
    private boolean      pub            = false;


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
    protected Entry(Document doc, float score)
    {
        this.score = score;
        try {
            if ( doc != null ) {
                setUrl(           new URL(doc.get(URL)) );
                setTitle(         doc.get(TITLE) );
                setAuthor(        doc.get(AUTHOR) );
                setAuthorEmail(   doc.get(AUTHOREMAIL) );
                setFileType(      doc.get(FILETYPE) );
                setContent(       doc.get(CONTENT) );
                setRaw(           doc.get(RAW) );
                setNote(          doc.get(NOTE) );
                setTags(          StringUtil.split(doc.get(TAGS), TAG_SEPARATOR) );
                setIssued(        StringUtil.parseEntryDate( doc.get(ISSUED) ) );
                setModified(      StringUtil.parseEntryDate( doc.get(MODIFIED) ) );
                setAdded(         StringUtil.parseEntryDate( doc.get(ADDED) ) );
                setEdited(        StringUtil.parseEntryDate( doc.get(EDITED) ) );
                setPublic(        StringUtil.parseBoolean( doc.get(PUBLIC) ) );
            }
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL '"+doc.get(URL)+"' is not a valid URL: "+ex.getMessage());
        }
    }


    /**
     *
     */
    protected Document getDocument()
    {
        Document doc = new Document();

        doc.add( Field.Keyword(ENTRY_MARKER,  ENTRY_MARKER) );
        doc.add( Field.Keyword(URL,           url.toString()) );
        doc.add( Field.Keyword(AUTHOR,        author) );
        doc.add( Field.Keyword(AUTHOREMAIL,   authorEmail) );
        doc.add( Field.Keyword(FILETYPE,      fileType) );
        doc.add( Field.Keyword(MODIFIED,      StringUtil.formatEntryDate(modified)) );
        doc.add( Field.Keyword(ADDED,         StringUtil.formatEntryDate(added)) );
        doc.add( Field.Keyword(EDITED,        StringUtil.formatEntryDate(edited)) );
        doc.add( Field.Keyword(ISSUED,        StringUtil.formatEntryDate(issued)) );
        doc.add( Field.Keyword(PUBLIC,        pub ? "true" : "false") );
        doc.add( Field.Text(CONTENT,          content.toString()) );
        
        Field titleField =  Field.Keyword(TITLE, title);
        titleField.setBoost(TITLE_BOOST); 
        doc.add(titleField);
        
        Field noteField = Field.Text(NOTE, note.toString());
        noteField.setBoost(NOTE_BOOST);
        doc.add(noteField);
        
        Field tagField = Field.Text(TAGS, StringUtil.join(tags, TAG_SEPARATOR));
        tagField.setBoost(TAG_BOOST);
        doc.add(tagField);
        
        doc.add( Field.UnIndexed(RAW, getRaw()) );
        doc.add( Field.UnStored(ALL,  title +" "+ author+" "+authorEmail+" "+
                                      content +" "+note+" "+StringUtil.join(tags, " ")) );
        return doc;
    }


    /**
     *
     */
    public String toString()
    {
        return "Entry: "+url.toString();
    }


    /**
     * two entries are equal if their URLs are equal 
     */
    public boolean equals(Object obj)
    {
        try {
            return url.equals( ((Entry) obj).getUrl() );
        } catch (ClassCastException ex) {
            return false;
        }
    }
    
    /**
     * 
     */
    protected float score()
    {
        return score;
    }
    
    // ============== Accessors ==============


    /**
     *
     */
    public void setPublic(boolean pub)
    {
        this.pub = pub;
    }


    /**
     *
     */
    public boolean getPublic()
    {
        return pub;
    }


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
    public Date getModified()
    {
        return modified;
    }


    /**
     * Sets the value of modified.
     * @param modified The value to assign modified.
     */
    public void setModified(Date modified)
    {
        this.modified = modified;
    }


    /**
     * Returns the value of lastFetched.
     */
    public Date getAdded()
    {
        return added;
    }


    /**
     * Sets the value of added.
     * @param added The value to assign added.
     */
    public void setAdded(Date added)
    {
        this.added = added;
    }


		/**
		 * Returns the value of Edited.
		 */
		public Date getEdited() {
				return edited;
		}


		/**
		 * Sets the value of Edited.
		 * @param Edited The value to assign Edited.
		 */
		public void setEdited(Date edited) {
				this.edited = edited;
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
      this.note = new StringBuffer(note == null ? "" : note);
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
        
        date = that.getModified();
        if ( date != null )
            setModified(date);
        
        String[] tags = that.getTags();
        if ( tags != null && tags.length > 0 )
            setTags(tags);
        
        setPublic(that.getPublic());
    }
    
}


