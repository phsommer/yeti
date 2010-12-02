/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tinyos.yeti.TinyOSPlugin;

/**
 * Used to write new XML-documents.
 * @author Benjamin Sigg
 */
public class XWriteStack{
    private Document document;
    
    private List<Element> stack = new ArrayList<Element>();
    
    public static XWriteStack open(){
        return new XWriteStack();
    }
    
    public XWriteStack(){
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            //This should never happen.
        	TinyOSPlugin.log( ex );
        }
    }
    
    public InputStream toInputStream(){
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write( out );
            return new ByteArrayInputStream( out.toByteArray() );
        }
        catch( IOException e ){
            throw new RuntimeException( e.getMessage(), e );
        }
        catch( TransformerException e ){
            throw new RuntimeException( e.getMessage(), e );
        }
    }
    
    public void write( File file ){
        try{
            BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( file ) );
            write( out );
            out.close();
        }
        catch( IOException ex ){
            TinyOSPlugin.log( new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, 0, ex.getMessage(), ex ) );
        }
        catch( TransformerException ex ){
            TinyOSPlugin.log( new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, 0, ex.getMessage(), ex ) );
        }
    }
    
    public void write( OutputStream out ) throws IOException, TransformerException{
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", new Integer(4));

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");

        DOMSource source = new DOMSource( document );
        StreamResult stream = new StreamResult( out );

        transformer.transform( source, stream );
    }
    
    public Document getDocument(){
        return document;
    }
    
    public void push( String name ){
        Element element = document.createElement( name );
        if( stack.size() == 0 ){
            document.appendChild( element );
        }
        else{
            stack.get( stack.size()-1 ).appendChild( element );
        }
        
        stack.add( element );
    }
    
    public void pop(){
        stack.remove( stack.size()-1 );
    }
    
    public void setAttribute( String name, String value ){
        stack.get( stack.size()-1 ).setAttribute( name, value );
    }
    
    public void setText( String text ){
        if( text == null )
            text = "";
        
        stack.get( stack.size()-1 ).appendChild( document.createTextNode( text ) );
    }
}
