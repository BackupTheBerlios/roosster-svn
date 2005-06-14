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

import org.roosster.Constants;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Output;
import org.roosster.store.EntryStore;
import org.roosster.store.Entry;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class SearchCommand extends AbstractCommand implements Command
{


    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        String query = (String) arguments.get(Constants.ARG_QUERY);
        
        EntryStore store = (EntryStore) registry.getPlugin("store");
        if ( query != null && !"".equals(query) ) {
            output.setEntries( store.search(query) );
        } else {
            output.addOutputMessage("No queryString specified. Returning recently added Entries!");
            Date oneWeekAgo = new Date(System.currentTimeMillis() - 604800000); 
            output.setEntries( store.getEntriesByDate(Entry.ADDED, oneWeekAgo,null) );
        }
    }


    /**
     */
    public String getName()
    {
        return "Search Entries";
    }
}
