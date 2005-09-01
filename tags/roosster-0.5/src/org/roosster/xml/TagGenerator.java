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
package org.roosster.xml;

import java.io.PrintStream;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.roosster.Constants;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.store.EntryStore;
import org.roosster.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class TagGenerator implements EntryTags 
{
    private static Logger LOG = Logger.getLogger(TagGenerator.class);
    
    public static final String TAGS = "tags";
    public static final String TAG  = "tag";
    

    /**
     */
    public void outputAllTags(Registry registry, PrintStream stream) throws OperationException
    {
        try {
            EntryStore store = (EntryStore) registry.getPlugin(Constants.PLUGIN_STORE);
            List allTags = store.getAllTags();
            
            
            Document doc = XmlUtil.getDocumentBuilder().newDocument();
            Element root = XmlUtil.createChild(doc, TAGS);
            
            for ( int i = 0; i < allTags.size(); i++ ) {
                XmlUtil.createTextChild(root, TAG, (String) allTags.get(i));
            }
            
            // now serialize it to the output stream
            XmlUtil.getTransformer().transform(new DOMSource(doc), new StreamResult(stream));
            
        } catch (Exception ex) {
            throw new OperationException("Error while generating <tags>", ex);
        }
        
    }
}
