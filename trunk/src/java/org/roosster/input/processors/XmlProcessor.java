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

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;

import org.roosster.store.Entry;
import org.roosster.Registry;
import org.roosster.InitializeException;
import org.roosster.OperationException;
import org.roosster.input.ContentTypeProcessor;
import org.roosster.xml.*;

/**
 * TODO: Tries to determine, if the stream is a feed, and parses it, if this is the case.
 * If not, it just indexes it.
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class XmlProcessor implements ContentTypeProcessor
{
    private static Logger LOG = Logger.getLogger(XmlProcessor.class.getName());


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
    {
    }


    /**
     *
     */
    public Entry[] process(URL url, InputStream stream, String encoding) throws Exception
    {
        if ( url == null || stream == null || encoding == null || "".equals(encoding) )
            throw new IllegalArgumentException("No parameter is allowed to be null");

        try {
            // TODO only use FeedParser if it's really a feed

            FeedParser parser = new FeedParser();
            return parser.parse(url, stream, encoding);

        } catch(Exception ex) {
            throw new OperationException(ex);
        }

    }
}
