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

import java.io.PrintWriter;
import java.util.Date;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.xml.EntryTags;
import org.roosster.util.XmlUtil;
import org.roosster.util.StringUtil;
import org.roosster.OperationException;
import org.roosster.Registry;
import org.roosster.Constants;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryGenerator implements EntryTags 
{
    private static Logger LOG = Logger.getLogger(EntryGenerator.class);

    /**
     * @param entries
     */
    public void createFeed(Registry registry, PrintWriter writer, EntryList entries)
                    throws OperationException
    {
        String truncStr = registry.getConfiguration().getProperty(Constants.PROP_TRUNCLENGTH, "-1");
        int truncate = Integer.valueOf(truncStr).intValue();
        
        try {
            Document doc = XmlUtil.getDocumentBuilder().newDocument();
                                       
            Element root = XmlUtil.createChild(doc, ENTRYLIST);
            root.setAttribute(TOTAL_ATTR,   String.valueOf(entries.getTotalSize()) );
            root.setAttribute(OFFSET_ATTR,  String.valueOf(entries.getOffset()) );
            root.setAttribute(LIMIT_ATTR,   String.valueOf(entries.getLimit()) );
            
            for ( int i = 0; i < entries.size(); i++ ) {
                Entry entry = entries.getEntry(i);
              
                Element entryNode = XmlUtil.createChild(root, ENTRY);
                entryNode.setAttribute(HREF_ATTR,     entry.getUrl().toString());
                entryNode.setAttribute(TITLE_ATTR,    entry.getTitle());
                entryNode.setAttribute(TYPE_ATTR,     entry.getFileType());
                
                entryNode.setAttribute(ISSUED_ATTR,   XmlUtil.formatW3cDate(entry.getIssued()) );
                entryNode.setAttribute(MODIFIED_ATTR, XmlUtil.formatW3cDate(entry.getLastModified()) );
                entryNode.setAttribute(FETCHED_ATTR,  XmlUtil.formatW3cDate(entry.getLastFetched()) );
                entryNode.setAttribute(EDITED_ATTR,   XmlUtil.formatW3cDate(entry.getLastEdited()) );
                
                XmlUtil.createTextChild(entryNode, NOTE,    entry.getNote());
                XmlUtil.createTextChild(entryNode, CONTENT, StringUtil.truncate(entry.getContent(), truncate));

                Element authorNode = XmlUtil.createChild(entryNode, AUTHOR);
                authorNode.setAttribute(NAME_ATTR, entry.getAuthor());
                authorNode.setAttribute(EMAIL_ATTR, entry.getAuthorEmail());
                
                
                String[] tags = entry.getTags();
                for ( int k = 0; k < tags.length; k++ ) {
                    XmlUtil.createTextChild(entryNode, TAG, tags[k]);
                }
            }
            
            // now serialize it to the output stream
            XmlUtil.getTransformer().transform(new DOMSource(doc), new StreamResult(writer));
            
        } catch (Exception ex) {
            throw new OperationException("Error while generating <entrylist>", ex);
        }
        
    }
}
