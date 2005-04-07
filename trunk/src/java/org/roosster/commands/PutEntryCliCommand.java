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
package org.roosster.commands;

import java.util.Map;
import java.util.Date;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;

import org.roosster.store.EntryStore;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.input.UrlFetcher;
import org.roosster.Constants;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.util.StringUtil;
import org.roosster.util.DateUtil;

/**
 * This command is not meant to be executed alone, but rather in a chain, 
 * as it only prepares the input, without really taking any actions.
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class PutEntryCliCommand extends AbstractCommand implements Command, Constants
{

    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        validateArguments(arguments, new String[] {ARG_URL});

        URL url = new URL((String) arguments.get(ARG_URL));

        LOG.debug("Preprocessing Entry in command '"+getName()+"'");
        
        Entry entry = new Entry(url);
        
        //
        // TODO give meaningful error message if date parsing fails 
        // (if date string is not null && not empty but parse method returns null
        //
        
        String title    = (String) arguments.get(ARG_TITLE);
        String note     = (String) arguments.get(ARG_NOTE);
        String tags     = (String) arguments.get(ARG_TAGS);
        String issued   = (String) arguments.get(ARG_ISSUED);
        String modified = (String) arguments.get(ARG_MODIFIED);
        String pubStr   = (String) arguments.get(ARG_PUBLIC);
        String type     = (String) arguments.get(ARG_FILETYPE);
        String author   = (String) arguments.get(ARG_AUTHOR);
        String email     = (String) arguments.get(ARG_AUTHOREMAIL);
        if ( title != null )
            entry.setTitle(title);
        if ( note != null )
            entry.setNote(note);
        if ( type != null ) 
            entry.setFileType(type);
        if ( author != null ) 
            entry.setAuthor(author);
        if ( email != null ) 
            entry.setAuthorEmail(email);
        if ( issued != null )
            entry.setIssued(DateUtil.parseEntryDate(issued));
        if ( modified != null )
            entry.setModified(DateUtil.parseEntryDate(modified));
        if ( tags != null ) 
            entry.setTags( StringUtil.split(tags, TAG_SEPARATOR) );
        if ( pubStr != null ) 
            entry.setPublic( StringUtil.parseBoolean(pubStr) );
        
        EntryList list = new EntryList();
        list.add(entry);
        
        LOG.debug("Putting the entryList back into the arguments at "+PARAM_ENTRIES);
        
        arguments.put(PARAM_ENTRIES, list);    
    }


    /**
     */
    public String getName()
    {
        return "Put Entry";
    }

}
