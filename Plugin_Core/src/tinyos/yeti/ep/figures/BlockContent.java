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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTFigureFactory;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

/**
 * A content that contains children and organizes them in blocks.
 * @author Benjamin Sigg
 */
public class BlockContent implements IASTFigureContent {
    public static final IGenericFactory<BlockContent> FACTORY = new IGenericFactory<BlockContent>(){
        public BlockContent create(){
            return new BlockContent();
        }
        
        public void write( BlockContent value, IStorage storage ) throws IOException{
            // children
            storage.out().writeInt( value.children.size() );
            for( IASTFigureContent[] child : value.children ){
                storage.out().writeInt( child.length );
                for( IASTFigureContent figure : child ){
                    storage.write( figure );
                }
            }
            
            // paths
            if( value.paths == null ){
                storage.out().writeInt( 0 );
            }
            else{
                storage.out().writeInt( value.paths.size() );
                for( IASTModelPath path : value.paths ){
                    storage.write( path );
                }
            }
        }
        
        public BlockContent read( BlockContent value, IStorage storage ) throws IOException{
            // children
            int size = storage.in().readInt();
            for( int i = 0; i < size; i++ ){
                int length = storage.in().readInt();
                IASTFigureContent[] content = new IASTFigureContent[ length ];
                for( int j = 0; j < length; j++ ){
                    content[j] = storage.read();
                }
                value.children.add( content );
            }
            
            // paths
            size = storage.in().readInt();
            for( int i = 0; i < size; i++ ){
                IASTModelPath path = storage.read();
                value.addPath( path );
            }
            
            return value;
        }
    };
    
    private List<IASTFigureContent[]> children = new ArrayList<IASTFigureContent[]>();
    private List<IASTModelPath> paths = null;
    
    public void addPath( IASTModelPath path ){
        if( paths == null )
            paths = new ArrayList<IASTModelPath>();
        paths.add( path );
    }
    
    public void addBlock( IASTFigureContent[] content ){
        if( content != null && content.length > 0 )
            children.add( content );
    }
    
    public IASTFigure createContent( IASTFigureFactory factory, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        int size = 0;
        for( IASTFigureContent[] array : children )
            size += array.length;
        
        monitor.beginTask( "Buidl Figure", size );
        
        ASTFigure figure = new ASTFigure();
        figure.setPaths( paths );
        figure.setLayoutManager( new ToolbarLayout() );
        boolean first = true;
        
        for( IASTFigureContent[] array : children ){
            if( first )
                first = false;
            else{
                Figure line = new SeparatorLine();
                line.setBorder( new MarginBorder( 2 ) );
                figure.add( line );
            }
            Figure list = new Figure();
            list.setLayoutManager( new ToolbarLayout() );
            list.setBorder( new MarginBorder( 3 ) );
            for( IASTFigureContent lazy : array ){
                list.add( lazy.createContent( factory, new SubProgressMonitor( monitor, 1 ) ));
                if( monitor.isCanceled() ){
                    monitor.done();
                    return null;
                }
            }
            figure.add( list );
        }
        
        monitor.done();
        return figure;
    }
}
