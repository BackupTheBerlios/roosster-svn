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
package org.roosster.output;

import java.io.PrintWriter;
import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.OutputMode;
import org.roosster.Output;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.util.StringUtil;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: TextMode.java,v 1.1 2004/12/03 14:30:14 firstbman Exp $
 */
public class TextMode implements OutputMode
{
    private String contentType = DEF_CONTENT_TYPE;

    /**
     *
     */
    public void output(Registry registry, Output output, PrintWriter stream, EntryList entries)
                throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        StringUtil util = new StringUtil(registry);
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.getEntry(i);
          
            stream.println("URL:           "+ entry.getUrl() );
            stream.println("Title:         "+ entry.getTitle() );
            stream.print("Author:        "+ entry.getAuthor() );
            stream.println( "".equals(entry.getAuthorEmail()) ? "" : " <"+entry.getAuthorEmail()+">");
            stream.println("Issued:        "+ entry.getIssued() );
            stream.println("Last-Modified: "+ entry.getLastModified() );
            stream.println("Last-Fetched:  "+ entry.getLastFetched() );
            stream.println("FileType:      "+ entry.getFileType() );
            stream.println("Tags:          "+ StringUtil.joinStrings(entry.getTags(), Entry.TAG_SEPARATOR) );
            stream.println("Note:          "+ util.truncate(entry.getNote()) );
            stream.println("Content:");
            stream.println( util.truncate(entry.getContent()) );
        }

        stream.println();
    }

    /**
     *
     */
    public String getContentType() 
    {
        return contentType;
    }

    
    /**
     *
     */
    public void setContentType(String type)
    {
        this.contentType = type;
    }

}
