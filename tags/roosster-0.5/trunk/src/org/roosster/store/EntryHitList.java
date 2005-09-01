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
import java.util.AbstractList;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryHitList extends AbstractList
{
    private static Logger LOG = Logger.getLogger(EntryHitList.class);
    private Hits hits;
    private IndexSearcher searcher;
  
    /**
     * Creates a new <code>HitList</code> instance.
     *
     * @param hits <code>Hits</code> to wrap
     */
    public EntryHitList(Hits hits, IndexSearcher searcher)
    {
        this.hits = hits;
        this.searcher = searcher;
    }
  
      
    /**
     * @return Objects of class Entry
     */
    public Object get(int index)
    {
        if ( hits == null || index < 0 || index >= size() )
            throw new ArrayIndexOutOfBoundsException(index);
      
        try {
            return new Entry(hits.doc(index), hits.score(index));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
  
      
    /**
     * @see java.util.List#size()
     */
    public int size() 
    {
        return hits == null ? 0 : hits.length();
    }
    
    
    /**
     * 
     */
    public void close() throws IOException 
    {
        if ( searcher != null ) 
            searcher.close(); 
    }

}