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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tinyos.yeti.TinyOSPlugin;

public class XReadStack{
    private Document document;
    
    private List<Level> stack = new ArrayList<Level>();

    public static XReadStack open( File file ){
        try{
            BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ));
            XReadStack result = new XReadStack( in );
            in.close();
            return result;
        }
        catch( IOException ex ){
            TinyOSPlugin.log( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, 0, ex.getMessage(), ex ) );
        }
        catch( SAXException ex ){
            TinyOSPlugin.log( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, 0, ex.getMessage(), ex ) );            
        }
        
        return null;
    }
    
    public XReadStack( InputStream in ) throws SAXException, IOException {
        try{
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( in );
            init( document );
        }
        catch( ParserConfigurationException ex ){
            throw new RuntimeException( ex.getMessage(), ex );
        }
    }
    
    public XReadStack( Document document ){
        init( document );
    }
    
    private void init( Document document ){
        this.document = document;
        stack.add( new Level( null, document.getChildNodes() ) );
    }
    
    public Document getDocument(){
        return document;
    }
    
    /**
     * Leaves the current element.
     */
    public void pop(){
        stack.remove( stack.size()-1 );
    }
    
    /**
     * Tells whether the current element has another child.
     * @return <code>true</code> if another child exists
     */
    public boolean hasNext(){
        return peek().hasNext();
    }
    
    /**
     * Selects the next child of the current element and pushes
     * that child.
     */
    public void next(){
        peek().next();
    }
    
    /**
     * Tells whether the current element has another child of the given name.
     * @param name the name of the child
     * @return <code>true</code> if such a child exists
     */
    public boolean hasNext( String name ){
        return peek().hasNext( name );
    }
    
    /**
     * Selects the next child which the name <code>name</code> of the current
     * element.
     * @param name the name to search for
     */
    public void next( String name ){
        peek().next( name );
    }
    
    /**
     * Goes to the next <code>name</code>.
     * @param name the name of searched next element
     * @return <code>true</code> if such an element was found, <code>false</code>
     * otherwise
     */
    public boolean go( String name ){
        if( hasNext( name ) ){
            next( name );
            return true;
        }
        return false;
    }
    
    /**
     * Searches for the first child with name <code>name</code> and does select
     * that child.
     * @param name the name to look out for
     * @return <code>true</code> if such a child was found, <code>false</code>
     * otherwise
     */
    public boolean search( String name ){
        return peek().search( name );
    }
    
    /**
     * Ensures that the child which {@link #next()} would select is again
     * the first child of the current element.
     */
    public void restart(){
        peek().restart();
    }
    
    /**
     * Gets the name of the current element
     * @return the name
     */
    public String getName(){
        return peek().getName();
    }
    
    /**
     * Gets the text content of the current element
     * @return the text
     */
    public String getText(){
        return peek().getText();
    }
    
    /**
     * Gets the value of an attribute of the current element.
     * @param name the name of the attribute
     * @return the value or <code>null</code> if the value does not exist
     */
    public String getAttribute( String name ){
        return peek().getAttribute( name );
    }
    
    public boolean getBoolean( String name, boolean defaultValue ){
        String attr = getAttribute( name );
        if( attr == null )
            return defaultValue;
        
        return Boolean.parseBoolean( attr );
    }
    
    public String getString( String name, String defaultValue ){
        String attr = getAttribute( name );
        if( attr == null )
            return defaultValue;
        
        return attr;
    }
    
    public double getDouble( String name, double defaultValue ){
        String attr = getAttribute( name );
        if( attr == null )
            return defaultValue;
        
        return Double.parseDouble( attr );
    }
    
    public int getInteger( String name, int defaultValue ){
        String attr = getAttribute( name );
        if( attr == null )
            return defaultValue;
        
        return Integer.parseInt( attr );
    }
    
    public long getLong( String name, int defaultValue ){
        String attr = getAttribute( name );
        if( attr == null )
            return defaultValue;
        
        return Long.parseLong( name );
    }
    
    private Level peek(){
        return stack.get( stack.size()-1 );
    }
    
    private class Level{
        private Element element;
        private Element[] list;
        private int index;
        
        public Level( Element element, NodeList nodes ){
            this.element = element;
            if( nodes == null )
                nodes = element.getChildNodes();
            
            List<Element> list = new ArrayList<Element>();
            for( int i = 0, n = nodes.getLength(); i<n; i++ ){
                Node node = nodes.item( i );
                if( node instanceof Element ){
                    list.add( (Element)node );
                }
            }
            this.list = list.toArray( new Element[ list.size() ] );
            
            index = 0;
        }
        
        public void restart(){
            index = 0;
        }
        
        public boolean hasNext(){
            return index < list.length;
        }
        
        public void next(){
            stack.add( new Level( list[ index++ ], null ) );
        }
        
        public boolean hasNext( String name ){
            for( int i = index; i<list.length; i++ ){
                Element check = list[i];
                if( check.getNodeName().equals( name ))
                    return true;
            }
            return false;
        }
        
        public void next( String name ){
            for( int i = index; i<list.length; i++ ){
                Element check = list[i];
                if( check.getNodeName().equals( name )){
                    index = i;
                    next();
                    return;
                }
            }
            throw new IllegalStateException( "no such node" );
        }
        
        public boolean search( String name ){
            for( int i = 0; i<list.length; i++ ){
                Element check = list[i];
                if( check.getNodeName().equals( name )){
                    index = i;
                    next();
                    return true;
                }
            }
            return false;
        }
        
        public String getName(){
            return element.getNodeName();
        }
        
        public String getText(){
            return element.getTextContent();
        }
        
        public String getAttribute( String name ){
            Attr attr = element.getAttributeNode( name );
            if( attr == null )
                return null;
            
            return attr.getNodeValue();
        }
        
    }
}
