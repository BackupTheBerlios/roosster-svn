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
import org.roosster.store.Entry;
import org.roosster.Command;
import org.roosster.Registry;
import org.roosster.Output;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: GetEntryCommand.java,v 1.1 2004/12/03 14:30:13 firstbman Exp $
 */
public class GetEntryCommand extends AbstractCommand implements Command
{

    public static final String ARG_URL = "url";


    /**
     *
     */
    public void execute(Map arguments, Registry registry, Output output)
                 throws Exception
    {
        validateArguments(arguments, new String[] {ARG_URL});

        EntryStore store = new EntryStore();
        Entry entry = store.getEntry( new URL((String) arguments.get(ARG_URL)) );

        LOG.info("Found entry: "+entry);

        if ( entry != null ) {
            output.addEntry(entry);
            output.setTruncateLength(-1); // disable truncation
        }
    }

}
