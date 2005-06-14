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
import java.net.URL;

import org.apache.log4j.Logger;

import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.util.StringUtil;
import org.roosster.Command;
import org.roosster.Constants;
import org.roosster.Registry;
import org.roosster.Output;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class AddUrlCliCommand extends AbstractCommand implements Command, Constants
{
    private static Logger LOG = Logger.getLogger(AddUrlCliCommand.class);
    
    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        validateArguments(arguments, new String[] {ARG_URL});

        Entry entry = new Entry(new URL( (String) arguments.get(ARG_URL)) );
        
        String title = (String) arguments.get(ARG_TITLE);
        entry.setTitle( title == null ? "" : title ); 
        
        String note  = (String) arguments.get(ARG_NOTE);
        entry.setNote( note == null ? "" : note );

        String type  = (String) arguments.get(ARG_FILETYPE);
        entry.setFileType( type == null ? "" : type );

        String author  = (String) arguments.get(ARG_AUTHOR);
        entry.setAuthor( author == null ? "" : author );

        String email  = (String) arguments.get(ARG_AUTHOREMAIL);
        entry.setAuthorEmail( email == null ? "" : email );

        String tags  = (String) arguments.get(ARG_TAGS);
        if ( tags != null )
            entry.setTags( StringUtil.split(tags, TAG_SEPARATOR) );
        
        entry.setPublic( StringUtil.parseBoolean( (String) arguments.get(ARG_PUBLIC) ) );
        
        EntryList list = new EntryList();
        list.add(entry);
        
        LOG.debug("Putting the one entry back into arguments at "+PARAM_ENTRIES);
        
        arguments.put(PARAM_ENTRIES, list);           
    }


    /**
     */
    public String getName()
    {
        return "Add URL (CLI)";
    }

}
