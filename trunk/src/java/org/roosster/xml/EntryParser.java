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

import java.io.*;
import java.util.logging.Logger;

import javax.xml.parsers.*;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.roosster.OperationException;
import org.roosster.store.EntryList;
import org.roosster.store.Entry;
import org.roosster.util.XmlUtil;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class EntryParser 
{
    private static Logger LOG = Logger.getLogger(EntryParser.class.getName());

    
    /**
     * @param stream 
     */
    public EntryList parse(String source, String encoding) 
                    throws ParseException, IOException
    {
        return parse(new StringReader(source), encoding);
    }
    
    
    /**
     * @param stream 
     */
    public EntryList parse(Reader source, String encoding) 
                    throws ParseException, IOException
    {
        if ( source == null || encoding == null || "".equals(encoding) )
            throw new IllegalArgumentException("Arguments are not allowed to be null");
      
        try {
            //DocumentBuilder parser = XmlUtil.getDocumentBuilder();
          
            EntrySaxHandler handler = new EntrySaxHandler(); 
            
            SAXParser parser = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
            
            //parser.setContentHandler(handler);
            
            
            InputSource inputSource = new InputSource(source);
            inputSource.setEncoding(encoding);
            
            LOG.finest("Now parsing xml document with encoding "+encoding);
            
            parser.parse(inputSource, handler);
            
            return handler.getEntries();
            
        } catch(ParserConfigurationException ex) {
            throw new ParseException(ex);
        } catch(SAXException ex) {
            throw new ParseException(ex);
        }
      
      
    }

}
