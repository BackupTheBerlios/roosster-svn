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

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.security.*;

import org.apache.log4j.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexWriter;

import org.roosster.util.StringUtil;
import org.roosster.*;

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
    public static final String PROP_CREATEIND = "store.createindex";

    public static final String DEF_INDEXDIR   = "index";

    private static final String URLHASH    = "urlhash";

    private Registry registry        = null;
    private String   indexDir        = null;
    private Class    analyzerClass   = null;
    private String   createIndexProp = null;
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

        createIndexProp = conf.getProperty(PROP_CREATEIND);
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
     */
    public EntryList getAllEntries() throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");
        
        IndexReader reader = null;
        EntryList entries = null;
        try {
            reader = getReader();
            int numdocs = reader.numDocs();
            
            entries = new EntryList(numdocs);
            
            int limit  = getLimit();
            int offset = getOffset();
            LOG.info("Total number of Entries is "+numdocs);
          
            if ( numdocs > offset ) {
                int lastElem = numdocs >= offset+limit ? offset+limit : numdocs;

                for (int i = offset; i < lastElem; i++) {
                    if ( !reader.isDeleted(i) ) 
                        entries.add( new Entry(reader.document(i), 0) );
                }
                
                LOG.info("Showing Entries "+offset+" to "+ lastElem);      
            }
            
        } finally {
            if ( reader != null ) 
                reader.close();          
        }
          
        return entries;
    }
    
      
    /**
     * @return an {@link EntryList EntryList}-objects that's never <code>null</code>
     */
    public EntryList search(String queryStr) throws IOException, ParseException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        QueryParser parser = new QueryParser(Entry.ALL, createAnalyzer());
        parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
        Query query = parser.parse(queryStr);

        LOG.info("Executing Query: "+query);

        IndexSearcher searcher = new IndexSearcher(indexDir);
        
        Sort sort = determineSort();
        LOG.debug("Sort Instance: "+sort);
        
        Hits hits = searcher.search(query, sort);
        
        LOG.info("Found "+hits.length()+" matches for query: <"+query+">");
        
        return fillEntryList(hits);
    }

    
    /**
     * @return an {@link EntryList EntryList}-objects that's never <code>null</code>
     */
    public EntryList getChangedEntries(Date after, Date before) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");
        
        if ( after == null )
            after = new Date();
        if ( before == null )
            before = new Date();
        
        LOG.debug("Getting Entries edited after "+after+" and before "+before);
        
        Term afterTerm = new Term(Entry.EDITED, StringUtil.formatEntryDate(after));
        Term beforeTerm = new Term(Entry.EDITED, StringUtil.formatEntryDate(before));
        
        Query query = new RangeQuery(afterTerm, beforeTerm, true); // search inclusively
        IndexSearcher searcher = new IndexSearcher(indexDir);
        
        Sort sort = determineSort();
        LOG.debug("Sort Instance: "+sort);
        
        Hits hits = searcher.search(query, sort);        

        LOG.info("Found "+hits.length()+" matches for query: <"+query+">");
        
        return fillEntryList(hits);
    }
    

    /**
     * @return <code>null</code> if there is no entry with the
     * specified URL
     */
    public Entry getEntry(URL url) throws IOException
    {
        Entry[] entries = getEntries(url, null);

        if ( entries.length > 1 )
            LOG.warn("More than one Entry found for URL "+url);

        return entries.length > 0 ? entries[0] : null;
    }


    /**
     *
     */
    public void addEntry(Entry entry) throws IOException
    {
        addEntry(entry, false);
    }


    /**
     *
     */
    public void addEntry(Entry entry, boolean force) throws IOException
    {
        addEntries(new Entry[] {entry}, force);
    }


    /**
     *
     */
    public void addEntries(Entry[] entries) throws IOException
    {
        addEntries(entries, false);
    }

    /**
     * @exception DuplicateEntryException
     */
    public void addEntries(Entry[] entries, boolean force) throws IOException
    {
        if ( entries != null ) {

            if ( !force && IndexReader.indexExists(indexDir) ) {
                IndexReader reader = null;
                try {
                    reader = getReader();
                    for (int i = 0; i < entries.length; i++) {
                        Entry[] stored = getEntries(entries[i].getUrl(), reader);
                        if ( stored.length > 0 )
                            throw new DuplicateEntryException(entries[i].getUrl());
                    }

                } finally {
                    if ( reader != null  )
                        reader.close();
                }
            }

            storeEntries(entries);
        }
    }


    /**
     *
     */
    public int deleteEntry(URL url) throws IOException
    {
        return deleteEntry(url, null);
    }


    // ============ protected Helper methods ============


    /**
     *
     */
    protected Entry[] getEntries(URL url, IndexReader reader) throws IOException
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

            Term term = new Term( URLHASH, computeHash(url.toString()) );
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
     *
     */
    protected int deleteEntry(URL url, IndexReader reader) throws IOException
    {
        if ( !isInitialized() )
            throw new IllegalStateException("Database must be initialized before use!");

        if ( url == null )
            throw new IllegalArgumentException("Parameter 'url' is not allowed to be null");

        boolean closeReader = false;
        try {
            LOG.debug("Deleting Entry with URL: "+url.toString());

            if ( reader == null ) {
                reader = getReader();
                closeReader = true;
            }

            Term term = new Term( URLHASH, computeHash(url.toString()) );
            int numDeleted = reader.delete(term);

            LOG.info("Deleted "+numDeleted+" Entries");

            return numDeleted;

        } finally {
            if  ( closeReader && reader != null )
                reader.close();
        }
    }



    /**
     * @param entry the <code>Entry</code>-object that should be added to the store,
     * if this is null, no action will be taken.
     * @exception IOException if the writing to the index fails due to some I/O reason
     * @exception IllegalStateException if an instance of this class was not properly
     * initialized before calling this method
     */
    private synchronized void storeEntries(Entry[] entries) throws IOException
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
                    deleteEntry(entries[i].getUrl(), reader);
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
                doc.add( Field.Keyword(URLHASH, computeHash(entries[i].getUrl().toString())) );
                writer.addDocument(doc);
            }

            writer.optimize(); // TODO should this be delayed in a web env?

        } finally {
            if ( writer != null )
                writer.close();

            if ( reader != null )
                reader.close();
        }
    }


    /**
     * @exception IllegalArgumentException if the provided sort field is not available
     * for sorting
     */
     protected Sort determineSort() 
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
        boolean createIndex = false;

        if ( createIndexProp == null  ) {
            // if it's not defined, see if an index exists, if not set flag to create one
            createIndex = IndexReader.indexExists(indexDir) ? false : true ;
        } else if ( "1".equals(createIndexProp.trim()) || "true".equalsIgnoreCase(createIndexProp.trim()) ) {
            createIndex = true;
        }

        IndexWriter writer = new IndexWriter(indexDir, createAnalyzer(), createIndex);
        writer.maxFieldLength = 1000000;
        return writer;
    }


    /**
     *
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
        if ( hits == null ) 
            throw new IllegalArgumentException("Parameter 'hits' is not allowed to be null");
      
        int hitsNum = hits.length();

        EntryList entries = new EntryList(hitsNum);

        int limit  = getLimit();
        int offset = getOffset();
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
    private String computeHash(String str)
    {
        String algorithm = "MD5";

        try {

            MessageDigest md5 = MessageDigest.getInstance(algorithm);
            byte[] end = md5.digest(str.getBytes());

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
