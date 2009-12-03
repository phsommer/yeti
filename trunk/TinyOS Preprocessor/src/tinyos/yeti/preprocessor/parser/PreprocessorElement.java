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
package tinyos.yeti.preprocessor.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;

/**
 * A {@link PreprocessorElement} is created by the preprocessor-parser.
 * Each element represents some instruction or statement of the 
 * preprocessor.
 * @author Benjamin Sigg
 */
public abstract class PreprocessorElement {
    private PreprocessorToken token;
    
    /** the index of the first character represented by this element */
    private int offset;
    /** the number of characters represented by this element */
    private int length;
    
    public PreprocessorElement( PreprocessorToken token ){
        this.token = token;
    }
    
    /**
     * Writes the contents of this element into <code>output</code>.<br>
     * This is a recursive search through the tree for {@link PreprocessorToken}s
     * with a non-<code>null</code> text, parents are processed first, children
     * from left to right.
     * @param writer the stream to write into
     */
    public void output( Writer writer ) throws IOException{
        if( token != null ){
            String text = token.getText();
            if( text != null )
                writer.write( text );
        }
        
        PreprocessorElement[] children = getChildren();
        if( children != null ){
            for( PreprocessorElement child : children )
                child.output( writer );
        }
    }
    
    /**
     * This method calculates the location and size of this element in the 
     * input and in the output, this method recursively calls the childrens method
     * {@link #calculateLocation(int)}.
     * @param offset the index of the first character of this element, if in doubt,
     * set it to zero.
     */
    public void calculateLocation( int offset ){
        this.offset = offset;
        this.length = 0;
        
        PreprocessorElement[] children = getChildren();
        if( token != null ){
            if( token.getText() != null ){
                int delta = token.getText().length();
                offset += delta;
                length += delta;
            }
        }
        if( children != null ){
            for( PreprocessorElement child : children ){
                child.calculateLocation( offset );
                int delta = child.getOutputLength();
                offset += delta;
                length += delta;
            }
        }
    }
    
    /**
     * Gets the index of the first character of this element in the output.
     * Only valid after a call to {@link #calculateLocation(int)}.
     * @return the offset
     */
    public int getOutputOffset() {
        return offset;
    }
    
    /**
     * Gets the number of characters that are represented by this element in the
     * output. Only valid after a call to {@link #calculateLocation(int)}.
     * @return the length
     */
    public int getOutputLength() {
        return length;
    }
    
    /**
     * Gets the origin of this element.
     * @return the origin, might be <code>null</code>
     */
    public PreprocessorToken getToken() {
        return token;
    }
        
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString( builder, 0 );
        return builder.toString();
    }
    
    protected abstract void toString( StringBuilder builder, int tabs );
    
    public void visit( ElementVisitor visitor ){
        visitor.visit( this );
        PreprocessorElement[] children = getChildren();
        for( int i = 0; i < children.length; i++ )
            children[i].visit( visitor );
        visitor.endVisit( this );
    }
    
    public void visitReverse( ElementVisitor visitor ){
        visitor.visit( this );
        PreprocessorElement[] children = getChildren();
        for( int i = children.length-1; i >= 0; i-- )
            children[i].visit( visitor );
        visitor.endVisit( this );
    }
    
    /**
     * Gets an independent array of the children of this element.
     * @return the children or <code>null</code>, the array must not contain
     * <code>null</code> elements.
     */
    public abstract PreprocessorElement[] getChildren();
    
    /**
     * Gets the number of children, can include <code>null</code> children
     * @return the number of children
     */
    public abstract int getChildrenCount();
    
    /**
     * Gets the index'th child, can be a <code>null</code> child
     * @param index the index of the child
     * @return the child or <code>null</code>
     */
    public abstract PreprocessorElement getChild( int index );
    
    protected void toString( StringBuilder builder, int tabs, String kind, String value, Collection<? extends PreprocessorElement> children ){
        toString( builder, tabs, kind, value, children.toArray( new PreprocessorElement[ children.size()] ) );
    }
    
    protected void toString( StringBuilder builder, int tabs, String kind, String value, PreprocessorElement... children ){
        addTabsTo( builder, tabs );
        
        int count = 0;
        if( children != null ){
            for( PreprocessorElement child : children ){
                if( child != null )
                    count++;
            }
        }
        
        if( value == null && count == 0 ){
            builder.append( kind );
        }
        else{
            builder.append( kind );
            builder.append( ": " );
            if( value != null )
                builder.append( value.replaceAll( "\\n|\\r", "\\\\n" ) ) ;
            
            if( children != null ){
                for( PreprocessorElement child : children ){
                    if( child != null ){
                        addChildTo( builder, tabs, child );
                    }
                }
            }
        }
    }
    
    protected void addTabsTo( StringBuilder builder, int tabs ){
        for( int i = 0; i < tabs; i++ ){
            builder.append( "  " );
        }
    }

    protected void addChildTo( StringBuilder builder, int tabs, PreprocessorElement element ){
        builder.append( "\n" );
        element.toString( builder, tabs+1 );
    }
}
