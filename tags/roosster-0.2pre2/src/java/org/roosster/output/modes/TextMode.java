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
package org.roosster.output.modes;

import java.io.PrintWriter;

import org.roosster.Output;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.store.Entry;
import org.roosster.store.EntryList;
import org.roosster.output.OutputMode;
import org.roosster.util.StringUtil;
import org.roosster.Constants;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class TextMode extends AbstractOutputMode implements OutputMode, Constants
{

    /**
     *
     */
    public void output(Output output, PrintWriter stream, EntryList entries)
                throws OperationException
    {
        if ( entries == null )
            throw new IllegalArgumentException("entries parameter is not allowed to be null");

        String truncStr = registry.getConfiguration().getProperty(PROP_TRUNCLENGTH, "-1");
        int truncate = Integer.valueOf(truncStr).intValue();
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.getEntry(i);
          
            stream.println("URL:           "+ entry.getUrl() );
            stream.println("Title:         "+ entry.getTitle() );
            stream.print("Author:        "+ entry.getAuthor() );
            stream.println( "".equals(entry.getAuthorEmail()) ? "" : " <"+entry.getAuthorEmail()+">");
            stream.println("Issued:        "+ entry.getIssued() );
            stream.println("Modified:      "+ entry.getModified() );
            stream.println("Added:         "+ entry.getAdded() );
            stream.println("Edited:        "+ entry.getEdited() );
            stream.println("FileType:      "+ entry.getFileType() );
            stream.println("Tags:          "+ StringUtil.join(entry.getTags(), TAG_SEPARATOR) );
            stream.println("Note:          "+ StringUtil.truncate(entry.getNote(), truncate) );
            stream.println("Content:");
            stream.println( StringUtil.truncate(entry.getContent(), truncate) );
        }

        stream.println();
    }

}
