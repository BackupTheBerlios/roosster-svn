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

import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.roosster.OperationException;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.util.XmlUtil;

/**
 * 
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntrySaxHandler extends DefaultHandler implements EntryTags
{
    private static Logger LOG = Logger.getLogger(EntrySaxHandler.class.getName());

    private EntryList   entries            = new EntryList();
    
    private LinkedList  elementStack       = new LinkedList();
    
    
    /**
     * 
     */
    public void startElement(String nsURI, String localName, String qName, Attributes atts)
                      throws SAXException
    {
        LOG.finest("PARSE: Pushing <"+qName+"> onto element stack");
        elementStack.add(qName);
    }
    
    
    /**
     * 
     */
    public void endElement(String nsURI, String localName, String qName)
                    throws SAXException
    {
        LOG.finest("PARSE: Popping <"+qName+"> from element stack");
        elementStack.removeLast();
    }
    
    
    /**
     * @return null if the parsed stream didn't contain a &lt;entrylist&gt;
     */
    public EntryList getEntries()
    {
        return entries;
    }

}
