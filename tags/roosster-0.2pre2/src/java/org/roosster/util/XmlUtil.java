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
package org.roosster.util;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.log4j.Logger;


import org.roosster.Constants;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class XmlUtil implements Constants
{
    private static Logger LOG = Logger.getLogger(XmlUtil.class.getName());


    /**
     * @return a <code>SimpleDateFormat</code> instance initialized with the 
     * {@link org.roosster.Constants#W3C_DATEFORMAT Constants.W3C_DATEFORMAT} 
     * pattern.
     */
    public static DateFormat getDateFormat() 
    {
        return getDateFormat(Constants.W3C_DATEFORMAT);
    }

    
    /**
     * 
     */
    public static DateFormat getDateFormat(String format) 
    {
        return new SimpleDateFormat(format);
    }

    
    /**
     */
    public static String formatW3cDate(Date date) throws ParseException
    {
        if ( date == null )
            return "";
        
        String dateStr = getDateFormat().format(date);
        return dateStr.substring(0, dateStr.length()-2) +":"+ dateStr.substring(dateStr.length()-2);
    }
      
      
    /**
     */
    public static Date parseW3cDate(String dateStr) throws ParseException
    {
        if ( dateStr == null || "".equals(dateStr) )
            return null;
        
        int len = dateStr.length();
        
        if ( ":".equals(dateStr.substring(len-3, len-2)) )
            return getDateFormat().parse(dateStr.substring(0, len-3) + dateStr.substring(len-2));
        else
            return getDateFormat().parse(dateStr);
    }
    
    
    /**
     * Creates a new <code>Node</code>-object with the specified <code>name</code>
     * and appends it as a child element to the provided <code>Node</code>-
     * parameter.
     * 
     * @param elem the <code>Node</code> to which the newly created 
     * <code>Node</code> will be appended to. Not allowed to be <code>null</code>.
     * @param elemName the name of the to-be-created <code>Element</code>, not allowed 
     * to be empty or <code>null</code>.
     * @return the created element
     * @exception IllegalArgumentException if <code>elem</code> and/ord
     * <code>elemName</code> are null (or empty in the case of <code>elemName</code>)
     */
    public static Element createChild(Node node, String elemName)
    {
        if ( node == null || elemName == null || "".equals(elemName) )
            throw new IllegalArgumentException("Arguments are not allowed to be null or empty");
      
        Document document = null;

        if (node instanceof Document) {
            document = (Document) node;
        } else if (node.getOwnerDocument() != null) {
            document = node.getOwnerDocument();
        } 
        
        Element newChild = null; 
        if ( document != null ) {
            newChild = document.createElement(elemName);
            node.appendChild(newChild);
        }
        
        return newChild;
    }


    
    
    /**
     * Same as {@link createChild(Node, String) createChild()}, but adds the
     * specified <code>text</code> to the newly created <code>Node</code>.
     * 
     * @see #createChild(Node, String)
     * @param elem the <code>Node</code> to which the newly created 
     * <code>Node</code> will be appended to. Not allowed to be <code>null</code>.
     * @param elemName the name of the to-be-created <code>Node</code>, not allowed 
     * to be empty or <code>null</code>.
     * @param text the text-contents of the created/inserted node  
     * @return the created element
     * @exception IllegalArgumentException if <code>elem</code> and/ord
     * <code>elemName</code> are null (or empty in the case of <code>elemName</code>)
     */
    public static Element createTextChild(Node node, String elemName, String text)
    {
        if ( node == null || elemName == null || "".equals(elemName) )
            throw new IllegalArgumentException("Arguments are not allowed to be null or empty");
      
        Document document = null;

        if (node instanceof Document) {
            document = (Document) node;
        } else if (node.getOwnerDocument() != null) {
            document = node.getOwnerDocument();
        } 
        
        Element newChild = null; 
        if ( document != null ) {
            newChild = document.createElement(elemName);
            node.appendChild(newChild); 
            newChild.appendChild(document.createTextNode(text));
        }
        
        return newChild;
    }

    
    /**
     * @see #createChild(Element, String, String)
     */
    public static Element createTextChild(Node elem, String elemName, Integer text)
    {
        return createTextChild(elem, elemName, String.valueOf(text));
    }
    
    
    /**
     * @see #createChild(Element, String, String)
     */
    public static Element createTextChild(Node elem, String elemName, Float text)
    {
        return createTextChild(elem, elemName, String.valueOf(text));
    }

    
    /**
     * @see #createChild(Element, String, String)
     */
    public static Element createTextChild(Node elem, String elemName, Boolean text)
    {
        return createTextChild(elem, elemName, text == null ? "false" : text.toString());
    }


    /**
     * 
     */
    public static XMLReader getXmlReader() throws SAXException
    {
        XMLReader parser = XMLReaderFactory.createXMLReader();
        return parser;
    }

    
    /**
     * 
     */
    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        factory.setNamespaceAware(false);
        
        return factory.newDocumentBuilder();
    } 

    
    /**
     * 
     */
    public static Transformer getTransformer() throws TransformerConfigurationException
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        
        return factory.newTransformer();
    }    
}
