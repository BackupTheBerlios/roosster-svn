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
import java.util.Date;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryList extends AbstractList
{
    private int totalSize   = 0;
    private int offset	     = 0;
    private int limit       = -1;
    private List list       = new ArrayList();

    
    /**
     * Constructs an <code>EntryList</code> for which there is always:
     * <pre>
     * getTotalSize() == size()
     * </pre>
     */
    public EntryList() 
    {
        this.totalSize = -1;
    }
    
    
    /**
     * Constructs an <code>EntryList</code> for which <code>getTotalSize</code>
     * will return the value of the param <code>totalSize</code>.
     * @param totalSize a positive integer, that will be returned when calling
     * <code>getTotalSize()</code>.
     * @exception IllegalArgumentException if <code>totalSize</code> is smaller 0
     */
    public EntryList(int totalSize) 
    {
        if ( totalSize < 0 ) 
            throw new IllegalArgumentException("totalSize must be a positive number");
        
        this.totalSize = totalSize;
    }

    
    /**
     * @return the <code>Date</code>-object representing the modification time 
     * of the last modified entry in this Object's list.
     */
    public Date getLastModified() 
    {
        Date newest = new Date( System.currentTimeMillis() );
        for(int i = 0; i < list.size(); i++) {
            Entry entry = (Entry) list.get(i);
            if ( entry.getLastModified() != null ) {
                if ( newest.before(entry.getLastModified()) ) 
                    newest = entry.getLastModified();
            }
        }
        
        return newest;
    }

    /**
     * 
     */
    public int size()
    {
        return list.size();
    }


    /**
     * 
     */
    public int getTotalSize()
    {
        return totalSize == -1 ? size() : totalSize;
    }

    
    /**
     * 
     */
    public boolean addEntry(Entry entry)
    {
        return list.add(entry);
    }

    
    /**
     * @exception ArrayIndexOutOfBoundsException
     */
    public boolean add(Object obj)
    {
        return list.add(obj);
    }

    
    /**
     * @exception ArrayIndexOutOfBoundsException
     */
    public Object get(int i)
    {
        return list.get(i);
    }

    
    /**
     * @return null if <code>url</code> is null, or if no Entry with the 
     * specified URL is contained within this list.
     */
    public Entry getEntry(URL url)
    {
        Entry entry = null;
        
        if ( url != null ) { 
            for ( int i = 0; i < list.size(); i++ ) {
                if ( url.equals(getEntry(i).getUrl()) ) {
                    entry = getEntry(i);
                    break;
                }
            }
        }
            
        return entry;
    }
    
    
    /**
     * @exception ArrayIndexOutOfBoundsException
     */
    public Entry getEntry(int i)
    {
        return (Entry) list.get(i);
    }
    
    
    /**
     * Returns the value of offset.
     */
    public int getOffset()
    {
        return offset;
    }
  
    
    /**
     * Sets the value of offset.
     * @param offset The value to assign offset.
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }
  
    
    /**
     * Returns the value of limit. If it was not eplicitly set before, this will
     * be the value of {@link #size()}.
     */
    public int getLimit()
    {
        return limit == -1 ? size() : limit ;
    }

    
    /**
     * Sets the value of limit.
     * @param limit The value to assign limit.
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }
}
