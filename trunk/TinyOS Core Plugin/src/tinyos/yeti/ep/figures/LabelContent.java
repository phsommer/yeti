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
package tinyos.yeti.ep.figures;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTFigureFactory;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.utility.Icon;

/**
 * A label that defines its icon through a set of {@link Tag}s.
 * @author Benjamin Sigg
 */
public class LabelContent implements IASTFigureContent{
    public static final IGenericFactory<LabelContent> FACTORY = new IGenericFactory<LabelContent>(){
        public LabelContent create(){
            return new LabelContent();
        }
        
        public void write( LabelContent value, IStorage storage ) throws IOException{
            storage.writeString( value.text );
            storage.write( value.icon );
            storage.write( value.path );
        }
        
        public LabelContent read( LabelContent value, IStorage storage ) throws IOException{
            value.text = storage.readString();
            value.icon = storage.read();
            value.path = storage.read();
            
            return value;
        }
    };
    
    private String text;
    private Icon icon;
    private IASTModelPath path;

    protected LabelContent(){
        // nothing
    }
    
    /**
     * Creates a new content.
     * @param text the text of this content
     * @param icon a description of the icon for this content
     * @param path the optional path
     */
    public LabelContent( String text, Icon icon, IASTModelPath path ){
        this.text = text;
        this.icon = icon;
        this.path = path;
    }
    
    public void setPath( IASTModelPath path ){
        this.path = path;
    }
    
    public IASTFigure createContent( IASTFigureFactory factory, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Create Label '" + text + "'", 1 );
        
        ASTLabel label = new ASTLabel( factory.getHoverManager(), path == null ? null : factory.getNode( path, new SubProgressMonitor( monitor, 1 ) ) );
        label.setPath( path );
        label.setFont( factory.getFontNormal() );
        
        if( text != null )
            label.setText( text );
 
        if( icon != null ){
            label.setIcon( icon );
        }
        
        monitor.done();
        return label;
    }
}
