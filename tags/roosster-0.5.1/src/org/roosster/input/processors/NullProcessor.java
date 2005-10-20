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
package org.roosster.input.processors;

import java.net.URL;
import java.util.Date;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.roosster.store.Entry;
import org.roosster.Registry;
import org.roosster.InitializeException;
import org.roosster.input.ContentTypeProcessor;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class NullProcessor implements ContentTypeProcessor
{
    /**
     * Contains the MIME type of text documents: <code>text/plain</code>
     */
    public static final String FILE_TYPE = "text/plain";
    
    
    /**
     * Sets the filetype of the returned <code>Entry</code> always to the value
     * of the public <code>FILE_TYPE</code> constant of this class.
     * 
     * @return an array of <code>Entry</code>-objects that always contain on
     * object, that's never <code>null</code>.
     */
    public Entry[] process(URL url, InputStream stream, String encoding) throws Exception
    {
        Entry entry = new Entry(url);
        entry.setFileType(FILE_TYPE);
        entry.setAdded(new Date());
        return new Entry[] { entry };
    }


    /**
     *
     */
    public void init(Registry registry) throws InitializeException
    {}


    /**
     *
     */
    public boolean isInitialized()
    {
        return true;
    }


    /**
     *
     */
    public void shutdown(Registry registry) throws Exception
    {}


}
