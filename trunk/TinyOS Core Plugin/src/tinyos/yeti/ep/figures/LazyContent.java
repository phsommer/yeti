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

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTFigureFactory;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.utility.Icon;

/**
 * A content that can be used to wrap whole nodes or other elements of the ast
 * @author Benjamin Sigg
 */
public class LazyContent implements IASTFigureContent{
    public static final IGenericFactory<LazyContent> FACTORY = new IGenericFactory<LazyContent>(){
        public LazyContent create(){
            return new LazyContent();
        }
        
        public void write( LazyContent value, IStorage storage ) throws IOException{
            storage.writeString( value.label );
            storage.write( value.icon );
            storage.write( value.path );
        }
        
        public LazyContent read( LazyContent value, IStorage storage ) throws IOException{
            value.label = storage.readString();
            value.icon = storage.read();
            value.path = storage.read();
            return value;
        }
    };
    
    private String label;
    private Icon icon;
    private IASTModelPath path;
    
    private LazyContent(){
        // do nothing
    }
    
    /**
     * Creates a new content.
     * @param label backup label if <code>path</code> can't be resolved
     * @param icon backup icon if <code>path</code> can't be resolved
     * @param path path to content or <code>null</code>
     */
    public LazyContent( String label, Icon icon, IASTModelPath path ){
        this.label = label;
        this.icon = icon;
        this.path = path;
    }
    
    public IASTFigure createContent( IASTFigureFactory factory, IProgressMonitor monitor ){
        return factory.create( path, label, icon, monitor );
    }
}
