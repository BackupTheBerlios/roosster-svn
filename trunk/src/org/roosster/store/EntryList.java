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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryList extends AbstractList
{
    private int totalSize = 0;
    private List list = new ArrayList();


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
    public void setTotalSize(int totalSize)
    {
        this.totalSize = totalSize;
    }


    /**
     * 
     */
    public int getTotalSize()
    {
        return totalSize;
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
     * @exception ArrayIndexOutOfBoundsException
     */
    public Entry getEntry(int i)
    {
        return (Entry) list.get(i);
    }
}
