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

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.roosster.Configuration;
import org.roosster.Constants;
import org.roosster.InitializeException;
import org.roosster.Plugin;
import org.roosster.Registry;
import org.roosster.util.DateUtil;

/**
 * TODO better synchronizing
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryStore implements Plugin, Constants
{
    private static Logger LOG = Logger.getLogger(EntryStore.class);


    /**
     */
    public static final String PROP_INDEXDIR  = "store.indexdir";
    public static final String PROP_ANALYZER  = "store.analyzerclass";

    public static final String DEF_INDEXDIR   = "index";

    private static final String URLHASH    = "urlhash";

    private Registry registry        = null;
    private String   indexDir        = null;
    private Class    analyzerClass   = null;
    private boolean  initialized     = false;
    
    
    /**
     */
    public EntryStore()
    {
    }


    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {
        this.registry = registry;
        Configuration conf = registry.getConfiguration();

        LOG.info("Initializing Plugin "+getClass());

        String className = null;
        try {
            className = conf.getProperty(PROP_ANALYZER);
            if ( className == null )
                throw new InitializeException("No '"+PROP_ANALYZER+"'-argument provided");

            LOG.debug("Trying to load analyzer-class: "+className);
            analyzerClass = Class.forName(className);
            Analyzer testInstance = (Analyzer) analyzerClass.newInstance();

            
        } catch (ClassCastException ex) {
            throw new InitializeException("Specified class is not an instance of "+
                                          Analyzer.class+": "+className);
        } catch (ClassNotFoundException ex) {
            throw new InitializeException("Can't load analyzer-class: "+className);
            
        } catch (Exception ex) {
            throw new InitializeException("Exception occured during database init", ex);
        }


        // determine indexDir and check if it exists
        indexDir = conf.getProperty(PROP_INDEXDIR);
        if ( indexDir == null || "".equals(indexDir) ) {
            String homeDir = conf.getHomeDir();
            if ( homeDir != null )
                indexDir = homeDir+"/"+DEF_INDEXDIR;
            else
                indexDir = DEF_INDEXDIR;
        }

        LOG.debug("Directory of index is: "+indexDir);
        LOG.info("Finished initialize of "+getClass());

        initialized = true;
    }


    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {
        LOG.debug("Shutting down EntryStore!");
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
    public int getLimit()
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        int limit = 10;

        try {
            String limitStr  = registry.getConfiguration().getProperty(PROP_LIMIT);
            limit = limitStr != null ?  Integer.valueOf(limitStr).intValue() : limit;
        } catch(NumberFormatException ex) { }

        return limit;
    }


    /**
     */
    public int getOffset()
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        int offset = 0;
        try {
            String offsetStr = registry.getConfiguration().getProperty(PROP_OFFSET);

            offset = offsetStr != null ?  Integer.valueOf(offsetStr).intValue() : offset;
            if ( offset < 0 )
                offset = 0;

        } catch(NumberFormatException ex) { }

        return offset;
    }


    /**
     */
    public int getDocNum() throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        int numdocs = 0;
        IndexReader reader = null;
        try {
            reader = getReader();
            numdocs = reader.numDocs();
        } finally {
            if ( reader != null ) 
                reader.close();
        }
        
        return numdocs; 
    }
    
    
    /**
     * 
     * @return
     * @throws IOException
     */
    public List getAllTags() throws IOException 
    {
        List tags = new ArrayList();
       
        if ( IndexReader.indexExists(indexDir) ) {
           
           IndexReader reader = null;
           try {
               LOG.debug("Getting all Tags from index");
               
               reader = getReader();
               TermEnum terms = reader.terms(new Term(Entry.TAGS, ""));
               
               while ( Entry.TAGS.equals( terms.term().field() ) ) {
                   tags.add( terms.term().text() );
    
                   if ( !terms.next() )
                       break;
               }           
               
           } finally {
               if ( reader != null )
                   reader.close();
           }
       }
       return tags;
    }
    
    
    /**
     * 
     */
    public EntryList getAllEntries(boolean pub) throws IOException
    {
        return getAllEntries(getOffset(), getLimit(), pub);
    }


    /**
     * @param pub if true, this method returns only Entries which pub: field is 
     * set to "true", if false all Entries are returned
     */
    public EntryList getAllEntries(int offset, int limit, boolean pub) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( !IndexReader.indexExists(indexDir) ) 
            return new EntryList();
        
        
        IndexSearcher searcher = null;
        try {      
            
            BooleanQuery query = new BooleanQuery();
            
            TermQuery term = new TermQuery(new Term(Entry.ENTRY_MARKER, Entry.ENTRY_MARKER));
            query.add(term, true, false);
            
            if ( pub ) {
                TermQuery pubTerm = new TermQuery(new Term(Entry.PUBLIC, "true"));
                query.add(pubTerm, true, false);
            }
            
            searcher = new IndexSearcher(indexDir);
            
            Sort sort = determineSort();
            LOG.debug("Sort Instance: "+sort);
            
            Hits hits = searcher.search(query, sort);        
    
            LOG.debug("Found "+hits.length()+" matches for query: <"+query+">");            
            
            return fillEntryList(hits, offset, limit);
            
        } finally {
            if ( searcher != null ) 
                searcher.close(); 
        }
    }
    
      
    /**
     * @return an {@link EntryList EntryList}-objects that's never <code>null</code>
     */
    public EntryList search(String queryStr) throws IOException, ParseException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( !IndexReader.indexExists(indexDir) ) 
            return new EntryList();

        
        IndexSearcher searcher = null;
        try {     
            QueryParser parser = new QueryParser(Entry.ALL, createAnalyzer());
            parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
            Query query = parser.parse(queryStr);
    
            searcher = new IndexSearcher(indexDir);
            
            Sort sort = determineSort();
            LOG.debug("Sort Instance: "+sort);
            
            Hits hits = searcher.search(query, sort);
            
            LOG.info("Found "+hits.length()+" matches for query: <"+query+">");
            
            return fillEntryList(hits);
            
        } finally {
            if  ( searcher != null )
                searcher.close();
        }
    }

    
    /**
     * @param field
     * @param after default to current Date
     * @param before default to current Date
     * @return an {@link EntryList EntryList}-objects that's never <code>null</code>
     */
    public EntryList getEntriesByDate(String field, Date after, Date before) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");
        
        if ( field == null || "".equals(field) )
            throw new IllegalArgumentException("Parameter 'field' is not allowed to be null or empty");
        
        if ( !IndexReader.indexExists(indexDir) ) 
            return new EntryList();

        
        if ( after == null )
            after = new Date();
        if ( before == null )
            before = new Date();
        
        
        IndexSearcher searcher = null;
        try {        
            Term afterTerm = new Term(field, DateUtil.formatSearchableEntryDate(after));
            Term beforeTerm = new Term(field, DateUtil.formatSearchableEntryDate(before));
            
            Query query = new RangeQuery(afterTerm, beforeTerm, true); // search inclusively
            searcher = new IndexSearcher(indexDir);
            
            Sort sort = determineSort();
            LOG.debug("Sort Instance: "+sort);
            
            Hits hits = searcher.search(query, sort);        
    
            LOG.debug("Found "+hits.length()+" matches for query: <"+query+">");
            
            return fillEntryList(hits, 0, Integer.MAX_VALUE);
            
        } finally {
            if  ( searcher != null )
                searcher.close();
        }
    }
    

    /**
     * @return <code>null</code> if there is no entry with the
     * specified URL
     */
    public Entry getEntry(URL url) throws IOException
    {
        Entry[] entries = new Entry[0];
        
        if ( IndexReader.indexExists(indexDir) ) {
            entries = getEntries(url, null);
    
            if ( entries.length > 1 )
                LOG.warn("More than one Entry found for URL "+url);
        }

        return entries.length > 0 ? entries[0] : null;
    }


    /**
     *
     */
    public Entry addEntry(Entry entry) throws IOException
    {
        return addEntry(entry, false);
    }


    /**
     *
     */
    public Entry addEntry(Entry entry, boolean force) throws IOException
    {
        return addEntries(new Entry[] {entry}, force)[0];
    }


    /**
     *
     */
    public Entry[] addEntries(Entry[] entries) throws IOException
    {
        return addEntries(entries, false);
    }

    /**
     * @exception DuplicateEntryException
     */
    public Entry[] addEntries(Entry[] entries, boolean force) throws IOException
    {
        if ( entries == null ) 
            return new Entry[0];

        // check if any of the Entries are already stored in index 
        if ( !force && IndexReader.indexExists(indexDir) ) {
          
            IndexReader reader = null;
            try {
                List duplicateUrls = new ArrayList();
              
                reader = getReader();
                for (int i = 0; i < entries.length; i++) {
                    Entry[] stored = getEntries(entries[i].getUrl(), reader);
                    if ( stored.length > 0 )
                        duplicateUrls.add(entries[i].getUrl());
                }
                    
                // now throw exception if we encountered one or more duplicate Entries
                if ( duplicateUrls.size() > 0 )
                    throw new DuplicateEntriesException((URL[]) duplicateUrls.toArray(new URL[0]) );                    
    
            } finally {
                if ( reader != null  )
                    reader.close();
            }
            
        }

        return storeEntries(entries);
    }


    /**
     *
     */
    public int deleteEntry(URL url) throws IOException
    {
        return deleteEntries(new URL[] {url}, null);
    }


    /**
     *
     */
    public int deleteEntries(URL[] urls) throws IOException
    {
        return deleteEntries(urls, null);
    }


    /**
     *
     */
    public int deleteEntries(Entry[] entries) throws IOException
    {
        if ( entries == null )
            throw new IllegalArgumentException("Parameter 'entries' is not allowed to be null!");
      
        List urls = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            if ( entries[i] != null )
                urls.add(entries[i].getUrl());
        }
        
        return deleteEntries((URL[]) urls.toArray(new URL[0]), null);
    }


    // ============ private Helper methods ============


    /**
     *
     */
    private Entry[] getEntries(URL url, IndexReader reader) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( url == null )
            throw new IllegalArgumentException("Parameter 'url' is not allowed to be null");

        boolean closeReader = false;
        TermDocs docs = null;
        try {
            LOG.debug("Getting Entry with URL: "+url);

            if ( reader == null ) {
                reader = getReader();
                closeReader = true;
            }

            Term term = new Term( URLHASH, computeHash(url) );
            docs = reader.termDocs(term);

            List entries = new ArrayList();
            while ( docs.next() ) {
                entries.add( new Entry( reader.document(docs.doc()), 0) );
            }

            LOG.debug("Found "+entries.size()+" entries for URL "+url);

            return (Entry[]) entries.toArray(new Entry[0]);

        } finally {

            if  ( closeReader && reader != null )
                reader.close();

            if ( docs != null )
                docs.close();
        }
    }

    
    /**
     * The deletion of the specified Entries is synchronized with the adding of
     * Entries. So it's not possible to simultaneously add and delete Entries.
     * 
     * @param urls the URLs of the Entries that should be deleted, may not be null
     * @return number of deleted Entries
     * @exception IOException if the writing to the index fails due to some I/O reason
     * @exception IllegalStateException if the object was not properly initialized yet.
     * @exception IllegalArgumentException if parameter <code>urls</code> is null.
     */
    private int deleteEntries(URL[] urls, IndexReader reader) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( urls == null )
            throw new IllegalArgumentException("Parameter 'urls' is not allowed to be null");

        
        int numDeleted = 0;
        boolean closeReader = false;
        
        try {
            if ( reader == null ) {
                reader = getReader();
                closeReader = true;
            }

            
            for (int i = 0; i < urls.length; i++) { 
                numDeleted += reader.delete( new Term(URLHASH, computeHash(urls[i])) );
            }

            LOG.debug("Deleted "+numDeleted+" Entries for URLs: "+ Arrays.asList(urls));

        } finally {
            if  ( closeReader && reader != null )
                reader.close();
        }
        
        persistLastUpdate();
        
        return numDeleted;
    }

    
    /**
     * It's not possible to simultaneously add and delete Entries.
     *
     * @param entry the <code>Entry</code>-object that should be added to the store,
     * if this is null, no action will be taken.
     * @exception IOException if the writing to the index fails due to some I/O reason
     * @exception IllegalStateException if the object was not properly initialized yet.
     */
    private synchronized Entry[] storeEntries(Entry[] entries) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( entries == null )
            throw new IllegalArgumentException("Parameter 'entries' is not allowed to be null");

        
        IndexWriter writer = null;
        IndexReader reader = null;
        try {
            if ( IndexReader.indexExists(indexDir) ) {
                reader = getReader();
                for (int i = 0; i < entries.length; i++ ) {
                    deleteEntries(new URL[] {entries[i].getUrl()}, reader);
                }
                reader.close();
                reader = null;
            }

            writer = getWriter();
            
            Date now = new Date();
            for (int i = 0; i < entries.length; i++ ) {
                LOG.debug("Adding Entry to index: "+ entries[i].getUrl().toString());

                entries[i].setEdited(now);
                
                Document doc = entries[i].getDocument();
                doc.add( Field.Keyword(URLHASH, computeHash(entries[i].getUrl())) );
                writer.addDocument(doc);
            }

            writer.optimize(); // TODO should this be delayed in a web env?

        } finally {
            if ( writer != null )
                writer.close();

            if ( reader != null )
                reader.close();
        }
        
        persistLastUpdate();

        return entries;
    }


    /**
     * @exception IllegalArgumentException if the provided sort field is not available
     * for sorting
     * @exception IllegalStateException if the object was not properly initialized yet.     
     */
     private Sort determineSort() 
     {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");
        
        String sortField = registry.getConfiguration().getProperty(PROP_SORTFIELD);
        
        LOG.info("Specified Sort Field: "+sortField);
            
        Sort sort = Sort.RELEVANCE; 
        if ( sortField != null && !"".equals(sortField) ) {
            
            for ( int i = 0; i < Entry.STRING_SORT_FIELDS.length; i++ ) {
                if ( Entry.STRING_SORT_FIELDS[i].equals(sortField) ) 
                    return new Sort( new SortField(sortField, SortField.STRING) );
            }
          
            for ( int i = 0; i < Entry.INTEGER_SORT_FIELDS.length; i++ ) {
                if ( Entry.INTEGER_SORT_FIELDS[i].equals(sortField) ) 
                    return new Sort( new SortField(sortField, SortField.INT) );
            }
          
            if ( sort == null )
                throw new IllegalArgumentException("Illegal sort field: "+sortField);
        } 
        
        return sort;
     }

    // ============ private Helper methods ============


    /**
     *
     */
    private IndexWriter getWriter() throws IOException
    {
        boolean createIndex = IndexReader.indexExists(indexDir) ? false : true ;

        IndexWriter writer = new IndexWriter(indexDir, createAnalyzer(), createIndex);
        writer.maxFieldLength = 1000000;
        
        return writer;
    }


    /**
     * @return the current {@link #reader reader}-object. Creates a new instance
     * if not happened already.
     */
    private IndexReader getReader() throws IOException
    {
        return IndexReader.open(indexDir);
    }


    /**
     *
     */
    private Analyzer createAnalyzer() throws IllegalStateException
    {
        String exceptionMsg = null;
        Analyzer analyzer   = null;

        try {

            analyzer =  (Analyzer) analyzerClass.newInstance();

        } catch (InstantiationException ex) {
            exceptionMsg = "The provided Analyzer-class could not be instantiated: "+ex.getMessage();
        } catch (ExceptionInInitializerError ex) {
            exceptionMsg = "The provided Analyzer-class could not be instantiated: "+ex.getMessage();
        } catch (SecurityException ex) {
            exceptionMsg = "The provided Analyzer-class could not be instantiated: "+ex.getMessage();
        } catch (IllegalAccessException ex) {
            exceptionMsg = "The provided Analyzer-class could not be instantiated: "+ex.getMessage();
        } finally {

            if ( exceptionMsg != null ) throw new IllegalStateException(exceptionMsg);

        }

        return analyzer;
    }
    
    
    /**
     * 
     */
    private EntryList fillEntryList(Hits hits) throws IOException
    {
        return fillEntryList(hits, getOffset(), getLimit());
    }

    
    /**
     * 
     */
    private EntryList fillEntryList(Hits hits, int offset, int limit) throws IOException
    {
        if ( hits == null ) 
            throw new IllegalArgumentException("Parameter 'hits' is not allowed to be null");
      
        int hitsNum = hits.length();

        EntryList entries = new EntryList(hitsNum);

        LOG.debug("Offset is : "+offset+" / Limit is: "+limit);

        entries.setLimit(limit);
        entries.setOffset(offset);
        
        if ( hitsNum > offset ) {
            // Hits class throws an IndexOutOfBoundsException just like an
            // array, when an element is requested, that's outside the
            // hits index bounds
            int lastElem = hitsNum >= offset+limit ? offset+limit : hitsNum;

            for(int i = offset; i < lastElem; i++) {
                entries.add( new Entry(hits.doc(i), hits.score(i)) );
            }
        }

        return entries;
    }


    /**
     *
     */
    private void persistLastUpdate() throws IOException
    {
        registry.getConfiguration().setProperty(LAST_UPDATE, System.currentTimeMillis() +"");
        registry.getConfiguration().persist(new String[] {LAST_UPDATE});
    }

    
    /**
     *
     */
    private String computeHash(URL url)
    {
        String algorithm = "MD5";

        try {

            MessageDigest md5 = MessageDigest.getInstance(algorithm);
            byte[] end = md5.digest(url.toString().getBytes());

            StringBuffer endString = new StringBuffer();
            for (int i=0; i < end.length; i++) {
                  // convert unsigned byte into signed int
                  int tmp = end[i] & 0xFF;
                  endString.append( tmp <  16 ? "0":"" ).append( Integer.toHexString(tmp) );
            }

            return endString.toString();

        } catch(NoSuchAlgorithmException ex) {
            throw new IllegalStateException("FATAL: Your system does not support '"+algorithm+"' hashing");
        }
    }

}
